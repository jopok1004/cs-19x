package app.mms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FileMmsWelcome extends Activity {
	Button sendBt;
	Button recvBt;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		sendBt = (Button)findViewById(R.id.sendBt);
		recvBt = (Button)findViewById(R.id.recvBt);
		
		sendBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(FileMmsWelcome.this, MmsSenderActivity.class);
				intent.putExtra("start?", "no");
				startActivity(intent);
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
	
}
