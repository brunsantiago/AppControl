package com.appcontrol.sab5.app.AlertDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.appcontrol.sab5.app.R;


public class RegisterAlertError extends DialogFragment {

//    private String tipoRegistro;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.ThemeDialogCustom);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_alert_registro_error, null))
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        setCancelable(false);
        // Create the AlertDialog object and return it
        return builder.create();
    }

//    public void setTipoRegistro(String tipoRegistro){
//        this.tipoRegistro = tipoRegistro;
//    }

}
