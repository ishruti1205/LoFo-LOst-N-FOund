package com.example.lofo.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.lofo.R;

public class ToastUtils {

    public static void show(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT); // default
    }

    public static void show(Context context, String message, int duration) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView tv = layout.findViewById(R.id.tvToastMessage);
        tv.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }
}