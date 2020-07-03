package com.example.sata.myapplication2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
//import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.Transition;
import com.example.sata.myapplication2.AlertDialog.LoadPhotoAlert;
import com.example.sata.myapplication2.AlertDialog.PuestoVencidoAlert;
import com.example.sata.myapplication2.POJO.Esquema;
import com.example.sata.myapplication2.POJO.HoraRegistrada;
import com.example.sata.myapplication2.POJO.Puesto;
import com.example.sata.myapplication2.POJO.PuestoAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.instacart.library.truetime.TrueTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class IngresoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,ResultListener<Date>{

    private static final String TAG = "Ingreso Activity";
    private static final String FECHA_INGRESO = "fechaIngreso";
    private static final String HORA_INGRESO = "horaIngreso";
    private static final String ID_PERSONAL = "idPersonal";
    private static final String TIPO_CUBRIMIENTO = "tipoCubrimiento";
    private static final String ESTADO_SESION = "estadoSesion";
    private static final String ULTIMA_SESION = "ultimaSesion";
    private static final String NOMBRE_CLIENTE = "nombreCliente";
    private static final String NOMBRE_OBJETIVO = "nombreObjetivo";
    private static final String ID_CLIENTE = "idCliente";
    private static final String ID_OBJETIVO = "idObjetivo";
    private static final String FECHA_EGRESO = "fechaEgreso";
    private static final String HORA_EGRESO = "horaEgreso";
    private static final String INGRESO_PUESTO = "ingresoPuesto" ;
    private static final String EGRESO_PUESTO = "egresoPuesto";
    private static final String NOMBRE_PERSONAL = "nombre";
    private static final String HORAS_TURNO = "horasTurno";
    private static final String FECHA_PUESTO = "fechaPuesto";
    private static final String SESION_ID = "sesionID";
    private static final String NOMBRE_PUESTO = "nombrePuesto";
    private static final String NOMBRE_TURNO = "nombreTurno";
    private static final String TIMESTAMP = "timestamp";
    private static final String TURNO_NOCHE = "turnoNoche";
    private static final String IMAGE_PATH = "imagePath";
    private static final String NRO_LEGAJO = "nroLegajo";
    private static final String PATH_TURNO = "pathTurno";

    private static final int REQUEST_TAKE_PHOTO = 1;

    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private FirebaseUser userAuth;
    private Button btnRegistrarIngreso;
    private Uri photoURI;
    private String currentPhotoPath;
    private ImageView imageViewCamara;
    private ArrayList<String> nombrePuestos;
    private ArrayList<Puesto> listaDePuestos;
    private PuestoAdapter puestoAdapter;
    private Boolean puestoSeleccionado;
    private ProgressDialog progressDialog=null;

    private String idCliente;
    private String idObjetivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso);

        synchronizeClock();

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        idCliente = prefs.getString(ID_CLIENTE,"");
        idObjetivo = prefs.getString(ID_OBJETIVO,"");

        nombrePuestos = new ArrayList<>();
        listaDePuestos = new ArrayList<>();
        puestoSeleccionado = false;

        database = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        btnRegistrarIngreso = findViewById(R.id.buttonRegistrarIngreso);
        Button btnBack = findViewById(R.id.buttonBack);
        imageViewCamara = findViewById(R.id.imageViewCamara);
        imageViewCamara.setVisibility(View.INVISIBLE);

        progressDialog= new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);

        btnRegistrarIngreso.setClickable(false);
        btnRegistrarIngreso.setAlpha(0.5f);
        btnRegistrarIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (puestoSeleccionado) {
                    dispatchTakePictureIntent();
                    btnRegistrarIngreso.setClickable(false);
                    btnRegistrarIngreso.setAlpha(0.5f);
                } else {
                    Toast.makeText(IngresoActivity.this, "Debe seleccionar un Puesto", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        chequearEstadoSesion();

    }

    @Override
    protected void onResume() {
        super.onResume();
        chequearEstadoSesionOnResume();
    }

    public void registrarIngreso() {

            SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

            Map<String, Object> ingreso = new HashMap<>();
            ingreso.put(FECHA_INGRESO, prefs.getString(FECHA_INGRESO,""));
            ingreso.put(HORA_INGRESO, prefs.getString(HORA_INGRESO,""));
            ingreso.put(FECHA_EGRESO, "");
            ingreso.put(HORA_EGRESO, "");
            ingreso.put(ID_PERSONAL, userAuth.getDisplayName());
            ingreso.put(TIPO_CUBRIMIENTO, "titular");
            ingreso.put(NOMBRE_PUESTO, prefs.getString(NOMBRE_PUESTO,""));
            ingreso.put(INGRESO_PUESTO, prefs.getString(INGRESO_PUESTO,""));
            ingreso.put(EGRESO_PUESTO, prefs.getString(EGRESO_PUESTO,""));
            ingreso.put(HORAS_TURNO,prefs.getString(HORAS_TURNO,""));
            ingreso.put(FECHA_PUESTO,prefs.getString(FECHA_PUESTO,""));
            ingreso.put(TIMESTAMP, FieldValue.serverTimestamp());
            ingreso.put(TURNO_NOCHE,prefs.getBoolean(TURNO_NOCHE,false));


        DocumentReference docRef = database.collection("clientes")
                    .document(idCliente)
                    .collection("objetivos")
                    .document(idObjetivo)
                    .collection("cobertura")
                    .document(prefs.getString(FECHA_PUESTO,""));

                    docRef.collection("puestos")
                    .add(ingreso)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String path = prefs.getString(NOMBRE_CLIENTE,"")+"/"+
                                          prefs.getString(NOMBRE_OBJETIVO,"")+"/"+ "CAPTURAS"+"/"+
                                          prefs.getString(FECHA_PUESTO,"")+"/"+ documentReference.getId();

                            documentReference.update(IMAGE_PATH,path+"/");
                            actualizarEstadoPersonal(documentReference);
                            subirArchivoImageView(path);
                            vencimientoSesion(prefs.getBoolean(TURNO_NOCHE,false),prefs.getString(FECHA_PUESTO,""),prefs.getString(EGRESO_PUESTO,""));
                            showRegisterAlert();
                            cargarFecha(docRef,prefs.getString(FECHA_PUESTO,""));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            Toast.makeText(IngresoActivity.this, "Error al cargar ingreso", Toast.LENGTH_SHORT).show();
                        }
                    });

    }

    private void cargarFecha(DocumentReference docRef,String fecha){

        Map<String, Object> docData = new HashMap<>();
        docData.put("fecha", armarDate(fecha,"00:00"));

        docRef.set(docData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    private void actualizarEstadoPersonal(DocumentReference documentReference) {

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SESION_ID,documentReference.getId());
        editor.apply();

        Map<String, Object> ultimaSesion = new HashMap<>();
        ultimaSesion.put(NOMBRE_CLIENTE, prefs.getString(NOMBRE_CLIENTE,""));
        ultimaSesion.put(NOMBRE_OBJETIVO, prefs.getString(NOMBRE_OBJETIVO,""));
        ultimaSesion.put(SESION_ID, documentReference.getId());
        ultimaSesion.put(PATH_TURNO,documentReference.getPath ());

        Query reference = database.collection("users").whereEqualTo("idPersonal", userAuth.getDisplayName());

        reference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                database.collection("users").document(document.getId()).update(
                                        ESTADO_SESION, true,
                                        ULTIMA_SESION, ultimaSesion
                                )
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(IngresoActivity.this, "No se pudo actualizar el estado de la sesion", Toast.LENGTH_SHORT).show();
                                        // Habria que ver cargar el estado de sesion como cerrada
                                    }
                                });

                            }

                        } else {
                            Log.d("TAG", "No such document");
                        }
                    }
                });


    }

    private void chequearEstadoSesion() {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        Boolean estadoSesion = prefs.getBoolean(ESTADO_SESION,false);
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
            spinnerPuesto.setVisibility(View.VISIBLE);
            PuestoAdapter puestoAdapter = new PuestoAdapter(this,listaDePuestos);
            spinnerPuesto.setAdapter(puestoAdapter);
            spinnerPuesto.setOnItemSelectedListener(this);
            progressDialog.dismiss();
        }
    }

    public void buscarEsquema(Date fechaHoraIngreso){

        nombrePuestos = new ArrayList<>();
        listaDePuestos = new ArrayList<>();
        nombrePuestos.add("No Disponible");

        Puesto puestoInicial = new Puesto();
        puestoInicial.setNombrePuesto("Seleccione un Puesto ...");

        listaDePuestos.add(puestoInicial);

        database.collection("clientes")
                .document(idCliente)
                .collection("objetivos")
                .document(idObjetivo)
                .collection("cubrimiento")
                .whereEqualTo("estado","VIGENTE")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean esquemaEncontrado=false;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Esquema esquema = document.toObject(Esquema.class);
                                if(dentroEsquema2(esquema.getFechaDesde(),esquema.getFechaHasta(),fechaHoraIngreso)){
                                    cargarPuestos(fechaHoraIngreso,document.getId());
                                    esquemaEncontrado=true;
                                } else{
                                    cambiarVigencia(document.getId(),"CADUCADO");
                                    if(!esquemaEncontrado){ buscarEsquemaActual(fechaHoraIngreso); }
                                }
                            }
                            if(!esquemaEncontrado){
                                Toast.makeText(IngresoActivity.this, "NO Entro al FOR Se encontro un estado VIGENTE", Toast.LENGTH_SHORT).show();
                                buscarEsquemaActual(fechaHoraIngreso);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void cambiarVigencia(String documentId, String estado){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        database.collection("clientes")
                .document(idCliente)
                .collection("objetivos")
                .document(idObjetivo)
                .collection("cubrimiento")
                .document(documentId)
                .update("estado",estado);
    }

    public void cargarPuestos(Date fechaHoraIngreso, String documentoVigenteID){
    //Carga la lista de puestos a seleccionar por el Usuario
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaIngreso = dateFormat.format(fechaHoraIngreso);

        int diaSemana = fechaHoraIngreso.getDay();


                database.collection("clientes")
                .document(idCliente)
                .collection("objetivos")
                .document(idObjetivo)
                .collection("cubrimiento")
                .document(documentoVigenteID)
                .collection("esquema")
                .whereEqualTo("documentData.numeroDia",diaSemana)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                     public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if( task.getResult().isEmpty()) {

                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.exists()) {
                                        Map<String, Object> datos = document.getData();
                                        //Recorro el Documento campo por campo (Puesto por Puesto)
                                        for (Map.Entry<String, Object> entry : datos.entrySet()) {
                                            String k = entry.getKey();//Nombre del Campo
                                            Object v = entry.getValue();// Contenido del Campo
                                            if (!k.equals("documentData")) {
                                                Puesto nuevoPuesto = new Puesto((Map<String, Object>) v); // Creo el Puesto con los datos obtenidos del Documento
                                                nuevoPuesto.setFechaPuesto(fechaIngreso);
                                                addPuestos(nuevoPuesto, fechaHoraIngreso); // Funcion que agrega el puesto si cumple con las condiciones
                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                        obtenerListaPuestosFiltrados(fechaHoraIngreso,documentoVigenteID);
                    }
                });

    }

    public void addPuestos(Puesto nuevoPuesto,Date fechaHoraIngreso){

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Date ingresoPuesto = null;
        Date egresoPuesto = null;
        Date horaIngreso = null;

        String fechaIngreso = dateFormat.format(fechaHoraIngreso);
        String horaIngresoStr = hourFormat.format(fechaHoraIngreso);

        try {
            ingresoPuesto = hourFormat.parse(nuevoPuesto.getIngresoPuesto());
            egresoPuesto = hourFormat.parse(nuevoPuesto.getEgresoPuesto());
            horaIngreso = hourFormat.parse(horaIngresoStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(nuevoPuesto.getTurnoNoche()!=null && nuevoPuesto.getTurnoNoche()){
            //Si el Ingreso Real es mayor al Ingreso Puesto y los dias son iguales (Ingreso y Puesto)
            if(horaIngreso.getTime() > ingresoPuesto.getTime()-60*60*1000 && nuevoPuesto.getFechaPuesto().equals(fechaIngreso)) {
                listaDePuestos.add(nuevoPuesto);
            }
            // Si el Ingreso Real es menor al ingreso Puesto y los dias son distintos (Ingreso y Puesto)
            else if (horaIngreso.getTime() < egresoPuesto.getTime()&& !(nuevoPuesto.getFechaPuesto().equals(fechaIngreso))){
                listaDePuestos.add(nuevoPuesto);
            }
        } else if (horaIngreso.getTime() < egresoPuesto.getTime() && horaIngreso.getTime() > ingresoPuesto.getTime()-60*60*1000) {
            listaDePuestos.add(nuevoPuesto);
        }
    }

    public void obtenerListaPuestosFiltrados(Date fechaHoraIngreso,String documentoVigenteID) {

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String hoyStr = dateFormat.format(fechaHoraIngreso);
        Date hoy = null;
        try {
            hoy = dateFormat.parse(hoyStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date ayer = new Date(hoy.getTime()-86400000);
        String fechaPuesto = dateFormat.format(ayer);
        int diaSemana = ayer.getDay();

        database.collection("clientes")
                .document(idCliente)
                .collection("objetivos")
                .document(idObjetivo)
                .collection("cubrimiento")
                .document(documentoVigenteID)
                .collection("esquema")
                .whereEqualTo("documentData.numeroDia",diaSemana)
                .whereEqualTo("documentData.turnoNoche",true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if( task.getResult().isEmpty()) {
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.exists()) {
                                        Map<String, Object> datos = document.getData();
                                        //Recorro el Documento campo por campo (Puesto por Puesto)
                                        for (Map.Entry<String, Object> entry : datos.entrySet()) {
                                            String k = entry.getKey();//Nombre del Campo
                                            Object v = entry.getValue();// Contenido del Campo
                                            if(!k.equals("documentData")){
                                                Puesto nuevoPuesto = new Puesto((Map<String, Object>) v); // Creo el Puesto con los datos obtenidos del Documento
                                                //if(nuevoPuesto.getTurnoNoche() && (nuevoPuesto.getTurnoNoche() != null)){
                                                if(nuevoPuesto.getTurnoNoche() != null){
                                                    nuevoPuesto.setFechaPuesto(fechaPuesto);
                                                    nuevoPuesto.setNombrePuesto(nuevoPuesto.getNombrePuesto());
                                                    addPuestos(nuevoPuesto,fechaHoraIngreso); // Funcion que agrega el puesto si cumple con las condiciones
                                                }
                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        for(Puesto unPuesto : listaDePuestos) {
                            if(nombrePuestos.size()==1){
                                nombrePuestos.set(0, "Seleccione un puesto ...");
                            }
                            nombrePuestos.add(unPuesto.getNombrePuesto());
                        }
                        cargarDatosPantallaIngreso(false);
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if(i>0){
            // On selecting a spinner item
            Puesto puesto = listaDePuestos.get(i);

            SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(NOMBRE_PUESTO, puesto.getNombrePuesto());
            editor.putString(NOMBRE_TURNO, puesto.getNombreTurno());
            editor.putString(INGRESO_PUESTO, puesto.getIngresoPuesto());
            editor.putString(EGRESO_PUESTO, puesto.getEgresoPuesto());
            editor.putString(HORAS_TURNO,puesto.getHorasTurno());
            editor.putString(FECHA_PUESTO,puesto.getFechaPuesto());
            if(puesto.getTurnoNoche() != null){
                editor.putBoolean(TURNO_NOCHE,puesto.getTurnoNoche());
            }
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
                        "com.example.android.fileprovider",
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

    public void showRegisterAlert(){
        LoadPhotoAlert myAlert = new LoadPhotoAlert();
        myAlert.setTipoRegistro("ingreso");
        myAlert.show(getSupportFragmentManager(),"Register Alert");
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
                        registrarIngreso();
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

    /**
     * init the TrueTime using a AsyncTask.
     */
    //Carga la fecha y hora desde Internet
    public void initTrueTime() {
        if (isNetworkConnected()) {
            if (!TrueTime.isInitialized()) {
                TrueTimeAsyncTask trueTime = new TrueTimeAsyncTask(this,this);
                trueTime.execute();
            } else {
                Date date = TrueTime.now(); // Obtengo la hora desde Internet
                //setFechaLoginSharedPreferences(date);
                buscarEsquema(date);
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
        //setFechaLoginSharedPreferences(resultado);
        buscarEsquema(resultado);
    }

    private void setFechaLoginSharedPreferences(Date fecha){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String fechaIngreso = dateFormat.format(fecha);
        String horaIngreso = hourFormat.format(fecha);

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FECHA_INGRESO,fechaIngreso);
        editor.putString(HORA_INGRESO,horaIngreso);
        editor.apply();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            if(sesionVigente()){
                SharedPreferences prefs = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(ESTADO_SESION,true);
                editor.apply();
                chequearEstadoSesion();
                cargarImagen();
            }else{
                showSesionVencidaAlert();
                initTrueTime();
            }
        }
    }

    public void vencimientoSesion(Boolean turnoNoche,String fechaPuesto, String egresoPuesto){
        Date fechaVence=null;
        if(turnoNoche!=null && turnoNoche){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date hoy = null;
            try {
                hoy = dateFormat.parse(fechaPuesto);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date diaPosterior = new Date(hoy.getTime()+86400000);
            fechaVence = armarDate(dateFormat.format(diaPosterior),egresoPuesto);
        }else if (turnoNoche!=null && !turnoNoche){
            fechaVence = armarDate(fechaPuesto,egresoPuesto);
        } else {
            Toast.makeText(this, "Falta cargar si es Turno Noche en sesion del servidor", Toast.LENGTH_SHORT).show();
        }

        Configurador miConf = Configurador.getInstance();
        miConf.setFinSesion(fechaVence);

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
        String fechaPuesto = prefs.getString(FECHA_PUESTO,"");
        String egresoPuesto = prefs.getString(EGRESO_PUESTO,"");

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
                    setFechaLoginSharedPreferences(fechaAhora);
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

    private boolean dentroEsquema2(Date fechaDesde,Date fechaHasta,Date fechaHoy){

        if( fechaDesde != null && fechaHasta != null && fechaHoy != null){
            return fechaHoy.getTime() > fechaDesde.getTime() && fechaHoy.getTime() < fechaHasta.getTime();
        } else {
            Toast.makeText(this, "El esquema actual no contiene alguna de sus fechas limites", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    public void buscarEsquemaActual(Date fechaHoraIngreso){

        nombrePuestos = new ArrayList<>();
        listaDePuestos = new ArrayList<>();
        nombrePuestos.add("No Disponible");

        //SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        database.collection("clientes")
                .document(idCliente)
                .collection("objetivos")
                .document(idObjetivo)
                .collection("cubrimiento")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean esquemaEncontrado=false;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Esquema esquema = document.toObject(Esquema.class);
                                if(dentroEsquema2(esquema.getFechaDesde(),esquema.getFechaHasta(),fechaHoraIngreso)){
                                    cargarPuestos(fechaHoraIngreso,document.getId());
                                    cambiarVigencia(document.getId(),"VIGENTE");
                                    esquemaEncontrado=true;
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        if(!esquemaEncontrado){
                            Toast.makeText(IngresoActivity.this, "No hay un esquema disponible", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
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

