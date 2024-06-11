package com.appcontrol.sab5.app.AlertDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.appcontrol.sab5.app.R;
import com.appcontrol.sab5.app.UpdateActivity;


public class UpdateAlert extends DialogFragment {

    private String versionNameServer;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.ThemeDialogCustom);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_alert_update, null))
                .setPositiveButton("Instalar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        confirmar();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        setCancelable(false);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void confirmar(){
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra("versionNameServer", this.versionNameServer);
        startActivity(intent);
        //getActivity().finish();
    }

    public void versionNameServer(String versionNameServer){
        this.versionNameServer = versionNameServer;
    }

}
