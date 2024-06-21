package com.appcontrol.sab5.app.MainActivityFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appcontrol.sab5.app.AlertDialog.RequestDeviceAlert;
import com.appcontrol.sab5.app.BuildConfig;
import com.appcontrol.sab5.app.Configurador;
import com.appcontrol.sab5.app.POJO.Cliente;
import com.appcontrol.sab5.app.POJO.ClienteAdapter;
import com.appcontrol.sab5.app.POJO.Objetivo;
import com.appcontrol.sab5.app.POJO.ObjetivoAdapter;
import com.appcontrol.sab5.app.POJO.SolicitudDispositivo;
import com.appcontrol.sab5.app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class ConfigFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private static final String NRO_LEGAJO = "nl";
    private static final String NOMBRE_PERSONAL = "an";
    private static final String USER_PROFILE = "user_profile";

    private ArrayList<Cliente> listaDeClientes;
    private ArrayList<Objetivo> listaDeObjetivos;
    private Boolean clienteSeleccionado;
    private Boolean objetivoSeleccionado;
    private FirebaseFirestore database;
    private ClienteAdapter clienteAdapter;
    private ObjetivoAdapter objetivoAdapter;
    private Spinner spinnerCliente;
    private Spinner spinnerObjetivo;
    private EditText editTextCliente;
    private EditText editTextObjetivo;
    private EditText editTextNroLinea;
    private String perfil;
    private SolicitudDispositivo solicitudDispositivo;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                final FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        database = FirebaseFirestore.getInstance();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_config, container, false);
        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

        spinnerCliente = root.findViewById(R.id.spinnerCliente);
        spinnerObjetivo = root.findViewById(R.id.spinnerObjetivo);
        editTextCliente = root.findViewById(R.id.editTextCliente);
        editTextObjetivo = root.findViewById(R.id.editTextObjetivo);
        editTextNroLinea = root.findViewById(R.id.editTextNroLinea);
        Button btnSolicitudDispositivo = root.findViewById(R.id.buttonSolicitudDispositivo);

        synchronizeClock(root);

        listaDeClientes = new ArrayList<Cliente>();
        listaDeObjetivos = new ArrayList<Objetivo>();
        clienteSeleccionado = false;
        objetivoSeleccionado = false;
        solicitudDispositivo = new SolicitudDispositivo();

        perfil = prefs.getString(USER_PROFILE,"");

        if(perfil.equals("admin")){
            spinnerCliente.setVisibility(View.VISIBLE);
            spinnerObjetivo.setVisibility(View.VISIBLE);
            editTextCliente.setVisibility(View.GONE);
            editTextObjetivo.setVisibility(View.GONE);
            getClientes();
        }else{
            spinnerCliente.setVisibility(View.GONE);
            spinnerObjetivo.setVisibility(View.GONE);
            editTextCliente.setVisibility(View.VISIBLE);
            editTextObjetivo.setVisibility(View.VISIBLE);
        }

        clienteAdapter = new ClienteAdapter(getContext(), listaDeClientes);
        spinnerCliente.setAdapter(clienteAdapter);
        spinnerCliente.setOnItemSelectedListener(this);

        Objetivo inicializarObjetivo = new Objetivo();
        inicializarObjetivo.setNombreObjetivo("Esperando un Cliente ...");
        listaDeObjetivos.add(inicializarObjetivo);

        objetivoAdapter = new ObjetivoAdapter(getContext(), listaDeObjetivos);
        spinnerObjetivo.setAdapter(objetivoAdapter);
        spinnerObjetivo.setOnItemSelectedListener(this);

        TextView tMarca = root.findViewById(R.id.textViewMarca);
        tMarca.setText(obtenerMarca());

        TextView tModelo = root.findViewById(R.id.textViewModelo);
        tModelo.setText(obtenerModelo());

        TextView tAndroidId = root.findViewById(R.id.textViewAndroidId);
        tAndroidId.setText(obtenerAndroidId());

        btnSolicitudDispositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

                if(perfil.equals("admin")){
                    if(clienteSeleccionado==false){
                        Toast.makeText(getContext(), "Debe seleccionar un Cliente", Toast.LENGTH_SHORT).show();
                    }else if (objetivoSeleccionado==false){
                        Toast.makeText(getContext(), "Debe seleccionar un Objetivo", Toast.LENGTH_SHORT).show();
                    }else if(String.valueOf(editTextNroLinea.getText()).equals("")){
                        Toast.makeText(getContext(), "Debe ingresar el numero de linea celular", Toast.LENGTH_SHORT).show();
                    }else{
                        solicitudDispositivo.setNombre(prefs.getString(NOMBRE_PERSONAL,""));
                        solicitudDispositivo.setNroLegajo(prefs.getString(NRO_LEGAJO,""));
                        Date date = new Date(System.currentTimeMillis());
                        solicitudDispositivo.setDate(date);
                        solicitudDispositivo.setEstado("pending");
                        solicitudDispositivo.setNroLinea(String.valueOf(editTextNroLinea.getText()));
                        sendRequestDevice();
                    }
                }else{
                    if(String.valueOf(editTextCliente.getText()).equals("")){
                        Toast.makeText(getContext(), "Debe ingresar el nombre del Cliente", Toast.LENGTH_SHORT).show();
                    }else if(String.valueOf(editTextObjetivo.getText()).equals("")){
                        Toast.makeText(getContext(), "Debe ingresar el nombre del Objetivo", Toast.LENGTH_SHORT).show();
                    }else if(String.valueOf(editTextNroLinea.getText()).equals("")){
                        Toast.makeText(getContext(), "Debe ingresar el numero de linea celular", Toast.LENGTH_SHORT).show();
                    }else{
                        solicitudDispositivo.setNombre(prefs.getString(NOMBRE_PERSONAL,""));
                        solicitudDispositivo.setNroLegajo(prefs.getString(NRO_LEGAJO,""));
                        Date date = new Date(System.currentTimeMillis());
                        solicitudDispositivo.setDate(date);
                        solicitudDispositivo.setEstado("pending");
                        solicitudDispositivo.setIdCliente(0);
                        solicitudDispositivo.setNombreCliente(String.valueOf(editTextCliente.getText()));
                        solicitudDispositivo.setIdObjetivo("");
                        solicitudDispositivo.setNombreObjetivo(String.valueOf(editTextObjetivo.getText()));
                        solicitudDispositivo.setNroLinea(String.valueOf(editTextNroLinea.getText()));
                        sendRequestDevice();
                    }
                }
            }
        });

        return root;
    }


    public String obtenerMarca() {
        String fabricante = Build.MANUFACTURER;
        solicitudDispositivo.setMarca(fabricante.toUpperCase());
        return primeraLetraMayuscula(fabricante);
    }

    public String obtenerModelo() {
        String modelo = Build.MODEL;
        solicitudDispositivo.setModelo(modelo.toUpperCase());
        return primeraLetraMayuscula(modelo);
    }

    public String obtenerAndroidId(){
        String androidId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        solicitudDispositivo.setAndroidId(androidId);
        return androidId;
    }

    private String primeraLetraMayuscula(String cadena) {
        if (cadena == null || cadena.length() == 0) {
            return "Desconocido";
        }
        char primeraLetra = cadena.charAt(0);
        if (Character.isUpperCase(primeraLetra)) {
            return cadena;
        } else {
            return Character.toUpperCase(primeraLetra) + cadena.substring(1);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.spinnerCliente:
                if(position>0){
                    Cliente cliente = listaDeClientes.get(position);
                    getObjetivos(view,cliente.getIdCliente());
                    solicitudDispositivo.setIdCliente(cliente.getIdCliente());
                    solicitudDispositivo.setNombreCliente(cliente.getNombreCliente());
                    clienteSeleccionado = true;
                } else {
                    clienteSeleccionado = false;
                }
                break;
            case R.id.spinnerObjetivo:
                if(position>0){
                    Objetivo objetivo = listaDeObjetivos.get(position);
                    solicitudDispositivo.setIdObjetivo(objetivo.getIdObjetivo());
                    solicitudDispositivo.setNombreObjetivo(objetivo.getNombreObjetivo());
                    objetivoSeleccionado = true;
                } else {
                    objetivoSeleccionado = false;
                }
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    private void sendRequestDevice() {

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            String URL = Configurador.API_PATH + "request_device";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("rdev_anid",solicitudDispositivo.getAndroidId());
            jsonBody.put("rdev_date",solicitudDispositivo.getDate());
            jsonBody.put("rdev_esta",solicitudDispositivo.getEstado());
            jsonBody.put("rdev_ccli",solicitudDispositivo.getIdCliente());
            jsonBody.put("rdev_cobj",solicitudDispositivo.getIdObjetivo());
            jsonBody.put("rdev_marc",solicitudDispositivo.getMarca());
            jsonBody.put("rdev_mode",solicitudDispositivo.getModelo());
            jsonBody.put("rdev_vers",BuildConfig.VERSION_NAME);
            jsonBody.put("rdev_nomb",solicitudDispositivo.getNombre());
            jsonBody.put("rdev_ncli",solicitudDispositivo.getNombreCliente());
            jsonBody.put("rdev_nobj",solicitudDispositivo.getNombreObjetivo());
            jsonBody.put("rdev_cper",solicitudDispositivo.getNroLegajo());
            jsonBody.put("rdev_nlin",solicitudDispositivo.getNroLinea());
            final String requestBody = jsonBody.toString();

            Log.d("JSONBODY", "sendRequestDevice: "+requestBody);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    RequestDeviceAlert myAlert = new RequestDeviceAlert();
                    myAlert.show(getActivity().getSupportFragmentManager(),"Register Alert");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(), "No se pudo cargar solicitud", Toast.LENGTH_SHORT).show();
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

    private void getClientes(){
        Cliente clienteInicial = new Cliente();
        clienteInicial.setNombreCliente("Seleccione un Cliente ...");
        listaDeClientes.add(clienteInicial);
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String mJSONURLString = Configurador.API_PATH + "clientes";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Method.GET,
                mJSONURLString,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject;
                            Cliente cliente = new Cliente();
                            try {
                                jsonObject = response.getJSONObject(i);
                                cliente.setIdCliente(Integer.parseInt(jsonObject.getString("OBJE_CODI")));
                                cliente.setNombreCliente(jsonObject.getString("OBJE_NOMB"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listaDeClientes.add(cliente);
                        }
                        spinnerCliente.setAdapter(clienteAdapter);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getContext(), "Error al cargar Clientes", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(jsonArrayRequest);
    }

    private void getObjetivos(View view,int idCliente){
        listaDeObjetivos.clear();
        Objetivo objetivoInicial = new Objetivo();
        objetivoInicial.setNombreObjetivo("Seleccione un Objetivo ...");
        listaDeObjetivos.add(objetivoInicial);
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String mJSONURLString = Configurador.API_PATH + "objetivos/"+idCliente;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Method.GET,
                mJSONURLString,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = null;
                            Objetivo objetivo = new Objetivo();
                            try {
                                jsonObject = response.getJSONObject(i);
                                objetivo.setIdObjetivo(jsonObject.getString("GRUP_CODI"));
                                objetivo.setNombreObjetivo(jsonObject.getString("GRUP_NOMB"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listaDeObjetivos.add(objetivo);
                        }
                        if(listaDeObjetivos.size() == 1){
                            Objetivo sinObjetivos = new Objetivo();
                            sinObjetivos.setNombreObjetivo("Sin Objetivos");
                            listaDeObjetivos.set(0,sinObjetivos);
                        }
                        spinnerObjetivo.setAdapter(objetivoAdapter);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getContext(), "Error al cargar Objetivos", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

}