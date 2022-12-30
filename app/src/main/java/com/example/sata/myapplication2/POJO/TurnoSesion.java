package com.example.sata.myapplication2.POJO;

import java.util.Map;

public class TurnoSesion {

//    private String fechaPuesto;
//    private String nombrePuesto;
//    private String egresoPuesto;
//    private String ingresoPuesto;
//    private String horaIngreso;
//    private String fechaIngreso;
//    private String horaEgreso;
//    private Boolean turnoNoche;

    private String fp;
    private String np;
    private String ep;
    private String ip;
    private String hi;
    private String fi;
    private String he;
    private Boolean tn;

    public TurnoSesion(String fechaPuesto, String nombrePuesto, String egresoPuesto, String ingresoPuesto, String horaIngreso, String fechaIngreso, String horaEgreso, Boolean turnoNoche) {
        this.fp = fechaPuesto;
        this.np = nombrePuesto;
        this.ep = egresoPuesto;
        this.ip = ingresoPuesto;
        this.hi = horaIngreso;
        this.fi = fechaIngreso;
        this.he = horaEgreso;
        this.tn = turnoNoche;
    }

    public TurnoSesion(Map<String, Object> map) {
        this.fp = (String) map.get("fp");
        this.np = (String) map.get("np");
        this.ep = (String) map.get("ep");
        this.ip = (String) map.get("ip");
        this.hi = (String) map.get("hi");
        this.fi = (String) map.get("fi");
        this.he = (String) map.get("he");
        this.tn = (Boolean) map.get("tn");
    }

    public String getFechaIngreso() {
        return fi;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fi = fechaIngreso;
    }

    public String getIngresoPuesto() {
        return ip;
    }

    public void setIngresoPuesto(String ingresoPuesto) {
        this.ip = ingresoPuesto;
    }

    public String getHoraIngreso() {
        return hi;
    }

    public void setHoraIngreso(String horaIngreso) {
        this.hi = horaIngreso;
    }

    public String getHoraEgreso() {
        return he;
    }

    public void setHoraEgreso(String horaEgreso) {
        this.he = horaEgreso;
    }

    public String getFechaPuesto() {
        return fp;
    }

    public void setFechaPuesto(String fechaPuesto) {
        this.fp = fechaPuesto;
    }

    public String getNombrePuesto() {
        return np;
    }

    public void setNombrePuesto(String nombrePuesto) {
        this.np = nombrePuesto;
    }

    public String getEgresoPuesto() {
        return ep;
    }

    public void setEgresoPuesto(String egresoPuesto) {
        this.ep = egresoPuesto;
    }

    public Boolean getTurnoNoche() {
        return tn;
    }

    public void setTurnoNoche(Boolean turnoNoche) {
        this.tn = turnoNoche;
    }

}
