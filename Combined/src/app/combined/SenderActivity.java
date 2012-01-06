package app.combined;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class SenderActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.sender);
	    EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
	    Intent intent = getIntent();
	    txtMessage.setText(intent.getStringExtra("smscount")+intent.getStringExtra("mmscount")+intent.getStringExtra("3Gcount"));
	    // TODO Auto-generated method stub
	}

}
