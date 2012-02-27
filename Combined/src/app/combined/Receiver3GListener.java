package app.combined;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.text.format.Time;
import android.util.Log;

public class Receiver3GListener implements PacketListener{
	
	private ReceiverActivity receiver;
	private String fileType;
	private int packetSize;
	private long messCtr;
	private HashMap<Integer, String> packetList = new HashMap<Integer, String>();

	public Receiver3GListener (ReceiverActivity receiver) {
		this.receiver = receiver;
		packetList.clear();
	}

	public void getPackets(String body, Time time) {
		int packetNum;
		String packetLine;
		
		String[] p = body.split("%&");
		for (int i = 1; i < p.length; i++) {
			String[] data = p[i].split(" ");
			
			packetNum = Integer.parseInt(data[0]);
			packetLine = data[1];
			packetList.put(packetNum, packetLine);
			
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
			Log.i("XMPPReceiver:Receiving", "Current Map size " + Integer.toString(packetList.size()));
			Log.i("XMPPReceiver:Receiving", "Goal Map size " + packetSize);
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
			String s = message.getBody().substring(11);

			String n = "";
			int i;
			for (i = 0; s.charAt(i) != ' '; i++) {
				n = n + s.charAt(i);
			}
			packetSize = Integer.parseInt(n);
			fileType = s.substring(i + 1);
			setMessCtr(0);
			Log.i("XMPPReceiver:Receiving", "Packet size = " + packetSize + " File type = " + fileType);
			
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
			if (packetList.size() == packetSize) {
				finishing(time);
					
				Log.i("XMPPReceiver:File", "Finished");
				reply.setBody("%&DONE");
				Log.e("XMPPReceiver:Sending", "Sending text [" + reply.getBody() + "]");
				receiver.getConnection().sendPacket(reply);
				receiver.finish();
				
			} else if (getMessCtr() == 10) {
				reply.setBody("%&CONTINUE");
				setMessCtr(0);
				Log.e("XMPPReceiver:Sending", "Sending text [" + reply.getBody() + "]");
				receiver.getConnection().sendPacket(reply);
			}
		}
	}
	
	private void  finishing(Time time) {
		try {
			time.setToNow();
			//receiver.setT1(time.toMillis(true));
			//receiver.getWriter().write(time.toString() + " : Before write to file\n");

			FileWriter writer = new FileWriter ("/sdcard/decode.txt");
			for (int j = 0; j < packetSize; j++) {
				writer.write(packetList.get(j) + "\n");
			}
			packetList.clear();
			writer.close();
			
			time.setToNow();
			//receiver.getWriter().write(time.toString() + " : Before decode\n");
			Base64FileDecoder.decodeFile("/sdcard/decode.txt", "/sdcard/file." + fileType + ".gz");
			File file = new File("/sdcard/decode.txt");
			file.delete();
			
			time.setToNow();
			//receiver.getWriter().write(time.toString() + " : After decode before unzip\n");
			//Compression.decompressGzip("/sdcard/file." + fileType + ".gz");
			file = new File("/sdcard/file." + fileType + ".gz");
			file.delete();
			
			time.setToNow();
			/*receiver.setT2(time.toMillis(true));
			receiver.getWriter().write(time.toString() + " : After unzip\n");
			receiver.getWriter().write(receiver.getT1() - receiver.getInitial() + " : start of receiveing to before processing\n");
			receiver.getWriter().write(receiver.getT2() - receiver.getT1() + " : processing time\n");
			receiver.getWriter().write(receiver.getT2() - receiver.getInitial() + " : start of receiveing to end of processing\n");
			receiver.getWriter().close();
			*/
		} catch (IOException e) {
			Log.e("XMPPReceiver:File", "ERROR in File Creation");
		}
	}

	public long getMessCtr() {
		return messCtr;
	}

	public void setMessCtr(long packetCtr) {
		this.messCtr = packetCtr;
	}
}
