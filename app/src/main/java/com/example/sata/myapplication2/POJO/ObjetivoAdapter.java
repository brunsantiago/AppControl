package com.example.sata.myapplication2.POJO;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sata.myapplication2.R;

import java.util.ArrayList;

public class ObjetivoAdapter extends ArrayAdapter {

    public ObjetivoAdapter(Context context, ArrayList<Objetivo> listaObjetivos){
        super(context,0,listaObjetivos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.spinner_item_objetivos, parent, false);
        }

        TextView textViewNombreObjetivo = convertView.findViewById(R.id.textViewNombreObjetivo);

        Objetivo objetivo = (Objetivo) getItem(position);

//        if(cliente != null){
//            if(cliente.getIngresoPuesto() != null || cliente.getEgresoPuesto() != null){
//                String nombrePuesto = puesto.getNombrePuesto();
//                textViewNombrePuesto.setText(nombrePuesto);
//                String horario = puesto.getNombreTurno() + " - " + puesto.getIngresoPuesto()+" a "+puesto.getEgresoPuesto();
//                textViewHorario.setText(horario);
//                textViewHorario.setVisibility(View.VISIBLE);
//            } else {
//                textViewNombrePuesto.setText(puesto.getNombrePuesto());
//                textViewHorario.setVisibility(View.GONE);
//            }
//
//        }

        if(objetivo != null){
            String nombreObjetivo = objetivo.getNombreObjetivo();
            textViewNombreObjetivo.setText(nombreObjetivo);
        }

        return convertView;
    }




}
