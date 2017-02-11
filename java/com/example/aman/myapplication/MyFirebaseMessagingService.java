package com.example.aman.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Gagan Sidhu on 1/24/2017.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
       // Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());


        //Calling method to generate notification
       // sendNotification(remoteMessage.getNotification().getBody());
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + String.valueOf(remoteMessage.getData()));
            try {
                //JSONObject json = new JSONObject(remoteMessage.getData().toString());
                JSONObject json= new JSONObject(remoteMessage.getData());
                //String clickAction=remoteMessage.getNotification().getClickAction();
                sendPushNotification(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }

    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Firebase Push Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    //this method will display the notification
    //We are passing the JSONObject that is received from
    //firebase cloud messaging
    private void sendPushNotification(JSONObject json) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON " + json.toString());
        try {

            //getting the json data
            //JSONObject data = json.getJSONObject("data");

            //parsing json data
            String title = json.getString("title");
            String message = json.getString("body");
            String imageUrl = "";

            //creating MyNotificationManager object
            MyNotificationManager mNotificationManager = new MyNotificationManager(getApplicationContext());

            //creating an intent for the notification
//            Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class).putExtra("title",title).putExtra("message",message);



            if (!MyNotificationManager.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("title",title);
                pushNotification.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                // play notification sound
                MyNotificationManager notificationUtils = new MyNotificationManager(getApplicationContext());
               // notificationUtils.playNotificationSound();
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(),ChatRoomActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                resultIntent.putExtra("title", title);
                resultIntent.putExtra("message", message);
                Log.e(TAG,"title="+title+" , message="+message);

                //if there is no image
                if(imageUrl.equals("null")){
                    //displaying small notification
                    mNotificationManager.showSmallNotification(title, message, resultIntent);
                }else{
                    //if there is an image
                    //displaying a big notification
                    mNotificationManager.showBigNotification(title, message, imageUrl, resultIntent);
                }
            }




        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }


}
