package app.mms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MmsSenderActivity extends Activity {
	String phoneNo = new String();
	Button btnSendMMS;
	EditText txtPhoneNo;
	EditText txtFileName;
	File selectedFile;
	int packetSize;
	int sent = 0;
	Random random = null;
	String subject;
	int randomNum;
	String sen = "";
	String sub;
	ProgressDialog dialog;
	private static final int CONTACT_PICKER_RESULT = 1001;
	private static final int FILE_EXPLORE_RESULT = 1002;
	ArrayList<String> packetList = new ArrayList<String>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		random = new Random();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		dialog = new ProgressDialog(MmsSenderActivity.this);
		btnSendMMS = (Button) findViewById(R.id.btnSendMMS);
		txtPhoneNo = (EditText) findViewById(R.id.phoneNumberText);
		txtFileName = (EditText) findViewById(R.id.fileNameText);

		btnSendMMS.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				randomNum = random.nextInt(1000);
				phoneNo = txtPhoneNo.getText().toString();
				if (phoneNo.length() > 0) {
					Log.i("sendFile", "Ready to send file");
					String temp= "";
					//temp= "%&sendViaMms 0 " + packetSize + " " + sub;
					temp= "%&sendViaMms";
					Log.i("temp", temp);
					sendSMS(phoneNo, "SENDVIAMMS"+" 0 "+packetSize+" "+sub+" "+randomNum); // %&sendViaMms
					sendSMS(phoneNo, "MESSAGE"); // %&sendViaMms
					Log.i("FINISHED", "DONE SENDING SMS");
					try {
						Log.i("SENDING MMS", "SENDING MMS");
						mms(phoneNo);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}											// startindex
																					// endindex
																					// filename
				} else
					Toast.makeText(getApplicationContext(),
							"Please enter phone number.", Toast.LENGTH_SHORT)
							.show();
			}
		});
	}

	private void mms(String phoneNum) throws IOException {
		int tracker = 0;
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage("Sending MMS...");
		dialog.setCancelable(false);
		dialog.show();
		Log.i("CHLOE", "I AM AT PARSER NOW");
		try {
			// dialog.show(Mms2Activity.this, "Sending SMS", "Please Wait");
			// 1024b * 300kb = 307200/160 char = 1920 packets
			for (; tracker < packetSize;) {

				String msg = "";
				for (int i = 0; i < 100 && tracker < packetSize; i++) {
					msg = msg + "&% " + tracker + " " + packetList.get(tracker)
							+ "\n";
					tracker++;
					Log.i("SUBMESSAGE", msg);
				}
				Log.i("parser", "before mms sending");
				sendMMS(phoneNum, msg);
				waiting(180);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dialog.cancel();

	}

	public static void waiting(int n) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		Log.i("INSIDE WAITING", Integer.toString(n));
		do {
			t1 = System.currentTimeMillis();
		} while ((t1 - t0) < (n * 1000));
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				Cursor cursor = null;
				String number = "";
				try {
					Uri result = data.getData();
					Log.v("Contact Picker",
							"Got a contact result: " + result.toString());

					// get the contact id from the Uri
					String id = result.getLastPathSegment();

					cursor = getContentResolver().query(Phone.CONTENT_URI,
							null, Phone.CONTACT_ID + "=?", new String[] { id },
							null);

					int numberIdx = cursor.getColumnIndex(Phone.DATA);

					// let's just get the first contact
					if (cursor.moveToFirst()) {
						number = cursor.getString(numberIdx);
						Log.v("Contact Picker", "Got mobile number: " + number);
					} else {
						Log.w("Contact Picker", "No results");
					}
				} catch (Exception e) {
					Log.e("Contact Picker", "Failed to get contact number", e);
				} finally {
					if (cursor != null) {
						cursor.close();
					}
					EditText numberEntry = (EditText) findViewById(R.id.phoneNumberText);
					numberEntry.setText(number);
					if (number.length() == 0) {
						Toast.makeText(this,
								"No mobile number found for contact.",
								Toast.LENGTH_LONG).show();
					}
				}
				break;
			case FILE_EXPLORE_RESULT:
				txtFileName.setText(data.getExtras().getString("fileName"));
				File file = new File(data.getExtras().getString("filePath")
						+ "/" + data.getExtras().getString("fileName"));
				selectedFile = file;
				int j;
				for (j = selectedFile.getName().length() - 1; selectedFile
						.getName().charAt(j) != '.'; j--)
					;

				sub = selectedFile.getName().substring(j + 1);

				txtFileName.setText(file.getName() + " "
						+ Long.toString((file.length())));

				compression.compressGzip(
						data.getExtras().getString("filePath"), data
								.getExtras().getString("fileName"));
				Log.i("Base 64", "Before Base 64");
				try {
					Log.i("FILE", data.getExtras().getString("filePath") + "/"
							+ data.getExtras().getString("fileName") + ".gz");
					packetList = Base64FileEncoder.encodeFile(data.getExtras()
							.getString("filePath")
							+ "/"
							+ data.getExtras().getString("fileName") + ".gz",
							data.getExtras().getString("filePath") + "/"
									+ "encodedFile.txt");
					Log.i("Base 64", "After Base 64");
					packetSize = packetList.size();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}

		} else {
			Log.w("OnActivityResult", "Warning: activity result not ok");
		}
	}

	public void exploreFiles(View view) {
		Intent fileExploreIntent = new Intent(MmsSenderActivity.this,
				FileExplore.class);
		startActivityForResult(fileExploreIntent, FILE_EXPLORE_RESULT);
	}

	public void doLaunchContactPicker(View view) {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
				Contacts.CONTENT_URI);
		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
	}

	private void sendMMS(String phoneNumber, String message) throws IOException {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra("sms_body", message);
		intent.putExtra("address", phoneNumber);
		intent.putExtra("subject", "mms"+randomNum);
		intent.setType("image/*");
		intent.setClassName("com.android.mms",
				"com.android.mms.ui.ComposeMessageActivity");
		startActivity(intent);
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