package com.example.sata.myapplication2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sata.myapplication2.AlertDialog.ExitAlert;
import com.example.sata.myapplication2.AlertDialog.LogOutAlert;
import com.example.sata.myapplication2.AlertDialog.SesionActivaAlert;
import com.example.sata.myapplication2.AlertDialog.SesionVencidaAlert;
import com.example.sata.myapplication2.AlertDialog.SesionVigenteAlert;
import com.example.sata.myapplication2.POJO.Cliente;
import com.example.sata.myapplication2.POJO.UltimaSesion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.instacart.library.truetime.TrueTime;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements ResultListener<Date> {

    private static final String CLIENTE = "cliente";
    private static final String OBJETIVO = "objetivo";
    private static final String IMEI = "imei";
    private static final String NOMBRE = "nombre";
    private static final String ESTADO_SESION = "estadoSesion";
    private static final String ULTIMA_SESION = "ultimaSesion";
    private static final String SESION_ID = "sesionID";
    private static final String FECHA_PUESTO = "fechaPuesto";
    private static final String FECHA_INGRESO_LOGIN = "fechaIngresoLogin";
    private static final String HORA_INGRESO_LOGIN = "horaIngresoLogin";
    private static final String NOMBRE_PUESTO = "nombrePuesto";
    private static final String NRO_LEGAJO = "nroLegajo";
    private static final String TAG = "MainActivity_TAG";
    private static final String INGRESO_PUESTO = "ingresoPuesto";
    private static final String HORA_INGRESO = "horaIngreso";
    private static final String HORA_EGRESO = "horaEgreso";
    private static final String EGRESO_PUESTO = "egresoPuesto";
    private static final String FECHA_INGRESO = "fechaIngreso";
    private static final String TURNO_NOCHE = "turnoNoche";

    private FirebaseFirestore database;
    private FirebaseUser userAuth;

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    private TelephonyManager telephonyManager;
    private String imei;
    private TextView textViewStatus;
    private Button backgroundCounter;
    private ProgressBar mProgressBar;
    private CountDownTimer countDownTimer;
    private ProgressDialog progressDialog=null;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        progressDialog= new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog);

        database = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance().getCurrentUser();

        synchronizeClock();

        getIMEI(this);

        Button btnPanico = findViewById(R.id.buttonPanic);

        CardView btnIngreso = findViewById(R.id.cardViewIngresar);
        CardView btnEgreso = findViewById(R.id.cardViewEgresar);
        CardView btnNovedad = findViewById(R.id.cardViewNovedad);
        CardView btnLlamar = findViewById(R.id.cardViewCall);

        backgroundCounter = findViewById(R.id.backgroundCounter);
        textViewStatus = findViewById(R.id.textViewStatus);
        mProgressBar = findViewById(R.id.progress_circular);


        btnIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, IngresoActivity.class);
                startActivity(intent);
            }
        });

        btnEgreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EgresoActivity.class);
                startActivity(intent);
            }
        });

        btnNovedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NovedadActivity.class);
                startActivity(intent);
            }
        });

        btnPanico.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        backgroundCounter.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        countDown();
                        break;
                    case MotionEvent.ACTION_UP:
                        backgroundCounter.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        countDownTimer.cancel();
                }
                return true;
            }

        });

    }

    @SuppressLint("HardwareIds")
    public void getIMEI(Context context) {

        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

        } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = telephonyManager.getImei();
            } else {
                imei = telephonyManager.getDeviceId();
            }
            cargarSharedPreferences(imei);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        imei = telephonyManager.getImei();
                    } else {
                        imei = telephonyManager.getDeviceId();
                    }
                    cargarSharedPreferences(imei);
                }
            }

        }
    }

    //Carga en SharedPreferences el CLIENTE - OBJETIVO - IMEI
    private void cargarSharedPreferences(final String imei) {

        DocumentReference reference = database.collection("devices")
                .document(imei);

        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    String cliente = (String) doc.get(CLIENTE);
                    String objetivo = (String) doc.get(OBJETIVO);

                    SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(CLIENTE, cliente);
                    editor.putString(OBJETIVO, objetivo);
                    editor.putString(IMEI, imei);
                    editor.apply();

                    cargarDatosPantallaPrincipal(cliente,objetivo);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    /**
     * init the TrueTime using a AsyncTask.
     */
    //Carga la fecha y hora desde Internet
    public void initTrueTime() {
        if (isNetworkConnected()) {
            if (!TrueTime.isInitialized()) {
                TrueTimeAsyncTask trueTime = new TrueTimeAsyncTask(this, this);
                trueTime.execute();
            } else {
                Date date = TrueTime.now();
                setFechaLoginSharedPreferences(date);
            }
        } else{
            Toast.makeText(this, "No esta conectado", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }


    private void setFechaLoginSharedPreferences(Date fecha){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String fechaIngreso = dateFormat.format(fecha);
        String horaIngreso = hourFormat.format(fecha);

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FECHA_INGRESO_LOGIN,fechaIngreso);
        editor.putString(HORA_INGRESO_LOGIN,horaIngreso);
        editor.apply();

        chequearEstadoSesion();
    }

    private void cargarDatosPantallaPrincipal(String cliente, String objetivo){

        TextView datosObjetivo = findViewById(R.id.textViewObjetive);
        TextView nombrePersonal = findViewById(R.id.textViewName);

        datosObjetivo.setText((cliente+" - "+objetivo).toUpperCase());
        textViewStatus.setText("");

        String nroLegajo = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        Query referenceUser = database.collection("users").whereEqualTo("idPersonal", nroLegajo);

        referenceUser
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String nombre = (String) doc.get(NOMBRE);
                        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(NOMBRE, nombre);
                        editor.apply();
                        nombrePersonal.setText(nombre.toUpperCase());
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No se pudo obtener el numero de legajo", Toast.LENGTH_SHORT).show();
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

        initTrueTime();
    }

    @Override
    public void finish(Date resultado) {
        setFechaLoginSharedPreferences(resultado);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        boolean estadoSesion = prefs.getBoolean(ESTADO_SESION,false);

        if(estadoSesion){
            showLogOutAlert();
        } else {
            showExitAlert();
        }
    }

    public void showLogOutAlert(){
        LogOutAlert myAlert = new LogOutAlert();
        myAlert.show(getSupportFragmentManager(),"Log Out Alert");
    }
    public void showExitAlert(){
        ExitAlert myAlert = new ExitAlert();
        myAlert.show(getSupportFragmentManager(),"Exit Alert");
    }

    private void chequearEstadoSesion() {

        Query reference = database.collection("users").whereEqualTo("idPersonal", userAuth.getDisplayName());

        reference
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Boolean estadoSesion = false;
                        if(document.get(ESTADO_SESION)!=null){
                            estadoSesion = (Boolean) document.get(ESTADO_SESION);
                        }
                        if (estadoSesion) {
                            UltimaSesion ultimaSesion = new UltimaSesion((Map<String, Object>) document.get(ULTIMA_SESION));
                            if(!cargaUltimaSesion(ultimaSesion)){
                                database.collection("users").document(document.getId()).update(ESTADO_SESION, false);
                                textViewStatus.setText("No registrado en el Objetivo");
                                textViewStatus.setTextColor(Color.RED);
                            }
                        } else {
                            SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(ESTADO_SESION,false);
                            editor.apply();
                            textViewStatus.setText("No registrado en el Objetivo");
                            textViewStatus.setTextColor(Color.RED);
                        }
                        progressDialog.dismiss();
                    }

                } else {
                    Log.d("TAG", "No such document");
                }
            }
        });
    }

    private Boolean cargaUltimaSesion(UltimaSesion ultimaSesion){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String clienteDispositivo = prefs.getString(CLIENTE,"").toUpperCase();
        String objetivoDispositivo = prefs.getString(OBJETIVO,"").toUpperCase();

        if (clienteDispositivo.equals(ultimaSesion.getCliente()) && objetivoDispositivo.equals(ultimaSesion.getObjetivo())){

            Date fechaLogin = null;
            Date fechaVence = null;

            fechaLogin = armarDate(prefs.getString(FECHA_INGRESO_LOGIN,""),prefs.getString(HORA_INGRESO_LOGIN,""));

            if(ultimaSesion.getTurnoNoche()!=null && ultimaSesion.getTurnoNoche()){
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date hoy = null;
                try {
                    hoy = dateFormat.parse(ultimaSesion.getFechaPuesto());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date diaPosterior = new Date(hoy.getTime()+86400000);
                fechaVence = armarDate(dateFormat.format(diaPosterior),ultimaSesion.getEgresoPuesto());
            }else if (ultimaSesion.getTurnoNoche()!=null && !ultimaSesion.getTurnoNoche()){
                fechaVence = armarDate(ultimaSesion.getFechaPuesto(),ultimaSesion.getEgresoPuesto());
            } else {
                Toast.makeText(this, "Falta cargar si es Turno Noche en sesion del servidor", Toast.LENGTH_SHORT).show();
            }

            Configurador miConf = Configurador.getInstance();
            miConf.setFinSesion(fechaVence);

            if (comparaFechas(fechaLogin,fechaVence)==2){
                showSesionVigenteAlert();
                editor.putBoolean(ESTADO_SESION,true);
                editor.putString(FECHA_PUESTO,ultimaSesion.getFechaPuesto());
                editor.putString(INGRESO_PUESTO,ultimaSesion.getIngresoPuesto());
                editor.putString(HORA_INGRESO,ultimaSesion.getHoraIngreso());
                editor.putString(FECHA_INGRESO,ultimaSesion.getFechaIngreso());
                editor.putString(HORA_EGRESO,ultimaSesion.getHoraEgreso());
                editor.putString(EGRESO_PUESTO,ultimaSesion.getEgresoPuesto());
                editor.putBoolean(TURNO_NOCHE,ultimaSesion.getTurnoNoche());
                editor.putString(SESION_ID,ultimaSesion.getSesionID());
                editor.putString(NOMBRE_PUESTO,ultimaSesion.getNombrePuesto());
                editor.apply();
                textViewStatus.setText("Registrado en el Objetivo");
                textViewStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.holo_green_light));
                return true;
            } else {
                //Sesion Vencida
                showSesionVencidaAlert();
                editor.putBoolean(ESTADO_SESION,false);
                editor.apply();
                textViewStatus.setText("No registrado en el Objetivo");
                textViewStatus.setTextColor(Color.RED);
                return false;
            }
        } else {
            //showSesionActivaAlert(ultimaSesion);
            showObjetivoVencidoAlert(ultimaSesion.getCliente(),ultimaSesion.getObjetivo(),ultimaSesion.getFechaPuesto());
            editor.putBoolean(ESTADO_SESION,false);
            editor.apply();
            textViewStatus.setText("No registrado en el Objetivo");
            textViewStatus.setTextColor(Color.RED);
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteCache(this);
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

//    public void showSesionActivaAlert(UltimaSesion ultimaSesion){
//        SesionActivaAlert myAlert = new SesionActivaAlert();
//        myAlert.setUltimaSesion(ultimaSesion);
//        myAlert.show(getSupportFragmentManager(),"Sesion Activa Alert");
//    }

    public void showSesionVigenteAlert(){
        SesionVigenteAlert myAlert = new SesionVigenteAlert();
        myAlert.show(getSupportFragmentManager(),"Sesion Vigente Alert");
    }

    public void showSesionVencidaAlert(){
        SesionVencidaAlert myAlert = new SesionVencidaAlert();
        myAlert.show(getSupportFragmentManager(),"Sesion Vencida Alert");
    }

    public void showObjetivoVencidoAlert(String cliente, String objetivo, String fecha){
        SesionVencidaAlert myAlert = new SesionVencidaAlert();
        myAlert.setObjetivoFecha(cliente,objetivo,fecha);
        myAlert.show(getSupportFragmentManager(),"Sesion Vencida Alert");
    }

    public int comparaFechas(Date date1, Date date2){
        if(date1.getTime()>date2.getTime()+60*60*1000){
            return 1; // Parametro 1 mas grande que parametro 2
        }else if(date1.getTime()<date2.getTime()+60*60*1000){
            return 2; // Parametro 2 mas grande que parametro 1
        } else {
            return 0; // Iguales
        }
    }

    public Date armarDate(String fecha, String hora){
        Date date = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            date = dateFormat.parse(fecha+" "+hora);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public void statusCheck(){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        if(prefs.contains(ESTADO_SESION)){
            boolean estadoSesion = prefs.getBoolean(ESTADO_SESION,false);
            if(estadoSesion){
                statusOn();
            } else{
                statusOff();
            }
        } else {
            statusEmpty();
        }
    }

    private void synchronizeClock(){
        TextView thour = findViewById(R.id.textViewClock);
        TextView tday = findViewById(R.id.textViewDay);
        TextView tdate = findViewById(R.id.textViewDate);

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE,");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                long date = System.currentTimeMillis();
                                String hourString = hourFormat.format(date);
                                String dayString = dayFormat.format(date);
                                String dateString = dateFormat.format(date);
                                String monthString = monthFormat.format(date);
                                dayString = Character.toUpperCase(dayString.charAt(0)) + dayString.substring(1);
                                monthString = Character.toUpperCase(monthString.charAt(0)) + monthString.substring(1);
                                thour.setText(hourString);
                                tday.setText(dayString);
                                tdate.setText(dateString+" de "+monthString);
                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();
    }

    public void statusOn(){
        textViewStatus.setText("Registrado en el Objetivo");
        textViewStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.holo_green_light));
    }

    public void statusOff(){
        textViewStatus.setText("No registrado en el Objetivo");
        textViewStatus.setTextColor(Color.RED);
    }

    public void statusEmpty(){
        textViewStatus.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        statusCheck();
    }

    private void countDown(){
        countDownTimer = new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {
                backgroundCounter.setTextSize(90);
                backgroundCounter.setText("" + millisUntilFinished / 1000);
            }
            public void onFinish() {
                mProgressBar.setVisibility(View.GONE);
                backgroundCounter.setTextSize(24);
                backgroundCounter.setText("Alarma Enviada !");
                enviarAlarma();
            }
        }.start();
    }


    private void enviarDatos(){
        String msg1 = "##,imei:358240051111110,A;";
        String msg2 = "358240051111110;";
        String msg3 = "imei:358240051111110,help me,1905122034,,F,203418.000,A,3432.5935,S,05828.5003,W,0.00,4.22,;";
        Cliente cliente = new Cliente(this);
        cliente.execute(msg3);
        Toast.makeText(this, "Alerta enviada al servidor: "+msg3, Toast.LENGTH_SHORT).show();
    }

    private void enviarAlarma(){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        Map<String, Object> data = new HashMap<>();
        data.put("idCliente", prefs.getString(CLIENTE,""));
        data.put("idObjetivo", prefs.getString(OBJETIVO,""));
        data.put("nroLegajo", prefs.getString(NRO_LEGAJO,""));
        data.put("timestamp", FieldValue.serverTimestamp());
        data.put("latitud", "-34.543225");
        data.put("longitud", "-58.474950");

        database.collection("alerts")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

}