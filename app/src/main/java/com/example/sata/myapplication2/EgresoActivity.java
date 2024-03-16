package com.example.sata.myapplication2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.example.sata.myapplication2.AlertDialog.FaceDetectionError;
import com.example.sata.myapplication2.AlertDialog.FaceRecognitionError;
import com.example.sata.myapplication2.AlertDialog.FacesDetectionError;
import com.example.sata.myapplication2.AlertDialog.RegisterAlert;
import com.example.sata.myapplication2.AlertDialog.RegisterAlertError;
import com.example.sata.myapplication2.FaceRecognition.FaceClassifier;
import com.example.sata.myapplication2.FaceRecognition.TFLiteFaceRecognition;
import com.example.sata.myapplication2.POJO.HoraRegistrada;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.instacart.library.truetime.TrueTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import com.bumptech.glide.request.RequestOptions;
//import com.bumptech.glide.request.transition.Transition;

public class EgresoActivity extends AppCompatActivity implements ResultListener<Date>{

    private static final String TAG = "Egreso_Activity_TAG";

    private static final String NOMBRE_OBJETIVO = "nombreObjetivo";
    private static final String NOMBRE_CLIENTE = "nombreCliente";
    private static final String ID_CLIENTE = "idCliente";
    private static final String ID_OBJETIVO = "idObjetivo";

    private static final String MAP_COOR = "map_coor";
    private static final String MAP_RADIO = "map_radio";

    private static final int REQUEST_TAKE_PHOTO = 1;

    private static final String PROFILE_PHOTO = "ProfilePhotoPath";

    private static final String HORA_EGRESO = "he";
    private static final String FECHA_EGRESO = "fe";
    private static final String FECHA_PUESTO = "fp";
    private static final String NOMBRE_PUESTO = "np";
    private static final String EGRESO_PUESTO = "ep";
    private static final String TURNO_NOCHE = "tn";
    private static final String INGRESO_PUESTO = "ip";
    private static final String IMAGE_PATH = "im";
    private static final String HORA_EGRESO_PARAM = "hep";
    private static final String NOMBRE_PERSONAL = "an";
    private static final String ESTADO_SESION = "es";
    private static final String SESION_ID = "si";
    private static final String NRO_LEGAJO = "nl";

    private static final String PERS_CODI = "pers_codi";
    private static final String ASIG_PUES = "asig_pues";
    private static final String HORA_INGRESO_TIMESTAMP = "hit";
    private static final String DEVI_UBIC = "devi_ubic";
    private static final String ASIG_ID = "asig_id";

    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private Button btnRegistrarSalida;
    private TextView textViewStatus;
    private TextView textViewUbicacion;
    private Uri photoURI;
    private ImageView imageViewCamara;
    private String currentPhotoPath;
    private String idCliente;
    private String idObjetivo;
    private TextView estadoDelIngreso;
    private FirebaseUser userAuth;

    private double branchRadio;
    private double branchLatitud;
    private double branchLongitud;

    // TODO declare face detector
    FaceDetector detector;

    FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build();

