package app.mms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class MmsReceiverActivity extends Activity {
	File file = new File("/sdcard/mmscolumns.txt");
	FileWriter fw = null;
	BufferedWriter bw = null;
	HashMap<Integer, String> al = new HashMap<Integer, String>();
	String phoneNum;
	String fileType;
	String fileName;
	int initial;
	int end;
	int size;
	int alsize = 0;
	public static final String MMSMON_RECEIVED_MMS = "MMStesting.intent.action.MMSMON_RECEIVED_MMS";

	Uri mmsInURI = Uri.parse("content://mms-sms");
	BroadcastReceiver mmsMonitorBroadcastReceiver;
	ContentObserver mmsObserver = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {

			Thread mmsNotify = new Thread() {
				@Override
				public void run() {
					Intent mIntent = new Intent(MMSMON_RECEIVED_MMS);
					sendBroadcast(mIntent);
					super.run();
				}
			};
			mmsNotify.start();
			super.onChange(selfChange);
			// try here

			Log.i("MMS Received", "MMS RECEIVED HAHA");
			try {
				Log.i("SEARCHING", "SEARCHING MMS AGAIN");
				checkMMSMessages();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// end try

		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getContentResolver().delete(Uri.parse("content://mms"),null,null);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receiver);
		// TRIAL
		Intent intent = this.getIntent();
		Log.i("NEWINTENT", "NEW INTENT " + intent.getStringExtra("start?"));
		if ((intent.getStringExtra("start?")).equals("startMmsReceive")) {
			Log.i("RECEIVED SMS", "RECEIVED SMS");
			phoneNum = intent.getStringExtra("phoneNum");
			Log.i("Phone num", phoneNum);
			initial = intent.getIntExtra("initial", 0);
			Log.i("initial", Integer.toString(initial));
			end = intent.getIntExtra("end", 0);
			Log.i("end", Integer.toString(end));
			size = end - initial;
			Log.i("size", "SIZE: " + Integer.toString(size));

			fileType = intent.getStringExtra("filetype");
			Log.i("fileType", fileType);

		}
		mmsMonitorBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				Log.i("MMS Received", "MMS RECEIVED HAHA");
				try {
					Log.i("SEARCHING", "SEARCHING MMS AGAIN");
					checkMMSMessages();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(MMSMON_RECEIVED_MMS);

		registerReceiver(mmsMonitorBroadcastReceiver, mIntentFilter);

		getApplicationContext().getContentResolver().registerContentObserver(
				mmsInURI, true, mmsObserver);
		getApplicationContext().getContentResolver().notifyChange(mmsInURI,
				mmsObserver);
		// end TRIAL

	}

	public void onNewIntent(Intent intent) {
		Log.i("NEWINTENT", "NEW INTENT " + intent.getStringExtra("start?"));
		if ((intent.getStringExtra("start?")).equals("startMmsReceive")) {
			Log.i("RECEIVED SMS", "RECEIVED SMS");
			phoneNum = intent.getStringExtra("phoneNum");
			Log.i("Phone num", phoneNum);
			initial = intent.getIntExtra("initial", 0);
			Log.i("initial", Integer.toString(initial));
			end = intent.getIntExtra("end", 0);
			Log.i("end", Integer.toString(end));
			size = end - initial;
			Log.i("size", "SIZE: " + Integer.toString(size));

			fileType = intent.getStringExtra("filetype");
			Log.i("fileType", fileType);

		}
	}

	private void checkMMSMessages() throws IOException {
		int tempalsize = alsize;
		alsize = al.size();
		String[] coloumns = null;
		String[] values = null;
		ArrayList<Integer> mid = new ArrayList<Integer>();
		fw = new FileWriter(file);

		bw = new BufferedWriter(fw);

		Cursor curPart = this
				.getApplicationContext()
				.getContentResolver()
				.query(Uri.parse("content://mms/inbox"), null, null, null, null);

		bw.write("TABLE 3\n");
		for (int i = 0; i < curPart.getColumnCount(); i++) {
			bw.write("column " + i + ": " + curPart.getColumnName(i));
			bw.write("\n");
		}

		bw.write("SUBJECTS " + curPart.getCount());
		curPart.moveToFirst();
		for (int i = 0; i < curPart.getCount(); i++) {
			bw.write("Subject " + i + ": "
					+ curPart.getString(curPart.getColumnIndex("sub"))
					+ "MESSAGE ID"
					+ curPart.getInt(curPart.getColumnIndex("_id")));
			bw.write("\n");

			// add to List the mid of the MMS that is currently in the
			// inbox

			mid.add(curPart.getInt(curPart.getColumnIndex("_id")));

			curPart.moveToNext();
		}

		for (int i = 0; i < mid.size(); i++) {
			bw.write("MID: " + mid.get(i) + "\n");
		}
		curPart.moveToFirst();

		curPart = this.getApplicationContext().getContentResolver()
				.query(Uri.parse("content://mms/part"), null, null, null, null);
		bw.write("CURPART TEXT\n");

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
								//bw.write("MID: " + mid.get(i) + "\tTEXT:"
								//		+ text + "\n");
								for (int j = 0; j < packets.length; j++) {
								//	bw.write("packet " + j + ": " + packets[j]
								//			+ "\n");
									Log.i("PACKETS", packets[j]);
									if (packets[j] != null) {
										packets2 = packets[j].split(" ");
										if (!packets2[0].equals("")) {
											Log.i("packets2[0]", packets2[0]);
											Log.i("packets2[1]", packets2[1]);
											int pNum = Integer
													.parseInt(packets2[0]);
											Log.i("pNum", "pNum: " + pNum);
											Log.i("packet", packets2[1]);
											al.put(pNum, packets2[1]);
											Log.i("AL SIZE","AL SIZE: "+al.size());
											if (al.size() == size) {
												receiveFile();
											}
										}

									}

								}
							}
						}

					}
					alsize = al.size();
					if(tempalsize!=alsize){
						sendSMS(phoneNum,"&%received");
					}
				}

			} while (curPart.moveToNext());
		}
		curPart.close();
		
		
		
	}
	public void onDestroy(){
		unregisterReceiver(mmsMonitorBroadcastReceiver);
		super.onDestroy();
	}
	public void receiveFile() {
		if (al.size() == size) {
			try {

				FileWriter fw1 = new FileWriter(new File("/sdcard/decode.txt"));
				BufferedWriter bw1 = new BufferedWriter(fw1);
				for (int i = 0; i < size; i++) {
					bw1.write(al.get(i) + "\n");

				}
				al.clear();
				bw1.close();
				fw1.close();

				Log.i("WRITING TO FILE", "FILEWRITER");

				Base64FileDecoder.decodeFile("/sdcard/decode.txt",
						"/sdcard/file." + fileType + ".gz");
				// File fl = new File("/sdcard/decode.txt");
				// fl.delete();
				compression.decompressGzip("/sdcard/file." + fileType + ".gz");
				File fl = new File("/sdcard/file." + fileType + ".gz");
				fl.delete();
				bw.close();
			
				Log.i("DONE!!!", "DONE");
				
				this.finish();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// sreceived=true;
		}
	}
	
	private void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getApplicationContext(), "SMS sent",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getApplicationContext(), "Generic failure",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getApplicationContext(), "No service",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getApplicationContext(), "Null PDU",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getApplicationContext(), "Radio off",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getApplicationContext(), "SMS delivered",
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getApplicationContext(), "SMS not delivered",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		Log.i("sms sent", "after sms sending");
	}
}