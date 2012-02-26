package app.combined;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class ReceiverActivity extends Activity {

	private XMPPConnection connection;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    // TODO Auto-generated method stub
	}

	public XMPPConnection getConnection() {
		return connection;
	}

	public void setConnection(XMPPConnection connection) {
		if (connection == null) {
			this.finish();
		}else {
	        this.connection = connection;
	        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
	        connection.addPacketListener(new Receiver3GListener(this), filter);
		}
	}
	
	public void logIn () {
		LogInSettings dialog;
        dialog = new LogInSettings(this);
        if (isOnline(this)) {
            dialog.show();
        }else {
        	Log.e("Receiver:3GConnection", "No network connection available");
        	finish();
        }
		
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

}
