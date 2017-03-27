package com.example.aman.myapplication;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.view.KeyEvent;

import com.google.firebase.messaging.FirebaseMessaging;


/**
 * Created by Amandeep Kaur on 5/9/2016.
 */
public class ChatRoomActivity extends AppCompatActivity implements AsyncRequest.newInterface,LoadImageTask.Listener {

    AsyncRequest req = null;
    private EditText messageET;
    private TextView subTitle;
    private ListView messagesContainer;
    private ImageView sendBtn,imageUploadBtn;
    private Bitmap bm;
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
    private static final int SELECT_PICTURE = 1;
    private static final int REQUEST_CAMERA = 2;
    private static final int UPLOAD_IMAGE=3;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA=123;
    private Context context;
    private File imageFile;
    private String imageUri=null;
    private AlertDialog.Builder alertDialogBuilder;
    LoadImageTask ld=null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_room);

        // Find the toolbar view inside the activity layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        toolbar.setTitle("");
        //toolbar.setNavigationIcon(R.drawable.ic_person_red);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        device_token = SharedPrefManager.getInstance(this).getDeviceToken();
        initControls();


    }
    private void initControls() {
        context=this;
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (ImageView) findViewById(R.id.chatSendButton);
        imageUploadBtn=(ImageView)findViewById(R.id.ivUploadImage);
        subTitle=(TextView)findViewById(R.id.subtitle);
        adapter = new ChatAdapter(ChatRoomActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
        alertDialogBuilder = new AlertDialog.Builder(this);

        bar=new ProgressDialog(ChatRoomActivity.this);
        bar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        bar.setTitle("Searching For Partner");
        bar.setMessage("Processing...");

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageText = messageET.getText().toString();
                Log.e("messageText", messageText);
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMessage(messageText);
                // chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
                String formattedDate = formatDate.format(new Date()).toString();
                Log.e("---date", formattedDate);
                chatMessage.setDate(formattedDate);
                chatMessage.setMe(true);
                chatMessage.setHaveImage(false);
                Log.e("chatMessage", chatMessage.getMessage());
                String image="";
                chatHistory.add(chatMessage);
                messageET.setText("");
                if (messageText != null) {
                    //sendMessage(messageText);
                    //Log.e("sendMessage",messageText);
                    sendFCMMessage(messageText,image,1);
                }
                Log.e("displayMessage", "called");
                displayMessage(chatMessage);

            }
        });

        imageUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkForPermission()) {
                        selectImageOption();
                    } else {
                        requestForPermission();

                    }
                }
                selectImageOption();
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
        //........Receive Messages from broadcast notifications and intents.........//
        getRegistrationBroadcastReceiver();
        getIntents();
    }
    public boolean checkForPermission(){
        //Getting the permission status
        int cameraResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        int writeToStorageResult= ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readStorageResult=ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        //If permission is granted returning true
        if (cameraResult == PackageManager.PERMISSION_GRANTED
                && writeToStorageResult==PackageManager.PERMISSION_GRANTED
                && readStorageResult==PackageManager.PERMISSION_GRANTED){
                return true;
        }
        //If permission is not granted returning false
        return false;

    }
    public void requestForPermission(){
        if ((ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.CAMERA))
                &&(ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                &&(ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,  Manifest.permission.READ_EXTERNAL_STORAGE))) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle("Permission necessary");
            alertBuilder.setMessage("Please provide these permissions!!!");
            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            });
            AlertDialog alert = alertBuilder.create();
            alert.show();
        } else {

            ActivityCompat.requestPermissions((Activity)context, new String[]{android.Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImageOption();
                } else {
                    //code for deny
                    Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void getRegistrationBroadcastReceiver(){
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    title=intent.getStringExtra("title");
                    if(title.equals("message")){
                        //set subTitle
                        subTitle.setText("Online");
                        bar.dismiss();
                        message = intent.getStringExtra("message");
                        String image=intent.getStringExtra("image");
                        Log.e("ChatRoomActivity", "image value " + image);

                        if(!(image.isEmpty())){
                            //bm=getBitmapImage(image);
                            //Log.e("bitmap2", String.valueOf(bm));
                            //chatMessage.setImage(bm);
                            //chatMessage.setHaveImage(true);

                            new LoadImageTask(ChatRoomActivity.this).execute(image);
                        }else{
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setId(122);//dummy
                            chatMessage.setMessage(message);
                            SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
                            String formattedDate = formatDate.format(new Date()).toString();
                            chatMessage.setDate(formattedDate);
                            chatMessage.setMe(false);
                            Log.e("NotificationMessage", chatMessage.getMessage());
                            chatHistory.add(chatMessage);
                            displayMessage(chatMessage);
                        }

                    }else if(title.equals("connected")){
                        Log.e("From Notification","status connected");
                        bar.hide();
                        bar.dismiss();
                        subTitle.setText("Online");
                        Toast.makeText(getApplicationContext(), "You can send message now ", Toast.LENGTH_LONG).show();
                    }else if(title.equals("Waiting")){
                        Log.e("From Notification","status waiting");
                        subTitle.setText("Offline");
                        bar.show();
                    }

                }
            }
        };
    }
    public void getIntents(){
        Intent intent = getIntent();
        String title1= intent.getExtras().getString("title");
        //Log.e("Value in intent", title);

        if(title1.equals("message")){
            subTitle.setText("Online");
            chatHistory=SharedPrefManager.getInstance(getApplicationContext()).getChatList();
            for(i=0;i<chatHistory.size();i++){
                displayMessage(chatHistory.get(i));
                Log.e("displaying "+i, String.valueOf(chatHistory.get(i)));
            }
            Log.e("inside", "if message");
            message=intent.getExtras().getString("message");
            String image=intent.getExtras().getString("image");

            if(!(image.isEmpty())){
               // bm=getBitmapImage(image);
               // Log.e("bitmap1", String.valueOf(bm));
               // chatMessage.setImage(bm);
               // chatMessage.setHaveImage(true);
                new LoadImageTask(ChatRoomActivity.this).execute(image);
            }else{
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMessage(message);
                SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
                String formattedDate = formatDate.format(new Date()).toString();
                chatMessage.setDate(formattedDate);
                chatMessage.setMe(false);
                chatHistory.add(chatMessage);
                displayMessage(chatMessage);
                Log.e("IntentMessage", chatMessage.getMessage());

            }


        }
        if(title1.equals("0")||title1.equals("waiting")){
            Log.e("From Intents:", "status waiting");
            subTitle.setText("Offline");
            bar.show();
        }
        else if(title1.equals("1")){
            Log.e("From Intents","status connected");
            subTitle.setText("Online");
            bar.dismiss();
            Toast.makeText(this,"You can send message now.",Toast.LENGTH_SHORT).show();
        }
        // Log.e("session_id", session_id);
        Log.e("chatroomActivity", "getIntents");

    }


    public void selectImageOption(){

       /* final Dialog openDialog = new Dialog(context);
        openDialog.setContentView(R.layout.custom_dialog);
        openDialog.setTitle("Custom Dialog Box");
        openDialog.show();
        TextView tvTakePhoto = (TextView)openDialog.findViewById(R.id.tvTakePhoto);
        TextView tvChooseFromGallery = (TextView)openDialog.findViewById(R.id.tvChooseFromGallery);
        TextView tvCancel = (TextView)openDialog.findViewById(R.id.tvCancel);
        tvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                imageFile = f;
                startActivityForResult(intent, REQUEST_CAMERA);
                openDialog.dismiss();
            }
        });
        tvChooseFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_PICTURE);
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });*/


        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        alertDialogBuilder.setTitle("Add Photo!");
        alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Take Photo")) {
                    dialog.cancel();
                    dialog.dismiss();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File directory=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"StrangerChat");
                    if(!directory.exists()){
                        if(!directory.mkdirs()){
                            Log.e("ChatRoomActivity", "Failed to create directory");
                        }
                    }
                    String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File f = new File(directory.getPath()+File.separator+"IMG_"+timeStamp+".jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    imageFile = f;
                    startActivityForResult(intent, REQUEST_CAMERA);

                } else if (items[which].equals("Choose from Library")) {
                    dialog.cancel();
                    dialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_PICTURE);
                } else if (items[which].equals("Cancel")) {
                    dialog.cancel();
                    dialog.dismiss();
                }
            }
        });
        alertDialogBuilder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.e("ChatRoomActivity",requestCode+" , "+resultCode+" , "+data.getData());
        if(resultCode==Activity.RESULT_OK){
            Log.e("ChatRoomActivity", "inside onActivityResult Method:"+resultCode);
            if (requestCode==REQUEST_CAMERA){
                Log.e("ChatRoomActivity","inside onActivityResult Method");
                File f=new File(Environment.getExternalStorageDirectory().toString());
                imageUri=imageFile.getPath();

                BitmapFactory.Options bmapOptions=new BitmapFactory.Options();
                bmapOptions.inSampleSize = 2;
                bm = BitmapFactory.decodeFile(imageUri, bmapOptions);

                final Dialog openDialog = new Dialog(context);
                openDialog.setContentView(R.layout.uploadimage_dialog);
                openDialog.setTitle("Upload Image");
                openDialog.show();
                ImageButton ibtnUpload = (ImageButton)openDialog.findViewById(R.id.SendImageButton);
                RelativeLayout rlImage=(RelativeLayout)openDialog.findViewById(R.id.LayoutUploadedImage);
                Drawable dr = new BitmapDrawable(bm);
                rlImage.setBackgroundDrawable(dr);
                ibtnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialog.hide();
                        openDialog.dismiss();

                        
                        sendImage(imageUri);
                    }
                });
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setMessage("");
                chatMessage.setId(122);//dummy
                chatMessage.setImage(imageUri);
                SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
                String formattedDate = formatDate.format(new Date()).toString();
                chatMessage.setDate(formattedDate);
                chatMessage.setMe(true);
                chatMessage.setHaveImage(true);
                chatHistory.add(chatMessage);
                messageText="";
                Log.e("displayMessage", "called");
                displayMessage(chatMessage);

               /* for (File temp:f.listFiles()){
                    if(temp.getName().equals("temp.jpg")){
                        f=temp;
                        break;
                    }
                    try{
                        BitmapFactory.Options bmapOptions=new BitmapFactory.Options();
                        bmapOptions.inSampleSize=2;
                        bm=BitmapFactory.decodeFile(f.getAbsolutePath(),bmapOptions);
                        Uri selectedImageUri=imageFile;
                        String tempath=getPath(selectedImageUri, this);

                        path=android.os.Environment.getExternalStorageState()+File.separator+"test";
                        f.delete();
                        OutputStream fOut=null;
                        File file=new File(path,String.valueOf(System.currentTimeMillis())+".jpg");
                        fOut=new FileOutputStream(file);
                        bm.compress(Bitmap.CompressFormat.JPEG,85,fOut);
                        fOut.flush();
                        fOut.close();

                        Intent i=new Intent(this,ImageUploadActivity.class);
                        i.putExtra("image", imageFile.getAbsolutePath());
                        startActivityForResult(i, UPLOAD_IMAGE);


                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }*/
            }else if (requestCode==SELECT_PICTURE){
                Uri selectedImageUri=data.getData();
                String tempath=getPath(selectedImageUri, this);
                imageUri=tempath;
                BitmapFactory.Options bmapOptions=new BitmapFactory.Options();
                bmapOptions.inSampleSize = 2;
                bm = BitmapFactory.decodeFile(tempath, bmapOptions);
                //Intent intent=new Intent(this,ImageUploadActivity.class);
                //intent.putExtra("image",tempath);
                //startActivityForResult(intent, UPLOAD_IMAGE);
                final Dialog openDialog = new Dialog(context);
                openDialog.setContentView(R.layout.uploadimage_dialog);
                openDialog.setTitle("Upload Image");
                openDialog.show();
                ImageButton ibtnUpload = (ImageButton)openDialog.findViewById(R.id.SendImageButton);
                RelativeLayout rlImage=(RelativeLayout)openDialog.findViewById(R.id.LayoutUploadedImage);
                Drawable dr = new BitmapDrawable(bm);
                rlImage.setBackgroundDrawable(dr);
                ibtnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialog.hide();
                        openDialog.dismiss();
                        sendImage(imageUri);
                    }
                });
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setMessage("");
                chatMessage.setId(122);//dummy
                chatMessage.setImage(imageUri);
                SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
                String formattedDate = formatDate.format(new Date()).toString();
                chatMessage.setDate(formattedDate);
                chatMessage.setMe(true);
                chatMessage.setHaveImage(true);
                chatHistory.add(chatMessage);
                messageText="";
                Log.e("displayMessage", "called");
                displayMessage(chatMessage);
            }
        }if(requestCode==3){
            String tempath=data.getStringExtra("image");
            BitmapFactory.Options bmapOptions=new BitmapFactory.Options();
            bmapOptions.inSampleSize = 2;
            bm = BitmapFactory.decodeFile(tempath, bmapOptions);

            subTitle.setText("Online");
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage("");
            chatMessage.setId(122);//dummy
            chatMessage.setImage(imageUri);
            chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
            chatMessage.setMe(true);
            chatMessage.setHaveImage(true);
            chatHistory.add(chatMessage);
            messageText="";
            final String image = getStringImage(bm);

            if (image != null) {
                //sendMessage(messageText);
                //Log.e("sendMessage",messageText);
                //sendFCMMessage(messageText,image);

                        sendImage(imageUri);


            }
            Log.e("displayMessage", "called");
            displayMessage(chatMessage);

        }

    }
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        Log.e("imageBytes", String.valueOf(imageBytes));
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.e("ChatRoomActivity---","encodedImage:  "+encodedImage);
        return encodedImage;
    }
    public Bitmap getBitmapImage(String image){
       /* Bitmap imageBitmap;
      //  byte[] bytes = image.getBytes();
        byte[] bytes=Base64.decode(image.getBytes(), Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        imageBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
        return imageBitmap;*/
        byte[] decodedString = Base64.decode(image.getBytes(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;

    }
    public String getPath(Uri uri, Activity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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


    public void sendFCMMessage(String messageText,String image,int which){
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
                data.put("image",image);
                data.put("uploadImage","false");
                data.put("which",which);
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


    public void sendImage(String imageUri){
        Log.e("chatroomActivity","sendImage*****"+imageUri);
        Bitmap resizedImage=CommonUtils.decodeSampledBitmapFromResource(imageUri,100,100);
        String imagePath=CommonUtils.saveTempImageToStorage(resizedImage);
        method="sendImage";
        try{
            initializeAsync();
            JSONObject params=new JSONObject();
            JSONObject data=new JSONObject();
            //contains all data objects
            JSONArray arr=new JSONArray();
            try{
                //FINAL OBJECT TO BE SENT IN ASYNC CLASS
                data.put("image",imagePath);
                data.put("uploadImage","true");
                arr.put(data);
                params.put("url", "http://ahapp.herokuapp.com/saveimageandgetlink");
                params.put("type","POST");
                params.put("enctype", "multipart/form-data");
                //method is for referencing this object later on
                params.put("method",method);
                params.put("fileToUpload",imageUri);
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
        SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
        String formattedDate = formatDate.format(new Date()).toString();
        chatMessage.setDate(formattedDate);
        chatMessage.setMe(true);
        Log.e("chatMessage", chatMessage.getMessage());
        chatHistory.add(chatMessage);
        messageET.setText("");
        String image="";
        if(messageText!=null){
            //sendMessage(messageText);
            //Log.e("sendMessage",messageText);
            sendFCMMessage(messageText,image,1);
        }
        Log.e("displayMessage", "called");
        displayMessage(chatMessage);

    }

    public void displayMessage(ChatMessage message) {
//        Log.e("display", message.getMessage());
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
        Log.e("MainActivity", "exitChat");
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
                    subTitle.setText("Offline");
                    Toast.makeText(this,"Partner is offline",Toast.LENGTH_SHORT).show();
                }
            }else if(method=="exitChat"){
               // bar.dismiss();
                String check=data.getString("deleted");
                Log.e("check:",check);
                if(check=="True"){
                    startActivity(new Intent(this,MainActivity.class));
                }
            }else if(method=="sendImage"){
                String imageLink=data.getString("imageurl");
                String msg="";
                Log.e("imageLink",imageLink);
                sendFCMMessage(msg,imageLink,0);
            }
        }catch (Exception e) {
//            Toast.makeText(ChatRoomActivity.this, "Login Failed !", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onImageLoaded(String path) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(122);//dummy
        chatMessage.setMessage("");
        SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
        String formattedDate = formatDate.format(new Date()).toString();
        chatMessage.setDate(formattedDate);
        chatMessage.setMe(false);
        //bm=getBitmapImage(image);
        Log.e("bitmap2", String.valueOf(bm));
        chatMessage.setImage(path);
        chatMessage.setHaveImage(true);
        chatHistory.add(chatMessage);
        displayMessage(chatMessage);
        //mImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Error Loading Image !", Toast.LENGTH_SHORT).show();
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

        Log.e("ChatRoomActivity", "onBackPressed()");
        SharedPrefManager.getInstance(getApplicationContext()).clearChatHistory();
        exitChat();
        //finish();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("ChatRoomActivity", "onResume()");
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        MyNotificationManager.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        Log.e("ChatRoomActivity","onPause()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        SharedPrefManager.getInstance(getApplicationContext()).saveChatList(chatHistory);
        super.onPause();
    }


}


