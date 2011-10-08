package app.sms;
 
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
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
            if((msgs[0].getMessageBody().toString()).equalsIgnoreCase("%&")){
            	this.abortBroadcast();
            }
            Toast.makeText(context,str, Toast.LENGTH_SHORT).show();
            //Intent i = new Intent(context, SmsMessagingActivity.class);
           // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            //context.startActivity(i);
        }                         
    }
}