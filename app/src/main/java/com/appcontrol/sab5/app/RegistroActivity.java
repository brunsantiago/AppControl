package com.appcontrol.sab5.app;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.appcontrol.sab5.app.AlertDialog.RegisterAlert;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RegistroActivity extends AppCompatActivity {

    private static final String TAG = "RegistroActivity";
    
    private TextView textViewVolverLogin;
    private EditText editTextNroLegajo;
    private EditText editTextDni;
    private EditText editTextFechaNac;
    private EditText editTextClave;
    private EditText editTextReingreseClave;
    private ImageButton btnTakePhoto;
    private Button btnRegistrar;

    private Calendar calendar;
    private ProgressDialog progressDialog = null;
    private DatePickerDialog datePickerDialog;
    private ImageView imageViewPhoto;

    private Uri photoURI;
    private String currentPhotoPath;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private FirebaseStorage storage;
    private FaceDetector detector;
    private FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build();
    
    // CAMBIO: Usar enum para mejor control del estado
    private enum PhotoState {
        NO_PHOTO,
        PROCESSING,
        READY,
        ERROR
    }
    private PhotoState currentPhotoState = PhotoState.NO_PHOTO;
    
    // ELIMINADO: boolean faceDetection; - Reemplazado por PhotoState

    // Interfaces para callbacks
    interface FaceDetectionListener {
        void onDetectionComplete(boolean success);
        void onDetectionError(String error);
    }
    
    interface UploadCompleteListener {
        void onUploadSuccess(String downloadUrl);
        void onUploadFailed(String error);
    }

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
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        textViewVolverLogin = findViewById(R.id.volverLogin);

        storage = FirebaseStorage.getInstance();
        imageViewPhoto = findViewById(R.id.imageView3);
        detector = FaceDetection.getClient(highAccuracyOpts);
        // ELIMINADO: faceDetection=false; - Ya no es necesario

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

        // CAMBIO: Validar número de legajo antes de tomar foto
        btnTakePhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Validar que el número de legajo esté ingresado
                if(editTextNroLegajo.getText().toString().trim().isEmpty()) {
                    Toast.makeText(RegistroActivity.this, 
                        "Por favor ingrese el número de legajo antes de tomar la foto", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                dispatchTakePictureIntent();
            }
        });

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

        clearFormRegister();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // CAMBIO: Deshabilitar botón mientras se procesa
            btnRegistrar.setEnabled(false);
            btnRegistrar.setText("Procesando foto...");
            cargarImagen();
        }
    }

    // CAMBIO: Método completamente reescrito para manejar callbacks
    public void cargarImagen(){
        // Mostrar indicador de carga
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog);
        currentPhotoState = PhotoState.PROCESSING;
        
        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true);

        Glide.with(getApplicationContext())
                .load(currentPhotoPath)
                .apply(requestOptions)
                .into(new BaseTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageViewPhoto.setImageDrawable(resource);
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        
                        // Ahora esperamos el resultado
                        faceVerification(bitmap, new FaceDetectionListener() {
                            @Override
                            public void onDetectionComplete(boolean success) {
                                progressDialog.dismiss();
                                currentPhotoState = success ? PhotoState.READY : PhotoState.ERROR;
                                
                                // Re-habilitar el botón
                                btnRegistrar.setEnabled(true);
                                btnRegistrar.setText("Registrar");
                                
                                if (success) {
                                    Toast.makeText(RegistroActivity.this, 
                                        "Foto validada correctamente", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            }
                            
                            @Override
                            public void onDetectionError(String error) {
                                progressDialog.dismiss();
                                currentPhotoState = PhotoState.ERROR;
                                
                                // Re-habilitar el botón
                                btnRegistrar.setEnabled(true);
                                btnRegistrar.setText("Registrar");
                                
                                Toast.makeText(RegistroActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    
                    @Override
                    public void getSize(@NonNull SizeReadyCallback cb) {
                        cb.onSizeReady(200, 200);
                    }
                    
                    @Override
                    public void removeCallback(@NonNull SizeReadyCallback cb) {}
                });
    }

    // NUEVO: Método mejorado para obtener bitmap de manera segura
    private Bitmap getBitmapFromImageView(ImageView imageView) {
        try {
            // Método más confiable para obtener el bitmap
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    return bitmap;
                }
            }
            
            // Método alternativo si el anterior falla
            int width = imageView.getWidth();
            int height = imageView.getHeight();
            if (width <= 0 || height <= 0) {
                // Si la vista no tiene dimensiones, usar valores por defecto
                width = 500;
                height = 500;
            }
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            imageView.draw(canvas);
            return bitmap;
            
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener bitmap: " + e.getMessage());
            return null;
        }
    }

    // NUEVO: Método para sanitizar el número de legajo
    private String sanitizeLegajo(String legajo) {
        if (legajo == null || legajo.trim().isEmpty()) {
            return null;
        }
        
        // Eliminar espacios al inicio y final
        legajo = legajo.trim();
        
        // Reemplazar caracteres no válidos para Firebase Storage
        legajo = legajo.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Asegurar que no empiece con punto
        if (legajo.startsWith(".")) {
            legajo = "_" + legajo.substring(1);
        }
        
        return legajo;
    }

    // NUEVO: Verificar conectividad
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // CAMBIO: Método completamente reescrito con mejor manejo de errores
    private void uploadProfilePhoto(final Bitmap bitmap, final String nroLegajo, 
                                   final UploadCompleteListener listener){
        
        // Verificar conexión
        if (!isNetworkAvailable()) {
            listener.onUploadFailed("No hay conexión a internet");
            return;
        }
        
        // Validar entrada
        if (bitmap == null) {
            Log.e(TAG, "Bitmap es null");
            listener.onUploadFailed("Error: Imagen no válida");
            return;
        }
        
        String legajoSanitizado = sanitizeLegajo(nroLegajo);
        if (legajoSanitizado == null) {
            Log.e(TAG, "Número de legajo inválido: " + nroLegajo);
            listener.onUploadFailed("Error: Número de legajo inválido");
            return;
        }
        
        try {
            // Comprimir imagen para reducir tamaño
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Comprimir con calidad del 80% para reducir tamaño
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            
            // Log del tamaño
            Log.d(TAG, "Tamaño de imagen a subir: " + (data.length / 1024) + " KB");
            
            // Verificar que no exceda límites razonables (ej: 10MB)
            if (data.length > 10 * 1024 * 1024) {
                listener.onUploadFailed("Error: Imagen demasiado grande");
                return;
            }
            
            // Construir path
            String path = "USERS/PROFILE_PHOTO/" + legajoSanitizado;
            String fileName = legajoSanitizado + "_profile_photo.jpg";
            
            Log.d(TAG, "Subiendo a path: " + path + "/" + fileName);
            
            // Referencia a Storage
            StorageReference storageRef = storage.getReference();
            StorageReference photoRef = storageRef.child(path + "/" + fileName);
            
            // Metadata opcional
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .setCustomMetadata("legajo", legajoSanitizado)
                    .setCustomMetadata("uploadDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
                                       Locale.getDefault()).format(new Date()))
                    .build();
            
            // Upload con metadata
            UploadTask uploadTask = photoRef.putBytes(data, metadata);
            
            // Listeners de progreso
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / 
                                     snapshot.getTotalByteCount();
                    Log.d(TAG, "Progreso upload: " + progress + "%");
                }
            });
            
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Upload exitoso");
                    
                    // Obtener URL de descarga si es necesario
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            Log.d(TAG, "URL de descarga: " + downloadUrl);
                            listener.onUploadSuccess(downloadUrl);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Si falla obtener la URL pero el upload fue exitoso
                            listener.onUploadSuccess("");
                        }
                    });
                }
            });
            
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "Error en upload: " + exception.getMessage());
                    listener.onUploadFailed("Error al subir foto: " + exception.getMessage());
                }
            });
            
            // Manejar cancelación
            uploadTask.addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.e(TAG, "Upload cancelado");
                    listener.onUploadFailed("Upload cancelado");
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Excepción en upload: " + e.getMessage());
            e.printStackTrace();
            listener.onUploadFailed("Error inesperado: " + e.getMessage());
        }
    }
    
    /* ELIMINADO: Método antiguo sin callback
    private void uploadProfilePhoto(Bitmap bitmap){
        String path = "USERS/PROFILE_PHOTO/"+editTextNroLegajo.getText();
        StorageReference storageRef = storage.getReference();
        StorageReference photoRef = storageRef.child(path+"/"+editTextNroLegajo.getText()+"_profile_photo.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(RegistroActivity.this, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
    }*/

    // CAMBIO: Método modificado para usar callback
    private void faceVerification(Bitmap input, final FaceDetectionListener listener){
        InputImage image = InputImage.fromBitmap(input, 0);
        
        Task<List<Face>> result = detector.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        if (faces.size() == 0) {
                            listener.onDetectionError("No se detectaron caras en la foto tomada");
                            listener.onDetectionComplete(false);
                        } else if (faces.size() > 1) {
                            listener.onDetectionError("Se detectó más de una cara en la foto tomada");
                            listener.onDetectionComplete(false);
                        } else {
                            // Éxito: exactamente una cara detectada
                            listener.onDetectionComplete(true);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error en detección facial: " + e.getMessage());
                        listener.onDetectionError("Error al procesar la imagen: " + e.getMessage());
                        listener.onDetectionComplete(false);
                    }
                });
    }
    
    /* ELIMINADO: Método antiguo sin callback
    private void faceVerification(Bitmap input){
        InputImage image = InputImage.fromBitmap(input, 0);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        if(faces.size() == 0){
                                            Toast.makeText(RegistroActivity.this, "No se detectaron caras en la foto tomada", Toast.LENGTH_SHORT).show();
                                            faceDetection=false;
                                        }else if(faces.size() > 1){
                                            Toast.makeText(RegistroActivity.this, "Se detecto mas de una cara en la foto tomada", Toast.LENGTH_SHORT).show();
                                            faceDetection=false;
                                        }else{
                                            faceDetection=true;
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                    }
                                });
    }*/

    // NUEVO: Método para eliminar foto huérfana de Firebase Storage
    private void deleteOrphanPhoto(String nroLegajo) {
        String legajoSanitizado = sanitizeLegajo(nroLegajo);
        if (legajoSanitizado == null) {
            Log.e(TAG, "No se puede eliminar foto: número de legajo inválido");
            return;
        }
        
        String path = "USERS/PROFILE_PHOTO/" + legajoSanitizado;
        String fileName = legajoSanitizado + "_profile_photo.jpg";
        
        Log.d(TAG, "Eliminando foto huérfana: " + path + "/" + fileName);
        
        StorageReference storageRef = storage.getReference();
        StorageReference photoRef = storageRef.child(path + "/" + fileName);
        
        // Eliminar la foto
        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Foto huérfana eliminada exitosamente");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Error al eliminar foto huérfana: " + exception.getMessage());
                // No mostramos error al usuario ya que es una operación de limpieza
            }
        });
    }

    // NUEVO: Método que primero sube la foto y luego registra al usuario
    private void uploadPhotoThenRegister(final String persCodi, final String nroLegajo, final String clave) {
        // Obtener bitmap de manera segura
        Bitmap bitmap = getBitmapFromImageView(imageViewPhoto);
        
        if (bitmap == null) {
            progressDialog.dismiss();
            Toast.makeText(RegistroActivity.this, 
                "Error: No se pudo procesar la imagen de perfil", 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        // Variable para trackear si la foto se subió
        final boolean[] photoUploaded = {false};
        
        // Primero subir la foto
        uploadProfilePhoto(bitmap, nroLegajo, new UploadCompleteListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Log.d(TAG, "Foto subida exitosamente, procediendo con el registro");
                photoUploaded[0] = true;
                // Solo si la foto se subió correctamente, registrar al usuario
                registrarUsuario(persCodi, nroLegajo, clave, photoUploaded[0]);
            }
            
            @Override
            public void onUploadFailed(String error) {
                progressDialog.dismiss();
                // NO registrar al usuario si la foto falló
                new AlertDialog.Builder(RegistroActivity.this)
                    .setTitle("Error en el registro")
                    .setMessage("No se pudo completar el registro debido a un error " +
                              "al subir la foto de perfil:\n\n" + error + 
                              "\n\nPor favor, intente nuevamente.")
                    .setPositiveButton("Aceptar", null)
                    .show();
            }
        });
    }

    // CAMBIO: Método modificado para manejar limpieza de fotos huérfanas
    private void registrarUsuario(String persCodi, String nroLegajo, String clave, final boolean photoWasUploaded) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = Configurador.API_PATH + "register/"+Configurador.ID_EMPRESA;
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
                            // La foto ya se subió exitosamente, mostrar éxito completo
                            progressDialog.dismiss();
                            showRegisterAlert();
                            clearFormRegister();
                            // Resetear estado de la foto
                            currentPhotoState = PhotoState.NO_PHOTO;
                            Log.d(TAG, "Registro completado exitosamente");
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(RegistroActivity.this, "Usuario ya registrado", Toast.LENGTH_SHORT).show();
                            
                            // LIMPIEZA: Si la foto se subió pero el usuario ya existe, eliminar la foto
                            if (photoWasUploaded) {
                                Log.d(TAG, "Usuario ya existe, eliminando foto subida");
                                deleteOrphanPhoto(nroLegajo);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(RegistroActivity.this, "Error al procesar respuesta del servidor", Toast.LENGTH_SHORT).show();
                        
                        // LIMPIEZA: Si hubo error procesando la respuesta, eliminar foto
                        if (photoWasUploaded) {
                            Log.d(TAG, "Error en respuesta del servidor, eliminando foto subida");
                            deleteOrphanPhoto(nroLegajo);
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    
                    // Determinar el tipo de error
                    String errorMessage = "Error al registrar usuario";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 409) {
                            errorMessage = "El usuario ya existe en el sistema";
                        } else if (statusCode == 400) {
                            errorMessage = "Datos de registro inválidos";
                        } else if (statusCode >= 500) {
                            errorMessage = "Error en el servidor. Por favor intente más tarde";
                        }
                    } else if (error.getMessage() != null) {
                        if (error.getMessage().contains("NoConnectionError")) {
                            errorMessage = "Error de conexión. Verifique su internet";
                        }
                    }
                    
                    Toast.makeText(RegistroActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    
                    // LIMPIEZA: Si el registro falló pero la foto se subió, eliminarla
                    if (photoWasUploaded) {
                        Log.d(TAG, "Registro falló, eliminando foto subida. Error: " + error.toString());
                        deleteOrphanPhoto(nroLegajo);
                    }
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
            progressDialog.dismiss();
            Toast.makeText(RegistroActivity.this, "Error al preparar datos de registro", Toast.LENGTH_SHORT).show();
            
            // LIMPIEZA: Si hubo error preparando los datos, eliminar foto
            if (photoWasUploaded) {
                Log.d(TAG, "Error preparando datos, eliminando foto subida");
                deleteOrphanPhoto(nroLegajo);
            }
        }
    }

    private void verificarPersonal(String nroLegajo){
        progressDialog.show();
        progressDialog.setContentView(R.layout.custom_progressdialog);

        String clave = editTextClave.getText().toString();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String mJSONURLString = Configurador.API_PATH + "personal/"+nroLegajo+"/"+Configurador.ID_EMPRESA;
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
                                        // CAMBIO: Primero subir la foto, luego registrar
                                        uploadPhotoThenRegister(persCodi, nroLegajo, clave);
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

    // CAMBIO: Método modificado para validar estado de la foto
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

        if(editTextClave.getText().length()<4){
            Toast.makeText(this, "Por favor verifique que la clave ingresada sea igual o mayor a cuatro caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        // CAMBIO: Verificar el estado de la foto usando el enum
        switch (currentPhotoState) {
            case NO_PHOTO:
                Toast.makeText(this, "Por favor tome una foto de perfil", Toast.LENGTH_SHORT).show();
                return false;
                
            case PROCESSING:
                Toast.makeText(this, "Por favor espere mientras se procesa la foto", Toast.LENGTH_SHORT).show();
                return false;
                
            case ERROR:
                Toast.makeText(this, "Por favor tome otra foto. La actual no es válida", Toast.LENGTH_SHORT).show();
                return false;
                
            case READY:
                // La foto está lista y validada
                break;
        }
        
        /* ELIMINADO: Validación antigua
        if(!faceDetection){
            Toast.makeText(this, "Por favor verifique que la foto este bien tomada", Toast.LENGTH_SHORT).show();
            return false;
        }*/

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
        // CAMBIO: También limpiar la imagen y resetear el estado
        imageViewPhoto.setImageResource(0); // Limpiar la imagen
        currentPhotoState = PhotoState.NO_PHOTO;
    }

    // CAMBIO: Método mejorado para manejar el nombre del archivo
    private File createImageFile() throws IOException {
        String nroLegajo = editTextNroLegajo.getText().toString().trim();
        String imageFileName;
        
        if(nroLegajo.isEmpty()) {
            // Usar timestamp temporal si no hay legajo
            imageFileName = "temp_" + System.currentTimeMillis() + "_profile_photo.jpg";
        } else {
            // Sanitizar el número de legajo para el nombre del archivo
            nroLegajo = sanitizeLegajo(nroLegajo);
            imageFileName = nroLegajo + "_profile_photo.jpg";
        }
        
        File storageDir = getApplicationContext().getCacheDir();
        File imageFile = new File(storageDir, imageFileName);
        
        if (imageFile.exists()) {
            imageFile.delete();
        }
        imageFile.createNewFile();
        
        currentPhotoPath = imageFile.getAbsolutePath();
        Log.d(TAG, "Archivo de imagen creado: " + currentPhotoPath);
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
                Log.e(TAG, "Error creando archivo de imagen: " + ex.getMessage());
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