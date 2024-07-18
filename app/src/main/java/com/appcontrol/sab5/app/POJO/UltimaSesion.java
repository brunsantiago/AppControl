package com.appcontrol.sab5.app.POJO;

import java.util.Map;

public class UltimaSesion {

    private String nc;
    private String no;
    private String fp;
    private String si;
    private String pt;


    public UltimaSesion(Map<String, Object> map) {
        this.nc = (String) map.get("nc");
        this.fp = (String) map.get("fp");
        this.no = (String) map.get("no");
        this.si = (String) map.get("si");
        this.pt = (String) map.get("pt");

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

    public String getPathTurno() {
        return pt;
    }

    public void setPathTurno(String pathTurno) {
        this.pt = pathTurno;
    }

}
