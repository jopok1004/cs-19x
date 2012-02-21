package smack.test;

import java.io.IOException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.text.format.Time;
import android.util.Log;
import android.view.View;

public class SendHandler implements View.OnClickListener {
	
	private XMPPSender sender;
	
	public SendHandler (XMPPSender sender) {
		this.sender = sender;
	}
	
	public void onClick(View v) {
		String to = sender.getRecipient().getText().toString();
		Roster r = sender.getConnection().getRoster();
		
		ChatManager chatManage = sender.getConnection().getChatManager();
        Chat nchat = chatManage.createChat(to, new ProcessReply());
        
        if (r.getPresence(to).isAvailable()) {
        	Log.i("XMPPSender", "ONLINE: Available");
        	Message message = new Message();
			message.setType(Message.Type.chat);
			message.setBody("%&sendfile " + sender.getPacketList().size() + " " + sender.getFileType());
			
			try {
				nchat.sendMessage(message);
				Log.e("XMPPSender:Sending", "Sending text [" + message.getBody() + "] SUCCESS");
			} catch (XMPPException e) {
				Log.e("XMPPSender:Sending", "Sending text [" + message.getBody() + "] FAILED");
			}
        }else {
        	Log.i("XMPPSender", "OFFLINE si " + to);
        }
	}
	
	private class ProcessReply implements MessageListener {

		private Integer currentPacket;
		private long t;
		
		public void waiting (int n){
	        long t0, t1;
	        t0 =  System.currentTimeMillis();
	        Log.i("INSIDE WAITING", Integer.toString(n));
	        do{
	            t1 = System.currentTimeMillis();
	        }
	        while ((t1 - t0) < (n * 1000));
	    }
		
		public void processMessage(Chat chat, Message message) {
			Log.e("XMPPSender:Receiving", "Received text [" + message.getBody() + "]");
			Time time = new Time();
			
			if (message.getBody().equals("%&proceed")) {
				time.setToNow();
				sender.setInitial(time.toMillis(true));
				setCurrentPacket(0);
				for (int i = 0; i < 10; i ++) {
					sendPackets(chat, time);
					waiting(2);
				}	
				
			}else if (message.getBody().equals("%&DONE")) {
				Log.e("XMPPSender:Sending", "Sending file SUCCESSFUL");
				setCurrentPacket(0);
		
				try {
					sender.getWriter().write((t - sender.getT1()) + " : total time\n");
					sender.getWriter().write(sender.getT2() - sender.getT1() + " : processing time\n");
					sender.getWriter().write((t - sender.getInitial()) + " : sending time\n");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					sender.getWriter().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sender.finish();
			
			}else if (message.getBody().equals("%&CONTINUE")) {
				for (int i = 0; i < 10; i ++) {
					sendPackets(chat, time);
					waiting(2);
				}	
			}
			
		}
		
		private void sendPackets (Chat chat, Time time) {
			try {
				int counter = 0;
				String line;
				String packet = "";
				while (counter < 40) {
					
					/*Message reply = new Message();
					reply.setType(Message.Type.chat);
					line = sender.getPacketList().get(getCurrentPacket());
					reply.setBody("%&" + getCurrentPacket() + " " + line);
					
					try {
						time.setToNow();
						sender.getWriter().write(time.toString() + " : Packet " + getCurrentPacket() +  "Sending\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					chat.sendMessage(reply);
					
					try {
						time.setToNow();
						sender.getWriter().write(time.toString() + " : Packet " + getCurrentPacket() +  "Sent\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Log.e("XMPPSender:Sending", "Sending text [" + reply.getBody() + "] SUCCESS");
					*/
					if (getCurrentPacket() == sender.getPacketList().size()) {
						time.setToNow();
						t = time.toMillis(true);
						break;
					}
					
					line = sender.getPacketList().get(getCurrentPacket());
					packet = packet + "%&" + getCurrentPacket() + " " + line;
					setCurrentPacket(getCurrentPacket() + 1);
					counter++;
					//waiting(2);
				}
				
				if (!packet.equals("")) {
					Message reply = new Message();
					reply.setType(Message.Type.chat);
					reply.setBody(packet);
					
					try {
						time.setToNow();
						sender.getWriter().write(time.toString() + " : Packet " + getCurrentPacket() +  "Sending\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					chat.sendMessage(reply);
					
					try {
						time.setToNow();
						sender.getWriter().write(time.toString() + " : Packet " + getCurrentPacket() +  "Sent\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Log.e("XMPPSender:Sending", "Sending text [" + reply.getBody() + "] SUCCESS");
					//waiting(2);
				}
			} catch (XMPPException e) {
				Log.e("XMPPSender:Sending", "Sending text packet FAILED");
			}
		}

		public Integer getCurrentPacket() {
			return currentPacket;
		}

		public void setCurrentPacket(Integer currentPacket) {
			this.currentPacket = currentPacket;
		}
		
	}
}