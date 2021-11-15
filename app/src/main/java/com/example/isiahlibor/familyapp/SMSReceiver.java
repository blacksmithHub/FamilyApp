package com.example.isiahlibor.familyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

    public SMSReceiver() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        SmsMessage[] smsm = null;
        String sms_str ="";

        SMSClass smsRecVal = new SMSClass();


        if (bundle != null) {
            // Get the SMS message
            Object[] pdus = (Object[]) bundle.get("pdus");
            smsm = new SmsMessage[pdus.length];
            for (int i=0; i<smsm.length; i++){
                smsm[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                smsRecVal.setNumber(smsm[i].getOriginatingAddress());
                smsRecVal.setMessage(smsm[i].getMessageBody().toString());
            }
        }

        // Start Application's  MainActivty activity
        Intent smsIntent = new Intent(context,MainActivity.class);
        smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        smsIntent.putExtra("sms_obj", smsRecVal);
        context.startActivity(smsIntent);
    }
}
