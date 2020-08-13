package com.example.sata.myapplication2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.sata.myapplication2.AlertDialog.LoadPhotoAlertError;
import com.example.sata.myapplication2.POJO.HoraRegistrada;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EgresoActivity extends AppCompatActivity implements ResultListener<Date>{

    private static final String HORA_EGRESO = "horaEgreso";
    private static final String ESTADO_SESION = "estadoSesion";
    private static final String ULTIMA_SESION = "ultimaSesion";
    private static final String FECHA_EGRESO = "fechaEgreso";
    private static final String NOMBRE_OBJETIVO = "nombreObjetivo";
    private static final String NOMBRE_CLIENTE = "nombreCliente";
    private static final String ID_CLIENTE = "idCliente";
    private static final String ID_OBJETIVO = "idObjetivo";
    private static final String FECHA_PUESTO = "fechaPuesto";
    private static final String SESION_ID = "sesionID";
    private static final String NOMBRE_PERSONAL = "nombre";
    private static final String NRO_LEGAJO = "nroLegajo";
    private static final String NOMBRE_PUESTO = "nombrePuesto";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String EGRESO_PUESTO = "egresoPuesto";
    private static final String TURNO_NOCHE = "turnoNoche";
    private static final String INGRESO_PUESTO = "ingresoPuesto";
    private static final String IMAGE_PATH = "imagePath";


    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private Button btnRegistrarSalida;
    private TextView estadoDelIngreso;
    private TextView textViewStatus;
    private Uri photoURI;
    private FirebaseUser userAuth;
    private ImageView imageViewCamara;
    private String currentPhotoPath;

    private String idCliente;
    private String idObjetivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egreso);

        synchronizeClock();

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
        idCliente = prefs.getString(ID_CLIENTE,"");
        idObjetivo = prefs.getString(ID_OBJETIVO,"");

        database = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        btnRegistrarSalida = findViewById(R.id.buttonRegistrarEgreso);
        Button btnBack = findViewById(R.id.buttonBack);
        estadoDelIngreso = findViewById(R.id.textViewStatus);
        imageViewCamara = findViewById(R.id.imageViewCamara);
        textViewStatus = findViewById(R.id.textViewStatus);

        btnRegistrarSalida.setClickable(false);
        btnRegistrarSalida.setAlpha(0.5f);
        btnRegistrarSalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    dispatchTakePictureIntent();
                    btnRegistrarSalida.setClickable(false);
                    btnRegistrarSalida.setAlpha(0.5f);
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
        //chequearEstadoSesion();
    }

    private void registrarSalida(String fechaEgreso,String horaEgreso) {

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);

        String path = prefs.getString(NOMBRE_CLIENTE,"")+"/"+
                      prefs.getString(NOMBRE_OBJETIVO,"")+"/"+"CAPTURAS"+"/"+
                      prefs.getString(FECHA_PUESTO,"")+"/"+
                      prefs.getString(SESION_ID,"");

        Map<String, Object> egreso = new HashMap<>();
        egreso.put(FECHA_EGRESO, fechaEgreso);
        egreso.put(HORA_EGRESO, horaEgreso);
        egreso.put(IMAGE_PATH, path+"/");

        DocumentReference reference = database.collection("clientes")
                .document(idCliente)
                .collection("objetivos")
                .document(idObjetivo)
                .collection("cobertura")
                .document(prefs.getString(FECHA_PUESTO,""))
                .collection("puestos")
                .document(prefs.getString(SESION_ID,""));

        reference.update(egreso)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        actualizarEstadoPersonal(fechaEgreso,horaEgreso);
                        servicioFinalizado();
                        subirArchivoImageView(path);
                        showRegisterAlert();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EgresoActivity.this, "No se pudo registrar la salida del servicio, por favor contactese con la Central de Operaciones", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarEstadoPersonal(String fechaEgreso, String horaEgreso) {

        Query reference = database.collection("users").whereEqualTo("idPersonal", userAuth.getDisplayName());

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FECHA_EGRESO, fechaEgreso);
        editor.putString(HORA_EGRESO, horaEgreso);
        editor.apply();

        reference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                database.collection("users").document(document.getId()).update(
                                        ESTADO_SESION, false)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EgresoActivity.this, "No se pudo actualizar el estado de la sesion", Toast.LENGTH_SHORT).show();
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
        String egresoPuesto = prefs.getString(EGRESO_PUESTO,"");
        String fechaEgreso = prefs.getString(FECHA_EGRESO,"");
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
            Toast.makeText(this, "No esta conectado", Toast.LENGTH_SHORT).show();
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
            textViewStatus.setText("Servicio Expirado");
            textViewStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorNaranja));
            showRegisterAlertError();
        } else {
            registrarSalida(fechaEgreso,horaEgreso);
        }
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
        myAlert.setTipoRegistro("salida");
        myAlert.show(getSupportFragmentManager(),"Register Alert");
    }

    public void showRegisterAlertError(){
        LoadPhotoAlertError myAlert = new LoadPhotoAlertError();
        myAlert.show(getSupportFragmentManager(),"Register Alert Error");
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
                        initTrueTime();
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

            SharedPreferences preferences = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ESTADO_SESION,false);
            editor.apply();
            //chequearEstadoSesion();
            cargarImagen();
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