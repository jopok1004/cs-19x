package smack.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jivesoftware.smack.XMPPConnection;

public class XMPPSender extends Activity {

    private Settings mDialog;
    private EditText mRecipient;
    private XMPPConnection connection;
    
    private static final int FILE_EXPLORE_RESULT = 1002;
    private ArrayList<String> packetList = new ArrayList<String>();
    private String encodedFile;
    private String fileType;
    private BufferedWriter bw;
    private long t1, t2, initial;
    
    private FileWriter fw1;
    private BufferedWriter bw1;
    
    private TelephonyManager Tel;
    private MyPhoneStateListener MyListener;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sender);

		String s = "/sdcard/senderTrace.txt";
        try {
			this.bw = new BufferedWriter (new FileWriter(s));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		String s1 = "/sdcard/senderSignal.txt";
        try {
        	this.fw1 = new FileWriter(s1);
			this.bw1 = new BufferedWriter (fw1);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        MyListener = new MyPhoneStateListener();
        Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        
        mRecipient = (EditText) this.findViewById(R.id.recipient);
              
        mDialog = new Settings(this);
        if (isOnline()) {
            mDialog.show();
        }else {
        	Log.e("XMPPSender:Connection", "No network connection available");
        	finish();
        }
        
        Debug.startMethodTracing("SenderActivityTrace");
                
        Button send = (Button) this.findViewById(R.id.send);
        send.setOnClickListener(new SendHandler(this));
    }
    
    public void exploreFiles (View v) {
		Log.i("XMPPSender:File", "Entering FileExplore");
		Intent fileExploreIntent = new Intent(XMPPSender.this,
				FileExplore.class);
		startActivityForResult(fileExploreIntent, FILE_EXPLORE_RESULT);
    }
    
	public boolean isOnline() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	            	InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) { 
	                	Log.e("XMMPReceiver:Status", inetAddress.getHostAddress().toString() );
	                	return true; 
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e("ServerActivity", ex.toString());
	    }
	    return false;
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Time time = new Time();
    	
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		
    		case FILE_EXPLORE_RESULT:
    			File file = new File (data.getExtras().getString("filePath") + "/"
    					+ data.getExtras().getString("fileName"));
    			
    			int i;
    			for (i = file.getName().length() - 1; file.getName().charAt(i) != '.'; i--);
    			
    			fileType = file.getName().substring(i + 1);
    			
    			Log.i("XMPPSender:File", "Compression Started");
    			time.setToNow();
				setT1(time.toMillis(true));
				try {
					bw.write(time.toString() + "before compression\n");
				} catch (IOException e2) {
					e2.printStackTrace();
				}
    			Compression.compressGzip(data.getExtras().getString("filePath"), 
    					data.getExtras().getString("fileName"));
				
    			time.setToNow();
    			try {
					bw.write(time.toString() + "after compression before base 64\n");
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
    			try {
    				Log.i("XMPPSender:File", "Base64 Started");
    				
					packetList = Base64FileEncoder.encodeFile(data.getExtras()
							.getString("filePath")
							+ "/"
							+ data.getExtras().getString("fileName") + ".gz",
							data.getExtras().getString("filePath") + "/"
									+ "encodedFile.txt");
					time.setToNow();
					setT2(time.toMillis(true));
					bw.write(time.toString() + "after b64\n");
					
					encodedFile = data.getExtras().getString("filePath") + "/"
							+ "encodedFile.txt";
				} catch (IOException e) {
					Log.e("XMPPSender:File", "Base64 Failed");
				}
    			
    			file = new File (data.getExtras()
							.getString("filePath")
							+ "/"
							+ data.getExtras().getString("fileName") + ".gz");
    			file.delete();
    		}
    	}else {
    		Log.e("XMPPSender:File", "File Explore failed");
    	}
    }
    
    private class MyPhoneStateListener extends PhoneStateListener {

		private Time time;
		
		public MyPhoneStateListener() {
			time = new Time();
		}
		
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			time.setToNow();
			if (fw1 != null && bw1 != null) {
				try {
					bw1.write("Signal Strength"
							+ time.toString()
							+ ": "
							+ String.valueOf(signalStrength.getGsmSignalStrength()) + " : "
							+ String.valueOf(signalStrength.getCdmaDbm()) + " : "
							+ String.valueOf(signalStrength.getCdmaEcio()) + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
    
    protected void onDestroy() {
    	if (this.connection != null) {
    		this.connection.disconnect();
    	}
    	try {
			//this.fw1.close();
	    	this.bw1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Debug.stopMethodTracing();
    	super.onDestroy();
    }

    public void setConnection (XMPPConnection connection) {
    	if (connection == null) {
    		this.finish();
    	}else {
            this.connection = connection;
    	}
    }
    
    public XMPPConnection getConnection () {
    	return this.connection;
    }
    
    public EditText getRecipient() {
    	return this.mRecipient;
    }

    public String getEncodedFile() {
		return encodedFile;
	}

	public String getFileType() {
		return fileType;
	}

	public ArrayList<String> getPacketList() {
		return packetList;
	}

	public BufferedWriter getWriter() {
		return bw;
	}

	public long getT1() {
		return t1;
	}

	public void setT1(long t1) {
		this.t1 = t1;
	}

	public long getT2() {
		return t2;
	}

	public void setT2(long t2) {
		this.t2 = t2;
	}

	public long getInitial() {
		return initial;
	}

	public void setInitial(long initial) {
		this.initial = initial;
	}
}