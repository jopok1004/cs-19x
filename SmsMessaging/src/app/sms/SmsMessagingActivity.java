package app.sms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
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
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SmsMessagingActivity extends Activity {
	String phoneNo = new String();
	Button btnSendSMS;
	EditText txtPhoneNo;
	EditText txtMessage;
	EditText txtFileName;
	EditText rcvdMessage;
	SmsReceiver rcvd;
	File selectedFile;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		rcvd = new SmsReceiver();
		btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
		txtMessage = (EditText) findViewById(R.id.txtMessage);
		txtPhoneNo = (EditText) findViewById(R.id.phoneNumberText);
		txtFileName = (EditText) findViewById(R.id.fileNameText);
		

		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(160);
		txtMessage.setFilters(FilterArray);

		btnSendSMS.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				phoneNo = txtPhoneNo.getText().toString();
				String message = txtMessage.getText().toString();
				if (phoneNo.length() > 0 && message.length() > 0)
					sendSMS(phoneNo, message);
				else
					Toast.makeText(getBaseContext(),
							"Please enter both phone number and message.",
							Toast.LENGTH_SHORT).show();
			}
		});

	}

	// ---sends an SMS message to another device---
	private void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS sent",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic failure",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No service",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Radio off",
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
					Toast.makeText(getBaseContext(), "SMS delivered",
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not delivered",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

	private static final int CONTACT_PICKER_RESULT = 1001;
	private static final int FILE_EXPLORE_RESULT = 1002;

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
			case FILE_EXPLORE_RESULT:
				txtFileName.setText(data.getExtras().getString("fileName"));
				File file = new File(data.getExtras().getString("filePath")
						+ "/" + data.getExtras().getString("fileName"));
				selectedFile = file;

				// File outFile = new
				// File(data.getExtras().getString("filePath") +"/"+
				// data.getExtras().getString("fileName")+".7z");
				// txtFileName.setText(outFile.getAbsolutePath());

				/*
				 * try {Log.i("wala pang error","WALA PANG MALI");
				 * SevenZip.sampleRun.compress7z(file, outFile); } catch
				 * (Exception e) { // TODO Auto-generated catch block
				 * Log.i("error here","ANONG MALI?"); }
				 */

				txtFileName.setText(Long.toString((file.length())));

				compression.compressGzip(
						data.getExtras().getString("filePath"), data
								.getExtras().getString("fileName"));
				Log.i("CHLOEBELLEEEE", "PAPASOK NG BASE 64");
				try {
					Log.i("FILE", data.getExtras().getString("filePath") + "/"
							+ data.getExtras().getString("fileName") + ".gz");
					Base64FileEncoder.encodeFile(
							data.getExtras().getString("filePath") + "/"
									+ data.getExtras().getString("fileName")
									+ ".gz",
							data.getExtras().getString("filePath") + "/"
									+ "encodedFile.txt");
					Log.i("CHLOEBELLEEEE", "AFTER BASE 64");
					parser(data.getExtras().getString("filePath") + "/"
							+ "encodedFile.txt", phoneNo);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}

		} else {
			Log.w("chloe", "Warning: activity result not ok");
		}
	}

	public void exploreFiles(View view) {
		Log.i("CHLOEBELLE", "CLICKED FILE");
		Intent fileExploreIntent = new Intent(SmsMessagingActivity.this,
				FileExplore.class);
		startActivityForResult(fileExploreIntent, FILE_EXPLORE_RESULT);
	}

	private void parser(String file, String phoneNumber) throws IOException {
		String submessage = new String();
		String headerBegin = new String();
		Log.i("CHLOE", "I AM AT PARSER NOW");

		try {
			BufferedReader reader;
			Log.i("FILE AT PARSER", file);
			File fle = new File(file);
			reader = new BufferedReader(new FileReader(fle));

			Log.i("AFTER READING FILE", "HAHAHAHAHA");
			String line;
			for (int counter = 0; (line = reader.readLine()) != null; counter++) {
				Log.i("SA LOOB NG FOR LOOP", "FOR LOOP");
				headerBegin = "%&" + counter + " ";

				submessage = headerBegin + line;
				Log.i("SUBMESSAGE", submessage);
				phoneNumber = txtPhoneNo.getText().toString();
				Log.i("PHONE NUMBER", phoneNumber);
				sendSMS(phoneNumber, submessage);
			}


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}