package app.combined;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.text.format.Time;
import android.util.Log;

public class Receiver3GListener implements PacketListener{
	
	private ReceiverActivity receiver;
	//private String fileType;
	//private int packetSize;
	private long messCtr;
	//private HashMap<Integer, String> packetList = new HashMap<Integer, String>();

	public Receiver3GListener (ReceiverActivity receiver) {
		this.receiver = receiver;
	}

	public void getPackets(String body, Time time) {
		int packetNum;
		String packetLine;
		
		String[] p = body.split("%&");
		for (int i = 1; i < p.length; i++) {
			String[] data = p[i].split(" ");
			
			packetNum = Integer.parseInt(data[0]);
			packetLine = data[1];
			receiver.al.put(packetNum, packetLine);
			
			time.setToNow();
			/*if (packetList.size() == 1) {
				receiver.setInitial(time.toMillis(true));
			}
			try {
				receiver.getWriter().write(time.toString() + " : Message " + packetNum + " Received\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}*/
			Log.i("XMPPReceiver:Receiving", "Received Packet number " + packetNum + " with value " + packetLine);
			Log.i("XMPPReceiver:Receiving", "Current Map size " + Integer.toString(receiver.al.size()));
			//Log.i("XMPPReceiver:Receiving", "Goal Map size " + packetSize);
		}
	}

	public void processPacket(Packet packet) {
		Time time = new Time();

		Message message = (Message)packet;
		String from = StringUtils.parseBareAddress(message.getFrom());
		Log.e("XMPPReceiver:Receiving", "Received text [" + message.getBody() + "] from " + from);

		Message reply = new Message();
		reply.setTo(from);
		reply.setType(Message.Type.chat);
		reply.setThread(message.getThread());

		if (message.getBody().startsWith("%&start3G")) {
			/*String s = message.getBody().substring(11);

			String n = "";
			int i;
			for (i = 0; s.charAt(i) != ' '; i++) {
				n = n + s.charAt(i);
			}
			packetSize = Integer.parseInt(n);
			fileType = s.substring(i + 1);
			Log.i("XMPPReceiver:Receiving", "Packet size = " + packetSize + " File type = " + fileType);
			*/
			setMessCtr(0);
			reply.setBody("%&proceed");
			Log.e("XMPPReceiver:Sending", "Sending text [" + reply.getBody() + "]");
			receiver.getConnection().sendPacket(reply);
		
		}else if (message.getBody().startsWith("%&")) {
			/*int packetNum;
			String packetLine;
			
			String s = message.getBody().substring(2);
			
			String n = "";
			int i;
			for (i = 0; s.charAt(i) != ' '; i++) {
				n = n + s.charAt(i);
			}
			packetNum = Integer.parseInt(n);
			packetLine = s.substring(i+1);
			packetList.put(packetNum, packetLine);
			
			time.setToNow();
			if (packetList.size() == 1) {
				receiver.setInitial(time.toMillis(true));
			}
			try {
				receiver.getWriter().write(time.toString() + " : Message " + packetNum + " Received\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Log.i("XMPPReceiver:Receiving", "Received Packet number " + packetNum + " with value " + packetLine);
			Log.i("XMPPReceiver:Receiving", "Current Map size " + Integer.toString(packetList.size()));
			Log.i("XMPPReceiver:Receiving", "Goal Map size " + packetSize);
			*/
			//String s = message.getBody().substring(2);
			getPackets(message.getBody(),time);
			setMessCtr(getMessCtr() + 1);
			if (receiver.al.size() == receiver.size) {
					
				Log.i("XMPPReceiver:File", "Finished");
				reply.setBody("%&DONE");
				Log.e("XMPPReceiver:Sending", "Sending text [" + reply.getBody() + "]");
				receiver.getConnection().sendPacket(reply);
				receiver.receiveFile();
				
			} else if (getMessCtr() == 10) {
				reply.setBody("%&CONTINUE");
				setMessCtr(0);
				Log.e("XMPPReceiver:Sending", "Sending text [" + reply.getBody() + "]");
				receiver.getConnection().sendPacket(reply);
			}
		}
	}
	
	public long getMessCtr() {
		return messCtr;
	}

	public void setMessCtr(long packetCtr) {
		this.messCtr = packetCtr;
	}
}
