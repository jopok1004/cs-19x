package app.sms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SmsReceiverActivity extends Activity {
	File file;
	FileWriter fw = null;
	BufferedWriter bw = null;
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
	Time time;
	long t1, t2, initial;
	long last, current;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receiver);

		time = new Time();
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
				String message = "%&start";
				if (phoneNo.length() > 0) {
					sendSMS(phoneNo, message);
					Toast.makeText(getBaseContext(),
							"Please do not close this application.",
							Toast.LENGTH_SHORT).show();
					Debug.startMethodTracing("receiver1", 32000000);
					btnSendConfirmation.setClickable(false);

					file = new File("/sdcard/outputreceiver1.txt");
					try {
						fw = new FileWriter(file);
						bw = new BufferedWriter(fw);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else
					Toast.makeText(getBaseContext(),
							"Please enter phone number.", Toast.LENGTH_SHORT)
							.show();
			}
		});
		if ((intent.getStringExtra("start?").toString()).equals("getConfirm")) {
			Log.i("GETCONFIRM", "GETCONFIRM");
			size = intent.getIntExtra("size", 10);
			fileT = intent.getStringExtra("fileType");

		}
	}

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
				if (al.containsKey(currentp - i - 1) == false) {
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

				time.setToNow();
				if (!initialR) {
					initial = time.toMillis(true);
					initialR = true;
				}
				try {
					bw.write(time.toString() + " : Message " + pn
							+ " Received\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				al.put(intent.getIntExtra("packetNum", 1000), intent
						.getStringExtra("message").toString());
				Toast.makeText(getBaseContext(), "Received Packet #" + pn,
						Toast.LENGTH_SHORT).show();

				Log.i("AL SIZE", Integer.toString(al.size()));
				Log.i("SIZE", Integer.toString(size));
				if (al.size() == size) {
					try {
						time.setToNow();
						t1 = time.toMillis(true);
						bw.write(time.toString() + " : Before write to file\n");

						FileWriter fw1 = new FileWriter(new File(
								"/sdcard/decode.txt"));
						for (int i = 0; i < size; i++) {
							fw1.write(al.get(i) + "\n");
						}
						al.clear();
						fw1.close();

						Log.i("WRITING TO FILE", "FILEWRITER");
						time.setToNow();
						bw.write(time.toString() + " : Before decode\n");

						Base64FileDecoder.decodeFile("/sdcard/decode.txt",
								"/sdcard/file." + fileT + ".gz");
						File fl = new File("/sdcard/decode.txt");
						fl.delete();

						time.setToNow();
						bw.write(time.toString()
								+ " : after decode and before unzip\n");

						compression.decompressGzip("/sdcard/file." + fileT
								+ ".gz");
						fl = new File("/sdcard/file." + fileT + ".gz");
						fl.delete();
						time.setToNow();
						t2 = time.toMillis(true);

						bw.write(time.toString() + " : after unzip\n ");
						bw.write(initial
								- t2
								+ " : start of receiveing to before processing\n");
						bw.write(t1 - t2 + " : processing time\n");
						bw.write(initial
								- t2
								+ " : start of receiveing to end of processing\n");
						Log.i("DONE!!!", "DONE");
						Toast.makeText(getBaseContext(),
								"File Received. Check your SD Card",
								Toast.LENGTH_LONG).show();
						bw.close();
						fw.close();

						Debug.stopMethodTracing();
						sendSMS(phoneNo, "%&done");
						this.finish();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					received = true;
				}
			}

		}
	}

	// COPIED METHODS FROM SMSMESSAGINGACTIVITY

	private void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		if (message.equals("%&done")) {
			Toast.makeText(SmsReceiverActivity.this,
					"File Received. Check your SD Card", Toast.LENGTH_LONG);
		}
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		// // ---when the SMS has been sent---
		// registerReceiver(new BroadcastReceiver() {
		// @Override
		// public void onReceive(Context arg0, Intent arg1) {
		// switch (getResultCode()) {
		// case Activity.RESULT_OK:
		//
		// Toast.makeText(getBaseContext(), "SMS sent",
		// Toast.LENGTH_SHORT).show();
		// break;
		// case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
		// Toast.makeText(getBaseContext(), "Generic failure",
		// Toast.LENGTH_SHORT).show();
		// break;
		// case SmsManager.RESULT_ERROR_NO_SERVICE:
		// Toast.makeText(getBaseContext(), "No service",
		// Toast.LENGTH_SHORT).show();
		// break;
		// case SmsManager.RESULT_ERROR_NULL_PDU:
		// Toast.makeText(getBaseContext(), "Null PDU",
		// Toast.LENGTH_SHORT).show();
		// break;
		// case SmsManager.RESULT_ERROR_RADIO_OFF:
		// Toast.makeText(getBaseContext(), "Radio off",
		// Toast.LENGTH_SHORT).show();
		// break;
		// }
		// }
		// }, new IntentFilter(SENT));
		//
		// // ---when the SMS has been delivered---
		// registerReceiver(new BroadcastReceiver() {
		// @Override
		// public void onReceive(Context arg0, Intent arg1) {
		// switch (getResultCode()) {
		// case Activity.RESULT_OK:
		// Toast.makeText(getBaseContext(), "SMS delivered",
		// Toast.LENGTH_SHORT).show();
		// break;
		// case Activity.RESULT_CANCELED:
		// Toast.makeText(getBaseContext(), "SMS not delivered",
		// Toast.LENGTH_SHORT).show();
		// break;
		// }
		// }
		// }, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		Log.i("PHONE NUMBER", phoneNumber);
		Log.i("MESSAGE", message);
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

	private static final int CONTACT_PICKER_RESULT = 1001;

	public void doLaunchContactPicker(View view) {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
				Contacts.CONTENT_URI);
		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				Cursor cursor = null;
				String email = "";
				try {
					Uri result = data.getData();
					Log.v("chloe", "Got a contact result: " + result.toString());

					// get the contact id from the Uri
					String id = result.getLastPathSegment();

					// query for everything email --> contact
					cursor = getContentResolver().query(Phone.CONTENT_URI,
							null, Phone.CONTACT_ID + "=?", new String[] { id },
							null);

					int emailIdx = cursor.getColumnIndex(Phone.DATA);

					// let's just get the first contact
					if (cursor.moveToFirst()) {
						email = cursor.getString(emailIdx);
						Log.v("chloe", "Got mobile number: " + email);
					} else {
						Log.w("chloe", "No results");
					}
				} catch (Exception e) {
					Log.e("chloe", "Failed to get contact number", e);
				} finally {
					if (cursor != null) {
						cursor.close();
					}
					EditText emailEntry = (EditText) findViewById(R.id.phoneNumberText);
					emailEntry.setText(email);
					if (email.length() == 0) {
						Toast.makeText(this,
								"No mobile number found for contact.",
								Toast.LENGTH_LONG).show();
					}

				}
				break;

			}

		} else {
			Log.w("chloe", "Warning: activity result not ok");
		}
	}

	public static void waiting(int n) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		Log.i("INSIDE WAITING", Integer.toString(n));
		do {
			t1 = System.currentTimeMillis();
		} while ((t1 - t0) < (n * 1000));
	}

	protected void onDestroy() {
		try {
			// unregisterReceiver(rcvd);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
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
			if (fw != null && bw != null) {
				try {
					bw.write("Signal Strength"
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
