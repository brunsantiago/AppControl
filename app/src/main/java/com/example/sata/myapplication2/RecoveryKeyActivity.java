package com.example.sata.myapplication2;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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
import com.example.sata.myapplication2.AlertDialog.FaceRecognitionError;
import com.example.sata.myapplication2.AlertDialog.RegisterAlert;
import com.example.sata.myapplication2.FaceRecognition.FaceClassifier;
import com.example.sata.myapplication2.FaceRecognition.TFLiteFaceRecognition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RecoveryKeyActivity extends AppCompatActivity {

    private ImageView imageViewPhoto;
    private TextView textViewVolverLogin;
    private EditText editTextNroLegajo;
    private EditText editTextDni;
    private EditText editTextFechaNac;
    private EditText editTextClave;
    private EditText editTextReingreseClave;
    private ImageButton btnTakePhoto;
    private Button btnCambiarClave;

    private Calendar calendar;

    private ProgressDialog progressDialog = null;

    private FirebaseStorage storage;
    private String profilePhotoPath;

    private Uri photoURI;
    private String currentPhotoPath;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private boolean faceDetection;
    private FaceDetector detector;
    private FaceDetectorOptions highAccuracyOpts;
    private FaceClassifier faceClassifier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_key);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(RecoveryKeyActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        editTextNroLegajo = findViewById(R.id.editTextNroLegajo);
        editTextDni = findViewById(R.id.editTextDni);
        editTextFechaNac = findViewById(R.id.editTextFechaNac);
        editTextClave = findViewById(R.id.editTextClave);
        editTextReingreseClave = findViewById(R.id.editTextReingreseClave);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnCambiarClave = findViewById(R.id.btnCambiarClave);
        textViewVolverLogin = findViewById(R.id.volverLogin);


        storage = FirebaseStorage.getInstance();
        highAccuracyOpts = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
        try {
            faceClassifier = TFLiteFaceRecognition.create(getAssets(),"facenet.tflite",160,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        faceDetection=false;

        progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setCancelable(false);

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
                new DatePickerDialog(RecoveryKeyActivity.this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        editTextDni.addTextChangedListener(new NumberTextWatcherForThousand(editTextDni));

        btnTakePhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnCambiarClave.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(RecoveryKeyActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        clearFormRecoveryKey();

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

    private void faceVerification(Bitmap input){

        InputImage image = InputImage.fromBitmap(input, 0);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        if(faces.size() == 0){
                                            Toast.makeText(RecoveryKeyActivity.this, "No se detectaron caras en la foto tomada", Toast.LENGTH_SHORT).show();
                                            faceDetection=false;
                                        }else if(faces.size() > 1){
                                            Toast.makeText(RecoveryKeyActivity.this, "Se detecto mas de una cara en la foto tomada", Toast.LENGTH_SHORT).show();
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
                                        downloadProfilePhoto(nroLegajo,persCodi,clave);
                                    }else{
                                        progressDialog.dismiss();
                                    }
                                }else{
                                    Toast.makeText(RecoveryKeyActivity.this, "Usuario no habilitado", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(RecoveryKeyActivity.this, "Error de Servidor", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }else{
                            Toast.makeText(RecoveryKeyActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(RecoveryKeyActivity.this, "Error de Servidor", Toast.LENGTH_SHORT).show();
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

    private void recoveryKey(String persCodi, String clave) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = Configurador.API_PATH + "recovery_key";
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("user_codi", persCodi);
            jsonBody.put("user_pass", clave);
            final String requestBody = jsonBody.toString();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getInt("result")==1){
                            showRecoveryKeyAlert();
                            progressDialog.dismiss();
                        }else{
                            Toast.makeText(RecoveryKeyActivity.this, "Usuario no registrado", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(RecoveryKeyActivity.this, "No se pudo modificar la clave, por favor contactese con la Central de Operaciones", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(RecoveryKeyActivity.this, "No se pudo modificar la clave, por favor contactese con la Central de Operaciones", Toast.LENGTH_SHORT).show();
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

    private void clearFormRecoveryKey(){
        editTextNroLegajo.setText("");
        editTextDni.setText("");
        editTextFechaNac.setText("");
        editTextClave.setText("");
        editTextReingreseClave.setText("");
    }

    public void showRecoveryKeyAlert(){
        RegisterAlert myAlert = new RegisterAlert();
        myAlert.setMensaje("CAMBIO DE CLAVE EXITOSO");
        myAlert.setTipoRegistro("registro");
        myAlert.show(getSupportFragmentManager(),"Register Alert");
    }

    private void downloadProfilePhoto(String nroLegajo, String persCodi,String clave){

        String fileName = nroLegajo+"_profile_photo.jpg";

        StorageReference photoRef = storage.getReference()
                .child("USERS")
                .child("PROFILE_PHOTO")
                .child(nroLegajo)
                .child(fileName);

        photoRef.getBytes(600*600)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        saveToInternalStorage(bitmap,persCodi,clave);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showFaceRecognitionError();
                        progressDialog.dismiss();
                    }
                });
    }

    private void saveToInternalStorage(Bitmap bitmapImage,String persCodi,String clave){

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory,"profile.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        profilePhotoPath = directory.getAbsolutePath();
        faceRegistration(persCodi,clave);
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

    private Bitmap loadImageFromStorage(){
        Bitmap bitmap = null;
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

    private void faceRegistration(String persCodi,String clave){

        String name = "profilePhoto";
        Bitmap input = loadImageFromStorage();
        InputImage image = InputImage.fromBitmap(input, 0);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {

                                        if(faces.size() == 0){
                                            //Log.d(TAG, "No se detectaron caras en la registracion");
                                        }else if(faces.size() > 1){
                                            //Log.d(TAG, "Se detecto mas de una cara en la registracion");
                                        }else{
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                Bitmap bitmap = croppedFace(bounds,input);
                                                FaceClassifier.Recognition recognitionRegistration = faceClassifier.recognizeImage(bitmap,true);
                                                faceClassifier.register(name,recognitionRegistration);
                                            }
                                            faceRecognition(persCodi,clave);
                                        }

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        showFaceRecognitionError();
                                        progressDialog.dismiss();
                                    }
                                });


    }

    private void faceRecognition(String persCodi,String clave){
        Bitmap input = ((BitmapDrawable) imageViewPhoto.getDrawable()).getBitmap();
        InputImage image = InputImage.fromBitmap(input, 0);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        if(faces.size() == 0){
                                            //Toast.makeText(IngresoActivity.this, "No se detecto un rostro en la registracion. Por favor vuelva a intentarlo", Toast.LENGTH_SHORT).show();
                                            //showFaceDetectionError();
                                        }else if(faces.size() > 1){
                                            //Toast.makeText(IngresoActivity.this, "Se detecto mas de un rostro en la registracion. Por favor vuelva a intentarlo", Toast.LENGTH_SHORT).show();
                                            //showFacesDetectionError();
                                        }else{
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                Bitmap bitmap = croppedFace(bounds,input);
                                                FaceClassifier.Recognition recognitionVerification = faceClassifier.recognizeImage(bitmap,false);
                                                if(recognitionVerification.getDistance()<=0.85){
                                                    recoveryKey(persCodi,clave);
                                                    //Toast.makeText(IngresoActivity.this, "Distancia del modelo = "+recognitionVerification.getDistance(), Toast.LENGTH_SHORT).show();
                                                }else{
                                                    showFaceRecognitionError();
                                                    progressDialog.dismiss();
                                                    //Toast.makeText(RecoveryKeyActivity.this, "Distancia del modelo = "+recognitionVerification.getDistance(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        showFaceRecognitionError();
                                        progressDialog.dismiss();
                                    }
                                });

    }

    public void showFaceRecognitionError(){
        FaceRecognitionError myAlert = new FaceRecognitionError();
        myAlert.show(getSupportFragmentManager(),"Face Recognition Error");
    }

}
