package com.example.sata.myapplication2;

import android.app.Activity;
import android.content.Context;

import java.util.Date;

public class Configurador {

    //public static final String API_PATH = "http://192.168.1.8:3000/api/"; // Dev Local
    //public static final String API_PATH = "http://186.182.25.11:3000/api/"; // Pro Server Datamanager
    //public static final String API_PATH = "https://api-datamanager.click/api/"; //AMAZON AWS EC2
    public static final String API_PATH = "https://app-server.com.ar/api-dm/"; // PLESK - app-server.com.ar

    private Date finSesion;

    private static Configurador miconfigurador;

    public  static Configurador getInstance() {
        if (miconfigurador==null) {
            miconfigurador = new Configurador();
        }
        return miconfigurador;
    }

    private Configurador(){ }

    public Date getFinSesion() {
        return finSesion;
    }

    public void setFinSesion(Date finSesion) {
        this.finSesion = finSesion;
    }
}
