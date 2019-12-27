package com.example.sata.myapplication2.POJO;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente extends AsyncTask {

    Socket cliente;
    PrintWriter writer;
    String msg;
    Context context;
    Handler handler = new Handler();

    public Cliente(Context context){
        this.context=context;
    }


    @Override
    protected Object doInBackground(Object[] objects) {

        try {
            msg =(String) objects[0];
            cliente = new Socket("37.59.77.107",19006);
            writer = new PrintWriter(cliente.getOutputStream());
            writer.write(msg);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
