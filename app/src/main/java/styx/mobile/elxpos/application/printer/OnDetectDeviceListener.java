package styx.mobile.elxpos.application.printer;

import com.epson.epos2.discovery.DeviceInfo;

/**
 * Created by amalg on 24-11-2017.
 */

public interface OnDetectDeviceListener {
    void onDetectDevice(DeviceInfo deviceInfo);

    void onDetectError();
}