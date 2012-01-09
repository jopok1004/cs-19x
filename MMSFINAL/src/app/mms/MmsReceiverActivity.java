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
import android.os.Debug;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class MmsReceiverActivity extends Activity {
	File file = new File("/sdcard/mmscolumns.txt");
	File fileoutput = new File("/sdcard/outputreceiver.txt");
	FileWriter fw = null;
	FileWriter fw1 = null;
	BufferedWriter bw = null;
	BufferedWriter bw1 = null;

	HashMap<Integer, String> al = new HashMap<Integer, String>();
	String phoneNum;
	String fileType;
	String fileName;
	int initial;
	int end;
	int size;
	int alsize = 0;
	boolean started = false;
	TelephonyManager Tel;
	MyPhoneStateListener MyListener;
	Time time;
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
		time = new Time();
		if (started == false) {
			Debug.startMethodTracing("mmsreceiver");
			started = true;
		}
		try {
			fw1 = new FileWriter(fileoutput);
			bw1 = new BufferedWriter(fw1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MyListener = new MyPhoneStateListener();
		Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		getContentResolver().delete(Uri.parse("content://mms"), null, null);
		super.onCreate(savedInstanceState);
		getContentResolver().delete(Uri.parse("content://mms"), null, null);
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

		if (bw1 != null) {
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
					.query(Uri.parse("content://mms/inbox"), null, null, null,
							null);

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

			curPart = this
					.getApplicationContext()
					.getContentResolver()
					.query(Uri.parse("content://mms/part"), null, null, null,
							null);
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
							time.setToNow();
							bw1.write(time.toString() + " : packet num:"
									+ al.size() + "\n");
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
												al.put(pNum, packets2[1]);
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
				
			}
			curPart.close();
			// for (int k = 0; k < mid.size(); k++) {
			// getContentResolver().delete(
			// Uri.parse("content://mms/inbox/" + mid.get(k)), null, null);
			//
			// }
			alsize = al.size();
			if (tempalsize != alsize) {
				Log.i("MMS", "NARECEIVE KO NA SI MMS");
				sendSMS(phoneNum, "&%mmsreceived");
				getContentResolver().delete(Uri.parse("content://mms"),
						null, null);
			}
		}
		
	}

	public void onDestroy() {
		unregisterReceiver(mmsMonitorBroadcastReceiver);
		bw1 = null;
		super.onDestroy();
	}

	public void receiveFile() {
		long t1, t2;
		if (bw1 != null) {

			if (al.size() == size) {
				try {

					FileWriter fw2 = new FileWriter(new File(
							"/sdcard/decode.txt"));
					BufferedWriter bw2 = new BufferedWriter(fw2);
					for (int i = 0; i < size; i++) {
						bw2.write(al.get(i) + "\n");

					}
					al.clear();
					bw2.close();
					fw2.close();

					Log.i("WRITING TO FILE", "FILEWRITER");
					time.setToNow();
					t1 = time.toMillis(true);
					Base64FileDecoder.decodeFile("/sdcard/decode.txt",
							"/sdcard/file." + fileType + ".gz");
					time.setToNow();
					t2 = time.toMillis(true);
					bw1.write("Decoding T2-T1: " + (t2 - t1) + "\n");

					time.setToNow();
					bw1.write(time.toString()
							+ " : after decode and before unzip\n");

					// File fl = new File("/sdcard/decode.txt");
					// fl.delete();
					time.setToNow();
					t1 = time.toMillis(true);
					compression.decompressGzip("/sdcard/file." + fileType
							+ ".gz");
					time.setToNow();
					t2 = time.toMillis(true);
					bw1.write("Decompressing T2-T1: " + (t2 - t1) + "\n");
					bw1.write(time.toString() + " : after decompress\n");
					bw1.write("After Decompression: T2-T1: " + (t2 - t1));
					File fl = new File("/sdcard/file." + fileType + ".gz");
					fl.delete();
					bw1.close();
					fw1.close();
					al.clear();
					bw1 = null;
					Log.i("DONE!!!", "DONE");
					sendSMS(phoneNum, "&%done");
					Debug.stopMethodTracing();
					this.finish();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// sreceived=true;
			}
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
					Toast.makeText(getApplicationContext(),
							"SMS not delivered", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		Log.i("sms sent", "after sms sending");
	}

	protected void onPause() {
		super.onPause();
		Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
	}

	/* Called when the application resumes */
	@Override
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
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			time.setToNow();
			if (fw1 != null && bw1 != null) {
				try {
					bw1.write("Signal Strength"
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
}