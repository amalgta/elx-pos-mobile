package styx.mobile.elxpos.application;

import android.app.Application;

import styx.mobile.elxpos.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by amalg on 15-11-2017.
 */

public class ELXPOSApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getString(R.string.RobotoRegular))
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
