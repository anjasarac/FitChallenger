package com.example.fitchallenger.fitchallenger;

import android.support.annotation.NonNull;

import java.util.Comparator;

public class User implements Comparable<User>{
    public String email, name, username,lastname,phone,picture,longitude,latitude;
    public long points,age;
    boolean visible;

    public User(){

    }


    public User(String email,String username,String name,String lastname,String phone,String picture,long age) {
        this.email = email;
        this.name=name;
        this.username=username;
        this.phone=phone;
        this.picture=picture;
        this.lastname = lastname;
        this.points = 0;
        this.age = age;
        this.visible=false;

    }

    @Override
    public String toString()
    {
        return this.username;
    }

    @Override
    public int compareTo(@NonNull User o) {
        return (int) (o.points-this.points);
    }
}

