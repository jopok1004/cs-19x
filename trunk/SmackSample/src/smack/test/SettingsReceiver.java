package smack.test;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

public class SettingsReceiver extends Dialog implements android.view.View.OnClickListener {

    private XMPPReceiver xmppClient;

    public SettingsReceiver(XMPPReceiver xmppClient) {
        super(xmppClient);
        this.xmppClient = xmppClient;
    }
	
	protected void onStart() {
        super.onStart();
        setContentView(R.layout.settingreceiver);
        getWindow().setFlags(4, 4);
        setTitle("XMPP Settings");
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);
    }
	
	public void onClick(View v) {
		 String username = getText(R.id.userid);
	     String password = getText(R.id.password);
	     
	     ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
	     XMPPConnection connection = new XMPPConnection(connConfig);
	     try {
	            connection.connect();
	            Log.i("XMPPClient", "[SettingsDialog] Connected to " + connection.getHost());
	        	connection.login(username, password);
	            Log.i("XMPPClient", "Logged in as " + connection.getUser());
	            
	            Presence presence = new Presence(Presence.Type.available, "", 24, Presence.Mode.chat);
	            connection.sendPacket(presence);
	            xmppClient.setConnection(connection);
	            
	     } catch (XMPPException ex) {
	            Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + connection.getHost());
	            xmppClient.setConnection(null);
	     }
	     dismiss();
	}

    private String getText(int id) {
        EditText widget = (EditText) this.findViewById(id);
        return widget.getText().toString();
    }
}
