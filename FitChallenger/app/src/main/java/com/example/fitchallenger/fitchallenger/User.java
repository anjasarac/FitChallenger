package com.example.fitchallenger.fitchallenger;
public class User {
    public String email, name, username,lastname,phone,picture;
    public int points,age;

    public User(){

    }


    public User(String email,String username,String name,String lastname,String phone,String picture,int age) {
        this.email = email;
        this.name=name;
        this.username=username;
        this.phone=phone;
        this.picture=picture;
        this.lastname = lastname;
        this.points = 0;
        this.age = age;
    }

    @Override
    public String toString()
    {
        return this.username;
    }

}
