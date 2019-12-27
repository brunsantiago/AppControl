package com.example.sata.myapplication2;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NovedadActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String INGRESO_PUESTO = "ingresoPuesto" ;
    private static final String EGRESO_PUESTO = "egresoPuesto";
    private static final String NOMBRE = "nombre";
    private static final String NOMBRE_CORTO = "nombreCorto";
    private static final String CANT_HORAS = "cantidadHoras";

    private static final String IMEI = "imei";
    private static final String CLIENTE = "cliente";
    private static final String OBJETIVO = "objetivo";
    private static final String SHARED_PREFERENCES_NAME = "MisPreferencias";
    private static final String TURNO_NOCHE = "turnoNoche";
    private static final String NUMERO_DIA ="numeroDia";
    private static final String TOTAL_HORAS = "totalHoras";
    private static final String NOMBRE_PUESTO = "nombrePuesto";
    private static final String ESTADO_SESION = "estadoSesion";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;
    private static final int REQUEST_CODE_CHOOSE = 300;
    public static final int REQUEST_CODE = 100;
    private static final String NRO_LEGAJO = "nroLegajo";
    private String imei;
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;

    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private ArrayList<String> listaAsuntos;
    private boolean asuntoSeleccionado;
    private String asunto;

    private static final int PERMISSION_TO_SELECT_IMAGE_FROM_GALLERY = 200;
    private static final int PICK_IMAGE_MULTIPLE = 100;

    TextView textViewCantidad;
    TextView textViewArchivosCargados;
    EditText editTextDescripcion;
    ImageView imageViewSelected;
    List<Uri> mSelected;
    private ProgressDialog progressDialog=null;
    Spinner spinnerAsunto;
    Matisse matisse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novedad);

        Button btnBack = findViewById(R.id.buttonBack);

        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        listaAsuntos = new ArrayList<>();
        asunto="";
        mSelected = new ArrayList<>();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        progressDialog= new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);

        cargarListaAsuntos();
        cargarDatosPantalla();
        synchronizeClock();
        checkPermissionREAD_EXTERNAL_STORAGE(this);
    }

    private void cargarListaAsuntos(){
        listaAsuntos.add("Seleccionar Asunto ...");
        listaAsuntos.add("Robo");
        listaAsuntos.add("Hurto");
        listaAsuntos.add("Novedad");
        listaAsuntos.add("Denuncia");
        listaAsuntos.add("Cobertura");
        listaAsuntos.add("Inspeccion");
        listaAsuntos.add("Otro");
    }

    private void cargarDatosPantalla(){

        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        String cliente = prefs.getString(CLIENTE,"").toUpperCase();
        String objetivo = prefs.getString(OBJETIVO,"").toUpperCase();
        String nombre = prefs.getString(NOMBRE,"").toUpperCase();
        boolean sesionVigente = prefs.getBoolean(ESTADO_SESION,false);

        TextView nombrePersonal = findViewById(R.id.textViewName);
        TextView nombreObjetivo = findViewById(R.id.textViewObjetive);
        TextView estadoSesion = findViewById(R.id.textViewStatus);
        //TextView archivosCargados = findViewById(R.id.textViewArchivosCargados);
        //TextView cantidadArchivos = findViewById(R.id.textViewCantidad);
        ImageView imageViewCamera = findViewById(R.id.imageViewCamera);
        Button buttonEnviarNovedad = findViewById(R.id.buttonEnviarNovedad);
        spinnerAsunto = findViewById(R.id.spinnerAsunto);
        textViewArchivosCargados = findViewById(R.id.textViewArchivosCargados);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        editTextDescripcion.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextDescripcion.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editTextDescripcion.setText("");
        imageViewSelected = findViewById(R.id.imageViewSelected);

        nombrePersonal.setText(nombre);
        nombreObjetivo.setText(cliente+" - "+objetivo);

        spinnerAsunto.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_asunto, listaAsuntos);
        spinnerAsunto.setAdapter(adapter);

        if(sesionVigente){
            estadoSesion.setText("Registrado en el Objetivo");
            estadoSesion.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.holo_green_light));
        } else {
            estadoSesion.setText("No registrado en el Objetivo");
            estadoSesion.setTextColor(Color.RED);
        }

        imageViewCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Matisse.from(NovedadActivity.this)
                        .choose(MimeType.ofImage(), true)
                        .showSingleMediaType(true)
                        .countable(true)
                        .maxSelectable(5)
                        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(0.85f)
                        .imageEngine(new PicassoEngine())
                        .theme(R.style.Matisse_Dracula)
                        .forResult(REQUEST_CODE_CHOOSE);

            }
        });

        buttonEnviarNovedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(asuntoSeleccionado){
                    if(editTextDescripcion.getText().length()>0){
                        progressDialog.show();
                        progressDialog.setContentView(R.layout.custom_progressdialog);
                        enviarNovedad();
                    }else{
                        Toast.makeText(NovedadActivity.this, "Debe describir los detalles", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(NovedadActivity.this, "Debe seleccionar un Asunto", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        textViewCantidad.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mSelected.size()>0){
//                    Intent intent = new Intent(NovedadActivity.this,PreviewActivity.class);
//                    intent.putParcelableArrayListExtra("uriList", (ArrayList<? extends Parcelable>) mSelected);
//                    startActivityForResult(intent,REQUEST_CODE);
//                } else{
//                    Toast.makeText(NovedadActivity.this, "No tiene imagenes seleccionadas", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        textViewArchivosCargados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelected.size()>0){
                    Intent intent = new Intent(NovedadActivity.this,PreviewActivity.class);
                    intent.putParcelableArrayListExtra("uriList", (ArrayList<? extends Parcelable>) mSelected);
                    startActivityForResult(intent,REQUEST_CODE);
                } else{
                    Toast.makeText(NovedadActivity.this, "No tiene imagenes seleccionadas", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enviarNovedad(){
        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);

        Map<String, Object> data = new HashMap<>();
        data.put("idCliente", prefs.getString(CLIENTE,""));
        data.put("idObjetivo", prefs.getString(OBJETIVO,""));
        data.put("nroLegajo", prefs.getString(NRO_LEGAJO,""));
        data.put("timestamp", FieldValue.serverTimestamp());
        data.put("asunto", asunto);
        data.put("descripcion", editTextDescripcion.getText().toString());

        database.collection("news")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if(mSelected!=null && mSelected.size()>0) {
                            cargarImagen(mSelected.size()-1, documentReference.getId());
                        } else{
                            progressDialog.dismiss();
                            Toast.makeText(NovedadActivity.this, "Novedad enviada con exito", Toast.LENGTH_SHORT).show();
                            mSelected.clear();
                            textoCargarArchivos();
                            spinnerAsunto.setSelection(0);
                            editTextDescripcion.setText("");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                    }
                });
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position>0){
            // On selecting a spinner item
            asunto = listaAsuntos.get(position);
            asuntoSeleccionado = true;
        } else {
            asuntoSeleccionado = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

//    public void showDialog(Activity activity, String msg){
//        final Dialog dialog = new Dialog(activity);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.custom_alert_take_photo);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//        TextView text = dialog.findViewById(R.id.textViewMessage);
//        text.setText(msg);
//
//        ImageView dialogBtn_camera =  dialog.findViewById(R.id.imageViewCamera);
//        dialogBtn_camera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                }
//                dialog.dismiss();
//            }
//        });
//
//        ImageView dialogBtn_gallery = dialog.findViewById(R.id.imageViewGallery);
//        dialogBtn_gallery.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
////                }
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null && requestCode == REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                mSelected = data.getParcelableArrayListExtra("mensaje");
                textoCargarArchivos();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
        try {
            // When an Image is picked
            if (data != null && requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
                // Get the Image from data
                mSelected = Matisse.obtainResult(data);
            } else {
                //Toast.makeText(this, "Imagenes no seleccionadas", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        if (mSelected.size()>0) {
            textoCargarArchivos();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void textoCargarArchivos(){
        if(mSelected.size()>0){
//            textViewCantidad.setText(Integer.toString(mSelected.size()));
//            textViewCantidad.setTextColor(getResources().getColor(R.color.colorNaranja));
//            textViewCantidad.setAlpha(0.8F);
            String archivosCargados = "Archivos Cargados: " + Integer.toString(mSelected.size());
            textViewArchivosCargados.setText(archivosCargados);
            textViewArchivosCargados.setTextColor((getResources().getColor(R.color.colorNaranja)));
            textViewArchivosCargados.setAlpha(0.8F);
        } else {
            mSelected.clear();
//            textViewCantidad.setText("0");
//            textViewCantidad.setTextColor(getResources().getColor(R.color.colorBlanco));
//            textViewCantidad.setAlpha(0.5F);
            String archivosCargados = "Archivos Cargados: 0";
            textViewArchivosCargados.setText(archivosCargados);
            textViewArchivosCargados.setTextColor((getResources().getColor(R.color.colorBlanco)));
            textViewArchivosCargados.setAlpha(0.5F);
        }
    }

    public void cargarImagen(int posSelected, String novedadId){

        if(posSelected>=0){
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                    .skipMemoryCache(true);

            Glide.with(getApplicationContext())
                    .load(mSelected.get(posSelected))
                    .apply(requestOptions)
                    .into(new BaseTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            imageViewSelected.setImageDrawable(resource);
                            subirArchivoImageView("NOVEDADES/",posSelected,novedadId);
                        }
                        @Override
                        public void getSize(@NonNull SizeReadyCallback cb) {
                            cb.onSizeReady(1080, 1080);
                        }
                        @Override
                        public void removeCallback(@NonNull SizeReadyCallback cb) {
                        }
                    })
            ;
        }
        else {
            progressDialog.dismiss();
            Toast.makeText(this, "Novedad enviada con exito", Toast.LENGTH_SHORT).show();
            mSelected.clear();
            textoCargarArchivos();
            spinnerAsunto.setSelection(0);
            editTextDescripcion.setText("");
        }
    }

    public void subirArchivoImageView(String path,int posSelected, String novedadId){
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        StorageReference photoRef = storageRef.child(path+"/"+novedadId+"/"+mSelected.get(posSelected).getLastPathSegment()+".jpg");
        // Get the data from an ImageView as bytes
        imageViewSelected.setDrawingCacheEnabled(true);
        imageViewSelected.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageViewSelected.getDrawable()).getBitmap();
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
                cargarImagen(posSelected-1,novedadId);
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

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(NovedadActivity.this, "Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }


}
