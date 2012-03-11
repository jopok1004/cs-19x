package app.combined;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
public class SenderActivity extends Activity {
	private String phoneNum;
	private ArrayList<String> packetList = new ArrayList<String>();
	private BroadcastReceiver threeGMonitorBroadcastReceiver;
	private int currentChannel; // 0 - SMS 1- MMS 2 - 3G
	private int packetCount;
	private int tracker= 0; 		//current packet number
	private Boolean done = false; 		//to check for end of file sharing
	private Boolean check10Received; // for SMS Protocol
	private int send10Resends;		// for SMS Protocol, number of resends per 10 packets
	private Boolean mmsReceived = false;
	private Boolean receiverIsOnline = false;
	private Boolean started = false;
	private static final int SEND_MMS = 1003;
	private Button btnDisconnect;
	private TextView txtCurrentChannel;
	private TextView txtSMS;
	private TextView txtMMS;
	TextView txt3G;
	private TextView txtTotalPackets;
	ProgressDialog dialog;
	private int smsCount = 0;
	private int mmsCount = 0;
	int threeGCount = 0;
	Chat nchat=null;
	Sender3GListener sender3GListener;
	private XMPPConnection connection; //for 3G connection
	private String username = "jvbsantos@gmail.com";
	private String password = "jayvee14";
	private String text="0";
	IntentFilter gIntentFilter = new IntentFilter();
	
