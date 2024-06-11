package com.appcontrol.sab5.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class UpdateActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private String versionNameServer;
    private ProgressBar progressBar;
    private TextView progressBarNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        storage = FirebaseStorage.getInstance();
        versionNameServer = getIntent().getExtras().getString("versionNameServer");
        progressBar = findViewById(R.id.progressBar);
        progressBarNumber = findViewById(R.id.progressBarNumber);

        downloadUpdateApp();

    }


    private void downloadUpdateApp(){

        String fileName = "app-control.apk";

        StorageReference updateRef = storage.getReference()
                .child("RELEASE")
                .child(versionNameServer)
                .child(fileName);

        File localFile = null;
        try {
            localFile = File.createTempFile("app-control", "apk");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File finalLocalFile = localFile;
        updateRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                if(finalLocalFile.exists()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uriFromFile(getApplicationContext(), finalLocalFile), "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        getApplicationContext().startActivity(intent);
                        finish();
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Log.e("TAG", "Error in opening the file!");
                        finish();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"No se encuentra el archivo de Instalacion",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateActivity.this, "Error al intentar actualizar ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressBar.setProgress((int) progress);
                progressBarNumber.setText((int) progress+" %");
            }
        });

    }

    private Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }
}