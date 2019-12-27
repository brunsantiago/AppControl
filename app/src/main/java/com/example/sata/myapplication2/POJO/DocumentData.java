package com.example.sata.myapplication2.POJO;

import java.util.Map;

public class DocumentData {

    private Long numeroDia;
    private String totalHoras;
    private Boolean turnoNoche;

    public DocumentData(){

    }

    public DocumentData(Long numeroDia, String totalHoras, Boolean turnoNoche) {
        this.numeroDia = numeroDia;
        this.totalHoras = totalHoras;
        this.turnoNoche = turnoNoche;
    }

    public DocumentData(Map<String, Object> map) {
        this.numeroDia = (Long) map.get("numeroDia");
        this.totalHoras = (String) map.get("totalHoras");
        this.turnoNoche = (Boolean) map.get("turnoNoche");
    }

    public Long getNumeroDia() {
        return numeroDia;
    }

    public void setNumeroDia(Long numeroDia) {
        this.numeroDia = numeroDia;
    }

    public String getTotalHoras() {
        return totalHoras;
    }

    public void setTotalHoras(String totalHoras) {
        this.totalHoras = totalHoras;
    }

    public Boolean getTurnoNoche() {
        return turnoNoche;
    }

    public void setTurnoNoche(Boolean turnoNoche) {
        this.turnoNoche = turnoNoche;
    }
}
