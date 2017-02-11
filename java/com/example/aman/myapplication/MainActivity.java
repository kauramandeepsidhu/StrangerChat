package com.example.aman.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class MainActivity extends AppCompatActivity implements AsyncRequest.newInterface {

    AsyncRequest req = null;
    private SharedPreferences profile;
    TextView txt;
    private ProgressDialog bar;
    private String method="checkOnline";
    private int status=0;
    private String session_id;
    private String device_token;
    private Handler progressBarHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("MainActicity", "onCreate");
        setContentView(R.layout.activity_main);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        device_token = SharedPrefManager.getInstance(this).getDeviceToken();
        bar=new ProgressDialog(MainActivity.this);
        bar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        bar.setTitle("Please Wait");
        bar.setMessage("Processing...");

        Log.e("device_token**:","device token: "+device_token);
        checkOnline();
        txt=(TextView)findViewById(R.id.Onlinetxt);
        Button startBtn = (Button) findViewById(R.id.startbtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new Thread(new Runnable() {
                  //  @Override
                    //public void run() {
                        // TODO Auto-generated method stub
                        /*connectingUser();*/
                connectFCMUser();
                      //  try
                        //{
                          //  Thread.sleep(5000);
                        //}catch(Exception e){}
                       // bar.dismiss();
                    //}
               // }).start();

                Toast.makeText(MainActivity.this,"Connecting",Toast.LENGTH_SHORT).show();



            }
        });
    }
    public void connectFCMUser(){
        Log.e("inside","connectFCMUser");
        method="connectFCMUser";
        bar.show();
        //initializeAsync
        try{
            initializeAsync();
            JSONObject params=new JSONObject();
            //contains all data objects
            JSONArray arr=new JSONArray();
            try{
                //FINAL OBJECT TO BE SENT IN ASYNC CLASS
                params.put("url","http://ahapp.herokuapp.com/fcm/connect");
                params.put("type","POST");
                params.put("device_token",device_token);
                //method is for referencing this object later on
                params.put("method",method);
                params.put("data",arr);
            }catch (JSONException e){
                e.printStackTrace();
            }
            req.execute(params);
        }catch (Exception e){
            e.printStackTrace();
        }
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

    public void connectingUser(){
        Log.e("inside", "connectingUser");
        method="connectingUser";
        //initializeAsync();
        try{
            initializeAsync();
            JSONObject params=new JSONObject();
            //contains all data objects
            JSONArray arr=new JSONArray();
            try{
                //FINAL OBJECT TO BE SENT IN ASYNC CLASS
                params.put("url","http://ahapp.herokuapp.com/connect");
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

    public void checkOnline() {
        Log.e("inside", "checkOnline()");
        method="checkOnline";
        //initializeAsync();
        try {
            initializeAsync();
            JSONObject params = new JSONObject();

            //contains all data objects
            JSONArray arr = new JSONArray();

            try {
                //FINAL OBJECT TO BE SENT IN ASYNC CLASS
                params.put("url", "http://ahapp.herokuapp.com/fcm/count");
                params.put("type", "GET");
                // method is for referencing this object later on.
                params.put("method", method);
                params.put("data", arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            req.execute(params);

        } catch (Exception e) {
//            Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void initializeAsync() {
        req = new AsyncRequest(this);
        req.listener = this;
    }

    @Override
    public void processResult(JSONArray result) throws JSONException, ParseException {

            try {
//                bar.dismiss();
                JSONObject data = result.getJSONObject(0);
                if(method=="checkOnline"){
                    int i = data.getInt("online");
                    txt.setText( String.valueOf(i));
                }else if(method=="connectingUser"){

                    status=data.getInt("connected");
                    session_id=data.getString("session_id");
                   // Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_SHORT).show();
                    Log.e("status", String.valueOf(status));
                    Log.e("session_id",session_id);
                    Intent i = new Intent(MainActivity.this, ChatRoomActivity.class);
                    i.putExtra("session_id", session_id);
                    startActivity(i);
                }else if(method=="connectFCMUser"){
                    int connected=data.getInt("connected");
                    Log.e("connected::::", String.valueOf(connected));
                    if(connected==2){
                        exitChat();
                    }else{
                        bar.dismiss();
                        Intent i = new Intent(MainActivity.this, ChatRoomActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.putExtra("title",String.valueOf(connected));
                        startActivity(i);
                    }

                }else if(method=="exitChat"){
                    connectFCMUser();
                }

            }catch (Exception e) {
                e.printStackTrace();
            }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash_screen, menu);

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //exitChat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //exitChat();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOnline();
    }
}