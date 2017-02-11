package com.example.aman.myapplication;

/**
 * Created by Gagan Sidhu on 5/19/2016.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
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
                JSONObject jsonObject = new JSONObject();
                JSONArray arr = (JSONArray) params[0].get("data");
                Log.e("JSONArray", String.valueOf(arr));
                if (arr.length() > 0) {
                    jsonObject.accumulate("message",arr.getJSONObject(0).get("message"));
                    Log.e("--------", (String) arr.getJSONObject(0).get("message"));
                   // String json = jsonObject.toString();
                   // DataOutputStream dataoutput = new DataOutputStream(urlConnection.getOutputStream());
                   // dataoutput .write(json.getBytes("UTF-8"));
                   // dataoutput.flush();

                   /* String urlParameters = "";

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        urlParameters += obj.get("key") + "=" + URLEncoder.encode((String) obj.get("value"), "UTF-8");
                        if (i != arr.length() - 1) {
                            urlParameters += "&";
                        }
                        urlParameters.replace("%2C", ",");

                    }
                    urlConnection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");

//                    urlConnection.setRequestProperty("Content-Length", "" +
//                            Integer.toString(urlParameters.getBytes().length));
                    urlConnection.setRequestProperty("Content-Language", "en-US");
                    urlConnection.setUseCaches(false);
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    DataOutputStream data = new DataOutputStream(urlConnection.getOutputStream());
                    data.writeBytes(urlParameters);
                    data.flush();
                    data.close();*/
                } //else {
                    jsonObject.accumulate("device_token",params[0].get("device_token"));
                    String json = jsonObject.toString();
                    DataOutputStream dataoutput = new DataOutputStream(urlConnection.getOutputStream());
                    dataoutput .write(json.getBytes("UTF-8"));
                    dataoutput.flush();



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
        }
        return null;
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
            listener.processResult(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
