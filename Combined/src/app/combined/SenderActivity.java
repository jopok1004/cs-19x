package app.combined;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

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
	private int randomNum;	private Random random = null;		// for MMS
	private int sub;
	private static final int SEND_MMS = 1003;
	ProgressDialog dialog;
	
	private XMPPConnection connection; //for 3G connection
	
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		phoneNum = intent.getStringExtra("phoneNum");
		packetCount = intent.getIntExtra("packetCount", 0);
		packetList = intent.getStringArrayListExtra("arraylist");
	}

	public void onNewIntent(Intent intent){
		//SMS
		if ((intent.getStringExtra("start?").toString()).equals("start sending")) {

			while(tracker < packetCount){
				//sms(intent.getStringExtra("phoneNum").toString(), 0);	
				//DEPENDE SA KUNG ANONG CHANNEL
//				if(){
//					
//					
//				}else{
//					//SEND VIA MMS
//					//sendViaMms(StartIndex)
//				}
				
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
				e.printStackTrace();
			}
		}
		//MMS
		if ((intent.getStringExtra("start?").toString())
				.equals("sendAnotherMms")) {
			try {
				if (tracker < packetCount) {
					send1mms(phoneNum);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// ################################################################################################### //
	//FUNCTIONS FOR SMS CHANNEL
	public void sendViaSms(String phoneNum, int startIndex) throws IOException{
		tracker = startIndex;
		send10(phoneNum);
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
	
	// ################################################################################################### //
	//FOR MMS CHANNEL
	private void sendViaMms(int startIndex){
		randomNum = random.nextInt(1000);
		sendSMS(phoneNum, "%& sendViaMms" + startIndex); // EDIT, REMOVE SUB
		sendSMS(phoneNum, "MESSAGE"); // %&sendViaMms
		Log.i("FINISHED", "DONE SENDING SMS");
		try {
			Log.i("SENDING MMS", "SENDING MMS");
			send1mms(phoneNum);
		} catch (IOException e1) {
			e1.printStackTrace();
		} 	
	}
	private void send1mms(String phoneNum) throws IOException {

		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage("Sending MMS...");
		dialog.setCancelable(false);
		dialog.show();
		Log.i("CHLOE", "I AM AT PARSER NOW");
		try {
			// dialog.show(Mms2Activity.this, "Sending SMS", "Please Wait");
			// 1024b * 300kb = 307200/160 char = 1920 packets

			String msg = "";
			for (int i = 0; i < 100 && tracker < packetCount; i++) {
				msg = msg + "&% " + tracker + " " + packetList.get(tracker)
						+ "\n";
				tracker++;
				Log.i("SUBMESSAGE", msg);
			}
			Log.i("parser", "before mms sending");
			
			sendMMS(phoneNum, msg);
			waiting(20);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		dialog.cancel();

	}


	private void sendMMS(String phoneNumber, String message) throws IOException {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra("sms_body", message);
		intent.putExtra("address", phoneNumber);
		intent.putExtra("subject", "mms" + randomNum);
		intent.setType("image/*");
		intent.setClassName("com.android.mms",
				"com.android.mms.ui.ComposeMessageActivity");
		startActivityForResult(intent, SEND_MMS);

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
			finish();
		}else {
			this.connection = connection;
		}
	}
	
	public void sendBy3G (String to, int startIndex) {
		Roster r = getConnection().getRoster();
		ChatManager chatManage = getConnection().getChatManager();
        Chat nchat = chatManage.createChat(to, new Sender3GListener(this));
        
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
}

