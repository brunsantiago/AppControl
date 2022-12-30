package com.example.sata.myapplication2.POJO;

import com.google.firebase.Timestamp;


import java.sql.Date;
import java.util.Map;

public class SolicitudDispositivo {

    private String nombreCliente;
    private String nombreObjetivo;
    private int idCliente;
    private String idObjetivo;
    private String marca;
    private String modelo;
    private String androidId;
    private String nombre;
    private String nroLegajo;
    private Date date;
    private String estado;
    private String nroLinea;

    public SolicitudDispositivo(){

    }

    public SolicitudDispositivo(Map<String, Object> map) {
        this.nombreCliente = (String) map.get("nombreCliente");
        this.nombreObjetivo = (String) map.get("nombreObjetivo");
        this.idCliente = (int) map.get("idCliente");
        this.idObjetivo = (String) map.get("idObjetivo");
        this.marca = (String) map.get("marca");
        this.modelo = (String) map.get("modelo");
        this.androidId = (String) map.get("androidId");
        this.nombre = (String) map.get("nombre");
        this.nroLegajo = (String) map.get("nroLegajo");
        this.date = (Date) map.get("date");
        this.estado = (String) map.get("estado");
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

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdObjetivo() {
        return idObjetivo;
    }

    public void setIdObjetivo(String idObjetivo) {
        this.idObjetivo = idObjetivo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNroLegajo() {
        return nroLegajo;
    }

    public void setNroLegajo(String nroLegajo) {
        this.nroLegajo = nroLegajo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNroLinea() {
        return nroLinea;
    }

    public void setNroLinea(String nroLinea) {
        this.nroLinea = nroLinea;
    }
}
