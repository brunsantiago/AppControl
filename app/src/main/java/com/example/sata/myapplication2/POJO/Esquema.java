package com.example.sata.myapplication2.POJO;

import java.util.Date;

public class Esquema {

    private Date fechaDesde;
    private Date fechaHasta;
    private boolean vigente;


    public Esquema() {}

    public Esquema(boolean vigente,Date fechaDesde,Date fechaHasta) {
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.vigente = vigente;
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

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }
}
