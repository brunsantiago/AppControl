package com.example.sata.myapplication2.AlertDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.example.sata.myapplication2.POJO.UltimaSesion;
import com.example.sata.myapplication2.R;

public class SesionActivaAlert extends DialogFragment {

    private UltimaSesion ultimaSesion;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String mensaje = "Se informa que Ud. no registro la salida del siguiente objetivo: ";
        String servicio = "Servicio: "+ultimaSesion.getCliente()+" - "+ultimaSesion.getObjetivo();
        String fecha = "Fecha: "+ultimaSesion.getFechaPuesto();
        String puesto = "Puesto: "+ultimaSesion.getNombrePuesto();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Atencion")
                .setIcon(R.drawable.danger)
                .setMessage(mensaje +"\n"+ servicio +"\n"+ fecha+"\n"+ puesto +"\n")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        setCancelable(false);
        // Create the AlertDialog object and return it
        return builder.create();
    }


    public void setUltimaSesion(UltimaSesion ultimaSesion){
        this.ultimaSesion = ultimaSesion;
    }
}
