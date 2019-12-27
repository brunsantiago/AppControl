package com.example.sata.myapplication2;

import android.app.Activity;
import android.content.Context;

import java.util.Date;

public class Configurador {

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
