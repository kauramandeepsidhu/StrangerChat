package com.example.aman.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Gagan Sidhu on 1/24/2017.
 */
public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "ChatSharedPref";
    private static final String TAG_TOKEN = "tagtoken";

    private static SharedPrefManager mInstance;
    private static Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    //this method will save the device token to shared preferences
    public boolean saveDeviceToken(String token){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TAG_TOKEN, token);
        editor.apply();
        return true;
    }

    //this method will fetch the device token from shared preferences
    public String getDeviceToken(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(TAG_TOKEN, null);
    }
    public void saveChatList(ArrayList<ChatMessage> chatHistory){
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(chatHistory);
        editor.putString("myChatHistory", json);
        editor.commit();
        Log.e("SharedPrefManager","chatHistory saved..."+json);
    }
    public ArrayList<ChatMessage> getChatList() {
        ArrayList<ChatMessage> chatHistory = new ArrayList<ChatMessage>();
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("myChatHistory", "");
        if (json.isEmpty()) {
            chatHistory = new ArrayList<ChatMessage>();
        } else {
            Type type = new TypeToken<ArrayList<ChatMessage>>() {
            }.getType();
            chatHistory = gson.fromJson(json, type);
        }
        Log.e("SharedPrefManager","chatHistory feched..."+json);
        return chatHistory;
    }
    public void clearChatHistory(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("myChatHistory").commit();
        Log.e("SharedPrefManager", "chatHistory cleared...");
    }

}
