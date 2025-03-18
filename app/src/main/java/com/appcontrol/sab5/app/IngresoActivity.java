
package com.appcontrol.sab5.app;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.appcontrol.sab5.app.AlertDialog.FaceDetectionError;
import com.appcontrol.sab5.app.AlertDialog.FaceRecognitionError;
import com.appcontrol.sab5.app.AlertDialog.FacesDetectionError;
import com.appcontrol.sab5.app.AlertDialog.PuestoVencidoAlert;
import com.appcontrol.sab5.app.AlertDialog.RegisterAlert;
import com.appcontrol.sab5.app.AlertDialog.RegistroIngresoError;
import com.appcontrol.sab5.app.FaceRecognition.FaceClassifier;
import com.appcontrol.sab5.app.FaceRecognition.TFLiteFaceRecognition;
import com.appcontrol.sab5.app.POJO.HoraRegistrada;
import com.appcontrol.sab5.app.POJO.PuestoAdapter;
import com.appcontrol.sab5.app.POJO.PuestoDM;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.instacart.library.truetime.TrueTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class IngresoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,ResultListener<Date>{

    private static final String TAG = "Ingreso Activity";

    private static final String NOMBRE_CLIENTE = "nombreCliente";
    private static final String NOMBRE_OBJETIVO = "nombreObjetivo";
    private static final String ID_CLIENTE = "idCliente";
    private static final String ID_OBJETIVO = "idObjetivo";

    private static final String MAP_COOR = "map_coor";
    private static final String MAP_RADIO = "map_radio";

    private static final String PROFILE_PHOTO = "ProfilePhotoPath";

    private static final String FECHA_INGRESO = "fi";
    private static final String HORA_INGRESO = "hi";
    private static final String INGRESO_PUESTO = "ip" ;
    private static final String EGRESO_PUESTO = "ep";
    private static final String FECHA_PUESTO = "fp";
    private static final String NOMBRE_PUESTO = "np";
    private static final String TURNO_NOCHE = "tn";
    private static final String HORA_INGRESO_PARAM = "hip";

    private static final String NRO_LEGAJO = "nl";
    private static final String NOMBRE_PERSONAL = "an";
    private static final String ESTADO_SESION = "es";

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String ASIG_OBJE = "asig_obje";
    private static final String ASIG_FECH = "asig_fech";
    private static final String ASIG_DHOR = "asig_dhor";
    private static final String ASIG_HHOR = "asig_hhor";
    private static final String ASIG_VISA = "asig_visa";
    private static final String ASIG_USUA = "asig_usua";
    private static final String ASIG_TIME = "asig_time";
    private static final String ASIG_PUES = "asig_pues";
    private static final String ASIG_BLOQ = "asig_bloq";
    private static final String ASIG_ESTA = "asig_esta";
    private static final String ASIG_FACM = "asig_facm";
    private static final String PERS_CODI = "pers_codi";
    private static final String HORA_INGRESO_TIMESTAMP = "hit";
    private static final String DEVI_UBIC = "devi_ubic";
    private static final String ASIG_ID = "asig_id";
    private static final String ASIG_VENC = "asig_venc";

    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private FirebaseUser userAuth;
    private Button btnRegistrarIngreso;
    private TextView textViewUbicacion;
    private Uri photoURI;
    private String currentPhotoPath;
    private ImageView imageViewCamara;
    private ImageView imageViewDownload;
    private ArrayList<String> nombrePuestos;
    private ArrayList<PuestoDM> listaDePuestos;

    private Boolean puestoSeleccionado;
    private ProgressDialog progressDialog=null;

    private String idCliente;
    private String idObjetivo;
    private Spinner spinnerPuesto;

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
        setContentView(R.layout.activity_ingreso);

        Toolbar toolbar = findViewById(R.id.toolbarIngreso);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        synchronizeClock();

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        idCliente = prefs.getString(ID_CLIENTE,"");
        idObjetivo = prefs.getString(ID_OBJETIVO,"");
        spinnerPuesto = findViewById(R.id.spinnerPuesto);

        nombrePuestos = new ArrayList<>();
        listaDePuestos = new ArrayList<>();
        puestoSeleccionado = false;

        database = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        textViewUbicacion = findViewById(R.id.textViewUbicacion);
        btnRegistrarIngreso = findViewById(R.id.buttonRegistrarIngreso);
        imageViewCamara = findViewById(R.id.imageViewCamara);
        imageViewCamara.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);

        btnRegistrarIngreso.setClickable(false);
        btnRegistrarIngreso.setAlpha(0.5f);
        btnRegistrarIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ubicacion = (String) textViewUbicacion.getText();
                if(ubicacion.equals("Dentro del Rango")  || ubicacion.equals("Desactivada")){
                    if (puestoSeleccionado) {
                        dispatchTakePictureIntent();
                        btnRegistrarIngreso.setClickable(false);
                        btnRegistrarIngreso.setAlpha(0.5f);
                    } else {
                        Toast.makeText(IngresoActivity.this, "Debe seleccionar un Puesto", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(IngresoActivity.this, "Por favor verifique que este dentro del rango de ubicacion", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        chequearEstadoSesionOnResume();
    }

    @Override
    public void finish(Date date) {
        cargarPuestos(date);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteCache(this);
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
                                            //Toast.makeText(IngresoActivity.this, "No se detecto un rostro en la registracion. Por favor vuelva a intentarlo", Toast.LENGTH_SHORT).show();
                                            showFaceDetectionError();
                                            btnRegistrarIngreso.setAlpha(1.0f);
                                            btnRegistrarIngreso.setClickable(true);
                                        }else if(faces.size() > 1){
                                            //Toast.makeText(IngresoActivity.this, "Se detecto mas de un rostro en la registracion. Por favor vuelva a intentarlo", Toast.LENGTH_SHORT).show();
                                            showFacesDetectionError();
                                            btnRegistrarIngreso.setAlpha(1.0f);
                                            btnRegistrarIngreso.setClickable(true);
                                        }else{
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                Bitmap bitmap = croppedFace(bounds,input);
                                                FaceClassifier.Recognition recognitionVerification = faceClassifier.recognizeImage(bitmap,false);
                                                if(recognitionVerification.getDistance()<=0.85){
                                                    //registrarIngreso();
                                                    registrarIngresoCompleto();
                                                    //Toast.makeText(IngresoActivity.this, "Distancia del modelo = "+recognitionVerification.getDistance(), Toast.LENGTH_SHORT).show();
                                                }else{
                                                    //Toast.makeText(IngresoActivity.this, "No se pudo reconocer el rostro. Por favor vuelva a intentarlo ", Toast.LENGTH_SHORT).show();
                                                    showFaceRecognitionError();
                                                    btnRegistrarIngreso.setAlpha(1.0f);
                                                    btnRegistrarIngreso.setClickable(true);
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

        String[] split = new String[0];

        if (coordenadas != null && coordenadas.contains(",") && coordenadas.length()>=3 ) {
            split = coordenadas.split(",");
            branchRadio = prefs.getInt(MAP_RADIO, 0);
            // Si la lat y lon son numericos continua
            if (isNumeric(split[0]) && isNumeric(split[1])){
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
                        } else {
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

            }else{
                textViewUbicacion.setText("Coordenadas Objetivo Incorrectas");
                textViewUbicacion.setTextColor(Color.RED);
            }

        }else{
            textViewUbicacion.setText("Coordenadas Objetivo Incorrectas");
            textViewUbicacion.setTextColor(Color.RED);
        }
    }

    public static boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

    private void registrarIngresoCompleto() {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = Configurador.API_PATH + "registro_completo/" + Configurador.ID_EMPRESA;
            JSONObject jsonBody = new JSONObject();

            // Datos para asigvigi
            jsonBody.put("asig_obje", prefs.getInt(ASIG_OBJE, 0));
            jsonBody.put("asig_vigi", prefs.getString(PERS_CODI, ""));
            jsonBody.put("asig_fech", prefs.getString(ASIG_FECH, ""));
            jsonBody.put("asig_dhor", prefs.getString(HORA_INGRESO_PARAM, ""));
            jsonBody.put("asig_visa", prefs.getInt(ASIG_VISA, 0));
            jsonBody.put("asig_usua", prefs.getInt(ASIG_USUA, 0));
            jsonBody.put("asig_time", prefs.getString(HORA_INGRESO_TIMESTAMP, ""));
            jsonBody.put("asig_pues", prefs.getInt(ASIG_PUES, 0));
            jsonBody.put("asig_bloq", prefs.getInt(ASIG_BLOQ, 0));
            jsonBody.put("asig_facm", prefs.getInt(ASIG_FACM, 0));
            jsonBody.put("asig_venc", prefs.getString(ASIG_VENC, ""));
            jsonBody.put("asig_empr", Configurador.ID_EMPRESA);

            // Datos para last_session
            jsonBody.put("last_cper", prefs.getString(PERS_CODI, ""));
            jsonBody.put("last_ccli", prefs.getInt(ASIG_OBJE, 0));
            jsonBody.put("last_cobj", prefs.getString(ID_OBJETIVO, ""));
            jsonBody.put("last_fech", prefs.getString(ASIG_FECH, ""));
            jsonBody.put("last_dhor", prefs.getString(ASIG_DHOR, ""));
            jsonBody.put("last_hhor", prefs.getString(ASIG_HHOR, ""));
            jsonBody.put("last_usua", prefs.getInt(ASIG_USUA, 0));
            jsonBody.put("last_time", prefs.getString(HORA_INGRESO_TIMESTAMP, ""));
            jsonBody.put("last_pues", prefs.getInt(ASIG_PUES, 0));
            jsonBody.put("last_npue", prefs.getString(NOMBRE_PUESTO, ""));
            jsonBody.put("last_ncli", prefs.getString(NOMBRE_CLIENTE, ""));
            jsonBody.put("last_nobj", prefs.getString(NOMBRE_OBJETIVO, ""));
            jsonBody.put("last_dhre", prefs.getString(HORA_INGRESO, ""));
            jsonBody.put("idEmpresa", Configurador.ID_EMPRESA);

            final String requestBody = jsonBody.toString();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, URL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    String asigId = response.getString("asigId");
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(ASIG_ID, asigId);
                                    editor.putBoolean(ESTADO_SESION, true);
                                    editor.apply();
                                    chequearEstadoSesion();
                                    showRegisterAlert();
                                } else {
                                    showRegistroIngresoError();
                                    Toast.makeText(IngresoActivity.this,
                                            "Error al cargar ingreso: " + response.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                showRegistroIngresoError();
                                Toast.makeText(IngresoActivity.this,
                                        "Error al procesar la respuesta",
                                        Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showRegistroIngresoError();
                            Toast.makeText(IngresoActivity.this,
                                    "Error de conexión con el servidor",
                                    Toast.LENGTH_SHORT).show();
                            Log.e("API_ERROR", "Error: " + error.toString());
                        }
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                                requestBody, "utf-8");
                        return null;
                    }
                }
            };

            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            showRegistroIngresoError();
            Toast.makeText(IngresoActivity.this,
                    "Error al preparar los datos",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void chequearEstadoSesion() {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        boolean estadoSesion = prefs.getBoolean(ESTADO_SESION,false);
                    if (estadoSesion) {
                        btnRegistrarIngreso.setAlpha(0.5f);
                        btnRegistrarIngreso.setClickable(false);
                        cargarDatosPantallaIngreso(estadoSesion);
                    } else {
                        progressDialog.show();
                        progressDialog.setContentView(R.layout.custom_progressdialog);
                        initTrueTime();
                        btnRegistrarIngreso.setAlpha(1.0f);
                        btnRegistrarIngreso.setClickable(true);
                    }
    }

    private void chequearEstadoSesionOnResume() {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        Boolean estadoSesion = prefs.getBoolean(ESTADO_SESION,false);
        if (estadoSesion) {
            btnRegistrarIngreso.setAlpha(0.5f);
            btnRegistrarIngreso.setClickable(false);
        } else {
            btnRegistrarIngreso.setAlpha(1.0f);
            btnRegistrarIngreso.setClickable(true);
        }
    }

    private void cargarDatosPantallaIngreso(Boolean estadoSesion){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        String cliente = prefs.getString(NOMBRE_CLIENTE,"").toUpperCase();
        String objetivo = prefs.getString(NOMBRE_OBJETIVO,"").toUpperCase();
        String nombre = prefs.getString(NOMBRE_PERSONAL,"").toUpperCase();
        String nombrePuesto = prefs.getString(NOMBRE_PUESTO,"");
        String horaIngreso = prefs.getString(HORA_INGRESO,"");
        String ingresoPuesto = prefs.getString(INGRESO_PUESTO,"");
        String egresoPuesto = prefs.getString(EGRESO_PUESTO,"");
        String fechaIngreso = prefs.getString(FECHA_INGRESO,"");
        String fechaPuesto = prefs.getString(FECHA_PUESTO,"");

        TextView nombrePersonal = findViewById(R.id.textViewName);
        TextView nombreObjetivo = findViewById(R.id.textViewObjetive);
        TextView puestoSeleccionado = findViewById(R.id.puestoSeleccionado);
        TextView textViewHorario = findViewById(R.id.textViewHorario);
        TextView textViewStatus = findViewById(R.id.textViewStatus);
        TextView textViewHoraRegistrada = findViewById(R.id.textViewHoraRegistrada);
        TextView textViewHoraIngreso = findViewById(R.id.textViewHoraIngreso);
        Spinner spinnerPuesto = findViewById(R.id.spinnerPuesto);

        nombrePersonal.setText(nombre);
        nombreObjetivo.setText(cliente+" - "+objetivo);

        if(estadoSesion){
            String horario = ingresoPuesto+" a "+egresoPuesto;
            puestoSeleccionado.setText(nombrePuesto);
            puestoSeleccionado.setVisibility(View.VISIBLE);
            textViewHorario.setText(horario);
            textViewHorario.setVisibility(View.VISIBLE);
            spinnerPuesto.setVisibility(View.GONE);
            textViewStatus.setText("Registrado en el Objetivo");
            textViewStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.holo_green_light));
            textViewHoraRegistrada.setText(HoraRegistrada.ingresoParametrizado(ingresoPuesto,fechaPuesto,horaIngreso,fechaIngreso));
            textViewHoraIngreso.setText(horaIngreso);
        } else {
            textViewStatus.setText("No registrado en el Objetivo");
            textViewStatus.setTextColor(Color.RED);
            if(listaDePuestos.size()==1 || listaDePuestos.size()==0){
                puestoSeleccionado.setText("No Disponibles");
                puestoSeleccionado.setVisibility(View.VISIBLE);
                spinnerPuesto.setVisibility(View.GONE);
            }else{
                spinnerPuesto.setVisibility(View.VISIBLE);
                PuestoAdapter puestoAdapter = new PuestoAdapter(this,listaDePuestos);
                spinnerPuesto.setAdapter(puestoAdapter);
                spinnerPuesto.setOnItemSelectedListener(this);
            }
            progressDialog.dismiss();
        }
    }

    public boolean addPuestos(PuestoDM nuevoPuesto, Date fechaHoraIngreso){
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        Date ingresoPuesto = null;
        Date egresoPuesto = null;

        String fechaIngreso = dayFormat.format(fechaHoraIngreso);

        try {
            ingresoPuesto = dateFormat.parse(nuevoPuesto.getPUES_FECH()+" "+nuevoPuesto.getPUES_DHOR());
            egresoPuesto = dateFormat.parse(nuevoPuesto.getPUES_FECH()+" "+nuevoPuesto.getPUES_HHOR());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(esTurnoNoche(nuevoPuesto.getPUES_DHOR(),nuevoPuesto.getPUES_HHOR())){
            egresoPuesto = new Date(egresoPuesto.getTime() + (1000 * 60 * 60 * 24));
            //Si el Ingreso Real es mayor al Ingreso Puesto y los dias son iguales (Ingreso y Puesto)
            if(fechaHoraIngreso.getTime() > ingresoPuesto.getTime()-60*60*1000 && nuevoPuesto.getPUES_FECH().equals(fechaIngreso)) {
                listaDePuestos.add(nuevoPuesto);
                return true;
            }
            // Si el Ingreso Real es menor al Egreso Puesto y los dias son distintos (Ingreso y Puesto)
            else if (fechaHoraIngreso.getTime() < egresoPuesto.getTime() && !nuevoPuesto.getPUES_FECH().equals(fechaIngreso)){
                listaDePuestos.add(nuevoPuesto);
                return true;
            }
        } else if (fechaHoraIngreso.getTime() > ingresoPuesto.getTime()-60*60*1000  && fechaHoraIngreso.getTime() < egresoPuesto.getTime()) {
            listaDePuestos.add(nuevoPuesto);
            return true;
        }

        return false;

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if(i>0){
            // On selecting a spinner item
            PuestoDM puesto = listaDePuestos.get(i);

            SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putInt(ASIG_OBJE, puesto.getPUES_OBJE());
            editor.putString(ASIG_FECH, puesto.getPUES_FECH());
            editor.putString(ASIG_DHOR, puesto.getPUES_DHOR());
            editor.putString(ASIG_HHOR, puesto.getPUES_HHOR());
            editor.putInt(ASIG_VISA, 0);
            editor.putInt(ASIG_USUA, 999);
            editor.putInt(ASIG_PUES, puesto.getPUES_CODI());
            editor.putInt(ASIG_BLOQ, 0);
            editor.putInt(ASIG_ESTA, 0);
            editor.putInt(ASIG_FACM, 0);
            editor.putString(NOMBRE_PUESTO, puesto.getPUES_NOMB());
            editor.putString(FECHA_PUESTO, puesto.getPUES_FECH());
            editor.putString(INGRESO_PUESTO, puesto.getPUES_DHOR());
            editor.putString(EGRESO_PUESTO, puesto.getPUES_HHOR());
            editor.putBoolean(TURNO_NOCHE, esTurnoNoche(puesto.getPUES_DHOR(),puesto.getPUES_HHOR()));
            editor.apply();
            puestoSeleccionado = true;
        } else {
            puestoSeleccionado = false;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        String imageFileName = prefs.getString(NRO_LEGAJO,"")+"_INGRESO.jpg";
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

    public void showRegisterAlert(){
        RegisterAlert myAlert = new RegisterAlert();
        myAlert.setTipoRegistro("ingreso");
        myAlert.show(getSupportFragmentManager(),"Register Alert");
    }

    public void showRegistroIngresoError(){
        RegistroIngresoError myAlert = new RegistroIngresoError();
        myAlert.show(getSupportFragmentManager(),"Registro Ingreso Error");
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
                .apply(requestOptions)
                .into(new BaseTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageViewCamara.setImageDrawable(resource);
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        faceVerification(bitmap);
                        //registrarIngreso();
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

    public void initTrueTime() {
        if (isNetworkConnected()) {
            if (!TrueTime.isInitialized()) {
                TrueTimeAsyncTask trueTime = new TrueTimeAsyncTask(this,this);
                trueTime.execute();
            } else {
                Date date = TrueTime.now(); // Obtengo la hora desde Internet
                cargarPuestos(date);
            }
        } else{
            cargarDatosPantallaIngreso(false);
            disableButtonRegistrarIngreso();
            Toast.makeText(this, "No esta conectado a Internet", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    private void setFechaHoraIngresoSharedPreferences(Date fecha){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String fechaPuesto = prefs.getString(FECHA_PUESTO,"");
        String ingresoPuesto = prefs.getString(INGRESO_PUESTO,"");

        String fechaIngreso = dateFormat.format(fecha);
        String horaIngreso = hourFormat.format(fecha);
        String horaIngresoTimestamp = timestampFormat.format(fecha);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FECHA_INGRESO,fechaIngreso);
        editor.putString(HORA_INGRESO,horaIngreso);
        editor.putString(HORA_INGRESO_PARAM, HoraRegistrada.ingresoParametrizado(ingresoPuesto,fechaPuesto,horaIngreso,fechaIngreso));
        editor.putString(HORA_INGRESO_TIMESTAMP,horaIngresoTimestamp);
        editor.apply();

    }

    private void setVencimientoPuesto(Date fecha){

        Date fechaVence = new Date(fecha.getTime()+60*60*1000);

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String vencimientoPuesto = timestampFormat.format(fechaVence);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ASIG_VENC,vencimientoPuesto);
        editor.apply();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            if(sesionVigente()){
                cargarImagen();
            }else{
                showSesionVencidaAlert();
                initTrueTime();
            }
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

    private boolean sesionVigente(){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        boolean turnoNoche = prefs.getBoolean(TURNO_NOCHE,false);
        String fechaPuesto = prefs.getString(ASIG_FECH,"");
        String egresoPuesto = prefs.getString(ASIG_HHOR,"");

        Date fechaVence;

        if(turnoNoche){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date hoy = null;
            try {
                hoy = dateFormat.parse(fechaPuesto);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date diaPosterior = new Date(hoy.getTime()+86400000);
            fechaVence = armarDate(dateFormat.format(diaPosterior),egresoPuesto);
        }else{
            fechaVence = armarDate(fechaPuesto,egresoPuesto);
        }

        Configurador miConf = Configurador.getInstance();
        miConf.setFinSesion(fechaVence);

        setVencimientoPuesto(fechaVence);

        return initTrueTimeVigente(fechaVence);

    }

    public boolean initTrueTimeVigente(Date fechaVence) {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (isNetworkConnected()) {
            if (!TrueTime.isInitialized()) {
                TrueTimeAsyncTask trueTime = new TrueTimeAsyncTask(this,this);
                trueTime.execute();
            } else {
                Date fechaAhora = TrueTime.now();
                if (comparaFechas(fechaAhora,fechaVence)==2){
                    setFechaHoraIngresoSharedPreferences(fechaAhora);
                    return true;
                } else {
                    //Sesion Vencida
                    editor.putBoolean(ESTADO_SESION,false);
                    editor.apply();
                    return false;
                }
            }
        } else{
            Toast.makeText(this, "No esta conectado a Internet", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public int comparaFechas(Date date1, Date date2){
        if(date1.getTime()>date2.getTime()){
            return 1; // Parametro 1 mas grande que parametro 2
        }else if(date1.getTime()<date2.getTime()){
            return 2; // Parametro 2 mas grande que parametro 1
        } else {
            return 0; // Iguales
        }
    }

    public void showSesionVencidaAlert(){
        PuestoVencidoAlert myAlert = new PuestoVencidoAlert();
        myAlert.show(getSupportFragmentManager(),"Puesto Vencido Alert");
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

    public void disableButtonRegistrarIngreso(){
        btnRegistrarIngreso.setEnabled(false);
    }

    public void buscarPuestos(Date ingresoDate, boolean feriadoCobertura, ArrayList<Integer> diasSemanaCobertura, boolean hoyFeriado, boolean ayerFeriado, JSONArray puestosServer){

        if (puestosServer != null) {

            listaDePuestos.clear();

            PuestoDM puestoInicial = new PuestoDM();
            puestoInicial.setPUES_NOMB("Seleccione un Puesto ...");
            listaDePuestos.add(puestoInicial);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Date ayer = new Date(ingresoDate.getTime()-86400000);

            String diaSemanaHoy = campoSemana(ingresoDate);
            String diaSemanaAyer = campoSemana(ayer);

            for (int i = 0; i < puestosServer.length(); i++) {

                try {

                    JSONObject jsonObject = puestosServer.getJSONObject(i);

                    boolean puestoFeriado = esPuestoFeriado(jsonObject);
                    boolean puestoTurnoNoche = esTurnoNoche(jsonObject.getString("PUES_DHOR"), jsonObject.getString("PUES_HHOR"));
                    boolean puestoAgregado = false;

                    PuestoDM puesto = new PuestoDM();
                    puesto.setPUES_CODI(jsonObject.getInt("PUES_CODI"));
                    puesto.setPUES_NOMB(jsonObject.getString("PUES_NOMB"));
                    puesto.setPUES_DHOR(jsonObject.getString("PUES_DHOR"));
                    puesto.setPUES_HHOR(jsonObject.getString("PUES_HHOR"));
                    puesto.setPUES_OBJE(jsonObject.getInt("PUES_OBJE"));

                    JSONObject dayHoyJSONObject = jsonObject.getJSONObject(diaSemanaHoy);
                    JSONObject dayAyerJSONObject = jsonObject.getJSONObject(diaSemanaAyer);

                    JSONArray dataHoy = dayHoyJSONObject.getJSONArray("data");
                    JSONArray dataAyer = dayAyerJSONObject.getJSONArray("data");

                    if (!feriadoCobertura) {
                        if (dataHoy.getInt(0) == 1 && !puestoAgregado) {
                            puesto.setPUES_FECH(dateFormat.format(ingresoDate));
                            puestoAgregado = addPuestos(puesto, ingresoDate);
                        }
                        if (dataAyer.getInt(0) == 1 && puestoTurnoNoche && !puestoAgregado) {
                            puesto.setPUES_FECH(dateFormat.format(ayer));
                            puestoAgregado = addPuestos(puesto, ingresoDate);
                        }
                    }else{
                        if (hoyFeriado && puestoFeriado && diaCobertura(diasSemanaCobertura,ingresoDate) && !puestoAgregado){
                            puesto.setPUES_FECH(dateFormat.format(ingresoDate));
                            puestoAgregado = addPuestos(puesto, ingresoDate);
                        }else if(dataHoy.getInt(0) == 1 && !hoyFeriado){
                            puesto.setPUES_FECH(dateFormat.format(ingresoDate));
                            puestoAgregado = addPuestos(puesto, ingresoDate);
                        }
                        if(ayerFeriado && puestoFeriado && diaCobertura(diasSemanaCobertura,ayer) && puestoTurnoNoche && !puestoAgregado){
                            puesto.setPUES_FECH(dateFormat.format(ayer));
                            puestoAgregado = addPuestos(puesto, ingresoDate);
                        }else if(dataAyer.getInt(0) == 1 && !ayerFeriado && puestoTurnoNoche && !puestoAgregado){
                            puesto.setPUES_FECH(dateFormat.format(ayer));
                            puestoAgregado = addPuestos(puesto, ingresoDate);
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(IngresoActivity.this, "No se encontraron puestos", Toast.LENGTH_SHORT).show();
                }
            }
            cargarDatosPantallaIngreso(false);
        }else{
            cargarDatosPantallaIngreso(false);
            Toast.makeText(IngresoActivity.this, "No se encontraron puestos", Toast.LENGTH_SHORT).show();
        }

    }


    private Boolean diaCobertura(ArrayList<Integer> diasSemanaCobertura, Date date){
        if(diasSemanaCobertura.get(date.getDay())==1){
            return true;
        }else{
            return false;
        }
    }

    private void esFeriado(Date ingresoDate, boolean feriadoCobertura, ArrayList<Integer> diasSemanaCobertura, JSONArray puestosServer){
        // Funcion que inicializa las variables "hoyFeriado" y "ayerFeriado" segun tabla de feriados
        Date ayer = new Date(ingresoDate.getTime()-86400000);

        ArrayList<String> feriados = new ArrayList<String>();

        RequestQueue requestQueue = Volley.newRequestQueue(IngresoActivity.this);
        String url = Configurador.API_PATH + "feriados";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                boolean hoyFeriado = false;
                boolean ayerFeriado = false;
                if (response != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            feriados.add(jsonObject.getInt("FERI_DIA")+"-"+jsonObject.getInt("FERI_MES"));
                        } catch (JSONException e) {
                            buscarPuestos(ingresoDate, feriadoCobertura,diasSemanaCobertura,hoyFeriado, ayerFeriado, puestosServer);
                        }
                    }

                    int mesHoy = ingresoDate.getMonth()+1;
                    String stHoy = ingresoDate.getDate()+"-"+mesHoy;

                    int mesAyer = ayer.getMonth()+1;
                    String stAyer = ayer.getDate()+"-"+mesAyer;

                    hoyFeriado = feriados.contains(stHoy);
                    ayerFeriado = feriados.contains(stAyer);

                    buscarPuestos(ingresoDate, feriadoCobertura,diasSemanaCobertura,hoyFeriado, ayerFeriado, puestosServer);

                }else{
                    buscarPuestos(ingresoDate, feriadoCobertura,diasSemanaCobertura,hoyFeriado, ayerFeriado, puestosServer);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cargarDatosPantallaIngreso(false);
                Toast.makeText(IngresoActivity.this, "Error de conexion con el Servidor", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    private void cargarPuestos(Date ingresoDate){
        // Funcion que indica si existe en la cobertura la carga de feriados
        // Genera un array de dias de cobertura de la semana
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String idClienteLocal = prefs.getString(ID_CLIENTE,"");
        String idObjetivoLocal = prefs.getString(ID_OBJETIVO,"");

        RequestQueue requestQueue = Volley.newRequestQueue(IngresoActivity.this);
        String url = Configurador.API_PATH + "puestos/" + idClienteLocal + "/" + idObjetivoLocal;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray puestosServer) {
                ArrayList<Integer> diasSemanaCobertura = new ArrayList<Integer>();
                // Inicializa la lista con 7 "1"(unos), si no quiero contemplar dias de cobertura
                // y dejo sin efecto el recorrido de los puestos
                for (int i = 0; i < 7; i++) {
                    diasSemanaCobertura.add(1);
                }
                boolean feriadoCobertura = false; // Existe feriado en la cobertura ?
                if (puestosServer != null) {
                    for (int i = 0; i < puestosServer.length(); i++) {
                        try {
                            JSONObject jsonObject = puestosServer.getJSONObject(i);

                            //VERIFICACION DE COBERTURA ANULADA

//                            JSONArray domingo = jsonObject.getJSONObject("PUES_DOMI").getJSONArray("data");
//                            if(domingo.getInt(0) == 1){
//                                diasSemanaCobertura.set(0,1); // Domingo
//                            }
//                            JSONArray lunes = jsonObject.getJSONObject("PUES_LUNE").getJSONArray("data");
//                            if(lunes.getInt(0 ) == 1){
//                                diasSemanaCobertura.set(1,1); // Lunes
//                            }
//                            JSONArray martes = jsonObject.getJSONObject("PUES_MART").getJSONArray("data");
//                            if (martes.getInt(0) == 1) {
//                                diasSemanaCobertura.set(2, 1); // Martes
//                            }
//                            JSONArray miercoles = jsonObject.getJSONObject("PUES_MIER").getJSONArray("data");
//                            if (miercoles.getInt(0) == 1) {
//                                diasSemanaCobertura.set(3, 1); // Miércoles
//                            }
//                            JSONArray jueves = jsonObject.getJSONObject("PUES_JUEV").getJSONArray("data");
//                            if (jueves.getInt(0) == 1) {
//                                diasSemanaCobertura.set(4, 1); // Jueves
//                            }
//                            JSONArray viernes = jsonObject.getJSONObject("PUES_VIER").getJSONArray("data");
//                            if (viernes.getInt(0) == 1) {
//                                diasSemanaCobertura.set(5, 1); // Viernes
//                            }
//                            JSONArray sabado = jsonObject.getJSONObject("PUES_SABA").getJSONArray("data");
//                            if (sabado.getInt(0) == 1) {
//                                diasSemanaCobertura.set(6, 1); // Sábado
//                            }

                            if(esPuestoFeriado(jsonObject)){
                                feriadoCobertura = true;
                            }

                        } catch (JSONException e) {
                            //cargarDatosPantallaIngreso(false);
                        }
                    }

                    esFeriado(ingresoDate,feriadoCobertura,diasSemanaCobertura,puestosServer);
                }else{
                    cargarDatosPantallaIngreso(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cargarDatosPantallaIngreso(false);
                Toast.makeText(IngresoActivity.this, "Error de conexion con el Servidor", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonArrayRequest);

    }

    private boolean esPuestoFeriado(JSONObject jsonObject){
        try {
            JSONObject dayHoyJSONObject = jsonObject.getJSONObject("PUES_FERI");
            JSONArray dataHoy = dayHoyJSONObject.getJSONArray("data");
            if(dataHoy.getInt(0) == 1){
                return true;
            }else{
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String campoSemana(Date date){
        ArrayList<String> dias = new ArrayList<>();
        dias.add("PUES_DOMI");
        dias.add("PUES_LUNE");
        dias.add("PUES_MART");
        dias.add("PUES_MIER");
        dias.add("PUES_JUEV");
        dias.add("PUES_VIER");
        dias.add("PUES_SABA");
        int diaSemana = date.getDay();
        return dias.get(diaSemana);
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

}

