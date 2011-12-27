package smack.test;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class XMPPReceiver extends Activity{

	private XMPPConnection connection;
	private Handler mHandler = new Handler();
    private SettingsReceiver mDialog;
    private EditText received;
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        
        received = (EditText) this.findViewById(R.id.message);
        
        mDialog = new SettingsReceiver(this);
        
        Button setup = (Button) this.findViewById(R.id.setup);
        setup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mDialog.show();
                    }
                });
            }
        });
        
    }
	
	public void setConnection (XMPPConnection connection) {
        this.connection = connection;
        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
        connection.addPacketListener(new MessageHandler(connection), filter);
    }
	
	private class MessageHandler implements PacketListener {
		private XMPPConnection connection;
		
		public MessageHandler (XMPPConnection connection) {
			this.connection = connection;
		}
		public void processPacket(Packet packet) {
			Message message = (Message)packet;
			String from = StringUtils.parseBareAddress(message.getFrom());
			Log.e("XMPPClient:Receiving", "Received text [" + message.getBody() + "] from " + from);
			Log.e("XMPPClient:Receiving", message.toXML());
			Message reply = new Message();
			reply.setTo(from);
			reply.setType(Message.Type.chat);
			reply.setBody("I received your message");
			Log.e("XMPPClient:Sending", "Sending text [" + reply.getBody() + "] SUCCESS");
			Log.e("XMPPClient:Sending", reply.toXML());
			connection.sendPacket(reply);
		}
	}
}
