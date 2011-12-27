package smack.test;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class XMPPSender extends Activity {

    private Handler mHandler = new Handler();
    private SettingsSender mDialog;
    private EditText mRecipient;
    private EditText mSendText;
    private EditText mReceiveText;
    private XMPPConnection connection;
    private Chat chat;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sender);
        
        mRecipient = (EditText) this.findViewById(R.id.recipient);
        mSendText = (EditText) this.findViewById(R.id.sendText);
        mReceiveText = (EditText) this.findViewById(R.id.received);
        
        mDialog = new SettingsSender(this);
        
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
        
        Button send = (Button) this.findViewById(R.id.send);
        send.setOnClickListener(new SendHandler(this));
    }

    /**
     * Called by Settings dialog when a connection is established with the XMPP server
     *
     * @param connection
     */
    public void setConnection (XMPPConnection connection, String target) {
        this.connection = connection;
        ChatManager chatManage = this.connection.getChatManager();
        this.chat = chatManage.createChat(target, new MessageListener() {
			public void processMessage(Chat chat, Message message) {
				Log.e("XMPPClient:Receiving", "Received text [" + message.getBody() + "]");
				Log.e("XMPPClient:Receiving", message.toXML());
				mReceiveText.setText(message.getBody());
			}

			
        });
        
    }
    
    private class SendHandler implements View.OnClickListener {
    	
    	public XMPPSender client;
    	
    	public SendHandler(XMPPSender connection)
    	{
    		this.client = connection;
    	}
    	
		public void onClick(View v) {
			Message message = new Message();
			message.setType(Message.Type.chat);
			message.setBody("Testing123");
			try {
				this.client.chat.sendMessage(message);
				Log.e("XMPPClient:Sending", "Sending text [" + message.getBody() + "] SUCCESS");
				Log.e("XMPPClient:Sending", message.toXML());
			} catch (XMPPException e) {
				Log.e("XMPPClient:Sending", "Sending text [" + message.getBody() + "] FAILED");
			}
		}
    	
    }

}