package com.appcontrol.sab5.app.MainActivityFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appcontrol.sab5.app.AlertDialog.ExitAlert;
import com.appcontrol.sab5.app.AlertDialog.LogOutAlert;
import com.appcontrol.sab5.app.AlertDialog.SesionVencidaAlert;
import com.appcontrol.sab5.app.AlertDialog.SesionVigenteAlert;
import com.appcontrol.sab5.app.Configurador;
import com.appcontrol.sab5.app.EgresoActivity;
import com.appcontrol.sab5.app.IngresoActivity;
import com.appcontrol.sab5.app.MainActivity;
import com.appcontrol.sab5.app.POJO.Cliente_Old;
import com.appcontrol.sab5.app.POJO.UltimaSesionDM;
import com.appcontrol.sab5.app.R;
import com.appcontrol.sab5.app.ResultListener;
import com.appcontrol.sab5.app.TrueTimeAsyncTask;
import com.instacart.library.truetime.TrueTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements ResultListener<Date> {

    private static final String TAG = "HomeFragment_TAG";

    private static final String NOMBRE_CLIENTE = "nombreCliente";
    private static final String NOMBRE_OBJETIVO = "nombreObjetivo";
    private static final String ID_CLIENTE = "idCliente";
    private static final String ID_OBJETIVO = "idObjetivo";

    private static final String FECHA_INGRESO_LOGIN = "fechaIngresoLogin";
    private static final String HORA_INGRESO_LOGIN = "horaIngresoLogin";

    private static final String FECHA_PUESTO = "fp";
    private static final String NOMBRE_PUESTO = "np";
    private static final String INGRESO_PUESTO = "ip";
    private static final String HORA_INGRESO = "hi";
    private static final String HORA_EGRESO = "he";
    private static final String EGRESO_PUESTO = "ep";
    private static final String FECHA_INGRESO = "fi";
    private static final String TURNO_NOCHE = "tn";

    private static final String NRO_LEGAJO = "nl";
    private static final String NOMBRE_PERSONAL = "an";
    private static final String ESTADO_SESION = "es";
    private static final String ID_ANDROID = "androidId";

    private static final String PERS_CODI = "pers_codi";
    private static final String HORA_INGRESO_TIMESTAMP = "hit";
    private static final String ASIG_PUES = "asig_pues";
    private static final String ASIG_ID = "asig_id";

    //private FirebaseFirestore database;

    private Button backgroundCounter;
    private ProgressBar mProgressBar;
    private ProgressDialog progressDialog=null;
    private TextView textViewStatus;

    private CardView btnIngreso=null;
    private CardView btnEgreso=null;
    private CardView btnNovedad=null;
    private CardView btnLlamar=null;
    private Button btnPanico=null;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private CountDownTimer countDownTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                boolean estadoSesion = prefs.getBoolean(ESTADO_SESION,false);
                if(estadoSesion){
                    showLogOutAlert();
                } else {
                    showExitAlert();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        ((MainActivity)getActivity()).cambiarItemMenu();

        progressDialog= new ProgressDialog(getContext());
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog);

        //database = FirebaseFirestore.getInstance();

        synchronizeClock(root);

        backgroundCounter = root.findViewById(R.id.backgroundCounter);
        mProgressBar = root.findViewById(R.id.progress_circular);

        btnIngreso = root.findViewById(R.id.cardViewIngresar);
        btnEgreso = root.findViewById(R.id.cardViewEgresar);
        btnNovedad = root.findViewById(R.id.cardViewNovedad);
        btnLlamar = root.findViewById(R.id.cardViewCall);
        btnPanico = root.findViewById(R.id.buttonPanic);

        backgroundCounter = root.findViewById(R.id.backgroundCounter);
        textViewStatus = root.findViewById(R.id.textViewStatus);
        mProgressBar = root.findViewById(R.id.progress_circular);

        cargarDatosPantallaPrincipal(root);

        btnIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), IngresoActivity.class);
                startActivity(intent);
            }
        });

        btnEgreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EgresoActivity.class);
                startActivity(intent);
            }
        });

        btnNovedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(getActivity(), NovedadActivity.class);
