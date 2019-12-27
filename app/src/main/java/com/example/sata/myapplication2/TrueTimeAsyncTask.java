package com.example.sata.myapplication2;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.util.Date;

public class TrueTimeAsyncTask extends AsyncTask<Void, Void, Date> {

    private static final String TAG = "TrueTimeAsyncTask";

    private Context context;
    private ResultListener<Date> dateResultListener;

    protected TrueTimeAsyncTask(Context context, ResultListener<Date> dateResultListener){
        this.context = context;
        this.dateResultListener = dateResultListener;
    }

    protected Date doInBackground(Void... params) {
        Date date = null;
        try {
            TrueTime.build()
                    .withNtpHost("time.google.com")
                    .withServerResponseDelayMax(500)
                    .initialize();
            date = TrueTime.now();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception when trying to get TrueTime", e);
        }
        return date;
    }

    @Override
    protected void onPostExecute(Date date) {
        super.onPostExecute(date);
        dateResultListener.finish(date);
    }
}
