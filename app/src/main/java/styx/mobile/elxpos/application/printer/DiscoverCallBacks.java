package styx.mobile.elxpos.application.printer;

import com.epson.epos2.discovery.DeviceInfo;

/**
 * Created by amalg on 25-11-2017.
 */

public interface DiscoverCallBacks {
    void onDeviceDetected(DeviceInfo deviceInfo);
}
