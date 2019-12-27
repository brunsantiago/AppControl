package com.example.sata.myapplication2.POJO;

public class Esquema {

    private String fechaDesde;
    private String fechaHasta;
    private boolean vigente;

    public Esquema() {}

    public Esquema(String fechaDesde, String fechaHasta, boolean vigente) {
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.vigente = vigente;
    }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }
}
