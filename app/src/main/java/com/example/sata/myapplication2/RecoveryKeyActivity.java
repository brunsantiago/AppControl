package com.example.sata.myapplication2;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecoveryKeyActivity extends AppCompatActivity {

    private TextView textViewVolverLogin;
    private EditText editTextNroLegajo;
    private EditText editTextDni;
    private EditText editTextFechaNac;
    private EditText editTextClave;
    private EditText editTextReingreseClave;
    private ImageButton btnTakePhoto;
    private Button btnCambiarClave;

    private ProgressDialog progressDialog = null;

    private boolean faceDetection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_key);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(RecoveryKeyActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        editTextNroLegajo = findViewById(R.id.editTextNroLegajo);
        editTextDni = findViewById(R.id.editTextDni);
        editTextFechaNac = findViewById(R.id.editTextFechaNac);
        editTextClave = findViewById(R.id.editTextClave);
        editTextReingreseClave = findViewById(R.id.editTextReingreseClave);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnCambiarClave = findViewById(R.id.btnCambiarClave);
        textViewVolverLogin = findViewById(R.id.volverLogin);

        faceDetection=false;

        progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);

        btnCambiarClave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(formValidateRegistro()){
                    String nroLegajo = editTextNroLegajo.getText().toString();
                    verificarPersonal(nroLegajo);
                }
            }
        });

        textViewVolverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecoveryKeyActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        clearFormRecoveryKey();

    }

    private boolean formValidateRegistro(){

        if(editTextNroLegajo.getText().toString().equals("") || editTextDni.getText().toString().equals("") ||
                editTextFechaNac.getText().toString().equals("") || editTextClave.getText().toString().equals("") ||
                editTextReingreseClave.getText().toString().equals("") ){
            Toast.makeText(this, "Por favor complete todos los campos solicitados", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!editTextClave.getText().toString().equals(editTextReingreseClave.getText().toString())){
            Toast.makeText(this, "Por favor verifique que las claves ingresadas coincidan", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(editTextClave.getText().length()<4){
            Toast.makeText(this, "Por favor verifique que la clave ingresada sea igual o mayor a cuatro caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!faceDetection){
            Toast.makeText(this, "Por favor verifique que la foto este bien tomada", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void verificarPersonal(String nroLegajo){

        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog);

        String clave = editTextClave.getText().toString();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String mJSONURLString = Configurador.API_PATH + "personal/"+nroLegajo;
        // Initialize a new JsonArrayRequest instance
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                mJSONURLString,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length()>0) {
                            JSONObject jsonObject;
                            try {
                                jsonObject = response.getJSONObject(0);
                                String persSector = jsonObject.getString("PERS_SECT");
                                String persEgreso = jsonObject.getString("PERS_FEGR");
                                String persDni = jsonObject.getString("PERS_NDOC");
                                String persFnac = jsonObject.getString("PERS_FNAC");
                                if(isUserEnable(persSector,persEgreso) ){
                                    String persCodi = jsonObject.getString("PERS_CODI");
                                    if(validateUserDataRegister(persDni,persFnac)){
                                        registrarUsuario(persCodi,nroLegajo,clave);
                                    }else{
                                        progressDialog.dismiss();
                                    }
                                }else{
                                    Toast.makeText(RegistroActivity.this, "Usuario no habilitado", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(RegistroActivity.this, "Error de Servidor", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }else{
                            Toast.makeText(RegistroActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(RegistroActivity.this, "Error de Servidor", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
        );
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonArrayRequest);

    }

    private boolean isUserEnable(String persSector, String persEgreso){
        if(persSector.equals("3")){
            return false;
        }else return persEgreso.equals("null");
    }

    private void clearFormRecoveryKey(){
        editTextNroLegajo.setText("");
        editTextDni.setText("");
        editTextFechaNac.setText("");
        editTextClave.setText("");
        editTextReingreseClave.setText("");
    }




}
