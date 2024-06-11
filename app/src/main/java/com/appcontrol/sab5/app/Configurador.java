
package com.appcontrol.sab5.app;

import java.util.Date;

public class Configurador {

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
