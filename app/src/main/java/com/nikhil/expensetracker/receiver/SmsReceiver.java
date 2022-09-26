package com.nikhil.expensetracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.MessageParser;
import com.nikhil.expensetracker.utils.Util;

import java.util.Objects;

public class SmsReceiver extends BroadcastReceiver {

    public SmsReceiver() {
        System.out.println("SMS RECEIVER REGISTERED");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msg_from;
            long msgAt;
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        msgAt = msgs[i].getTimestampMillis();
                        String msgBody = msgs[i].getMessageBody();
                        if ((msg_from.contains("Axis") || msg_from.equalsIgnoreCase("AD-AxisBk")) && (msgBody.contains("Debit") || msgBody.contains("Credit"))) {
                            Transaction transaction = MessageParser.parseMessage("axis", msgBody, msgAt);
                            if (transaction != null) {
                                MainActivity.getInstance().database.addTransaction(transaction);
                                SystemClock.sleep(1000);
                                Toast.makeText(context, "Transaction added", Toast.LENGTH_SHORT).show();
                                MainActivity.getInstance().refreshAdapterData();
                            }
                        }
//                        } else if (msg_from.contains("SBIUPI")) {
//                            Transaction transaction = MessageParser.parseMessage("sbi", msgBody, msgAt);
//                            if (transaction != null) {
//                                System.out.println("SAVING TRANSACTION INSIDE RECEIVER...");
//                                MainActivity.getInstance().database.addTransaction(transaction);
//                                SystemClock.sleep(1000);
//                                Toast.makeText(context, "Transaction added", Toast.LENGTH_SHORT).show();
//                                MainActivity.getInstance().refreshAdapterData();
//                            }
//                        } else if (msg_from.contains("HDFCBK")) {
//                            Transaction transaction = MessageParser.parseMessage("hdfc", msgBody, msgAt);
//                            if (transaction != null) {
//                                MainActivity.getInstance().database.addTransaction(transaction);
//                                SystemClock.sleep(1000);
//                                Toast.makeText(context, "Transaction added", Toast.LENGTH_SHORT).show();
//                                MainActivity.getInstance().refreshAdapterData();
//                            }
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
