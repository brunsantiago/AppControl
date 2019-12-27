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

public class PuestoAdapter extends ArrayAdapter {

    public PuestoAdapter(Context context, ArrayList<Puesto> listaPuestos){
        super(context,0,listaPuestos);
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
                    R.layout.spinner_item_puestos, parent, false);
        }

        TextView textViewNombrePuesto = convertView.findViewById(R.id.textViewNombrePuesto);
        TextView textViewHorario = convertView.findViewById(R.id.textViewHorario);

        Puesto puesto = (Puesto) getItem(position);

        if(puesto != null){
            if(puesto.getIngresoPuesto() != null || puesto.getEgresoPuesto() != null){
                String nombrePuesto = puesto.getNombrePuesto();
                textViewNombrePuesto.setText(nombrePuesto);
                String horario = puesto.getNombreTurno() + " - " + puesto.getIngresoPuesto()+" a "+puesto.getEgresoPuesto();
                textViewHorario.setText(horario);
                textViewHorario.setVisibility(View.VISIBLE);
            } else {
                textViewNombrePuesto.setText(puesto.getNombrePuesto());
                textViewHorario.setVisibility(View.GONE);
            }

        }

        return convertView;
    }
}
