package smack.test;


import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Gather the xmpp settings and create an XMPPConnection
 */
public class SettingsSender extends Dialog implements android.view.View.OnClickListener {
    private XMPPSender xmppClient;

    public SettingsSender(XMPPSender xmppClient) {
        super(xmppClient);
        this.xmppClient = xmppClient;
    }

    protected void onStart() {
        super.onStart();
        setContentView(R.layout.settingsender);
        getWindow().setFlags(4, 4);
        setTitle("XMPP Settings");
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(this);
    }

    public void onClick(View v) {
        String username = getText(R.id.userid);
        String password = getText(R.id.password);
        String target = getText(R.id.target);
        
        // Create a connection
        ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
        XMPPConnection connection = new XMPPConnection(connConfig);
        try {
            connection.connect();
            Log.i("XMPPClient", "[SettingsDialog] Connected to " + connection.getHost());
        	connection.login(username, password);
            Log.i("XMPPClient", "Logged in as " + connection.getUser());
            
            Presence presence = new Presence(Presence.Type.available, "", 24, Presence.Mode.chat);
            connection.sendPacket(presence);
            xmppClient.setConnection(connection, target);
            
        } catch (XMPPException ex) {
            Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + connection.getHost());
            xmppClient.setConnection(null,null);
        }
        dismiss();
    }

    private String getText(int id) {
        EditText widget = (EditText) this.findViewById(id);
        return widget.getText().toString();
    }
}