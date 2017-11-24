package styx.mobile.elxpos.application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.github.javiersantos.bottomdialogs.BottomDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import styx.mobile.elxpos.activity.AddEntryActivity;
import styx.mobile.elxpos.activity.MainActivity;

/**
 * Created by amalg on 15-11-2017.
 */

public class Utils {
    public static void setTitleColor(Activity activity, int color) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public static String getToday() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DateFormat.PrintDate, Locale.ENGLISH);
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public static String getTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DateFormat.PrintDate, Locale.ENGLISH);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getPersistData(Activity activity, String key) {
        return activity.getSharedPreferences(Constants.dbStorageKey, Context.MODE_PRIVATE)
                .getString(key, null);
    }

    public static void persistData(Activity activity, String key, String value) {
        SharedPreferences settings = activity.getSharedPreferences(Constants.dbStorageKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void hideKeyboard(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
       /* InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && activity.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }*/
    }
}
