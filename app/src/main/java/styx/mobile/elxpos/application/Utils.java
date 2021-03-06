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

    public static String getParsedCalendar(Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DateFormat.PrintDate, Locale.ENGLISH);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static Calendar getTomorrow(Calendar calendar) {
        Calendar calendar2 = (Calendar) calendar.clone();
        calendar2.add(Calendar.DAY_OF_YEAR, 1);
        return calendar2;
    }

    public static String getPersistData(Context context, String key) {
        return context.getSharedPreferences(Constants.dbStorageKey, Context.MODE_PRIVATE)
                .getString(key, null);
    }

    public static void persistData(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(Constants.dbStorageKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void hideKeyboard1(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static String leadingZeros(String s, int length) {
        if (s.length() >= length) return s;
        else return String.format("%0" + (length - s.length()) + "d%s", 0, s);
    }

    public static String getDateOrdinal(int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}
