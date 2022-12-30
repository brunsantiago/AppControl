package com.example.sata.myapplication2.POJO;

import java.util.Date;

public class Esquema {

    private Date fechaDesde;
    private Date fechaHasta;
    private String estado;


    public Esquema() {}

    public Esquema(Date fechaDesde,Date fechaHasta, String estado) {
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaHasta() {



        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

}
