package com.example.sata.myapplication2.POJO;

import java.util.Map;

public class UltimaSesion {

    private String nombreCliente;
    private String nombreObjetivo;
    private String fechaPuesto;
    private String sesionID;
    private String nombrePuesto;
    private String egresoPuesto;
    private String ingresoPuesto;
    private String horaIngreso;
    private String fechaIngreso;
    private String horaEgreso;
    private Boolean turnoNoche;

    private String pathTurno;

    public UltimaSesion(String nombreCliente, String fechaPuesto, String nombreObjetivo, String sesionID, String nombrePuesto, String egresoPuesto, String ingresoPuesto, String horaIngreso, String fechaIngreso, String horaEgreso, Boolean turnoNoche) {
        this.nombreCliente = nombreCliente;
        this.fechaPuesto = fechaPuesto;
        this.nombreObjetivo = nombreObjetivo;
        this.sesionID = sesionID;
        this.nombrePuesto = nombrePuesto;
        this.egresoPuesto = egresoPuesto;
        this.ingresoPuesto = ingresoPuesto;
        this.horaIngreso = horaIngreso;
        this.fechaIngreso = fechaIngreso;
        this.horaEgreso = horaEgreso;
        this.turnoNoche = turnoNoche;
    }

    public UltimaSesion(Map<String, Object> map) {
        this.nombreCliente = (String) map.get("nombreCliente");
        this.fechaPuesto = (String) map.get("fechaPuesto");
        this.nombreObjetivo = (String) map.get("nombreObjetivo");
        this.sesionID = (String) map.get("sesionID");
        this.nombrePuesto = (String) map.get("nombrePuesto");
        this.egresoPuesto = (String) map.get("egresoPuesto");
        this.ingresoPuesto = (String) map.get("ingresoPuesto");
        this.horaIngreso = (String) map.get("horaIngreso");
        this.fechaIngreso = (String) map.get("fechaIngreso");
        this.horaEgreso = (String) map.get("horaEgreso");
        this.turnoNoche = (Boolean) map.get("turnoNoche");
        this.pathTurno = (String) map.get("pathTurno");
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreObjetivo() {
        return nombreObjetivo;
    }

    public void setNombreObjetivo(String nombreObjetivo) {
        this.nombreObjetivo = nombreObjetivo;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getIngresoPuesto() {
        return ingresoPuesto;
    }

    public void setIngresoPuesto(String ingresoPuesto) {
        this.ingresoPuesto = ingresoPuesto;
    }

    public String getHoraIngreso() {
        return horaIngreso;
    }

    public void setHoraIngreso(String horaIngreso) {
        this.horaIngreso = horaIngreso;
    }

    public String getHoraEgreso() {
        return horaEgreso;
    }

    public void setHoraEgreso(String horaEgreso) {
        this.horaEgreso = horaEgreso;
    }

    public String getFechaPuesto() {
        return fechaPuesto;
    }

    public void setFechaPuesto(String fechaPuesto) {
        this.fechaPuesto = fechaPuesto;
    }

    public String getSesionID() {
        return sesionID;
    }

    public void setSesionID(String sesionID) {
        this.sesionID = sesionID;
    }

    public String getNombrePuesto() {
        return nombrePuesto;
    }

    public void setNombrePuesto(String nombrePuesto) {
        this.nombrePuesto = nombrePuesto;
    }

    public String getEgresoPuesto() {
        return egresoPuesto;
    }

    public void setEgresoPuesto(String egresoPuesto) {
        this.egresoPuesto = egresoPuesto;
    }

    public Boolean getTurnoNoche() {
        return turnoNoche;
    }

    public void setTurnoNoche(Boolean turnoNoche) {
        this.turnoNoche = turnoNoche;
    }

    public String getPathTurno() {
        return pathTurno;
    }

    public void setPathTurno(String pathTurno) {
        this.pathTurno = pathTurno;
    }

}
