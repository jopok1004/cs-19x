package app.combined;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
public class ReceiverActivity extends Activity {
	Boolean received = false;
	Boolean initialR = false;
	String phoneNo = new String();
	Button btnSendConfirmation;
	EditText txtPhoneNo;
	SmsReceiver rcvd;
	HashMap<Integer, String> al = new HashMap<Integer, String>();
	TelephonyManager Tel;
	int size; // number of messages to be received
	String fileT;
	int messageSize = 10;
	long last, current;
	private XMPPConnection connection;
	public static final String MMSMON_RECEIVED_MMS = "MMStesting.intent.action.MMSMON_RECEIVED_MMS";
	private Time time;
	int temporary=0;
	int tempalsize;
	int alsize = 0;
	private Uri mmsInURI = Uri.parse("content://mms-sms");
	private BroadcastReceiver mmsMonitorBroadcastReceiver;
	private BroadcastReceiver threeGMonitorBroadcastReceiver;
	private int initial;
	ContentObserver mmsObserver;
	private int end;
	private String fileType;
	private boolean started;
	private File fileoutput = new File("/sdcard/outputreceiver.txt");
	
	private String username;
	private String password;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receiver);
		Intent intent = getIntent();
		rcvd = new SmsReceiver();
		btnSendConfirmation = (Button) findViewById(R.id.btnSendConfirmation);
		txtPhoneNo = (EditText) findViewById(R.id.phoneNumberText);

		btnSendConfirmation.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				phoneNo = txtPhoneNo.getText().toString();
				if (phoneNo.length() > 0) {
					if(isOnline(getBaseContext())){
						sendSMS(phoneNo, "%& start 1");
					}else{
						sendSMS(phoneNo, "%& start 0");
					}
					Toast.makeText(getBaseContext(),
							"Please do not close this application.",
							Toast.LENGTH_SHORT).show();
					btnSendConfirmation.setClickable(false);

				} else
					Toast.makeText(getBaseContext(),
							"Please enter phone number.", Toast.LENGTH_SHORT)
							.show();
			}
		});
		
		threeGMonitorBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
			     Log.d("app","Network connectivity change");
			     if(intent.getExtras()!=null) {
			        NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
			        if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
			            Log.i("app","Network "+ni.getTypeName()+" connected");
			            sendSMS(phoneNo,"%& receiverConnectivity 1");
			            //send sms na connected
			        }
			     }
			     if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
			            Log.e("app","There's no network connectivity");
			            sendSMS(phoneNo,"%& receiverConnectivity 0");
			          //send sms na di connected
			     }
				
			}
		};
		//FOR MMS
		mmsMonitorBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				Log.i("MMS Received", "MMS RECEIVED HAHA");
				try {
					Log.i("SEARCHING", "SEARCHING MMS AGAIN");
					checkMMSMessages();
					temporary = tempalsize;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(MMSMON_RECEIVED_MMS);
		IntentFilter gIntentFilter = new IntentFilter();
		gIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(threeGMonitorBroadcastReceiver,gIntentFilter);
		registerReceiver(mmsMonitorBroadcastReceiver, mIntentFilter);

		getApplicationContext().getContentResolver().registerContentObserver(
				mmsInURI, true, mmsObserver);
		getApplicationContext().getContentResolver().notifyChange(mmsInURI,
				mmsObserver);
	}
	//FOR SMS
	public void onNewIntent(Intent intent) {
		Log.i("INTENT", intent.getStringExtra("start?").toString());
		if ((intent.getStringExtra("start?").toString()).equals("getConfirm")) {
			Log.i("GETCONFIRM", "GETCONFIRM");
			size = intent.getIntExtra("size", 10);
			fileT = intent.getStringExtra("fileType");
			//reply on button click
		}
		if ((intent.getStringExtra("start?").toString()).equals("check10")) {
			Boolean okay = true;
			Log.i("check10", "check for missing packets");
			waiting(30);
			String resend = "";
			int currentp;
			currentp = Integer.parseInt(intent.getStringExtra("tracker"));
			resend = "%& resend ";
			for (int i = (currentp % 10); i >= 0 && currentp < size; i--) {
				if (al.containsKey(currentp - i - 1) == false && received ==false) {
					resend = resend + (currentp - i - 1) + " ";
					Log.i("if not containskey", "checking for missing packets");
					Log.i("resend", resend);
					okay = false;
				}

			}
			if (al.size() < currentp) {
				for (int k = 0; k < currentp - 1; k++) {
					if (!al.containsKey(k)) {
						resend = resend + k + " ";
						Log.i("if not containskey",
								"checking for missing packets");
						
						Log.i("resend", resend);
						okay = false;
					}
				}
			}
			if (okay == true) {
				sendSMS(phoneNo, "%& resend none");
			} else {
				sendSMS(phoneNo, resend);
			}

		}
		if ((intent.getStringExtra("start?").toString())
				.equals("start receiving")) {
			if (received) {
				// do nothing
			} else {

				int pn;
				pn = intent.getIntExtra("packetNum", 1000);
				al.put(intent.getIntExtra("packetNum", 1000), intent
						.getStringExtra("message").toString());
				Toast.makeText(getBaseContext(), "Received Packet #" + pn,
						Toast.LENGTH_SHORT).show();

				Log.i("AL SIZE", Integer.toString(al.size()));
				Log.i("SIZE", Integer.toString(size));
			}

		}
		//FOR MMS
		//Log.i("NEWINTENT", "NEW INTENT " + intent.getStringExtra("start?"));
		if ((intent.getStringExtra("start?")).equals("startMmsReceive")) {
			time = new Time();
			if (started == false) {
				Debug.startMethodTracing("mmsreceiver");
				started = true;
			}
			Log.i("RECEIVED SMS", "RECEIVED SMS");
			phoneNo = intent.getStringExtra("phoneNum");
			Log.i("Phone num", phoneNo);
			initial = intent.getIntExtra("initial", 0);
			Log.i("initial", Integer.toString(initial));
			end = intent.getIntExtra("end", 0);
			Log.i("end", Integer.toString(end));
			//size = end - initial;
			Log.i("size", "SIZE: " + Integer.toString(size));

			//fileType = intent.getStringExtra("filetype");
			Log.i("fileType", fileType);

		}
		//FOR 3G
		if ((intent.getStringExtra("start?")).equals("start3GReceive")) {
			logIn();
		}
	}
	//FOR MMS
	private void checkMMSMessages() throws IOException {
		
			tempalsize = alsize;
			alsize = al.size();
			String[] coloumns = null;
			String[] values = null;
			ArrayList<Integer> mid = new ArrayList<Integer>();

			Cursor curPart = this
					.getApplicationContext()
					.getContentResolver()
					.query(Uri.parse("content://mms/inbox"), null, null, null,
							null);

			

			
			curPart.moveToFirst();
			for (int i = 0; i < curPart.getCount(); i++) {
				

				// add to List the mid of the MMS that is currently in the
				// inbox

				mid.add(curPart.getInt(curPart.getColumnIndex("_id")));

				curPart.moveToNext();
			}

			
			curPart.moveToFirst();

			curPart = this
					.getApplicationContext()
					.getContentResolver()
					.query(Uri.parse("content://mms/part"), null, null, null,
							null);

			if (curPart.moveToFirst()) {
				do {
					coloumns = curPart.getColumnNames();

					if (values == null)
						values = new String[coloumns.length];

					// just get the TEXT part
					for (int i = 0; i < mid.size(); i++) {

						String[] packets;
						String[] packets2;

						if (curPart.getInt(curPart.getColumnIndex("mid")) == mid
								.get(i)) {
							String text = curPart.getString(curPart
									.getColumnIndex("text"));
							if (text != null) {
								if (text.startsWith("&%")) {
									packets = text.split("&% ");
									// bw.write("MID: " + mid.get(i) + "\tTEXT:"
									// + text + "\n");
									for (int j = 0; j < packets.length; j++) {
										// bw.write("packet " + j + ": " +
										// packets[j]
										// + "\n");
										Log.i("PACKETS", packets[j]);
										if (packets[j] != null) {
											packets2 = packets[j].split(" ");
											if (!packets2[0].equals("")) {
												Log.i("packets2[0]",
														packets2[0]);
												Log.i("packets2[1]",
														packets2[1]);
												int pNum = Integer
														.parseInt(packets2[0]);
												Log.i("pNum", "pNum: " + pNum);
												Log.i("packet", packets2[1]);
												if (!al.containsKey(pNum)) {
													al.put(pNum, packets2[1]);
												}

												Log.i("AL SIZE", "AL SIZE: "
														+ al.size());
												if (al.size() == size) {
													receiveFile();
												}
											}

										}

									}
								}
							}

						}

					}

				} while (curPart.moveToNext());
				alsize = al.size();
				
				if (tempalsize != alsize) {
					Log.i("SIZES", "ALSIZES: tempalsize: "+tempalsize + " alsize: "+alsize);
					Log.i("MMS", "NARECEIVE KO NA SI MMS");
					if(received==false) {
						sendSMS(phoneNo, "%& mmsreceived");
					}
					
					getContentResolver().delete(Uri.parse("content://mms"), null,
							null);
				}
			}
			curPart.close();

			
		
		

	}
	public void receiveFile() {

		if (al.size() == size) {
			try {
				FileWriter fw1 = new FileWriter(new File("/sdcard/decode.txt"));
				for (int i = 0; i < size; i++) {
					fw1.write(al.get(i) + "\n");
				}
				fw1.close();

				Log.i("WRITING TO FILE", "FILEWRITER");
				
				Base64FileDecoder.decodeFile("/sdcard/decode.txt","/sdcard/file." + fileT + ".gz");
				File fl = new File("/sdcard/decode.txt");
				fl.delete();

				compression.decompressGzip("/sdcard/file." + fileT + ".gz");
				fl = new File("/sdcard/file." + fileT + ".gz");
				fl.delete();
				
				Log.i("DONE!!!", "DONE");
				Toast.makeText(getBaseContext(),"File Received. Check your SD Card",Toast.LENGTH_LONG).show();
				fw1.close();
				sendSMS(phoneNo, "%& done");
				this.finish();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			received = true;
		}
	}
	
	//FOR 3G
	
	public XMPPConnection getConnection() {
		return connection;
	}

	public void setConnection(XMPPConnection connection) {
		if (connection == null) {
			this.finish();
		}else {
	        this.connection = connection;
	        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
	        connection.addPacketListener(new Receiver3GListener(this), filter);
		}
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void logIn () {
        if (isOnline(this)) {
        	if (getUsername() == null && getPassword() == null) {
        		LogInSettings lDialog;
                lDialog = new LogInSettings(this);
                lDialog.show();
        	}else {
        		establishConnection(getUsername(), getPassword());
        	}
        }else {
        	Log.e("Receiver:3GConnection", "No internet connectivity available");
        }
		
	}
	
	public void establishConnection(String user, String pwd) {
		 SASLAuthentication.supportSASLMechanism("PLAIN");
	     ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
	     XMPPConnection conn = new XMPPConnection(connConfig);
	     try {
	            conn.connect();
	            Log.i("XMPPClient", "[SettingsDialog] Connected to " + conn.getHost());
	        	conn.login(user, pwd);
	            Log.i("XMPPClient", "Logged in as " + conn.getUser());
	            
	            Presence presence = new Presence(Presence.Type.available, "", 24, Presence.Mode.chat);
	            conn.sendPacket(presence);
	            
	            setConnection(conn);
	     } catch (XMPPException ex) {
	            Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + conn.getHost());
	            setConnection(null);
	     }
	}
	
	public boolean isOnline(Context ctx) {
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

	    if (info == null || !info.isConnected()) {
	        return false;
	    }
	    //if (info.isRoaming()) {
	        // here is the roaming option you can change it if you want to
	        // disable internet while roaming, just return false
	    //    return false;
	    //}
	    return true;
	}

	
	

	
	
	//COMMON FUNCTIONS
	private void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}
	public static void waiting(int n) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		Log.i("INSIDE WAITING", Integer.toString(n));
		do {
			t1 = System.currentTimeMillis();
		} while ((t1 - t0) < (n * 1000));
	}
	
	public void onDestroy() {
		unregisterReceiver(mmsMonitorBroadcastReceiver);
		super.onDestroy();
	}
}
