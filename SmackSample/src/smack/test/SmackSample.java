package smack.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SmackSample extends Activity {
	
	Button serverBt;
	Button clientBt;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		serverBt = (Button)findViewById(R.id.serverBt);
		clientBt = (Button)findViewById(R.id.clientBt);
		
		serverBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SmackSample.this, XMPPSender.class);
				intent.putExtra("start?", "no");
				startActivity(intent);
			}
		});
		clientBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SmackSample.this, XMPPReceiver.class);
				intent.putExtra("start?", "no");
				startActivity(intent);
			}
		});
		
	}
}
