package com.appcontrol.sab5.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class NetworkUtil {
    private Context context;
    private TelephonyManager telephonyManager;
    private SignalStrengthListener signalStrengthListener;

    public NetworkUtil(Context context) {
        this.context = context;
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.signalStrengthListener = new SignalStrengthListener();
    }

    // Método para obtener el tipo de red a la que está conectado
    public String getConnectionType() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "WIFI";
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return "Datos Móviles";
                }
            }
        }
        return "Sin Conexión";
    }

    // Método para medir la señal de la red
    public int getSignalStrength() {
        int signalStrength = -1; // Valor predeterminado si no está conectado
        String connectionType = getConnectionType();

        if ("WIFI".equals(connectionType)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            signalStrength = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5); // Escala de 0 a 5
        } else if ("Datos Móviles".equals(connectionType)) {
            // Escucha la intensidad de la señal
            telephonyManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            return signalStrengthListener.getCurrentSignalStrength();
        }
        return signalStrength;
    }

    // Clase interna para manejar la señal
    private class SignalStrengthListener extends PhoneStateListener {
        private int currentSignalStrength = -1;

        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            // Para API 29 y superior
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                currentSignalStrength = signalStrength.getLevel();
//            } else {
                currentSignalStrength = signalStrength.getGsmSignalStrength(); // Para versiones anteriores
                if (currentSignalStrength != 99) {
                    currentSignalStrength = Math.round((currentSignalStrength / 31.0f) * 4); // Convertir a una escala del 0 al 4
                } else {
                    currentSignalStrength = -1; // Sin señal
                }
            //}
        }

        public int getCurrentSignalStrength() {
            return currentSignalStrength;
        }
    }
}
