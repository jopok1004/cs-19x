package app.combined;

import java.util.ArrayList;

import org.jivesoftware.smack.XMPPConnection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SenderActivity extends Activity {
	private String phoneNum;
	private ArrayList<String> packetList = new ArrayList<String>();
	private int packetCount;
	
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		phoneNum = intent.getStringExtra("phoneNum");
		packetCount = intent.getIntExtra("packetCount", 0);
		packetList = intent.getStringArrayListExtra("arraylist");
	}

	public void setConnection(XMPPConnection connection) {
		// TODO Auto-generated method stub
		
	}
	
}

