package smack.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Debug;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;

public class XMPPReceiver extends Activity{

	private XMPPConnection connection;
    private Settings mDialog;
    private BufferedWriter bw;
    private long t1, t2, initial;
    
    private FileWriter fw1;
    private BufferedWriter bw1;
    
    private TelephonyManager Tel;
    private MyPhoneStateListener MyListener;
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        
		String s = "/sdcard/receiverTrace.txt";
        try {
			this.bw = new BufferedWriter (new FileWriter(s));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		String s1 = "/sdcard/receiverSignal.txt";
        try {
        	this.fw1 = new FileWriter(s1);
			this.bw1 = new BufferedWriter (fw1);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        MyListener = new MyPhoneStateListener();
        Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        
        mDialog = new Settings(this);
        if (isOnline(this)) {
            mDialog.show();
        }else {
        	Log.e("XMPPReceiver:Connection", "No network connection available");
        	finish();
        } 
        Debug.startMethodTracing("ReceiverActivityTrace");
    }
	
	public boolean isOnline(Context ctx) {
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

	    if (info == null || !info.isConnected()) {
	        return false;
	    }
	    //if (info.isRoaming()) {
	        // here is the roaming option you can change it if you want to
	        // disable internet while roaming, just return false
	    //    return false;
	    //}
	    return true;
	}
	
	public void setConnection (XMPPConnection connection) {
		if (connection == null) {
			this.finish();
		}else {
	        this.connection = connection;
	        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
	        connection.addPacketListener(new ReceiveHandler(this), filter);
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
	
	public XMPPConnection getConnection () {
		return this.connection;
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
