package app.sms;

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
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SmsReceiverActivity extends Activity {
	String phoneNo = new String();
	Button btnSendConfirmation;
	EditText txtPhoneNo;
	SmsReceiver rcvd;
	HashMap<Integer, String> al = new HashMap();
	int size;
	String fileT;

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
				String message = "%&start";
				if (phoneNo.length() > 0) {
					sendSMS(phoneNo, message);
					Toast.makeText(getBaseContext(),
							"Please do not close this application.",
							Toast.LENGTH_SHORT).show();
				} else
					Toast.makeText(getBaseContext(),
							"Please enter phone number.", Toast.LENGTH_SHORT)
							.show();
			}
		});
		if ((intent.getStringExtra("start?").toString())
				.equals("getConfirm")) {
			Log.i("GETCONFIRM","GETCONFIRM");
			size = intent.getIntExtra("size",10);
			fileT = intent.getStringExtra("fileType");
			
		}
	}

	public void onNewIntent(Intent intent) {
		Log.i("INTENT",intent.getStringExtra("start?").toString());
		if ((intent.getStringExtra("start?").toString())
				.equals("getConfirm")) {
			Log.i("GETCONFIRM","GETCONFIRM");
			size = intent.getIntExtra("size",10);
			fileT = intent.getStringExtra("fileType");
			
		}
		
		if ((intent.getStringExtra("start?").toString())
				.equals("start receiving")) {
			al.put(intent.getIntExtra("packetNum", 1000), intent
					.getStringExtra("message").toString());

			Log.i("AL SIZE", Integer.toString(al.size()));
			Log.i("SIZE", Integer.toString(size));
			if (al.size() == size) {
				try {

					FileWriter fw = new FileWriter(new File(
							"/sdcard/decode.txt"));
					for (int i = 0; i < size; i++) {
						fw.write(al.get(i)+"\n");
						
					}
					al.clear();
					fw.close();

					Log.i("WRITING TO FILE", "FILEWRITER");
					
					Base64FileDecoder.decodeFile("/sdcard/decode.txt", "/sdcard/file."+fileT+".gz");
					File fl = new File("/sdcard/decode.txt");
					fl.delete();
					compression.decompressGzip("/sdcard/file."+fileT+".gz");
					fl = new File("/sdcard/file."+fileT+".gz");
					fl.delete();
					Log.i("DONE!!!","DONE");
					Toast.makeText(getBaseContext(), "File Received. Check your SD Card", Toast.LENGTH_LONG).show();
					sendSMS(phoneNo,"%&done");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
	}

	// COPIED METHODS FROM SMSMESSAGINGACTIVITY

	private void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";
		
		if(message.equals("%&done")){
			Toast.makeText(SmsReceiverActivity.this, "File Received. Check your SD Card", Toast.LENGTH_LONG);
		}
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

}
