package com.example.sata.myapplication2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sata.myapplication2.AlertDialog.ExitAlert;
import com.example.sata.myapplication2.AlertDialog.LogOutAlert;
import com.example.sata.myapplication2.POJO.UltimaSesionDM;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//Se referencian las Clases necesarias para la conexion con el Servidor MySQL
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String ESTADO_SESION = "es";
    private static final String NRO_LEGAJO = "nl";
    //private static final String PROFILE_PHOTO = "ProfilePhotoPath";
    private AppBarConfiguration mAppBarConfiguration;
    //private FirebaseStorage storage;

    //private ProgressDialog progressDialog;

    private InAppUpdate inAppUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        //storage = FirebaseStorage.getInstance();

        //progressDialog = new ProgressDialog(this);
        //progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //progressDialog.setCancelable(false);

        //progressDialog.show();
        //progressDialog.setContentView(R.layout.custom_progressdialog);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_config, R.id.nav_exit)
                .setDrawerLayout(drawer)
                .build();


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.getMenu().getItem(0).setChecked(true);

        Menu menu = navigationView.getMenu();
        MenuItem titleNav = menu.findItem(R.id.titleNav);
        MenuItem titleDisp = menu.findItem(R.id.titleDisp);
        SpannableString sTitleNav = new SpannableString(titleNav.getTitle());
        SpannableString sTitleDisp = new SpannableString(titleDisp.getTitle());
        sTitleNav.setSpan(new TextAppearanceSpan(this, R.style.NavigationDrawerTitle), 0, sTitleNav.length(), 0);
        sTitleDisp.setSpan(new TextAppearanceSpan(this, R.style.NavigationDrawerTitle), 0, sTitleDisp.length(), 0);
        titleNav.setTitle(sTitleNav);
        titleDisp.setTitle(sTitleDisp);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_exit) {
                    SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                    boolean estadoSesion = prefs.getBoolean(ESTADO_SESION,false);
                    if(estadoSesion){
                        showLogOutAlert();
                    } else {
                        showExitAlert();
                    }
                } else {
                    NavigationUI.onNavDestinationSelected(item, navController);
                    drawer.closeDrawers();
                }
                return false;
            }
        });

        inAppUpdate = new InAppUpdate(MainActivity.this);
        inAppUpdate.checkForAppUpdate();

        //downloadProfilePhoto();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inAppUpdate.onActivityResult(requestCode, resultCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inAppUpdate.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inAppUpdate.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void cambiarItemMenu(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    public void showLogOutAlert(){
        LogOutAlert myAlert = new LogOutAlert();
        myAlert.show(getSupportFragmentManager(),"Log Out Alert");
    }

    public void showExitAlert(){
        ExitAlert myAlert = new ExitAlert();
        myAlert.show(getSupportFragmentManager(),"Exit Alert");
    }

    public void setNavigationHeaderData(String nombre,String objetivo){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navNombre = (TextView) headerView.findViewById(R.id.textViewNombre);
        navNombre.setText(nombre);
        TextView navObjetivo = (TextView) headerView.findViewById(R.id.textViewObjetivo);
        navObjetivo.setText(objetivo);
        navObjetivo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gris));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

//    private void downloadProfilePhoto(){
//
//        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
//        String nroLegajo = prefs.getString(NRO_LEGAJO,"");
//        String fileName = nroLegajo+"_profile_photo.jpg";
//
//        StorageReference photoRef = storage.getReference()
//                .child("USERS")
//                .child("PROFILE_PHOTO")
//                .child(nroLegajo)
//                .child(fileName);
//        photoRef.getBytes(600*600)
//                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//                        //imageViewDownload.setImageBitmap(bitmap);
//                        saveToInternalStorage(bitmap);
//                        //loadImageFromStorage(path);
//                    }
//                });
//    }

//    private void saveToInternalStorage(Bitmap bitmapImage){
//
//        SharedPreferences prefs = getSharedPreferences("MisPreferencias",MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//
//        ContextWrapper cw = new ContextWrapper(getApplicationContext());
//        // path to /data/data/yourapp/app_data/imageDir
//        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//        // Create imageDir
//        File mypath = new File(directory,"profile.jpg");
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(mypath);
//            // Use the compress method on the BitMap object to write image to the OutputStream
//            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        editor.putString(PROFILE_PHOTO, directory.getAbsolutePath());
//        editor.apply();
//        progressDialog.dismiss();
//        //return directory.getAbsolutePath();
//    }
}