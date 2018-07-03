package com.example.fitchallenger.fitchallenger;

import com.google.android.gms.maps.model.LatLng;

public class Challenge
{
    public boolean dynamic;
    public String type,tasks,latitude,longitude,startDate,endDate,userID,username;
    public int points;
    public Challenge(){}

    public Challenge(Boolean dynamic,String type,String tasks,String lat,String lon, String sDate,String eDate,String userId,String username,int points)
    {
        this.dynamic = dynamic;
        this.type = type;
        this.tasks = tasks;
        this.latitude = lat;
        this.longitude = lon;
        this.startDate = sDate;
        this.endDate = eDate;
        this.userID = userId;
        this.username = username;
        this.points = points;
    }



}
