package com.example.aman.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.view.KeyEvent;

import com.google.firebase.messaging.FirebaseMessaging;


/**
 * Created by Gagan Sidhu on 5/9/2016.
 */
public class ChatRoomActivity extends AppCompatActivity implements AsyncRequest.newInterface {

    AsyncRequest req = null;
    private EditText messageET;
    private ListView messagesContainer;
    private ImageView sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory=new ArrayList<>();
    private int i;
    private String message;
    private String title;
    private ProgressDialog bar;
    String session_id;
    String messageText;
    String method;
    Toolbar toolbar;
    private String device_token;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_room);

        // Find the toolbar view inside the activity layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        toolbar.setTitle("Stranger");
        toolbar.setNavigationIcon(R.drawable.ic_person_red);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        device_token = SharedPrefManager.getInstance(this).getDeviceToken();
        initControls();



        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    title=intent.getStringExtra("title");
                    if(title.equals("message")){
                        message = intent.getStringExtra("message");
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setId(122);//dummy
                        chatMessage.setMessage(message);
                        chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                        chatMessage.setMe(false);
                        Log.e("chatMessage", chatMessage.getMessage());
                        chatHistory.add(chatMessage);
                        Log.e("displayMessage", "called");
                        displayMessage(chatMessage);
                    }else if(title.equals("connected")){
                        bar.dismiss();
                        Toast.makeText(getApplicationContext(), "You can send message now ", Toast.LENGTH_LONG).show();
                    }else if(title.equals("Waiting")){

                    }

                }
            }
        };





//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.main_title_bar);
        Intent intent = getIntent();
        String title1= intent.getExtras().getString("title");
        //Log.e("Value in intent", title);

        if(title1.equals("message")){
            chatHistory=SharedPrefManager.getInstance(getApplicationContext()).getChatList();
            for(i=0;i<chatHistory.size();i++){
                displayMessage(chatHistory.get(i));
                Log.e("displaying "+i, String.valueOf(chatHistory.get(i)));
            }
            Log.e("inside", "if message");
            message=intent.getExtras().getString("message");
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(122);//dummy
            chatMessage.setMessage(message);
            chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
            chatMessage.setMe(false);
            Log.e("chatMessage", chatMessage.getMessage());
            chatHistory.add(chatMessage);
            Log.e("displayMessage", "called");
            displayMessage(chatMessage);
        } if(title1.equals("1")){
            Toast.makeText(this,"You can send message now.",Toast.LENGTH_SHORT).show();
        }else if(title1.equals("0")||title1.equals("waiting")){
            Log.e("chatroomActivity:","Waiting for partner");
            bar=new ProgressDialog(ChatRoomActivity.this);
            bar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            bar.setTitle("Searching For Partner");
            bar.setMessage("Processing...");
            bar.show();
        }
       // Log.e("session_id", session_id);
        Log.e("chatroomActivity", "oncreate");
        //checkForPartner();
        //rec();





//        adapter = new ChatAdapter(ChatRoomActivity.this, new ArrayList<ChatMessage>());
//        messagesContainer.setAdapter(adapter);


/*        TextView.OnEditorActionListener exampleListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    //match this behavior to your 'Send' (or Confirm) button
                    messageText = messageET.getText().toString();
                    Log.e("---messageText",messageText);
                    if (TextUtils.isEmpty(messageText)) {
                        return false;
                    }

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(122);//dummy
                    chatMessage.setMessage(messageText);
                    chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date().getTime()));
                    chatMessage.setMe(true);
                    Log.e("---chatMessage", chatMessage.getMessage());
                    chatHistory.add(chatMessage);
                    messageET.setText("");
                    if(messageText!=null){
                        sendMessage(messageText);
                        Log.e("sendMessage",messageText);
                    }
                    Log.e("displayMessage","called");
                    displayMessage(chatMessage);

                }
                return true;
            }
        };

*/

    }
    private void initControls() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (ImageView) findViewById(R.id.chatSendButton);

        adapter = new ChatAdapter(ChatRoomActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageText = messageET.getText().toString();
                Log.e("messageText",messageText);
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMessage(messageText);
                // chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
                String formattedDate = formatDate.format(new Date()).toString();
                Log.e("---date",formattedDate);
                chatMessage.setDate(formattedDate);
                chatMessage.setMe(true);
                Log.e("chatMessage", chatMessage.getMessage());
                chatHistory.add(chatMessage);
                messageET.setText("");
                if(messageText!=null){
                    //sendMessage(messageText);
                    //Log.e("sendMessage",messageText);
                    sendFCMMessage(messageText);
                }
                Log.e("displayMessage","called");
                displayMessage(chatMessage);

            }
        });
        messageET.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    send();
                    return true;
                }
                return false;
            }
        });

    }

