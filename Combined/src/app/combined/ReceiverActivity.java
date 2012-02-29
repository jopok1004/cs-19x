package app.combined;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Debug;
import android.telephony.*;
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
	HashMap<Integer, String> al = new HashMap();
	TelephonyManager Tel;
	MyPhoneStateListener MyListener;
	int size; // number of messages to be received
	String fileT;
	int messageSize = 10;
	long last, current;
	private XMPPConnection connection;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receiver);

		MyListener = new MyPhoneStateListener();
		Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		Intent intent = getIntent();
		rcvd = new SmsReceiver();
		btnSendConfirmation = (Button) findViewById(R.id.btnSendConfirmation);
		txtPhoneNo = (EditText) findViewById(R.id.phoneNumberText);

		btnSendConfirmation.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				phoneNo = txtPhoneNo.getText().toString();
				if (phoneNo.length() > 0) {
					if(haveInternet(getBaseContext())){
						//sendSMS(phoneNo, "%& start 1");
					}else{
						//sendSMS(phoneNo, "%& start 0");
					}
					Toast.makeText(getBaseContext(),
							"Please do not close this application.",
							Toast.LENGTH_SHORT).show();
					Debug.startMethodTracing("receiver1", 32000000);
					btnSendConfirmation.setClickable(false);

				} else
					Toast.makeText(getBaseContext(),
							"Please enter phone number.", Toast.LENGTH_SHORT)
							.show();
			}
		});
	}
	//FOR SMS
	public void onNewIntent(Intent intent) {
		Log.i("INTENT", intent.getStringExtra("start?").toString());
		if ((intent.getStringExtra("start?").toString()).equals("getConfirm")) {
			Log.i("GETCONFIRM", "GETCONFIRM");
			size = intent.getIntExtra("size", 10);
			fileT = intent.getStringExtra("fileType");

		}
		if ((intent.getStringExtra("start?").toString()).equals("check10")) {
			Boolean okay = true;
			Log.i("check10", "check for missing packets");
			waiting(30);
			String resend = "";
			int currentp;
			currentp = Integer.parseInt(intent.getStringExtra("tracker"));
			resend = "%&resend ";
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
				sendSMS(phoneNo, "%&resend none");
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
	
	public void logIn () {
		LogInSettings dialog;
        dialog = new LogInSettings(this);
        if (isOnline(this)) {
            dialog.show();
        }else {
        	Log.e("Receiver:3GConnection", "No network connection available");
        	finish();
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
	
	public boolean haveInternet(Context ctx) {

	    NetworkInfo info = (NetworkInfo) ((ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

	    if (info == null || !info.isConnected()) {
	        return false;
	    }
	    if (info.isRoaming()) {
	        // here is the roaming option you can change it if you want to
	        // disable internet while roaming, just return false
	        return false;
	    }
	    return true;
	}
}