//                startActivity(intent);
                Toast.makeText(getContext(), "Modulo Novedades inhabilitado", Toast.LENGTH_SHORT).show();
            }
        });

        btnLlamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + ""));
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        btnPanico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Boton de Panico inhabilitado", Toast.LENGTH_SHORT).show();
            }
        });

//        btnPanico.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        backgroundCounter.setVisibility(View.VISIBLE);
//                        mProgressBar.setVisibility(View.VISIBLE);
//                        countDown();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        backgroundCounter.setVisibility(View.GONE);
//                        mProgressBar.setVisibility(View.GONE);
//                        countDownTimer.cancel();
//                }
//                return true;
//            }
//
//        });

        return root;
    }

    public void showLogOutAlert(){
        LogOutAlert myAlert = new LogOutAlert();
        myAlert.show(getParentFragmentManager(),"Log Out Alert");
    }

    public void showExitAlert(){
        ExitAlert myAlert = new ExitAlert();
        myAlert.show(getParentFragmentManager(),"Exit Alert");
    }

    //Carga la fecha y hora desde Internet
    public void initTrueTime() {
        if (isNetworkConnected()) {
            if (!TrueTime.isInitialized()) {
                TrueTimeAsyncTask trueTime = new TrueTimeAsyncTask(getContext(), this);
                trueTime.execute();
            } else {
                Date date = TrueTime.now();
                setFechaLoginSharedPreferences(date);
            }
        } else{
            disableButtons();
            progressDialog.dismiss();
            Toast.makeText(getContext(), "No esta conectado a Internet", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    private void setFechaLoginSharedPreferences(Date fecha){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String fechaIngreso = dateFormat.format(fecha);
        String horaIngreso = hourFormat.format(fecha);

        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FECHA_INGRESO_LOGIN,fechaIngreso);
        editor.putString(HORA_INGRESO_LOGIN,horaIngreso);
        editor.apply();

        chequearEstadoSesion();
    }

    private void cargarDatosPantallaPrincipal(View fragmentView){

        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String nombre = prefs.getString(NOMBRE_PERSONAL,"");
        String nombreCliente = prefs.getString(NOMBRE_CLIENTE,null);
        String nombreObjetivo = prefs.getString(NOMBRE_OBJETIVO,null);

        TextView datosObjetivo = fragmentView.findViewById(R.id.textViewObjetiveHome);
        TextView nombrePersonal = fragmentView.findViewById(R.id.textViewName);

        nombrePersonal.setText(nombre.toUpperCase());

        if(nombreCliente==null || nombreObjetivo==null){
            datosObjetivo.setText("Sin Asignar");
            //datosObjetivo.setTextColor(ContextCompat.getColor(getContext(), R.color.colorNaranja));
            datosObjetivo.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBlanco));
            ((MainActivity)getActivity()).setNavigationHeaderData(nombre,"OBJETIVO SIN ASIGNAR");
            disableButtons();
            progressDialog.dismiss();
        }else{
            datosObjetivo.setText((nombreCliente+" - "+nombreObjetivo).toUpperCase());
            ((MainActivity)getActivity()).setNavigationHeaderData(nombre,nombreCliente+" - "+nombreObjetivo);
            initTrueTime();
        }

    }

    @Override
    public void finish(Date resultado) {
        setFechaLoginSharedPreferences(resultado);
    }

    private void chequearEstadoSesion(){

        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String persCodi = prefs.getString(PERS_CODI,"");

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String mJSONURLString = Configurador.API_PATH + "last_session/"+persCodi;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                mJSONURLString,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.length()>0) {
                            boolean estadoSesion = false;
                            JSONObject jsonObject;
                            try {
                                jsonObject = response.getJSONObject(0);
                                if(jsonObject.getInt("LAST_ESTA")==1){
                                    estadoSesion = true;
                                }
                                if (estadoSesion) {
                                    UltimaSesionDM ultimaSesion = new UltimaSesionDM();
                                    ultimaSesion.setLAST_CCLI(jsonObject.getInt("LAST_CCLI"));
                                    ultimaSesion.setLAST_COBJ(jsonObject.getString("LAST_COBJ"));
                                    ultimaSesion.setLAST_CPER(jsonObject.getInt("LAST_CPER"));
                                    ultimaSesion.setLAST_FECH(jsonObject.getString("LAST_FECH"));
                                    ultimaSesion.setLAST_DHOR(jsonObject.getString("LAST_DHOR"));
                                    ultimaSesion.setLAST_HHOR(jsonObject.getString("LAST_HHOR"));
                                    ultimaSesion.setLAST_USUA(jsonObject.getInt("LAST_USUA"));
                                    ultimaSesion.setLAST_PUES(jsonObject.getInt("LAST_PUES"));
                                    ultimaSesion.setLAST_NPUE(jsonObject.getString("LAST_NPUE"));
                                    ultimaSesion.setLAST_ESTA(jsonObject.getInt("LAST_ESTA"));
                                    ultimaSesion.setLAST_NCLI(jsonObject.getString("LAST_NCLI"));
                                    ultimaSesion.setLAST_NOBJ(jsonObject.getString("LAST_NOBJ"));
                                    ultimaSesion.setLAST_DHRE(jsonObject.getString("LAST_DHRE"));
                                    ultimaSesion.setLAST_TIME(jsonObject.getString("LAST_TIME"));
                                    ultimaSesion.setLAST_ASID(jsonObject.getString("LAST_ASID"));
                                    //ultimaSesion.setLAST_DHPA(jsonObject.getString("LAST_DHPA"));
                                    cargaUltimaSesion(ultimaSesion);

                                } else {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean(ESTADO_SESION,false);
                                    editor.apply();
                                    textViewStatus.setText("No registrado en el Objetivo");
                                    textViewStatus.setTextColor(Color.RED);
                                }
                                progressDialog.dismiss();

                            } catch (JSONException e) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean(ESTADO_SESION,false);
                                editor.apply();
                                textViewStatus.setText("No registrado en el Objetivo");
                                textViewStatus.setTextColor(Color.RED);
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Error al intentar conectarse al Servidor ", Toast.LENGTH_SHORT).show();
                                Log.d("catch: ",e.toString());
                            }
                        }else{
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(ESTADO_SESION,false);
                            editor.apply();
                            textViewStatus.setText("No registrado en el Objetivo");
                            textViewStatus.setTextColor(Color.RED);
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(ESTADO_SESION,false);
                        editor.apply();
                        textViewStatus.setText("No registrado en el Objetivo");
                        textViewStatus.setTextColor(Color.RED);
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error al intentar conectarse al Servidor", Toast.LENGTH_SHORT).show();
                        Log.d("onErrorResponse: ",error.toString());
                    }
                }
        );

        requestQueue.add(jsonArrayRequest);

    }

    private void cargaUltimaSesion(UltimaSesionDM ultimaSesion){

        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int idClienteDispositivo = Integer.parseInt(prefs.getString(ID_CLIENTE,"").toUpperCase());
        String idObjetivoDispositivo = prefs.getString(ID_OBJETIVO,"").toUpperCase();

        if (idClienteDispositivo == ultimaSesion.getLAST_CCLI() && idObjetivoDispositivo.equals(ultimaSesion.getLAST_COBJ())){
            obtenerTurno(ultimaSesion);
        } else {
            // Si no coinciden el ID CLIENTE y ID OBJETIVO es porque se realizo un loguin desde otro dispositivo/objetivo
            showObjetivoVencidoAlert(ultimaSesion.getLAST_NCLI(),ultimaSesion.getLAST_NOBJ(),dateToString(ultimaSesion.getLAST_FECH()));
            editor.putBoolean(ESTADO_SESION,false);
            editor.apply();
            cerrarEstadoSesion(ultimaSesion.getLAST_CPER());
            textViewStatus.setText("No registrado en el Objetivo");
            textViewStatus.setTextColor(Color.RED);
        }
    }

    private void cerrarEstadoSesion(int persCodi){
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String URL = Configurador.API_PATH + "last_session/"+persCodi;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Toast.makeText(getContext(), "Estado Sesion cerrada correctamente", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getContext(), "Error al cerrar sesion", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);

    }

    private void obtenerTurno(UltimaSesionDM ultimaSesion){

        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Date fechaLogin = null;
        Date fechaVence = null;

        fechaLogin = armarDate(prefs.getString(FECHA_INGRESO_LOGIN,""),prefs.getString(HORA_INGRESO_LOGIN,""));

        if(esTurnoNoche(ultimaSesion.getLAST_DHOR(),ultimaSesion.getLAST_HHOR())){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date hoy = null;
            try {
                hoy = dateFormat.parse(ultimaSesion.getLAST_FECH());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date diaPosterior = new Date(hoy.getTime()+86400000);
            fechaVence = armarDate(dateFormat.format(diaPosterior),ultimaSesion.getLAST_HHOR());
        }else{
            fechaVence = armarDate(dateToString(ultimaSesion.getLAST_FECH()),ultimaSesion.getLAST_HHOR());
        }

        Configurador miConf = Configurador.getInstance();
        miConf.setFinSesion(fechaVence);

        if (comparaFechas(fechaLogin,fechaVence)==2){

            showSesionVigenteAlert();

//            String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
//            SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
//            sdf.setTimeZone(TimeZone.getTimeZone("GMT-3:00"));
//
//            Date date = null;
//            try {
//                date = sdf.parse(ultimaSesion.getLAST_TIME());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//
//            SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//            String horaIngresoTimestamp = timestampFormat.format(date);
//
//            Log.d(TAG, "obtenerTurno: "+horaIngresoTimestamp);

            editor.putBoolean(ESTADO_SESION,true);
            editor.putInt(ASIG_PUES,ultimaSesion.getLAST_PUES());
            editor.putString(FECHA_PUESTO,dateToString(ultimaSesion.getLAST_FECH()));
            editor.putString(INGRESO_PUESTO,ultimaSesion.getLAST_DHOR());
            editor.putString(HORA_INGRESO,ultimaSesion.getLAST_DHRE());
            //editor.putString(HORA_INGRESO_TIMESTAMP,horaIngresoTimestamp);
            editor.putString(HORA_INGRESO_TIMESTAMP,ultimaSesion.getLAST_TIME());
            editor.putString(FECHA_INGRESO,dateToString(ultimaSesion.getLAST_FECH())); // Deberia ir la fecha de ingreso real, se coloca la FECHA PUESTO
            editor.putString(HORA_EGRESO,ultimaSesion.getLAST_HHOR());
            editor.putString(EGRESO_PUESTO,ultimaSesion.getLAST_HHOR());
            editor.putBoolean(TURNO_NOCHE,esTurnoNoche(ultimaSesion.getLAST_DHOR(),ultimaSesion.getLAST_HHOR()));
            editor.putString(NOMBRE_PUESTO,ultimaSesion.getLAST_NPUE());
            editor.putString(ASIG_ID,ultimaSesion.getLAST_ASID());

            editor.apply();
            textViewStatus.setText("Registrado en el Objetivo");
            textViewStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.holo_green_light));

        } else {
            //Sesion Vencida
            showSesionVencidaAlert();
            editor.putBoolean(ESTADO_SESION,false);
            editor.apply();

            cerrarEstadoSesion(ultimaSesion.getLAST_CPER());

            textViewStatus.setText("No registrado en el Objetivo");
            textViewStatus.setTextColor(Color.RED);
        }

    }

    public boolean esTurnoNoche(String horaIngreso, String horaEgreso) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date dateIngreso = null;
        Date dateEgreso = null;
        try {
            dateIngreso = dateFormat.parse("2022-01-01"+" "+horaIngreso);
            dateEgreso = dateFormat.parse("2022-01-01"+" "+horaEgreso);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(dateIngreso == dateEgreso){
            return true;
        }else if (dateIngreso.getTime() > dateEgreso.getTime()){
            return true;
        }else{
            return false;
        }
    }

    public String dateToString(String dateStr){
        return dateStr.substring(0,10);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteCache(getContext());
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

    public void showSesionVigenteAlert(){
        SesionVigenteAlert myAlert = new SesionVigenteAlert();
        myAlert.show(getParentFragmentManager(),"Sesion Vigente Alert");
    }

    public void showSesionVencidaAlert(){
        SesionVencidaAlert myAlert = new SesionVencidaAlert();
        myAlert.show(getParentFragmentManager(),"Sesion Vencida Alert");
    }

    public void showObjetivoVencidoAlert(String cliente, String objetivo, String fecha){
        SesionVencidaAlert myAlert = new SesionVencidaAlert();
        myAlert.setObjetivoFecha(cliente,objetivo,fecha);
        myAlert.show(getParentFragmentManager(),"Sesion Vencida Alert");
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
        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", MODE_PRIVATE);
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

    private void synchronizeClock(View fragmentView) {

        TextView thour = fragmentView.findViewById(R.id.textViewClock);
        TextView tday = fragmentView.findViewById(R.id.textViewDay);
        TextView tdate = fragmentView.findViewById(R.id.textViewDate);

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE,");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        mHandler.post(new Runnable() {
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
                    Toast.makeText(getContext(), "Se interrumpio la sincronizacion", Toast.LENGTH_SHORT).show();
                }
            }
        };
        t.start();


    }

    public void statusOn(){
        textViewStatus.setText("Registrado en el Objetivo");
        textViewStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.holo_green_light));
    }

    public void statusOff(){
        textViewStatus.setText("No registrado en el Objetivo");
        textViewStatus.setTextColor(Color.RED);
    }

    public void statusEmpty(){
        textViewStatus.setText("No registrado en el Objetivo");
        textViewStatus.setTextColor(Color.RED);
    }

    @Override
    public void onResume() {
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
                //enviarAlarma();
            }
        }.start();
    }

    private void enviarDatos(){
        String msg1 = "##,imei:358240051111110,A;";
        String msg2 = "358240051111110;";
        String msg3 = "imei:358240051111110,help me,1905122034,,F,203418.000,A,3432.5935,S,05828.5003,W,0.00,4.22,;";
        Cliente_Old clienteOld = new Cliente_Old(getContext());
        clienteOld.execute(msg3);
        Toast.makeText(getContext(), "Alerta enviada al servidor: "+msg3, Toast.LENGTH_SHORT).show();
    }

//    private void enviarAlarma(){
//        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", MODE_PRIVATE);
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("idCliente", prefs.getString(ID_CLIENTE,""));
//        data.put("idObjetivo", prefs.getString(ID_OBJETIVO,""));
//        data.put("nombreCliente", prefs.getString(NOMBRE_CLIENTE,""));
//        data.put("nombreObjetivo", prefs.getString(NOMBRE_OBJETIVO,""));
//        data.put("nroLegajo", prefs.getString(NRO_LEGAJO,""));
//        data.put("timestamp", FieldValue.serverTimestamp());
//        data.put("latitud", "-34.543225");
//        data.put("longitud", "-58.474950");
//
//        database.collection("alerts")
//                .add(data)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding document", e);
//                    }
//                });
//    }

    private void disableButtons(){
        btnIngreso.setEnabled(false);
        btnEgreso.setEnabled(false);
        btnNovedad.setEnabled(false);
        btnLlamar.setEnabled(false);
        btnPanico.setEnabled(false);
    }


}