Handler handler;
    Runnable myRunnable;

    public void rec(){

        handler=  new Handler();
        myRunnable = new Runnable() {
            public void run() {
                checkForPartner();
                if(i==3){
                    Toast.makeText(ChatRoomActivity.this,"Not yet connected",Toast.LENGTH_SHORT).show();
                    toolbar.setSubtitle(null);
                    rec();
                }else if(i==2){
                    Toast.makeText(ChatRoomActivity.this,"Your partner is offline",Toast.LENGTH_SHORT).show();
                    toolbar.setSubtitle("offline");
                    rec();
                }else  if(i==1){
                    toolbar.setSubtitle("online");
                   // Toast.makeText(ChatRoomActivity.this,"Waiting for message",Toast.LENGTH_SHORT).show();
                   // bar.dismiss();
                    rec();
                }else if(i==0){
//                    Log.e("Message",message);
                    toolbar.setSubtitle("typing...");
                    loadDummyHistory();
                    rec();
                }
                else {
                   // onStop();
                }
            }
        };
        handler.postDelayed(myRunnable, 3000);
    }
   /* protected void onStop() {
        super.onStop();
       // handler.removeCallbacks(myRunnable);
    }*/




    public void checkForPartner(){
        Log.e("chatroomActivity", "checkforpartner");
        //initializeAsync();
        method="checkForPartner";
        try{
            initializeAsync();
            JSONObject params=new JSONObject();
            //contains all data objects
            JSONArray arr=new JSONArray();
            try{
                //FINAL OBJECT TO BE SENT IN ASYNC CLASS
                params.put("url","http://ahapp.herokuapp.com/check/?sess="+session_id);
                params.put("type","GET");
                //method is for referencing this object later on
                params.put("method",method);
                params.put("data",arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            req.execute(params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void sendFCMMessage(String messageText){
        Log.e("chatroomActivity","sendFCMMessage");
        method="sendFCMMessage";
        try{
            initializeAsync();
            JSONObject params=new JSONObject();
            JSONObject data=new JSONObject();
            //contains all data objects
            JSONArray arr=new JSONArray();
            try{
                //FINAL OBJECT TO BE SENT IN ASYNC CLASS
                data.put("message", messageText);
                arr.put(data);
                params.put("url","http://ahapp.herokuapp.com/fcm/send");
                params.put("type","POST");
                params.put("device_token",device_token);
                //method is for referencing this object later on
                params.put("method",method);
                params.put("data",arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            req.execute(params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void sendMessage(String messageText){
        Log.e("chatroomActivity","sendMessage");
        method="sendMessage";
        try{
            initializeAsync();
            JSONObject params=new JSONObject();
            //contains all data objects
            JSONArray arr=new JSONArray();
            try{
                //FINAL OBJECT TO BE SENT IN ASYNC CLASS
                params.put("url","http://ahapp.herokuapp.com/send/?msgbox="+messageText+"&session="+session_id);
                params.put("type","GET");
                //method is for referencing this object later on
                params.put("method",method);
                params.put("data",arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            req.execute(params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    void initializeAsync() {
        req = new AsyncRequest(this);
        req.listener = this;
    }



    public void send(){
        messageText = messageET.getText().toString();
        Log.e("messageText",messageText);
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(122);//dummy
        chatMessage.setMessage(messageText);
        chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatMessage.setMe(true);
        Log.e("chatMessage", chatMessage.getMessage());
        chatHistory.add(chatMessage);
        messageET.setText("");
        if(messageText!=null){
            //sendMessage(messageText);
            //Log.e("sendMessage",messageText);
            sendFCMMessage(messageText);
        }
        Log.e("displayMessage", "called");
        displayMessage(chatMessage);

    }

    public void displayMessage(ChatMessage message) {
        Log.e("display", message.getMessage());
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();

    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void loadDummyHistory(){
        ChatMessage msg = new ChatMessage();
        msg.setId(1);
        msg.setMe(false);
        msg.setMessage(message);
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);
        //ChatMessage msg1 = new ChatMessage();
        //msg1.setId(2);
        //msg1.setMe(false);
        //msg1.setMessage("How r u doing???");
        //msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        //chatHistory.add(msg1);


        //for(int i=0; i<chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(chatHistory.size()-1);
            Log.e("message"+i,message.getMessage());
            displayMessage(message);
        //}
    }

    public void exitChat(){
        Log.e("MainActivity","exitChat");
        method="exitChat";
//        bar.show();
        try{
            initializeAsync();
            JSONObject params=new JSONObject();
            //contains all data objects
            JSONArray arr=new JSONArray();
            try{
                //FINAL OBJECT TO BE SENT IN ASYNC CLASS
                params.put("url","http://ahapp.herokuapp.com/fcm/delete");
                params.put("type","POST");
                params.put("device_token",device_token);
                //method is for referencing this object later on
                params.put("method",method);
                params.put("data",arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            req.execute(params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void processResult(JSONArray result) throws JSONException, ParseException {
        Log.e("chatroomActivity", "processRersult");
        try {
            JSONObject data = result.getJSONObject(0);
            if(method=="checkForPartner"){
                i = data.getInt("flag");
                Log.e("flag=", String.valueOf(i));
                if(i==0){
                    message=data.getString("message");
                    Log.e("processMessage",message);
                }
            }else if(method=="sendMessage"){
                Log.e("messageSend", String.valueOf(data.getInt("id")));
            }else if(method=="sendFCMMessage"){
                i=data.getInt("connected");
                if(i==0){
                    Toast.makeText(this,"Partner is offline",Toast.LENGTH_SHORT).show();
                }
            }else if(method=="exitChat"){
               // bar.dismiss();
                String check=data.getString("deleted");
                Log.e("check:",check);
                if(check=="True"){
                    startActivity(new Intent(this,MainActivity.class));
                }
            }
        }catch (Exception e) {
//            Toast.makeText(ChatRoomActivity.this, "Login Failed !", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

       // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.menu_chat_room, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_Exit:
                SharedPrefManager.getInstance(getApplicationContext()).clearChatHistory();
                exitChat();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPrefManager.getInstance(getApplicationContext()).clearChatHistory();
        exitChat();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        MyNotificationManager.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        SharedPrefManager.getInstance(getApplicationContext()).saveChatList(chatHistory);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //m.exitChat();
    }
}


