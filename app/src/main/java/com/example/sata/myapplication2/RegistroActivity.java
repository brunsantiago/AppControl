package com.example.sata.myapplication2;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.example.sata.myapplication2.AlertDialog.RegisterAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RegistroActivity extends AppCompatActivity {

    //private static final String API_PATH = "http://192.168.1.8:3000/api/";
    //private static final String API_PATH = "http://186.182.25.11:3000/api/";

    private TextView textViewVolverLogin;
    private EditText editTextNroLegajo;
    private EditText editTextDni;
    private EditText editTextFechaNac;
    private EditText editTextClave;
    private EditText editTextReingreseClave;
    private Button btnRegistrar;
    private Calendar calendar;
    private ProgressDialog progressDialog = null;
    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(RegistroActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
        btnRegistrar = findViewById(R.id.btnRegistrar);
        textViewVolverLogin = findViewById(R.id.volverLogin);

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                updateCalendar();
            }

            private void updateCalendar(){
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                editTextFechaNac.setText(sdf.format(calendar.getTime()));
            }
        };

        editTextFechaNac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                new DatePickerDialog(RegistroActivity.this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        editTextDni.addTextChangedListener(new NumberTextWatcherForThousand(editTextDni));

        progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(RegistroActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        clearFormRegister();
        super.onResume();
    }

    private void registrarUsuario(String persCodi, String nroLegajo, String clave) {

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = Configurador.API_PATH + "register";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_codi", persCodi);
            jsonBody.put("user_lega", nroLegajo);
            jsonBody.put("user_perf", "");
            jsonBody.put("user_pass", clave);

            final String requestBody = jsonBody.toString();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getInt("result")==1){
                            showRegisterAlert();
                            progressDialog.dismiss();
                        }else{
                            Toast.makeText(RegistroActivity.this, "Usuario ya registrado", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(RegistroActivity.this, "Usuario ya registrado", Toast.LENGTH_SHORT).show();
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

        return true;
    }

    private boolean validateUserDataRegister(String persDni, String persFnac){

        String formDni = editTextDni.getText().toString().replaceAll("\\p{Punct}|\\p{Space}", "");
        String formFechaNac = editTextFechaNac.getText().toString();

        if (! validateDni(formDni,persDni)){
            Toast.makeText(this, "Dni ingresado incorrecto", Toast.LENGTH_SHORT).show();
            return false;
        }else if (! validateDates(formFechaNac,persFnac)){
            Toast.makeText(this, "Fecha de nacimiento ingresada incorrecta", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }

    private boolean validateDates(String formFechaNac,String persFnac){

        String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateBD = null;
        try {
            dateBD = sdf.parse(persFnac);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int dayBD = dateBD.getDate();
        int monthBD = dateBD.getMonth();
        int yearBD = dateBD.getYear();

        String LOCAL_FORMAT = "dd/MM/yyyy";
        SimpleDateFormat sdfLocal = new SimpleDateFormat(LOCAL_FORMAT);
        Date dateForm = null;
        try {
            dateForm = sdfLocal.parse(formFechaNac);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int dayForm = dateForm.getDate();
        int monthForm = dateForm.getMonth();
        int yearForm = dateForm.getYear();

        return dayForm == dayBD && monthForm == monthBD && yearForm == yearBD;
    }

    private boolean validateDni(String formDni, String persDni){
        return formDni.equals(persDni);
    }

    public void showRegisterAlert(){
        RegisterAlert myAlert = new RegisterAlert();
        myAlert.setTipoRegistro("registro");
        myAlert.show(getSupportFragmentManager(),"Register Alert");
    }

    private void clearFormRegister(){
        editTextNroLegajo.setText("");
        editTextDni.setText("");
        editTextFechaNac.setText("");
        editTextClave.setText("");
        editTextReingreseClave.setText("");
    }

}
