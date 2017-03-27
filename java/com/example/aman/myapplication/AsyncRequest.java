package com.example.aman.myapplication;

/**
 * Created by Amandeep Kaur on 5/19/2016.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Scanner;

/**
 * THIS CLASS IS USED FOR FIRING ALL KINDS OF ASYNC REQUESTS ON SERVER
 */

public class AsyncRequest extends AsyncTask<JSONObject, Integer, JSONArray> {

    public Activity activity;
    Boolean isInternetAvailable = true;
    Integer count = 0;
    Boolean hasError = false;

    public AsyncRequest(Activity activity) {
        this.activity = activity;
    }

    public interface newInterface {
        void processResult(JSONArray result) throws JSONException, ParseException;
    }

    public newInterface listener;


    @Override
    protected void onPreExecute() {
        Log.e("inside","onPreExecute");
        CheckConnection c = new CheckConnection();
        if (!c.haveNetworkConnection(activity)) {
            Toast.makeText(activity, "NO INTERNET CONNECTIVITY", Toast.LENGTH_SHORT).show();
            isInternetAvailable = false;
        }


        super.onPreExecute();
    }


    @Override
    protected JSONArray doInBackground(JSONObject... params) {
        // param 0 --> Json object of entire request

        // Json object will contain url , data , type in keys
        Log.e("inside","doInBackground");
        InputStream inputStream = null;

        while (true) {
            if (isInternetAvailable == false) {
                return new JSONArray();
            }
            count += 1;
            URL url = null;
            try {
                url = new URL((String) params[0].get("url"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                urlConnection.setRequestMethod((String) params[0].get("type"));
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
               // JSONObject jsonObject = new JSONObject();
                JSONArray arr = (JSONArray) params[0].get("data");
                Log.e("JSONArray", String.valueOf(arr));
                /*if (arr.length() > 0) {
                    jsonObject.accumulate("message",arr.getJSONObject(0).get("message"));
                    jsonObject.accumulate("fileToUpload",arr.getJSONObject(0).get("image"));
                    Log.e("--------", (String) arr.getJSONObject(0).get("message"));
                    Log.e("--------", (String) arr.getJSONObject(0).get("image"));

                }
                    jsonObject.accumulate("device_token",params[0].get("device_token"));
                    String json = jsonObject.toString();
                    DataOutputStream dataoutput = new DataOutputStream(urlConnection.getOutputStream());
                    dataoutput .write(json.getBytes("UTF-8"));
                    dataoutput.flush();*/
                if (!(arr.length()>0)){
                    Log.e("AsyncRequest","connect api called");
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.accumulate("device_token",params[0].get("device_token"));
                    String json = jsonObject.toString();
                    DataOutputStream dataoutput = new DataOutputStream(urlConnection.getOutputStream());
                    dataoutput .write(json.getBytes("UTF-8"));
                    dataoutput.flush();
                }else{
                    if(arr.getJSONObject(0).get("uploadImage").equals("false")){
                        Log.e("AsyncRequest","send message api called");
                        JSONObject jsonObject=new JSONObject();
                        if(arr.getJSONObject(0).get("which")==0){

                            Log.e("AsyncRequest","image sent");
                            jsonObject.accumulate("message","");
                            jsonObject.accumulate("image",arr.getJSONObject(0).get("image"));

                        }else if(arr.getJSONObject(0).get("which")==1) {
                            Log.e("AsyncRequest","text message sent");
                            jsonObject.accumulate("message", arr.getJSONObject(0).get("message"));
                            jsonObject.accumulate("image","");
                            Log.e("--------", (String) arr.getJSONObject(0).get("message"));
                        }
                        jsonObject.accumulate("device_token",params[0].get("device_token"));
                        String json = jsonObject.toString();
                        DataOutputStream dataoutput = new DataOutputStream(urlConnection.getOutputStream());
                        dataoutput .write(json.getBytes("UTF-8"));
                        dataoutput.flush();
                    }

                    else if(arr.getJSONObject(0).get("uploadImage").equals("true")){
                       // urlConnection.setRequestProperty("ENCTYPE",(String) params[0].get("enctype"));
                       // JSONObject jsonObject=new JSONObject();
                       // jsonObject.accumulate("fileToUpload",arr.getJSONObject(0).get("image"));
                       // Log.e("image--------", (String) arr.getJSONObject(0).get("image"));
                       // String json = jsonObject.toString();
                        Log.e("AsyncRequest","imageUpload api called");
                        String imageUri= (String) arr.getJSONObject(0).get("image");

                        String lineEnd = "\r\n";
                        String twoHyphens = "--";
                        String boundary = "*****";
                        int bytesRead, bytesAvailable, bufferSize;
                        byte[] buffer;
                        int maxBufferSize = 1 * 1024 * 1024;
                        Log.e("AsyncRequest", "imageUri=" + imageUri);
                        File sourceFile = new File(imageUri);
                        String fileName="image.jpg";
                        byte[] bytes = new byte[0];

                        try{
                            FileInputStream fileInputStream = new FileInputStream(sourceFile);
                            bytes = new byte[(int) sourceFile.length()];
                            fileInputStream.read(bytes);
                            fileInputStream.close();
                            Log.e("FileInputStream", String.valueOf(fileInputStream));
                            urlConnection.setDoInput(true); // Allow Inputs
                            urlConnection.setDoOutput(true); // Allow Outputs
                            urlConnection.setUseCaches(false); // Don't use a Cached Copy
                            urlConnection.setRequestProperty("Connection", "Keep-Alive");
                            urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                            urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                            //urlConnection.setRequestProperty("Content-Type", "application/json;boundary=" + boundary);
                           // urlConnection.setRequestProperty("uploaded_file", fileName);


                            DataOutputStream dataoutput = new DataOutputStream(urlConnection.getOutputStream());

                            dataoutput.writeBytes(twoHyphens + boundary + lineEnd);
                            //dataoutput.writeBytes("Content-Disposition: form-data; name=" + fileName + "\"" + lineEnd);
                            dataoutput.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"123456.jpg\"" + lineEnd);
                            dataoutput.writeBytes("Content-Type: image/*" + lineEnd);
                            //dataoutput.writeBytes("Content-Length: " + sourceFile.length() + lineEnd);
                            dataoutput.writeBytes(lineEnd);

                            // create a buffer of  maximum size
                            int bufferLength = 1024;
                            for (int i = 0; i < bytes.length; i += bufferLength) {
                                // publishing the progress....
                                Log.e("AsyncRequest-i", String.valueOf(i));
                                if (bytes.length - i >= bufferLength) {
                                    dataoutput.write(bytes, i, bufferLength);
                                } else {
                                    dataoutput.write(bytes, i, bytes.length - i);
                                }
                            }

                           /* bytesAvailable = fileInputStream.available();

                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            buffer = new byte[bufferSize];

                            // read file and write it into form...
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                            Log.e("bytesRead=", String.valueOf(bytesRead));
                            while (bytesRead > 0) {
                                dataoutput.write(buffer, 0, bufferSize);
                                bytesAvailable = fileInputStream.available();
                                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                                Log.e("writing to dataoutput", String.valueOf(dataoutput));
                            }*/
                            // send multipart form data necesssary after file data...
                            dataoutput.writeBytes(lineEnd);
                            dataoutput.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                            // Responses from the server (code and message)
                            int serverResponseCode = urlConnection.getResponseCode();
                            String serverResponseMessage = urlConnection.getResponseMessage();

                            Log.i("uploadFile", "HTTP Response is : "
                                    + serverResponseMessage + ": " + serverResponseCode);

                            if(serverResponseCode == 200){

                                Log.e("AsyncRequest", "File Upload Completed");
                            }


                            //close the streams //
                            fileInputStream.close();
                            dataoutput.flush();
                        }catch (Exception e){
                            e.printStackTrace();
                        }





                       // dataoutput .write(json.getBytes("UTF-8"));
                       // dataoutput.flush();
                    }

                }

                    //urlConnection.addRequestProperty("session_id", params[0].getString("session_id"));
                //}
                urlConnection.connect();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                 inputStream = urlConnection.getInputStream();
                break;
            } catch (IOException e) {

                if (count == 5) {
                    hasError = true;
                    return new JSONArray();
                }
                e.printStackTrace();
            }
        }
        StringBuffer buffer = new StringBuffer();
        String temp = "";
        Scanner s;
        try {
            publishProgress(30);
            s = new Scanner(inputStream);
        } catch (Exception e) {
            s = null;
        }
        while (s.hasNext()) {
            buffer.append(s.nextLine());
        }

        String jsonString = buffer.toString();
        try {
            return jsonParse(params[0], jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }


    JSONArray jsonParse(JSONObject param, String output) throws JSONException {

        if (param.get("method") == "connectingUser") {
            Log.e("method","connectingUser");
            return responseConnectingUser(output);
        }else if(param.get("method")=="checkOnline"){
            return responseCheckOnline(output);
        }else if(param.get("method")=="checkForPartner"){
            return responseCheckForPartner(output);
        }else if(param.get("method")=="sendMessage"){
            return responseSendMessage(output);
        }else if (param.get("method")=="connectFCMUser"){
            return responseConnectFCMUser(output);
        }else if(param.get("method")=="sendFCMMessage"){
            return responseSendFCMMessage(output);
        }else if(param.get("method")=="exitChat"){
            return responseExitChat(output);
        }else if(param.get("method")=="sendImage"){
            return responseSendImage(output);
        }
        return null;
    }


    JSONArray responseSendImage(String output)throws JSONException{
        JSONArray arr=new JSONArray();
        JSONObject obj=new JSONObject(output);
        arr.put(obj);
        return arr;
    }
    JSONArray responseExitChat(String output)throws JSONException{
        JSONArray arr=new JSONArray();
        JSONObject obj=new JSONObject(output);
        arr.put(obj);
        return arr;
    }
    JSONArray responseSendFCMMessage(String output)throws JSONException{
        JSONArray arr=new JSONArray();
        JSONObject obj=new JSONObject(output);
        arr.put(obj);
        return arr;
    }
    JSONArray responseConnectFCMUser(String output)throws JSONException{
        JSONArray arr=new JSONArray();
        JSONObject obj=new JSONObject(output);
        arr.put(obj);
        return arr;
    }
    JSONArray responseSendMessage(String output)throws JSONException{
        JSONArray arr=new JSONArray();
        JSONObject obj=new JSONObject(output);
        arr.put(obj);
        return arr;
    }
    JSONArray responseCheckForPartner(String output)throws JSONException{
        JSONArray arr =new JSONArray();
        JSONObject obj=new JSONObject(output);
        arr.put(obj);
        return arr;
    }
    JSONArray responseCheckOnline(String output) throws JSONException {
        JSONArray arr = new JSONArray();
       // arr.put(1);
        JSONObject obj = new JSONObject(output);
        arr.put(obj);
        return arr;
    }
    JSONArray responseConnectingUser(String output)throws  JSONException{
        JSONArray arr=new JSONArray();
        JSONObject obj=new JSONObject(output);
        arr.put(obj);
        return arr;
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        super.onPostExecute(jsonArray);
        if (hasError == true) {
            Toast.makeText(activity, "There is some error , please try after some time !", Toast.LENGTH_LONG).show();
            }
        try {
            Log.e("response", String.valueOf(jsonArray));
            listener.processResult(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