    // TODO declare face recognizer
    FaceClassifier faceClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egreso);

        Toolbar toolbar = findViewById(R.id.toolbarEgreso);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        synchronizeClock();

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        idCliente = prefs.getString(ID_CLIENTE,"");
        idObjetivo = prefs.getString(ID_OBJETIVO,"");

        database = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        btnRegistrarSalida = findViewById(R.id.buttonRegistrarEgreso);
        estadoDelIngreso = findViewById(R.id.textViewStatus);
        imageViewCamara = findViewById(R.id.imageViewCamara);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewUbicacion = findViewById(R.id.textViewUbicacion);

        btnRegistrarSalida.setClickable(false);
        btnRegistrarSalida.setAlpha(0.5f);
        btnRegistrarSalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ubicacion = (String) textViewUbicacion.getText();
                if(ubicacion.equals("Dentro del Rango")  || ubicacion.equals("Desactivada")){
                    dispatchTakePictureIntent();
                    btnRegistrarSalida.setClickable(false);
                    btnRegistrarSalida.setAlpha(0.5f);
                }else{
                    Toast.makeText(EgresoActivity.this, "Por favor verifique que este dentro del rango de ubicacion", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //TODO initialize face detector
        detector = FaceDetection.getClient(highAccuracyOpts);

        //TODO initialize face recognition model
        try {
            faceClassifier = TFLiteFaceRecognition.create(getAssets(),"facenet.tflite",160,false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        chequearUbicacion();

        chequearEstadoSesion();

        faceRegistration();

    }

    private Bitmap loadImageFromStorage(){
        Bitmap bitmap = null;
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String profilePhotoPath = prefs.getString(PROFILE_PHOTO, "");
        try {
            File f = new File(profilePhotoPath, "profile.jpg");
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public Bitmap croppedFace(Rect bound, Bitmap input){
        if(bound.top < 0){
            bound.top = 0;
        }
        if(bound.left < 0){
            bound.left = 0;
        }
        if(bound.right > input.getWidth()){
            bound.right = input.getWidth()-1;
        }
        if(bound.bottom > input.getHeight()){
            bound.bottom = input.getHeight()-1;
        }
        Bitmap croppedFace = Bitmap.createBitmap(input,bound.left,bound.top,bound.width(),bound.height());
        croppedFace = Bitmap.createScaledBitmap(croppedFace,160,160,false);
        return croppedFace;
    }

    private void faceRegistration(){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String name = prefs.getString(NOMBRE_PERSONAL, "");
        Bitmap input = loadImageFromStorage();
        InputImage image = InputImage.fromBitmap(input, 0);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {

                                        if(faces.size() == 0){
                                            Log.d(TAG, "No se detectaron caras en la registracion");
                                        }else if(faces.size() > 1){
                                            Log.d(TAG, "Se detecto mas de una cara en la registracion");
                                        }else{
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                Bitmap bitmap = croppedFace(bounds,input);
                                                FaceClassifier.Recognition recognitionRegistration = faceClassifier.recognizeImage(bitmap,true);
                                                faceClassifier.register(name,recognitionRegistration);
                                            }
                                        }

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });


    }

    private void faceVerification(Bitmap input){

        InputImage image = InputImage.fromBitmap(input, 0);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        if(faces.size() == 0){
                                            showFaceDetectionError();
                                            btnRegistrarSalida.setAlpha(1.0f);
                                            btnRegistrarSalida.setClickable(true);
                                        }else if(faces.size() > 1){
                                            showFacesDetectionError();
                                            btnRegistrarSalida.setAlpha(1.0f);
                                            btnRegistrarSalida.setClickable(true);
                                        }else{
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                Bitmap bitmap = croppedFace(bounds,input);
                                                FaceClassifier.Recognition recognitionVerification = faceClassifier.recognizeImage(bitmap,false);
                                                if(recognitionVerification.getDistance()<=0.85){
                                                    initTrueTime();
                                                }else{
                                                    showFaceRecognitionError();
                                                    btnRegistrarSalida.setAlpha(1.0f);
                                                    btnRegistrarSalida.setClickable(true);
                                                }
                                            }
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...

                                    }
                                });

    }

    private void chequearUbicacion(){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        int ubicacion = prefs.getInt(DEVI_UBIC,0);

        if(ubicacion==1){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            } else {
                locationStart();
            }
        }else {
            textViewUbicacion.setText("Desactivada");
            textViewUbicacion.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorNaranja));
        }

    }

    private void locationStart() {

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        String coordenadas = prefs.getString(MAP_COOR,"");

        String[] split = coordenadas.split(",");

        branchRadio = prefs.getInt(MAP_RADIO,0);
        branchLatitud = Double.parseDouble(split[0]);
        branchLongitud = Double.parseDouble(split[1]);

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }

        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Location markerLocation = new Location("");
                markerLocation.setLatitude(branchLatitud);
                markerLocation.setLongitude(branchLongitud);

                if (location.distanceTo(markerLocation) < branchRadio) {
                    textViewUbicacion.setText("Dentro del Rango");
                    textViewUbicacion.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.holo_green_light));
                }else{
                    textViewUbicacion.setText("Fuera del Rango");
                    textViewUbicacion.setTextColor(Color.RED);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                textViewUbicacion.setText("GPS Activado");
                textViewUbicacion.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.holo_green_light));
            }

            @Override
            public void onProviderDisabled(String provider) {
                textViewUbicacion.setText("GPS Desactivado");
                textViewUbicacion.setTextColor(Color.RED);
            }
        });

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void registrarSalida(String fechaEgreso,String horaEgreso) {

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        String fechaPuesto = prefs.getString(FECHA_PUESTO,"");
        String egresoPuesto = prefs.getString(EGRESO_PUESTO,"");
        Boolean turnoNoche = prefs.getBoolean(TURNO_NOCHE,false);
        String asigId = prefs.getString(ASIG_ID,"");

        String horaEgresoParametrizado = HoraRegistrada.egresoParametrizado(egresoPuesto,fechaPuesto,horaEgreso,fechaEgreso,turnoNoche);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = Configurador.API_PATH + "asigvigi/"+asigId;
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("horaEgreso", horaEgresoParametrizado);
            final String requestBody = jsonBody.toString();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getInt("result")==1){
                            showRegisterAlert();
                            actualizarEstadoPersonal(fechaEgreso,horaEgreso);
                            servicioFinalizado();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(EgresoActivity.this, "No se pudo registrar la salida del servicio, por favor contactese con la Central de Operaciones", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(EgresoActivity.this, "No se pudo registrar la salida del servicio, por favor contactese con la Central de Operaciones", Toast.LENGTH_SHORT).show();
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

    private void egresoAnticipado(String fechaEgreso,String horaEgreso) {

        Date horaEgresoReal = null;
        Date horaEgresoPuesto = null;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        String fechaPuesto = prefs.getString(FECHA_PUESTO,"");
        String egresoPuesto = prefs.getString(EGRESO_PUESTO,"");
        Boolean turnoNoche = prefs.getBoolean(TURNO_NOCHE,false);
        String asigId = prefs.getString(ASIG_ID,"");

        String horaEgresoParametrizado = HoraRegistrada.egresoParametrizado(egresoPuesto,fechaPuesto,horaEgreso,fechaEgreso,turnoNoche);


        try {
            horaEgresoReal = dateFormat.parse(fechaEgreso+" "+horaEgreso);
            horaEgresoPuesto = dateFormat.parse(fechaPuesto+" "+egresoPuesto);
            if(turnoNoche){
                horaEgresoPuesto = new Date(horaEgresoPuesto.getTime()+86400000);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Long diferenciaEnMinutos = (horaEgresoPuesto.getTime() - horaEgresoReal.getTime())/60/1000;

        if(diferenciaEnMinutos>=15){
            showEarlyRetirement(horaEgresoParametrizado,fechaEgreso,horaEgreso);
        }else{
            registrarSalida(fechaEgreso,horaEgreso);
        }
    }

    private void actualizarEstadoPersonal(String fechaEgreso, String horaEgreso) {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FECHA_EGRESO, fechaEgreso);
        editor.putString(HORA_EGRESO, horaEgreso);
        editor.putBoolean(ESTADO_SESION,false);
        editor.apply();
        int persCodi = Integer.parseInt(prefs.getString(PERS_CODI,""));
        cerrarEstadoSesion(persCodi);
    }

    private void cerrarEstadoSesion(int persCodi){

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = Configurador.API_PATH + "last_session/"+persCodi;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void chequearEstadoSesion(){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        boolean estadoSesion = prefs.getBoolean(ESTADO_SESION,false);
                    if (!estadoSesion) {
                        btnRegistrarSalida.setAlpha(0.5f);
                        btnRegistrarSalida.setClickable(false);
                    } else {
                        btnRegistrarSalida.setAlpha(1.0f);
                        btnRegistrarSalida.setClickable(true);
                    }
        cargarDatosPantallaIngreso(estadoSesion);
    }

    private void servicioFinalizado(){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        String horaEgreso = prefs.getString(HORA_EGRESO,"--:--");
        String fechaEgreso = prefs.getString(FECHA_EGRESO,"");
        String egresoPuesto = prefs.getString(EGRESO_PUESTO,"");
        String fechaPuesto = prefs.getString(FECHA_PUESTO,"");
        boolean turnoNoche = prefs.getBoolean(TURNO_NOCHE,false);

        TextView textViewHoraRegistrada = findViewById(R.id.textViewHoraRegistrada);
        TextView textViewHoraEgreso = findViewById(R.id.textViewHoraEgreso);

        btnRegistrarSalida.setAlpha(0.5f);
        btnRegistrarSalida.setClickable(false);
        textViewStatus.setText("Servicio Finalizado");
        textViewStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorNaranja));
        textViewHoraRegistrada.setText(HoraRegistrada.egresoParametrizado(egresoPuesto,fechaPuesto,horaEgreso,fechaEgreso,turnoNoche));
        textViewHoraEgreso.setText(horaEgreso);

    }

    public void initTrueTime() {
        if (isNetworkConnected()) {
            if (!TrueTime.isInitialized()) {
                TrueTimeAsyncTask trueTime = new TrueTimeAsyncTask(this, this);
                trueTime.execute();
            } else {
                Date date = TrueTime.now();
                setFechaExitSharedPreferences(date);
            }
        } else{
            Toast.makeText(this, "No esta conectado a Internet", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    @Override
    public void finish(Date resultado) {
        setFechaExitSharedPreferences(resultado);
    }

    private void setFechaExitSharedPreferences(Date fecha){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String fechaEgreso = dateFormat.format(fecha);
        String horaEgreso = hourFormat.format(fecha);

        if(sesionVencida(fechaEgreso,horaEgreso)){
            cerrarEstadoSesionApp();
            textViewStatus.setText("Servicio Expirado");
            textViewStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorNaranja));
            showRegisterAlertError();
        } else {
            chequearEstadoSesionServer(fechaEgreso,horaEgreso);
        }
    }

    private void cerrarEstadoSesionApp(){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ESTADO_SESION,false);
        editor.apply();
    }

    private void cargarDatosPantallaIngreso(Boolean estadoSesion){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        String cliente = prefs.getString(NOMBRE_CLIENTE,"").toUpperCase();
        String objetivo = prefs.getString(NOMBRE_OBJETIVO,"").toUpperCase();
        String nombre = prefs.getString(NOMBRE_PERSONAL,"").toUpperCase();
        String nombrePuesto = prefs.getString(NOMBRE_PUESTO,"");
        String ingresoPuesto = prefs.getString(INGRESO_PUESTO,"");
        String egresoPuesto = prefs.getString(EGRESO_PUESTO,"");

        TextView nombrePersonal = findViewById(R.id.textViewName);
        TextView nombreObjetivo = findViewById(R.id.textViewObjetive);
        TextView puestoSeleccionado = findViewById(R.id.puestoSeleccionado);
        TextView textViewHorario = findViewById(R.id.textViewHorario);
        TextView textViewStatus = findViewById(R.id.textViewStatus);

        nombrePersonal.setText(nombre);
        nombreObjetivo.setText(cliente+" - "+objetivo);

        if(estadoSesion){
            puestoSeleccionado.setText(nombrePuesto);
            String horario = ingresoPuesto+" a "+egresoPuesto;
            textViewHorario.setText(horario);
            textViewHorario.setVisibility(View.VISIBLE);
            textViewStatus.setText("Registrado en el Objetivo");
            textViewStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.holo_green_light));
        } else {
            puestoSeleccionado.setText("No seleccionado");
            textViewStatus.setText("No registrado en el Objetivo");
            textViewStatus.setTextColor(Color.RED);
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        String imageFileName = prefs.getString(NRO_LEGAJO,"")+"_EGRESO.jpg";

        File storageDir = getApplicationContext().getCacheDir();
        File imageFile = new File(storageDir, imageFileName);

        if (!imageFile.createNewFile()) {
            imageFile.delete();
            imageFile = new File(storageDir, imageFileName);
        }

        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void subirArchivoImageView(String path){
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        StorageReference photoRef = storageRef.child(path+"/"+photoURI.getLastPathSegment());
        // Get the data from an ImageView as bytes
        imageViewCamara.setDrawingCacheEnabled(true);
        imageViewCamara.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageViewCamara.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
    }

    public void showEarlyRetirement(String horaEgresoParametrizada,String fechaEgreso,String horaEgreso){
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.ThemeDialogCustom);
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.custom_alert_early_retirement, null);
        TextView textView = (TextView) dialogLayout.findViewById(R.id.textAlert);
        textView.setText("Su egreso se registrará a las "+horaEgresoParametrizada+" ¿Desea continuar de todos modos?");
        builder.setView(dialogLayout)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        registrarSalida(fechaEgreso,horaEgreso);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        btnRegistrarSalida.setAlpha(1.0f);
                        btnRegistrarSalida.setClickable(true);
                    }
                });
        builder.create();
        builder.show();
    }

    public void showRegisterAlert(){
        RegisterAlert myAlert = new RegisterAlert();
        myAlert.setTipoRegistro("salida");
        myAlert.show(getSupportFragmentManager(),"Register Alert");
    }

    public void showRegisterAlertError(){
        RegisterAlertError myAlert = new RegisterAlertError();
        myAlert.show(getSupportFragmentManager(),"Register Alert Error");
    }

    public void showFaceRecognitionError(){
        FaceRecognitionError myAlert = new FaceRecognitionError();
        myAlert.show(getSupportFragmentManager(),"Face Recognition Error");
    }

    public void showFaceDetectionError(){
        FaceDetectionError myAlert = new FaceDetectionError();
        myAlert.show(getSupportFragmentManager(),"Face Detection Error");
    }

    public void showFacesDetectionError(){
        FacesDetectionError myAlert = new FacesDetectionError();
        myAlert.show(getSupportFragmentManager(),"Faces Detection Error");
    }

    public void cargarImagen(){
        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                .skipMemoryCache(true);

        Glide.with(getApplicationContext())
                .load(currentPhotoPath)
                .placeholder(R.drawable.camera_icon)
                .apply(requestOptions)
                .into(new BaseTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageViewCamara.setImageDrawable(resource);
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        faceVerification(bitmap);
                        //initTrueTime();
                    }
                    @Override
                    public void getSize(@NonNull SizeReadyCallback cb) {
                        cb.onSizeReady(200, 200);
                    }
                    @Override
                    public void removeCallback(@NonNull SizeReadyCallback cb) {
                    }
                })
        ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            cargarImagen();
        }else{
            btnRegistrarSalida.setAlpha(1.0f);
            btnRegistrarSalida.setClickable(true);
        }
    }

    public Boolean sesionVencida(String fechaEgreso, String horaEgreso){
        Date now = armarDate(fechaEgreso,horaEgreso);
        Configurador miConf = Configurador.getInstance();
        if (comparaFechas(now,miConf.getFinSesion())==2){
            return false;
        } else {
            //Sesion Vencida
            return true;
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

    public int comparaFechas(Date date1, Date date2){
        if(date1.getTime()>date2.getTime()+60*60*1000){
            return 1; // Parametro 1 mas grande que parametro 2
        }else if(date1.getTime()<date2.getTime()+60*60*1000){
            return 2; // Parametro 2 mas grande que parametro 1
        } else {
            return 0; // Iguales
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


    public void chequearEstadoSesionServer(String fechaEgreso, String horaEgreso){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String persCodi = prefs.getString(PERS_CODI,"");

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String mJSONURLString = Configurador.API_PATH + "last_session/"+persCodi;

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
                                if(jsonObject.getInt("LAST_ESTA")==1){
                                    //registrarSalida(fechaEgreso,horaEgreso);
                                    egresoAnticipado(fechaEgreso,horaEgreso);
                                }else{
                                    cerrarEstadoSesionApp();
                                    textViewStatus.setText("Servicio cerrado por Operador");
                                    textViewStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorNaranja));
                                    showRegisterAlertError();
                                }
                            } catch (JSONException e) {
                                Log.d(TAG, "No se pudo extraer estado de la ultima sesion");
                            }
                        }else{
                            Log.d(TAG, "No se encontro personal");
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d(TAG, "Error en el servidor");
                    }
                }
        );

        requestQueue.add(jsonArrayRequest);

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


}