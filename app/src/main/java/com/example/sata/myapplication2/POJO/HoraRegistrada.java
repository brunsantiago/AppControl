package com.example.sata.myapplication2.POJO;

import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HoraRegistrada {

    private String horaIngreso;
    private String horaEgreso;
    private String ingresoPuesto;
    private String egresoPuesto;

    public HoraRegistrada() {
    }

    public HoraRegistrada(String horaIngreso, String horaEgreso, String ingresoPuesto, String egresoPuesto) {
        this.horaIngreso = horaIngreso;
        this.horaEgreso = horaEgreso;
        this.ingresoPuesto = ingresoPuesto;
        this.egresoPuesto = egresoPuesto;
    }

    public static String ingresoParametrizado(String ingresoPuestoStr, String fechaPuestoStr, String horaIngresoStr, String fechaIngresoStr){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Date ingresoParam = null;
        Date horaIngreso = null;
        Date ingresoPuesto = null;

        try {
            horaIngreso = dateFormat.parse(fechaIngresoStr+" "+horaIngresoStr);
            ingresoPuesto = dateFormat.parse(fechaPuestoStr+" "+ingresoPuestoStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(horaIngreso.getTime() == ingresoPuesto.getTime()){
            ingresoParam = ingresoPuesto;
        }else if(horaIngreso.getTime() < ingresoPuesto.getTime()){
            ingresoParam = ingresoPuesto;
        }else if(horaIngreso.getTime() > ingresoPuesto.getTime()){
            return cuartoPosterior(horaIngreso);
        }
        return hourFormat.format(ingresoParam);
    }

    public static String egresoParametrizado(String egresoPuestoStr, String fechaPuestoStr, String horaEgresoStr, String fechaEgresoStr, boolean turnoNoche){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Date egresoParam = null;
        Date horaEgreso = null;
        Date egresoPuesto = null;

        try {
            horaEgreso = dateFormat.parse(fechaEgresoStr+" "+horaEgresoStr);
            egresoPuesto = dateFormat.parse(fechaPuestoStr+" "+egresoPuestoStr);
            if(turnoNoche){
                egresoPuesto = new Date(egresoPuesto.getTime()+86400000);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (horaEgreso != null && egresoPuesto != null) {
            if(horaEgreso.getTime() == egresoPuesto.getTime()){
                egresoParam = egresoPuesto;
            }else if(horaEgreso.getTime() > egresoPuesto.getTime()){
                egresoParam = egresoPuesto;
            }else if(horaEgreso.getTime() < egresoPuesto.getTime()){
                return cuartoPosterior(horaEgreso);
            }
            return hourFormat.format(egresoParam);
        }

        return "--:--";

    }

    private static String cuartoPosterior(Date ingresoParam){
        SimpleDateFormat minutesFormat = new SimpleDateFormat("mm", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH", Locale.getDefault());

        int minutos =  Integer.parseInt(minutesFormat.format(ingresoParam));
        int hora =  Integer.parseInt(hourFormat.format(ingresoParam));

        String minutosParametrizados="";
        String horaParametrizada="";

        if (minutos==0){
            minutosParametrizados = "00";
        } else if (minutos>0 && minutos<=15){
            minutosParametrizados = "15";
        } else if (minutos>15 && minutos<=30){
            minutosParametrizados = "30";
        } else if (minutos>30 && minutos<=45){
            minutosParametrizados = "45";
        } else if (minutos>45 && minutos<60){
            minutosParametrizados = "00";
            hora++;
        }

        if(hora==24){
            horaParametrizada = "00";
        } else {
            horaParametrizada = Integer.toString(hora);
            if(horaParametrizada.length()==1){
                horaParametrizada = "0"+horaParametrizada;
            }
        }

        return horaParametrizada + ":" + minutosParametrizados;
    }

}
