package com.appcontrol.sab5.app.AlertDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.appcontrol.sab5.app.R;

public class SesionVencidaAlert extends DialogFragment {

    private String objetivoFecha="";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View viewAlert = inflater.inflate(R.layout.custom_alert_vencida, null);

        TextView textViewObjetivo = viewAlert.findViewById(R.id.textViewObjetivoFechaPuesto);
        textViewObjetivo.setText(objetivoFecha);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.ThemeDialogCustom);
        builder.setView(viewAlert)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        setCancelable(false);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setObjetivoFecha(String cliente, String objetivo, String fecha){
        String yearString = fecha.substring(0,4);
        String monthString = fecha.substring(5,7);
        String dayString = fecha.substring(8,10);
        this.objetivoFecha = "\n"+cliente+" - "+objetivo+"\n"+dayString+" - "+monthString+" - "+yearString;
    }

}
