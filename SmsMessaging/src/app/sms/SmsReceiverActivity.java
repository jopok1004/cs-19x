package app.sms;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SmsReceiverActivity extends Activity{
	String phoneNo = new String();
	Button btnSendConfirmation;
	EditText txtPhoneNo;
	SmsReceiver rcvd;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receiver);
		rcvd = new SmsReceiver();
		btnSendConfirmation = (Button) findViewById(R.id.btnSendConfirmation);
		txtPhoneNo = (EditText) findViewById(R.id.phoneNumberText);

		btnSendConfirmation.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				phoneNo = txtPhoneNo.getText().toString();
				String message = "%&start";
				if (phoneNo.length() > 0){
					//sendSMS(phoneNo, message);
					Toast.makeText(getBaseContext(),"Please do not close this application.",Toast.LENGTH_SHORT).show();
				}else
					Toast.makeText(getBaseContext(),"Please enter phone number.",Toast.LENGTH_SHORT).show();
			}
		});

	}
}
