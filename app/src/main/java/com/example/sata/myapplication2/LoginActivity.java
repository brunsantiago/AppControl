package com.example.sata.myapplication2;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sata.myapplication2.AlertDialog.DeviceAlertError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class LoginActivity extends AppCompatActivity {

    private static final String NOMBRE_PERSONAL = "an";
    private static final String PERS_CODI = "pers_codi";
    private static final String USER_PROFILE = "user_profile";
    private static final String MAP_COOR = "map_coor";
    private static final String MAP_RADIO = "map_radio";
    private static final String DEVI_UBIC = "devi_ubic";

    private TextView textViewNroLegajo;
    private TextView textViewClave;
    private Button botonIngresar;
    private TextView textViewRegistrarse;
    private ProgressDialog progressDialog = null;

    private static final String NRO_LEGAJO = "nl";

    private static final String NOMBRE_CLIENTE = "nombreCliente";
    private static final String NOMBRE_OBJETIVO = "nombreObjetivo";
    private static final String ID_CLIENTE = "idCliente";
    private static final String ID_OBJETIVO = "idObjetivo";
    private static final String ID_ANDROID = "androidId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_login);

        textViewNroLegajo = findViewById(R.id.nrolegajo);
        textViewClave = findViewById(R.id.clave);
        botonIngresar = findViewById(R.id.ingresar);
        textViewRegistrarse = findViewById(R.id.registrarse);

        progressDialog = new ProgressDialog(LoginActivity.this);
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

        textViewRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegistroActivity.class);
                startActivity(intent);
                finish();
            }
        });
        sharedPreferencesClear();
    }

    private void sharedPreferencesClear(){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    private void initUserData(String nroLegajo){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String mJSONURLString = Configurador.API_PATH + "personal/"+nroLegajo;
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
                                if(isUserEnable(persSector,persEgreso)){
                                    String nombre = jsonObject.getString("PERS_NOMB");
                                    String persCodi = jsonObject.getString("PERS_CODI");
                                    setUserProfile(Integer.parseInt(persCodi));
                                    editor.putString(NRO_LEGAJO,nroLegajo);
                                    editor.putString(NOMBRE_PERSONAL, nombre);
                                    editor.putString(PERS_CODI, persCodi);
                                    editor.apply();
                                    //progressDialog.dismiss();
                                    loadDevice();
                                }else{
                                    Toast.makeText(LoginActivity.this, "Usuario inhabilitado", Toast.LENGTH_SHORT).show();
                                    botonIngresar.setClickable(true);
                                    progressDialog.dismiss();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(LoginActivity.this, "Error de Servidor", Toast.LENGTH_SHORT).show();
                                botonIngresar.setClickable(true);
                                progressDialog.dismiss();
                            }
                        }else{
                            Toast.makeText(LoginActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                            botonIngresar.setClickable(true);
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(LoginActivity.this, "Error de Servidor", Toast.LENGTH_SHORT).show();
                        botonIngresar.setClickable(true);
                        progressDialog.dismiss();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);

    }

    public void setUserProfile(int persCodi){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String mJSONURLString = Configurador.API_PATH + "users/"+persCodi;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                mJSONURLString,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String userProfile = "";
                        try {
                            userProfile = response.getString("result");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        editor.putString(USER_PROFILE, userProfile);
                        editor.apply();
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        editor.putString(USER_PROFILE, "");
                        editor.apply();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    private void signIn(String nroLegajo, String clave) {

        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog);

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = Configurador.API_PATH + "login";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_lega", nroLegajo);
            jsonBody.put("user_pass", clave);

            final String requestBody = jsonBody.toString();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getString("result").equals("CORRECT_LOGIN")){
                            sharedPreferencesClear();
                            initUserData(nroLegajo);
                        }else if(response.getString("result").equals("INCORRECT_LOGIN")){
                            Toast.makeText(LoginActivity.this, "Numero de legajo y/o clave incorrectas", Toast.LENGTH_SHORT).show();
                            botonIngresar.setClickable(true);
                            progressDialog.dismiss();
                        }else if(response.getString("result").equals("NOT_FOUND")){
                            Toast.makeText(LoginActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                            botonIngresar.setClickable(true);
                            progressDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        botonIngresar.setClickable(true);
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(LoginActivity.this, "Error al iniciar sesion", Toast.LENGTH_SHORT).show();
                    botonIngresar.setClickable(true);
                    progressDialog.dismiss();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }
            };
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserEnable(String persSector, String persEgreso){
        if(persSector.equals("3")){
            return false;
        }else return persEgreso.equals("null");
    }

    private String getAndroidID(){
        @SuppressLint("HardwareIds")
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    private void loadDevice(){

        String idAndroid = getAndroidID();

        if(idAndroid!=null){
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String url = Configurador.API_PATH + "devices";
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if (response != null) {
                        JSONArray devicesJSONArray = response;
                        String nombreCliente = null;
                        String nombreObjetivo = null;
                        boolean disabledDevice = false;
                        for (int i = 0; i < devicesJSONArray.length(); i++) {
                            try {
                                JSONObject jsonObject = devicesJSONArray.getJSONObject(i);
                                if (jsonObject.getString("DEVI_ANID").equals(idAndroid)) {

                                    if (jsonObject.getString("DEVI_ESTA").equals("ACTIVO")) {
                                        nombreCliente = jsonObject.getString("DEVI_NCLI");
                                        nombreObjetivo = jsonObject.getString("DEVI_NOBJ");
                                        String idCliente = jsonObject.getString("DEVI_CCLI");
                                        String idObjetivo = jsonObject.getString("DEVI_COBJ");
                                        int ubicacion = jsonObject.getInt("DEVI_UBIC");
                                        String mapCoordenada = jsonObject.getString("DEVI_COOR");
                                        int mapRadio = jsonObject.getInt("DEVI_RADI");

                                        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);

                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString(NOMBRE_CLIENTE, nombreCliente);
                                        editor.putString(NOMBRE_OBJETIVO, nombreObjetivo);
                                        editor.putString(ID_CLIENTE, idCliente);
                                        editor.putString(ID_OBJETIVO, idObjetivo);
                                        editor.putInt(DEVI_UBIC, ubicacion);
                                        editor.putString(MAP_COOR, mapCoordenada);
                                        editor.putInt(MAP_RADIO, mapRadio);
                                        editor.putString(ID_ANDROID, idAndroid);
                                        editor.apply();
                                        loadLoginActivity();
                                    }else{
                                        disabledDevice = true;
                                    }
                                }
                            } catch (JSONException e) {
                                loadLoginActivity();
                            }
                        }
                        if(nombreCliente==null || nombreObjetivo==null){
                            if(!disabledDevice){
                                loadLoginActivity();
                            }else{
                                resetFormLogin();
                                progressDialog.dismiss();
                                showDeviceErrorAlert();
                            }
                        }
                    }else{
                        loadLoginActivity();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadLoginActivity();
                }
            });
            requestQueue.add(jsonArrayRequest);
        }else{
            loadLoginActivity();
        }
    }

    private void loadLoginActivity(){
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDeviceErrorAlert(){
        DeviceAlertError myAlert = new DeviceAlertError();
        myAlert.show(getSupportFragmentManager(),"Device Error");
    }

    private void resetFormLogin(){
        botonIngresar.setClickable(true);
        textViewNroLegajo.setText("");
        textViewClave.setText("");
    }

}

