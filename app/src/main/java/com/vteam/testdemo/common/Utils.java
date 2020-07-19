package com.vteam.testdemo.common;

public class Utils {
    public static String setOneToOneChat(String uid1, String uid2) {
        if(uid1.compareTo(uid2)<0){
            return uid1+uid2;
        }else{
            return uid2+uid1;
        }
    }

}
