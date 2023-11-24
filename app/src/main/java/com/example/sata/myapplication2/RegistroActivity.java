package com.example.sata.myapplication2;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.sata.myapplication2.AlertDialog.RegisterAlert;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
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

    private static final String NRO_LEGAJO = "nl";
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
    private boolean faceDetection;

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
        faceDetection=false;

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

        btnTakePhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
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
        //clearFormRegister();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            cargarImagen();
        }
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
                        imageViewPhoto.setImageDrawable(resource);
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        faceVerification(bitmap);
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

    private void uploadProfilePhoto(Bitmap bitmap){
        String path = "USERS/PROFILE_PHOTO/"+editTextNroLegajo.getText();
        StorageReference storageRef = storage.getReference();
        StorageReference photoRef = storageRef.child(path+"/"+photoURI.getLastPathSegment());
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
                                        // ...

                                    }
                                });

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
                            imageViewPhoto.setDrawingCacheEnabled(true);
                            imageViewPhoto.buildDrawingCache();
                            Bitmap bitmap = ((BitmapDrawable) imageViewPhoto.getDrawable()).getBitmap();
                            uploadProfilePhoto(bitmap);
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

        if(editTextClave.getText().length()<4){
            Toast.makeText(this, "Por favor verifique que la clave ingresada sea igual o mayor a cuatro caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!faceDetection){
            Toast.makeText(this, "Por favor verifique que la foto este bien tomada", Toast.LENGTH_SHORT).show();
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = editTextNroLegajo.getText()+"_profile_photo.jpg";
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
