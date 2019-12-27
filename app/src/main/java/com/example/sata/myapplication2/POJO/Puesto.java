package com.example.sata.myapplication2.POJO;

import java.util.Map;

public class Puesto {

    private String nombrePuesto;
    private String nombreTurno;
    private String ingresoPuesto;
    private String egresoPuesto;
    private String horasTurno;
    private Boolean turnoNoche;
    private String fechaPuesto;
    private String estado;

    public Puesto() {}

    public Puesto(String nombrePuesto, String ingresoPuesto, String egresoPuesto, String horasTurno, Boolean turnoNoche, String fechaPuesto) {
        this.nombrePuesto = nombrePuesto;
        this.ingresoPuesto = ingresoPuesto;
        this.egresoPuesto = egresoPuesto;
        this.horasTurno = horasTurno;
        this.turnoNoche = turnoNoche;
        this.fechaPuesto = fechaPuesto;
    }

    public Puesto(Map<String,Object> map) {
        this.nombrePuesto = (String) map.get("nombrePuesto");
        this.nombreTurno = (String) map.get("nombreTurno");
        this.ingresoPuesto = (String) map.get("ingresoPuesto");
        this.egresoPuesto = (String) map.get("egresoPuesto");
        this.horasTurno = (String) map.get("horasTurno");
        this.turnoNoche = (Boolean) map.get("turnoNoche");
        this.fechaPuesto = (String) map.get("fechaPuesto");
    }


    public String getNombrePuesto() {
        return nombrePuesto;
    }

    public void setNombrePuesto(String nombrePuesto) {
        this.nombrePuesto = nombrePuesto;
    }

    public String getNombreTurno() {
        return nombreTurno;
    }

    public void setNombreTurno(String nombreTurno) {
        this.nombreTurno = nombreTurno;
    }

    public String getIngresoPuesto() {
        return ingresoPuesto;
    }

    public void setIngresoPuesto(String ingresoPuesto) {
        this.ingresoPuesto = ingresoPuesto;
    }

    public String getEgresoPuesto() {
        return egresoPuesto;
    }

    public void setEgresoPuesto(String egresoPuesto) {
        this.egresoPuesto = egresoPuesto;
    }

    public String getHorasTurno() {
        return horasTurno;
    }

    public void setHorasTurno(String cantidadHoras) {
        this.horasTurno = cantidadHoras;
    }

    public Boolean getTurnoNoche() {
        return turnoNoche;
    }

    public void setTurnoNoche(Boolean turnoNoche) {
        this.turnoNoche = turnoNoche;
    }

    public String getFechaPuesto() {
        return fechaPuesto;
    }

    public void setFechaPuesto(String fecha) {
        this.fechaPuesto = fecha;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
