package com.example.sata.myapplication2;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class LoginActivity extends AppCompatActivity {

    private static final String NRO_LEGAJO = "nroLegajo";
    private TextView textViewNroLegajo;
    private TextView textViewClave;
    private Button botonIngresar;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        textViewNroLegajo = findViewById(R.id.nrolegajo);
        textViewClave = findViewById(R.id.clave);
        botonIngresar = findViewById(R.id.ingresar);
        TextView botonSinDatos = findViewById(R.id.sinDatos);

        progressDialog= new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);


        botonIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nroLegajo = textViewNroLegajo.getText().toString();
                String clave = textViewClave.getText().toString();

                if((nroLegajo!=null && !nroLegajo.equals(""))&&(clave!=null && !clave.equals(""))){
                    signIn(nroLegajo,clave);
                    botonIngresar.setClickable(false);
                } else {
                    Toast.makeText(LoginActivity.this, "Por favor ingrese un Numero de Legajo y/o Clave valida", Toast.LENGTH_SHORT).show();
                }
            }
        });

        botonSinDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nroLegajo = textViewNroLegajo.getText().toString();
                String clave = textViewClave.getText().toString();
                createAccount(nroLegajo,clave);
            }
        });

        sharedPreferencesClear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        botonIngresar.setClickable(true);
    }

    private void createAccount(final String nrolegajo, String password){
        String email = nrolegajo+"@sab5.com.ar";
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            sharedPreferencesClear();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user,nrolegajo);
                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn(final String nrolegajo, String password){
        //agregas un mensaje en el ProgressDialog
        //progressDialog.setMessage("Iniciado sesión");
        //muestras el ProgressDialog
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog);
        String email = nrolegajo+"@sab5.com.ar";
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            sharedPreferencesClear();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user,nrolegajo);
                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            progressDialog.dismiss();
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            botonIngresar.setClickable(true);
                            progressDialog.dismiss();
                        }

                        // ...
                    }
                });
    }


    private void updateUI(FirebaseUser user, String nroLegajo){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(NRO_LEGAJO,nroLegajo);
        editor.apply();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nroLegajo)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                });
    }

    private void sharedPreferencesClear(){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }


}

