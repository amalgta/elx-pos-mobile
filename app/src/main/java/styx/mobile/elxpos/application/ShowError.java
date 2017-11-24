package styx.mobile.elxpos.application;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.github.javiersantos.bottomdialogs.BottomDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import styx.mobile.elxpos.R;

/**
 * Created by amalg on 15-11-2017.
 */

public class ShowError {

    public static BottomDialog showProgress(Activity activity) {
        return new BottomDialog.Builder(activity)
                .setTitle("Awesome!")
                .setContent("What can we improve? Your feedback is always welcome.")
                .setIcon(R.drawable.ic_print_white)
                .setCancelable(false)
                //.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher))
                .show();
    }

    public static void onError(Activity activity,
                               String message,
                               String positiveText,
                               BottomDialog.ButtonCallback onPositiveClick) {
        new BottomDialog.Builder(activity)
                .setTitle("Error")
                .setContent(message)
                .setPositiveText(positiveText)
                .onPositive(onPositiveClick)
                .show();
    }

    public static void onError(Activity activity,
                               String message,
                               String positiveText,
                               BottomDialog.ButtonCallback onPositiveClick,
                               String negativeText,
                               BottomDialog.ButtonCallback onNegativeClick) {
        new BottomDialog.Builder(activity)
                .setTitle("Error")
                .setContent(message)
                .setPositiveText(positiveText)
                .onPositive(onPositiveClick)
                .setNegativeText(negativeText)
                .onNegative(onNegativeClick)
                .show();
    }

    public static void onError(Activity runnable, String disconnect) {

    }
}
