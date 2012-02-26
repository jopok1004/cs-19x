package app.combined;

import java.io.IOException;
import java.util.ArrayList;

import org.jivesoftware.smack.XMPPConnection;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SenderActivity extends Activity {
	private String phoneNum;
	private ArrayList<String> packetList = new ArrayList<String>();
	private int packetCount;
	private int tracker; 		//current packet number
	private Boolean done; 		//to check for end of file sharing
	private Boolean check10Received; // for SMS Protocol
	private int totalresends;		// for SMS Protocol, set to zero every after start of new SMS mode
	private String sen = "";		// for SMS Protocol, i forgot kung para saan to. LOL
	
	ProgressDialog dialog;
	
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		phoneNum = intent.getStringExtra("phoneNum");
		packetCount = intent.getIntExtra("packetCount", 0);
		packetList = intent.getStringArrayListExtra("arraylist");
	}

	
	
	//FUNCTIONS FOR SMS PROTOCOL
	public void sms(String phoneNum, int startIndex) throws IOException{
		tracker = startIndex;
		send10(phoneNum);
	}
	
	public void onNewIntent(Intent intent){
		if ((intent.getStringExtra("start?").toString()).equals("start sending")) {

			try {
				//sms(intent.getStringExtra("phoneNum").toString(), 0);	
				//DEPENDE SA KUNG ANONG CHANNEL
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if ((intent.getStringExtra("start?").toString()).equals("done receiving")) {
			done = true;
			Toast.makeText(getBaseContext(), "Done Sending", Toast.LENGTH_SHORT);
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

				for (int i = 0; i < num.length; i++) {

					if (!num[i].equals(" ") || !num[i].equals("") || !num[i].equals("\n")) {
						Log.e("-----NUM[i]-----", num[i]);
						int j = Integer.parseInt(num[i]);
						
						Log.e("RESEND LIST", num[i]);
						sendSMS(phoneNum, "&% " + j + " " + packetList.get(j));
						Log.i("RESENT", packetList.get(j));
						totalresends++;
					}

				}

			}

			try {
				Log.i("send10", "Before send10");
				send10(phoneNum);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private void send10(String phoneNumber) throws IOException {
		String submessage = new String();
		String headerBegin = new String();
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage("Sending SMS...");
		dialog.setCancelable(false);
		dialog.setProgress(0);
		Log.i("send10", "I AM AT send10");
		dialog.show();

		// dialog.show(SmsMessagingActivity.this, "Sending SMS", "Please Wait");
		sen = "sending";
		for (int counter = 0; counter < 10; counter++) {
			Log.i("send10", "inside send10 for loop");
			headerBegin = "&% " + tracker + " ";
			submessage = headerBegin + packetList.get(tracker);
			tracker++;
			Log.i("SUBMESSAGE", submessage);
			Log.i("PHONE NUMBER", phoneNumber);
			sendSMS(phoneNumber, submessage);
			waiting(3);

		}
		check10Received= false;
		sendSMS(phoneNumber, "%&check10 " + tracker);
		Log.i("After send tracker", "tracker" + tracker);
	
		Thread thread = new waitThread();
		thread.start();
		dialog.cancel();

	}
	class waitThread extends Thread {
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
				sendSMS(phoneNum, "%&check10 " + tracker);
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
	}
}

