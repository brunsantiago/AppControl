package com.appcontrol.sab5.app;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appcontrol.sab5.app.AlertDialog.DeviceAlertError;
import com.appcontrol.sab5.app.AlertDialog.UpdateAlert;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private TextView textViewRecoveryKey;
    private TextView textViewRegistrarse;
    private TextView textViewVersion;
    private ProgressDialog progressDialog = null;

    private static final String NRO_LEGAJO = "nl";

    private static final String NOMBRE_CLIENTE = "nombreCliente";
    private static final String NOMBRE_OBJETIVO = "nombreObjetivo";
    private static final String ID_CLIENTE = "idCliente";
    private static final String ID_OBJETIVO = "idObjetivo";
    private static final String ID_ANDROID = "androidId";

    private FirebaseStorage storage;
    private static final String PROFILE_PHOTO = "ProfilePhotoPath";

    private int versionCodeApp = BuildConfig.VERSION_CODE;
    private String versionNameApp = BuildConfig.VERSION_NAME;
    private int versionCodeServer;
    private String versionNameServer;
    private int versionPriority;
    private String androidId;

    UpdateAlert myAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_login);

        androidId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        textViewNroLegajo = findViewById(R.id.nrolegajo);
        textViewClave = findViewById(R.id.clave);
        botonIngresar = findViewById(R.id.ingresar);
        textViewRecoveryKey = findViewById(R.id.recoverKey);
        textViewRegistrarse = findViewById(R.id.registrarse);
        textViewVersion = findViewById(R.id.version);

        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);

        myAlert = null;

        textViewVersion.setText("Version "+versionNameApp);

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

        textViewRecoveryKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RecoveryKeyActivity.class);
                startActivity(intent);
                finish();
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

    }

    @Override
    protected void onResume() {
        sharedPreferencesClear();
        deleteCache(this);
        checkForUpdates();
        super.onResume();
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
        String mJSONURLString = Configurador.API_PATH + "personal/"+nroLegajo+"/"+Configurador.ID_EMPRESA;
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
        String mJSONURLString = Configurador.API_PATH + "users/"+persCodi+"/"+Configurador.ID_EMPRESA;
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
            String URL = Configurador.API_PATH + "login/"+Configurador.ID_EMPRESA;
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
            //TODO Se optimizo la solicitud de busqueda
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String mJSONURLString = Configurador.API_PATH + "devices/"+idAndroid+"/"+Configurador.ID_EMPRESA;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    mJSONURLString,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (response != null) {
                                String nombreCliente = null;
                                String nombreObjetivo = null;
                                boolean disabledDevice = false;
                                try {
                                    if (response.getString("DEVI_ESTA").equals("ACTIVO")) {
                                        nombreCliente = response.getString("DEVI_NCLI");
                                        nombreObjetivo = response.getString("DEVI_NOBJ");
                                        String idCliente = response.getString("DEVI_CCLI");
                                        String idObjetivo = response.getString("DEVI_COBJ");
                                        int ubicacion = response.getInt("DEVI_UBIC");
                                        String mapCoordenada = response.getString("DEVI_COOR");
                                        int mapRadio = response.getInt("DEVI_RADI");

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
                                } catch (JSONException e) {
                                    loadLoginActivity();
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
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error){
                            loadLoginActivity();
                        }
                    });
            requestQueue.add(jsonObjectRequest);
        }else{
            loadLoginActivity();
        }
    }

    private void loadLoginActivity(){
        downloadProfilePhoto();
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

    private void downloadProfilePhoto(){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String nroLegajo = prefs.getString(NRO_LEGAJO,"");
        String fileName = nroLegajo+"_profile_photo.jpg";

        StorageReference photoRef = storage.getReference()
                //.child("BROUCLEAN") or .child("CONSISA")
                .child("USERS") // SAB-5 esta en el directorio raiz
                .child("PROFILE_PHOTO")
                .child(nroLegajo)
                .child(fileName);
        photoRef.getBytes(600*600)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        saveToInternalStorage(bitmap);
                    }
                });
    }

    private void saveToInternalStorage(Bitmap bitmapImage){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory,"profile.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editor.putString(PROFILE_PHOTO, directory.getAbsolutePath());
        editor.apply();

        loadMainActivity();
    }

    private void loadMainActivity(){
        progressDialog.dismiss();
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void checkForUpdates(){

        updateVersionDevice(); // Actualiza la version del dispositivo en el servidor

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String mJSONURLString = Configurador.API_PATH + "app_version/last_version/"+Configurador.ID_EMPRESA;
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
                                versionCodeServer = jsonObject.getInt("version_code");
                                versionNameServer = jsonObject.getString("version_name");
                                versionPriority = jsonObject.getInt("version_priority");
                                if(versionCodeApp<versionCodeServer){
                                    if(versionPriority==0){ // Alta Prioridad
                                        Intent intent = new Intent(LoginActivity.this, UpdateActivity.class);
                                        intent.putExtra("versionNameServer", versionNameServer);
                                        startActivity(intent);
                                    }else{ // Baja Prioridad
                                        showUpdateAlert(versionNameServer);
                                    }
                                }
                            } catch (JSONException e) {
                                Toast.makeText(LoginActivity.this, "Error en Actualizacion", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            //Toast.makeText(LoginActivity.this, "No hay actualizaciones pendientes", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(LoginActivity.this, "Error de Servidor", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);

    }

    public void showUpdateAlert(String versionNameServer){
        myAlert = new UpdateAlert();
        myAlert.versionNameServer(versionNameServer);
        myAlert.show(getSupportFragmentManager(),"Update Alert");
    }

    private void updateVersionDevice(){

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = Configurador.API_PATH + "devices/"+androidId+"/"+Configurador.ID_EMPRESA;
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("appVersion", versionNameApp);
            final String requestBody = jsonBody.toString();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getInt("result")==1){
                            // Si sale OK !
                            Toast.makeText(LoginActivity.this, "versionNameServer: "+versionNameServer+"Android ID: "+androidId, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "No se pudo cargar la version en el dispositivo", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(LoginActivity.this, "No se pudo cargar la version en el dispositivo", Toast.LENGTH_SHORT).show();
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


    @Override
    protected void onDestroy() {
        deleteCache(this);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if(myAlert!=null){
            myAlert.dismiss();
        }
        super.onStop();
    }
}



