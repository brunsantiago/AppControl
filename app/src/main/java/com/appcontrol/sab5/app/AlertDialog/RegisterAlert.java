package com.appcontrol.sab5.app.AlertDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.appcontrol.sab5.app.LoginActivity;
import com.appcontrol.sab5.app.R;


public class RegisterAlert extends DialogFragment {

    private String tipoRegistro;
    private String mensaje;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View viewAlert = inflater.inflate(R.layout.custom_alert_registro, null);

        //TextView textViewMensaje = viewAlert.findViewById(R.id.textViewMensaje);
        //textViewMensaje.setText(mensaje);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.ThemeDialogCustom);
        builder.setView(viewAlert)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(tipoRegistro.equals("registro")){
                            Intent intent = new Intent(getContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }
                });
        setCancelable(false);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setTipoRegistro(String tipoRegistro){
        this.tipoRegistro = tipoRegistro;
    }

    public void setMensaje(String mensaje){
        this.mensaje = mensaje;
    }


}
