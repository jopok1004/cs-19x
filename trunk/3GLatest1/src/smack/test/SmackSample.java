package smack.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SmackSample extends Activity {
	
	Button senderBt;
	Button receiverBt;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		senderBt = (Button)findViewById(R.id.senderBt);
		receiverBt = (Button)findViewById(R.id.receiverBt);
		
		senderBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SmackSample.this, XMPPSender.class);
				intent.putExtra("start?", "no");
				startActivity(intent);
			}
		});
		receiverBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SmackSample.this, XMPPReceiver.class);
				intent.putExtra("start?", "no");
				startActivity(intent);
			}
		});
		
	}
}
