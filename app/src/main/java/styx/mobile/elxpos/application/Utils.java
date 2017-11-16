package styx.mobile.elxpos.application;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public static String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("DD-MMM-YYYY h:m:s a", Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
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
}
