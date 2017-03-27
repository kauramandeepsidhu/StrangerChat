package com.example.aman.myapplication;

import android.graphics.Bitmap;

/**
 * Created by Amandeep Kaur on 5/9/2016.
 */

public class ChatMessage {
    private long id;
    private boolean isMe;
    private String message;
    private Long userId;
    private String dateTime;
    //private Bitmap bitmap=null;
    private String imgUri=null;
    private boolean haveImage=false;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public boolean getIsme() {
        return isMe;
    }
    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDate() {
        return dateTime;
    }
    public void setDate(String dateTime) {
        this.dateTime = dateTime;
    }

    //public Bitmap getImage(){return bitmap;}
    //public void setImage(Bitmap bm){this.bitmap=bm;}

    public String getImage(){return imgUri;}
    public void setImage(String imgUri){this.imgUri=imgUri;}

    public boolean getHaveImage(){return haveImage;}
    public void setHaveImage(boolean haveImage){this.haveImage=haveImage;}
}