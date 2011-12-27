package app.mms;
 
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
 
public class SmsReceiver extends BroadcastReceiver
{
	
    String str = "";
    public void onReceive(Context context, Intent intent) 
    {
    	
        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
                
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];        
            //Toast.makeText(context, pdus.length, Toast.LENGTH_SHORT).show();
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                str += "SMS from " + msgs[i].getOriginatingAddress();                     
                str += " :";
                str += msgs[i].getMessageBody().toString();
                str += "\n";        
            }
            //---display the new SMS message---
            //abort broadcast if message is special
            if((msgs[0].getMessageBody().toString()).startsWith("%&") ||(msgs[0].getMessageBody().toString()).startsWith("&%") ){
            	//this.abortBroadcast();
            }
            
            //RECEIVER SIDE
            if(msgs[0].getMessageBody().toString().startsWith("SENDVIAMMS")){
            	this.abortBroadcast();
            	Intent i = new Intent(context, MmsReceiverActivity.class);
            	Log.i("RECEIVED VIA MMS", "RECEIVED VIA MMS");
                i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK   );
                i.putExtra("start?","startMmsReceive");
                i.putExtra("phoneNum",msgs[0].getOriginatingAddress().toString());
                String sub = msgs[0].getMessageBody().substring(11);
                String tokens[] = sub.split(" ");
                for(int j=0;j<tokens.length;j++) {
                	Log.i("TOKEN "+j,tokens[j]);
                }
                
                i.putExtra("initial", Integer.parseInt(tokens[0]));
                i.putExtra("end", Integer.parseInt(tokens[1]));
                i.putExtra("filetype", tokens[2]);
                context.startActivity(i);
            }
  
            //SENDER SIDE
            if(msgs[0].getMessageBody().toString().startsWith("&%mmsreceived")){
            	this.abortBroadcast();
            	Intent i = new Intent(context,MmsSenderActivity.class);
            	i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK   );
            	i.putExtra("start?","sendAnotherMms");
            	context.startActivity(i);
            }
            
        }                         
    }
    
}