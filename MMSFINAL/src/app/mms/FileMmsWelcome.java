package app.mms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class FileMmsWelcome extends Activity {
	Button sendBt;
	Button recvBt;
	private static final int SEND_MMS_RESULT = 1001;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		sendBt = (Button)findViewById(R.id.sendBt);
		recvBt = (Button)findViewById(R.id.recvBt);
		
		sendBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(FileMmsWelcome.this, MmsSenderActivity.class);
				intent.putExtra("start?", "no");
				startActivityForResult(intent,SEND_MMS_RESULT);
			}
		});
		recvBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(FileMmsWelcome.this, MmsReceiverActivity.class);
				intent.putExtra("start?", "no");
				startActivity(intent);
			}
		});
		
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SEND_MMS_RESULT:
				this.finish();
				break;

			}

		} else {
			Log.w("OnActivityResult", "Warning: activity result not ok");
		}
	}
	
}