	//variables for log files
	private Time time= new Time();
	private long t1, t2, t3;
	private FileWriter logfw = null, logfw1 = null;
	private BufferedWriter logbw = null, logbw1 = null;
	private File senderLog = new File("/sdcard/senderLog.txt");
	private File senderSignal = new File("/sdcard/senderSignal.txt");
	//for signal strength
	TelephonyManager Tel;
	MyPhoneStateListener MyListener;
	Handler handler;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Debug.startMethodTracing("sender");
		setContentView(R.layout.senderactivity);
		btnDisconnect =(Button) this.findViewById(R.id.btnConnect);
		txtCurrentChannel = (TextView)this.findViewById(R.id.txtCurrentChannel);
		txtSMS = (TextView)this.findViewById(R.id.txtSMS);
		txtMMS = (TextView)this.findViewById(R.id.txtMMS);
		txt3G = (TextView)this.findViewById(R.id.txt3G);
		txtTotalPackets = (TextView)this.findViewById(R.id.txtTotalPackets);
		Log.e("SENDER ACT","SENDER ACT");
		Intent intent = getIntent();
		MyListener = new MyPhoneStateListener();
		Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		t1 = intent.getLongExtra("temp1", 0);
		t2 = intent.getLongExtra("temp2", 0);
		phoneNum = intent.getStringExtra("phoneNum");
		packetCount = intent.getIntExtra("packetCount", 0);
		packetList = intent.getStringArrayListExtra("arraylist");
		handler = new Handler();
		try {
			logfw = new FileWriter(senderLog);
			logbw = new BufferedWriter(logfw);
			logfw1 = new FileWriter(senderSignal);
			logbw1 = new BufferedWriter(logfw1);
			time.setToNow();
			t1= time.toMillis(true);
			logbw.write(time.toString() + "SENDER START\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threeGMonitorBroadcastReceiver = new BroadcastReceiver() {
			
			public void onReceive(Context context, Intent intent) {
			     Log.d("app","Network connectivity change");
			     if(intent.getExtras()!=null) {
			        NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
			        if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED&&started) {
			            Log.i("app","Network "+ni.getTypeName()+" connected");
			            if(receiverIsOnline){
			            	currentChannel = 2;
			            	Log.e("BRECEIVER","I AM AT BRECEIVER");
			            	handler.post(new Runnable() {
								
								public void run() {
									txtCurrentChannel.setText("3G");
								}
							});
			            	
			            	sendBy3G("dummy19x@gmail.com",tracker);
			            }else{
			            	currentChannel = 1;
			            	Log.e("BRECEIVER","I AM AT BRECEIVER");
			            	handler.post(new Runnable() {
								
								public void run() {
									txtCurrentChannel.setText("MMS");
								}
							});
			            	
			            	sendViaMms(tracker);
			            }
			            
			            //send sms na connected
			        }
			     }
			     if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
			            Log.e("app","There's no network connectivity");
			            currentChannel = 1;
			            Log.e("BRECEIVER","I AM AT BRECEIVER");
			            handler.post(new Runnable() {
							
							public void run() {
								txtCurrentChannel.setText("MMS");
							}
						});
		            	
			           
			            sendViaMms(tracker);
			          //send sms na di connected
			     }
				
			}
		};
		IntentFilter gIntentFilter = new IntentFilter();
		gIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(threeGMonitorBroadcastReceiver,gIntentFilter);
	}

	public void onNewIntent(Intent intent){
		//SMS
		Log.e("NEW","NEW INTENT");
		if ((intent.getStringExtra("start?").toString()).equals("start sending")) {	
			Log.e("START SENDING", "START SENDING");
			//sms(intent.getStringExtra("phoneNum").toString(), 0);	
			//DEPENDE SA KUNG ANONG CHANNEL
			if(intent.getStringExtra("isOnline").equals("1")) {
				receiverIsOnline = true;
			}
			else{
				receiverIsOnline = false;
			}
			
			
			if(isOnline(getBaseContext()) && intent.getStringExtra("isOnline").equals("1")){
				
				
				currentChannel = 2;
				handler.post(new Runnable() {
					
					public void run() {
						txtCurrentChannel.setText("3G");
					}
				});
            	
				
				sendBy3G("dummy19x@gmail.com", tracker);
			}else{
				currentChannel = 1;
				handler.post(new Runnable() {
					
					public void run() {
						txtCurrentChannel.setText("MMS");
					}
				});
            	
				sendViaMms(tracker);
			}
			started = true;
			
		}
		
		if ((intent.getStringExtra("start?").toString()).equals("done receiving")) {
			Debug.stopMethodTracing();
			time.setToNow();
			t3 = time.toMillis(true);
			try {
				logbw.write("Receiving time:"+ (t3-t2) +"\n");
				logbw.write("Processing time:"+ (t2-t1) +"\n");
				logbw.write("Total time:"+ (t3-t1) +"\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			done = true;
			Toast.makeText(getBaseContext(), "Done Sending", Toast.LENGTH_SHORT);
			this.finish();
		}
		
		if ((intent.getStringExtra("start?").toString()).equals("sendAgain")) {
			check10Received= true;
			Log.i("sendAgain", "inside sendAgain");
			String resend = intent.getStringExtra("resendPackets");
			if (resend.equals("none")) {
				// do nothing
			} else {
				String[] num;
				num = resend.split(" ");
				send10Resends = 0;
				for (int i = 0; i < num.length; i++) {

					if (!num[i].equals(" ") || !num[i].equals("") || !num[i].equals("\n")) {
						Log.e("-----NUM[i]-----", num[i]);
						int j = Integer.parseInt(num[i]);
						
						Log.e("RESEND LIST", num[i]);
						sendSMS(phoneNum, "&% " + j + " " + packetList.get(j));
						Log.i("RESENT", packetList.get(j));
						send10Resends++;
					}

				}

			}

			try {
				Log.i("send10", "Before send10");
				if(currentChannel == 0){
					handler.post(new Runnable() {
						
						public void run() {
							txtCurrentChannel.setText("SMS");
						}
					});
	            	
					if(send10Resends<5){
						send10(phoneNum);
						//continue with sms since resends < 5
					}else{
						//shift to mms
						currentChannel = 1;
						handler.post(new Runnable() {
							
							public void run() {
								txtCurrentChannel.setText("MMS");
							}
						});
		            	
						sendViaMms(tracker);
					}
					
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//MMS
		if ((intent.getStringExtra("start?").toString())
				.equals("sendAnotherMms")) {
			
			Log.e("MMS","SEND ANOTHER MMS");
			mmsReceived= true;
			try {
				if (tracker < packetCount && currentChannel == 1) {
					handler.post(new Runnable() {
						
						public void run() {
							txtCurrentChannel.setText("MMS");
						}
					});
	            	
					send1mms(phoneNum);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//3G
		if ((intent.getStringExtra("start?").toString()).equals("receiverConnectivity")) {
			if(intent.getStringExtra("isOnline").toString().equals("1")){
				receiverIsOnline= true;
				if(isOnline(this)) {
					currentChannel = 2;
					handler.post(new Runnable() {
						
						public void run() {
							txtCurrentChannel.setText("3G");
						}
					});
	            	
					sendBy3G("dummy19x@gmail.com",tracker);
				}
				
			}else{
				receiverIsOnline= false;
				currentChannel = 1;
				handler.post(new Runnable() {
					
					public void run() {
						txtCurrentChannel.setText("MMS");
					}
				});
            	
				sendViaMms(tracker);
			}
		}
	}
	
	// ################################################################################################### //
	//FUNCTIONS FOR SMS CHANNEL

	public void sendViaSms(String phoneNo, int startIndex) throws IOException{

		sendSMS(phoneNum, "%& sendViaSms" + startIndex);
		if(currentChannel == 0){
			handler.post(new Runnable() {
				
				public void run() {
					txtCurrentChannel.setText("SMS");
				}
			});
        	
			send10(phoneNo);
		}
		
	}

	private void send10(String phoneNo) throws IOException {
		String submessage = new String();
		String headerBegin = new String();
		
		Log.i("send10", "I AM AT send10");
		

		// dialog.show(SmsMessagingActivity.this, "Sending SMS", "Please Wait");
		for (int counter = 0; counter < 10; counter++) {
			Log.i("send10", "inside send10 for loop");
			headerBegin = "&% " + tracker + " ";
			submessage = headerBegin + packetList.get(tracker);
			tracker++;
			
			handler.post(new Runnable() {
				
				public void run() {
					txtTotalPackets.setText(Integer.toString(tracker));
				}
			});
        	
			
			Log.i("SUBMESSAGE", submessage);
			Log.i("PHONE NUMBER", phoneNo);
			sendSMS(phoneNo, submessage);
			waiting(3);

		}
		check10Received= false;
		sendSMS(phoneNo, "%& check10 " + tracker);
		Log.i("After send tracker", "tracker" + tracker);
	
		Thread thread = new smsWaitThread();
		thread.start();
		time.setToNow();
		logbw.write(time.toString()  + "Sending via SMS INSIDE send10\n");

	}
	class smsWaitThread extends Thread {
	    // This method is called when the thread runs
	    public void run() {
	    	long t0, t1;
			t0 = System.currentTimeMillis();
			do {
				t1 = System.currentTimeMillis();
			} while ((t1 - t0) < (90 * 1000) && check10Received==false && done==false); //wait for 90seconds
			if(check10Received||done==true){
				//do nothing
			}else{
				Log.i("resend check10", "tracker" + tracker);
				sendSMS(phoneNum, "%& check10 " + tracker);
				//resend check10
			}
			
	    }
	    
	}


	
	//FUNCTIONS FOR ALL CHANNELS
	
	public static void waiting(int n) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		Log.i("INSIDE WAITING", Integer.toString(n));
		do {
			t1 = System.currentTimeMillis();
		} while ((t1 - t0) < (n * 1000));
	}

	// ---sends an SMS message to another device---
	private void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		Log.e("SMS", "SMS Sent");
		this.smsCount++;
		handler.post(new Runnable() {
			
			public void run() {
				txtSMS.setText(Integer.toString(smsCount));
			}
		});
    	
		
	}
	
	// ################################################################################################### //
	//FOR MMS CHANNEL
	private void sendViaMms(int startIndex){
		sendSMS(phoneNum, "%& sendViaMms" + startIndex); // EDIT, REMOVE SUB

		Log.i("FINISHED", "DONE SENDING SMS");
		try {
			Log.i("SENDING MMS", "SENDING MMS");
			if(currentChannel == 1){
				handler.post(new Runnable() {
					
					public void run() {
						txtCurrentChannel.setText("MMS");
					}
				});
				
				send1mms(phoneNum);
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} 	
	}
	private void send1mms(String phoneNum) throws IOException {

//		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//		dialog.setMessage("Sending MMS...");
//		dialog.setCancelable(false);
//		dialog.show();
		Log.i("CHLOE", "I AM AT PARSER NOW");
		try {
			// dialog.show(Mms2Activity.this, "Sending SMS", "Please Wait");
			// 1024b * 300kb = 307200/160 char = 1920 packets

			String msg = "";
			for (int i = 0; i < 100 && tracker < packetCount; i++) {
				msg = msg + "&% " + tracker + " " + packetList.get(tracker)
						+ "\n";
				tracker++;
				handler.post(new Runnable() {
					
					public void run() {
						txtTotalPackets.setText(Integer.toString(tracker));
					}
				});
				
				Log.i("SUBMESSAGE", msg);
			}
			Log.i("parser", "before mms sending");
			mmsReceived= false;
			sendMMS(phoneNum, msg);
			Thread thread = new mmsWaitThread();
			thread.start();
			waiting(20);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		time.setToNow();
		logbw.write(time.toString()  + "Sending via MMS\n");
		mmsCount++;
		handler.post(new Runnable() {
			
			public void run() {
				txtMMS.setText(Integer.toString(mmsCount));
			}
		});
		
	}


	private void sendMMS(String phoneNumber, String message) throws IOException {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra("sms_body", message);
		intent.putExtra("address", phoneNumber);
		intent.putExtra("subject", "mms");
		intent.setType("image/*");
		intent.setClassName("com.android.mms",
				"com.android.mms.ui.ComposeMessageActivity");
		startActivityForResult(intent, SEND_MMS);
		
	}
	
	class mmsWaitThread extends Thread {
	    // This method is called when the thread runs
	    public void run() {
	    	long t0, t1;
			t0 = System.currentTimeMillis();
			do {
				t1 = System.currentTimeMillis();
			} while ((t1 - t0) < (300 * 1000) && mmsReceived==false && done==false); //wait for 5 minutes
			if(mmsReceived||done==true){
				//do nothing
			}else{
				Log.i("shift to sms", "shift to sms");
				
				try {
					currentChannel = 0;
					handler.post(new Runnable() {
						
						public void run() {
							txtCurrentChannel.setText("SMS");
						}
					});
					
					sendViaSms(phoneNum, tracker);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//SHIFT TO SMS
				
			}
			
	    }
	    
	}
	// ################################################################################################### //
	//FOR 3G CHANNEL
	
	public ArrayList<String> getPacketList() {
		return this.packetList;
	}
	
	public Integer getTracker() {
		return this.tracker;
	}
	
	public void setTracker(int track) {
		this.tracker = track;
	}

	public XMPPConnection getConnection() {
		return connection;
	}

	public void setConnection(XMPPConnection connection) {
		if (connection == null) {
			Log.e("Receiver:3GConnection", "Connection failure");
		}else {
			this.connection = connection;
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

	public void sendBy3G (String to, int startIndex) {
		
		logIn();
		while(getConnection() == null) {
			//do nothing
		}
		sendSMS(phoneNum, "%& sendVia3G");
		waiting(30);
		Roster r = getConnection().getRoster();
		ChatManager chatManage = getConnection().getChatManager();
		sender3GListener =  new Sender3GListener(this);
        nchat = chatManage.createChat(to,sender3GListener);
        if (r.getPresence(to).isAvailable()) {
        	Log.i("XMPPSender", "ONLINE: Available");
        	Message message = new Message();
			message.setType(Message.Type.chat);
			
			//message.setBody("%&sendfile " + packetList.size() + " " + getFileType());
			message.setBody("%&start3G");
			
			try {
				nchat.sendMessage(message);
				Log.e("XMPPSender:Sending", "Sending text [" + message.getBody() + "] SUCCESS");
			} catch (XMPPException e) {
				Log.e("XMPPSender:Sending", "Sending text [" + message.getBody() + "] FAILED");
			}
        }else {
        	Log.i("XMPPSender", "OFFLINE si " + to);
        }
        
	}
	
	public void logIn () {
		Log.e("LOGIN","LOGIN");
		
		
		
        if (isOnline(this)) {
        	if (getUsername().equals("null") && getPassword().equals("null")) {
        		LogInSettings lDialog;
                lDialog = new LogInSettings(this);
                Log.e("SHOW","SHOWING DIALOG BOX");
                lDialog.show();
        	}else {
        		establishConnection(getUsername(), getPassword());
        	}
        }else {
        	Log.e("Receiver:3GConnection", "No internet connectivity available");
        }
		
	}
	
	public void logOut() {
		if (this.connection != null) {
			this.connection.disconnect();
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
	
	public void disconnectWifi(View view) {
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(wifi.isWifiEnabled()) {
			wifi.disconnect();
			wifi.setWifiEnabled(false);
			btnDisconnect.setText("Reconnect");
		}
		else {
			wifi.setWifiEnabled(true);
			btnDisconnect.setText("Disconnect");
			wifi.reconnect();
			
			
		}
		
	}
	public void setText3G(String text2) {
		this.text = text2;
		handler.post(new Runnable() {
			
			public void run() {
				txt3G.setText(text);
			}
		});
		
	}
	public BufferedWriter getWriter() {
		return logbw;
	}
	public void finish() {
		Intent data = new Intent();
		
		setResult(RESULT_OK, data);
		
		super.finish();
	}
	public void onDestroy() {
		unregisterReceiver(threeGMonitorBroadcastReceiver);
		if(nchat !=null){
			nchat.removeMessageListener(sender3GListener);
		}
		
		logOut();
		try {
			logbw.close();
			logbw1.close();
			logfw.close();
			logfw1.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.onDestroy();
	}
	
	// FOR SIGNAL STRENGTH

		protected void onPause() {
			super.onPause();
			Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
		}

		/* Called when the application resumes */
		
		protected void onResume() {
			super.onResume();
			Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}

		/* ÑÑÑÑÑÑÑÑÑÐ */
		/* Start the PhoneState listener */
		/* ÑÑÑÑÑÑÑÑÑÐ */
		private class MyPhoneStateListener extends PhoneStateListener {
			/*
			 * Get the Signal strength from the provider, each tiome there is an
			 * update
			 */
			
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);
				time.setToNow();
				if (logfw1 != null && logbw1 != null) {
					try {
						logbw1.write("Signal Strength"
								+ time.toString()
								+ ": "
								+ String.valueOf(signalStrength
										.getGsmSignalStrength()) + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};/* End of private Class */
		
		//FOR LOG
		
}

