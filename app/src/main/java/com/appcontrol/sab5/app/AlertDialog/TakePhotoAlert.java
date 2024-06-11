package com.appcontrol.sab5.app.AlertDialog;

import android.app.Activity;
import android.app.Dialog;

import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.appcontrol.sab5.app.R;

public class TakePhotoAlert {

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.ThemeDialogCustom);
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        builder.setView(inflater.inflate(R.layout.custom_alert_take_photo, null))
//                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//            }
//        });
//        setCancelable(false);
//        // Create the AlertDialog object and return it
//        return builder.create();
//    }

    public void showDialog(Activity activity, String msg){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_alert_take_photo);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView text = dialog.findViewById(R.id.textViewMessage);
        text.setText(msg);

        ImageView dialogBtn_camera =  dialog.findViewById(R.id.imageViewCamera);
        dialogBtn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    Toast.makeText(getApplicationContext(),"Cancel" ,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        ImageView dialogBtn_gallery = dialog.findViewById(R.id.imageViewGallery);
        dialogBtn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    Toast.makeText(getApplicationContext(),"Okay" ,Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        dialog.show();
    }
}
