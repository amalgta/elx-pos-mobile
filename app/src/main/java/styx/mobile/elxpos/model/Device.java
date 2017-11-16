package styx.mobile.elxpos.model;

import com.epson.epos2.discovery.DeviceInfo;

/**
 * Created by amalg on 16-11-2017.
 */

public class Device {
    private int deviceType;
    private String target;
    private String deviceName;
    private String ipAddress;
    private String macAddress;
    private String bdAddress;

    public Device(DeviceInfo deviceInfo) {
        deviceType = deviceInfo.getDeviceType();
        target = deviceInfo.getTarget();
        deviceName = deviceInfo.getDeviceName();
        ipAddress = deviceInfo.getIpAddress();
        macAddress = deviceInfo.getMacAddress();
        bdAddress = deviceInfo.getBdAddress();
    }

    public Device(int deviceType, String target, String deviceName, String ipAddress, String macAddress, String bdAddress) {
        this.deviceType = deviceType;
        this.target = target;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.bdAddress = bdAddress;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getBdAddress() {
        return bdAddress;
    }

    public void setBdAddress(String bdAddress) {
        this.bdAddress = bdAddress;
    }
}
