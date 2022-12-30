package com.example.sata.myapplication2.POJO;

import java.util.Map;

public class UltimaSesion {

//    private String nombreCliente;
//    private String nombreObjetivo;
//    private String fechaPuesto;
//    private String sesionID;
//    private String pathTurno;
//    private String nombrePuesto;
//    private String egresoPuesto;
//    private String ingresoPuesto;
//    private String horaIngreso;
//    private String fechaIngreso;
//    private String horaEgreso;
//    private Boolean turnoNoche;

    private String nc;
    private String no;
    private String fp;
    private String si;
    private String pt;

//    public UltimaSesion(String nombreCliente, String fechaPuesto, String nombreObjetivo, String sesionID, String nombrePuesto, String egresoPuesto, String ingresoPuesto, String horaIngreso, String fechaIngreso, String horaEgreso, Boolean turnoNoche) {
//        this.nc = nombreCliente;
//        this.fp = fechaPuesto;
//        this.no = nombreObjetivo;
//        this.si = sesionID;
//        this.nombrePuesto = nombrePuesto;
//        this.egresoPuesto = egresoPuesto;
//        this.ingresoPuesto = ingresoPuesto;
//        this.horaIngreso = horaIngreso;
//        this.fechaIngreso = fechaIngreso;
//        this.horaEgreso = horaEgreso;
//        this.turnoNoche = turnoNoche;
//    }

    public UltimaSesion(Map<String, Object> map) {
        this.nc = (String) map.get("nc");
        this.fp = (String) map.get("fp");
        this.no = (String) map.get("no");
        this.si = (String) map.get("si");
        this.pt = (String) map.get("pt");
//        this.nombrePuesto = (String) map.get("nombrePuesto");
//        this.egresoPuesto = (String) map.get("egresoPuesto");
//        this.ingresoPuesto = (String) map.get("ingresoPuesto");
//        this.horaIngreso = (String) map.get("horaIngreso");
//        this.fechaIngreso = (String) map.get("fechaIngreso");
//        this.horaEgreso = (String) map.get("horaEgreso");
//        this.turnoNoche = (Boolean) map.get("turnoNoche");

    }

    public String getNombreCliente() {
        return nc;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nc = nombreCliente;
    }

    public String getNombreObjetivo() {
        return no;
    }

    public void setNombreObjetivo(String nombreObjetivo) {
        this.no = nombreObjetivo;
    }

//    public String getFechaIngreso() {
//        return fechaIngreso;
//    }
//
//    public void setFechaIngreso(String fechaIngreso) {
//        this.fechaIngreso = fechaIngreso;
//    }
//
//    public String getIngresoPuesto() {
//        return ingresoPuesto;
//    }
//
//    public void setIngresoPuesto(String ingresoPuesto) {
//        this.ingresoPuesto = ingresoPuesto;
//    }
//
//    public String getHoraIngreso() {
//        return horaIngreso;
//    }
//
//    public void setHoraIngreso(String horaIngreso) {
//        this.horaIngreso = horaIngreso;
//    }
//
//    public String getHoraEgreso() {
//        return horaEgreso;
//    }
//
//    public void setHoraEgreso(String horaEgreso) {
//        this.horaEgreso = horaEgreso;
//    }

    public String getFechaPuesto() {
        return fp;
    }

    public void setFechaPuesto(String fechaPuesto) {
        this.fp = fechaPuesto;
    }

    public String getSesionID() {
        return si;
    }

    public void setSesionID(String sesionID) {
        this.si = sesionID;
    }

//    public String getNombrePuesto() {
//        return nombrePuesto;
//    }
//
//    public void setNombrePuesto(String nombrePuesto) {
//        this.nombrePuesto = nombrePuesto;
//    }
//
//    public String getEgresoPuesto() {
//        return egresoPuesto;
//    }
//
//    public void setEgresoPuesto(String egresoPuesto) {
//        this.egresoPuesto = egresoPuesto;
//    }
//
//    public Boolean getTurnoNoche() {
//        return turnoNoche;
//    }
//
//    public void setTurnoNoche(Boolean turnoNoche) {
//        this.turnoNoche = turnoNoche;
//    }

    public String getPathTurno() {
        return pt;
    }

    public void setPathTurno(String pathTurno) {
        this.pt = pathTurno;
    }

}
