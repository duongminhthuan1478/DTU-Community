package com.example.tunguyencomputer.dtu_community.Ultil;

import android.content.Context;
import android.widget.Toast;

public class ShowToast {

    public static void showToast(Context context, String messsage){
        Toast.makeText(context, messsage, Toast.LENGTH_SHORT).show();
    }
}
