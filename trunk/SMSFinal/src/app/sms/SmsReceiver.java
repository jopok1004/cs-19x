package app.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

	String str = "";
	Boolean done = false;
	int sizeT;
	public void onReceive(Context context, Intent intent) {

		// ---get the SMS message passed in---
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		
		if (bundle != null) {
			// ---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			// Toast.makeText(context, pdus.length, Toast.LENGTH_SHORT).show();
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				str += "SMS from " + msgs[i].getOriginatingAddress();
				str += " :";
				str += msgs[i].getMessageBody().toString();
				str += "\n";
			}
			// ---display the new SMS message---
			// abort broadcast if message is special
			if ((msgs[0].getMessageBody().toString()).startsWith("%&")
					|| (msgs[0].getMessageBody().toString()).startsWith("&%")) {
				this.abortBroadcast();
			}

			// SENDER SIDE
			if (msgs[0].getMessageBody().toString().equals("%&start")) {
				Intent i = new Intent(context, SmsSenderActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("start?", "start sending");
				i.putExtra("phoneNum", msgs[0].getOriginatingAddress()
						.toString());
				context.startActivity(i);

			}
			if (msgs[0].getMessageBody().toString().startsWith("%&done")) {
				Intent i = new Intent(context, SmsSenderActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("start?", "done receiving");
				Toast.makeText(context, "File sent.", Toast.LENGTH_SHORT)
						.show();
				context.startActivity(i);

			}
			if (msgs[0].getMessageBody().toString().startsWith("%&resend")) {
				Intent i = new Intent(context, SmsSenderActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("start?", "sendAgain");
				i.putExtra("phoneNum", msgs[0].getOriginatingAddress()
						.toString());
				String sub = msgs[0].getMessageBody().substring(9);

				i.putExtra("resendPackets", sub);

				context.startActivity(i);
			}

			// RECEIVER SIDE
			if (msgs[0].getMessageBody().toString().startsWith("%&check10")) {
				Intent i = new Intent(context, SmsReceiverActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("start?", "check10");
				i.putExtra("phoneNum", msgs[0].getOriginatingAddress()
						.toString());
				String sub = msgs[0].getMessageBody().substring(10);
				Log.i("inside check10", sub);
				i.putExtra("tracker", sub);

				context.startActivity(i);
			}
			if (msgs[0].getMessageBody().toString().contains("%& sendFile ")) {
				Intent i = new Intent(context, SmsReceiverActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("start?", "getConfirm");
				i.putExtra("phoneNum", msgs[0].getOriginatingAddress()
						.toString());
				String sub = msgs[0].getMessageBody().substring(12);
				int j, size;
				String num = "";
				for (j = 0; sub.charAt(j) != ' '; j++) {
					num = num + sub.charAt(j);
				}
				Log.i("NUM", num);
				size = Integer.parseInt(num);
				sub = sub.substring(j + 1);
				i.putExtra("fileType", sub);
				i.putExtra("size", size);
				context.startActivity(i);

			}
			if (msgs[0].getMessageBody().toString().startsWith("%&sent")) {
				Intent i = new Intent(context, SmsReceiverActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("start?", "getMore");
				i.putExtra("phoneNum", msgs[0].getOriginatingAddress()
						.toString());
				String sub = msgs[0].getMessageBody().substring(7);
				int j, size;
				String num = "";
				for (j = 0; j < sub.length(); j++) {
					num = num + sub.charAt(j);
				}
				Log.i("NUM", num);
				size = Integer.parseInt(num);

				i.putExtra("sentPackets", size);

				context.startActivity(i);
			}

			if (msgs[0].getMessageBody().toString().startsWith("&%")) {
				Intent i = new Intent(context, SmsReceiverActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("start?", "start receiving");
				Log.i("MESSAGE", msgs[0].getMessageBody());
				String sub = msgs[0].getMessageBody().substring(3);
				Log.i("START RECEIVING SUB", sub);
				String num = "";
				int j;
				for (j = 0; sub.charAt(j) != ' '; j++) {
					num = num + sub.charAt(j);
				}
				Log.i("START RECEIVING NUM", num);
				sub = sub.substring(j + 1, sub.length());
				int packetNum = Integer.parseInt(num);
				i.putExtra("packetNum", packetNum);
				i.putExtra("message", sub);
				Log.i("MESSAGE TO BE DECODED", sub);
				context.startActivity(i);

			}

			// Toast.makeText(context,str, Toast.LENGTH_SHORT).show();

		}
	}

}