package com.example.sata.myapplication2.POJO;

import java.util.Map;

public class UltimaSesion {

    private String cliente;
    private String fechaPuesto;
    private String objetivo;
    private String sesionID;
    private String nombrePuesto;
    private String egresoPuesto;
    private String ingresoPuesto;
    private String horaIngreso;
    private String fechaIngreso;
    private String horaEgreso;
    private Boolean turnoNoche;

    public UltimaSesion(String cliente, String fechaPuesto, String objetivo, String sesionID, String nombrePuesto, String egresoPuesto, String ingresoPuesto, String horaIngreso, String horaEgreso, Boolean turnoNoche) {
        this.cliente = cliente;
        this.fechaPuesto = fechaPuesto;
        this.objetivo = objetivo;
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
        this.cliente = (String) map.get("cliente");
        this.fechaPuesto = (String) map.get("fechaPuesto");
        this.objetivo = (String) map.get("objetivo");
        this.sesionID = (String) map.get("sesionID");
        this.nombrePuesto = (String) map.get("nombrePuesto");
        this.egresoPuesto = (String) map.get("egresoPuesto");
        this.ingresoPuesto = (String) map.get("ingresoPuesto");
        this.horaIngreso = (String) map.get("horaIngreso");
        this.fechaIngreso = (String) map.get("fechaIngreso");
        this.horaEgreso = (String) map.get("horaEgreso");
        this.turnoNoche = (Boolean) map.get("turnoNoche");
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

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getFechaPuesto() {
        return fechaPuesto;
    }

    public void setFechaPuesto(String fechaPuesto) {
        this.fechaPuesto = fechaPuesto;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
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

}
