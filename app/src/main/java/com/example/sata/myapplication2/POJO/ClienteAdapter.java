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

public class ClienteAdapter extends ArrayAdapter {

    public ClienteAdapter(Context context, ArrayList<Cliente> listaClientes){
        super(context,0,listaClientes);
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
                    R.layout.spinner_item_clientes, parent, false);
        }

        TextView textViewNombreCliente = convertView.findViewById(R.id.textViewNombreCliente);

        Cliente cliente = (Cliente) getItem(position);

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

        if(cliente != null){
            String nombreCliente = cliente.getNombreCliente();
            textViewNombreCliente.setText(nombreCliente);
        }

        return convertView;
    }




}
