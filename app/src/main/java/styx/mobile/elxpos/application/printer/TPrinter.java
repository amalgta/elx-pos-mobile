package styx.mobile.elxpos.application.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.epson.epos2.ConnectionListener;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import java.util.Arrays;

import styx.mobile.elxpos.BuildConfig;
import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.model.Entry;

/**
 * Created by amalg on 24-11-2017.
 */

public class TPrinter implements ReceiveListener {
    private static final String TAG = "PrinterLog";

    private Printer printer;
    private Context context;
    private String printerTarget;
    private PrinterCallBacks printerCallBacks;

    public TPrinter(Context context, PrinterCallBacks printerCallBacks) {
        this(context, printerCallBacks, "");
    }

    public TPrinter(Context context, PrinterCallBacks printerCallBacks, String printerTarget) {
        this.context = context;
        this.printerTarget = printerTarget;
        this.printerCallBacks = printerCallBacks;
    }

    public void setPrinterTarget(String printerTarget) {
        this.printerTarget = printerTarget;
    }

    public boolean initiatePrinter() {
        try {
            printer = new Printer(0, 0, context);
        } catch (Exception e) {
            printerCallBacks.onError("Unable to initiate printer.");
            onLog(e);
            return false;
        }
        printer.setReceiveEventListener(this);
        return true;
    }

    public boolean printBuffer(Entry entry) {
        if (!generateDataBuffer(entry)) {
            recycle();
            return false;
        }


        if (!printData()) {
            recycle();
            return false;
        }
        return true;
    }

    private boolean generateDataBuffer(Entry entry) {
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(context.getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (printer == null) {
            return false;
        }

        try {
            method = "addTextAlign";
            printer.addTextAlign(Printer.ALIGN_CENTER);

            method = "addImage";
            printer.addImage(logoData, 0, 0,
                    logoData.getWidth(),
                    logoData.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);

            method = "addFeedLine";
            printer.addFeedLine(1);

/*            textData.append("THE STORE 123 (555) 555 – 5555\n");
            textData.append("STORE DIRECTOR – John Smith\n");
            textData.append("\n");
            textData.append("7/01/07 16:58 6153 05 0191 134\n");
            textData.append("ST# 21 OP# 001 TE# 01 TR# 747\n");*/

            textData.append("------------------------------\n");
            method = "addText";
            printer.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("GIPL TOLL PLAZA - NH47.\n");
            textData.append("Thrishur-Edapally\n");
            textData.append("=================================\n");
            textData.append("Tran. Number   :     " + entry.getTransactionNumber() + "\n");
            textData.append("Date           :     " + Utils.getToday() + "\n");
            textData.append("Lane           :     " + entry.getLane() + "\n");
            textData.append("Operator       :     " + "cksgh" + "\n");
            textData.append("Vehicle Class  :     " + entry.getVehicleClass() + "\n");
            textData.append("Payment Method :     " + entry.getPaymentMethod() + "\n");
            textData.append("Pass Type      :     " + entry.getPassType() + "\n");
            textData.append("Expiry         :     " + Utils.getTomorrow() + "\n");
            textData.append("Reg.No         :     " + "3820" + "\n");
            textData.append("Amount Paid    :     " + entry.getAmountPaid() + "\n");
            textData.append(entry.getColumnNumber() + "\n");
            textData.append("=================================\n");
            textData.append("GIPL WISHES YOU" + "\n");
            textData.append("*HAPPY JOURNEY*. Free Services" + "\n");
            textData.append("Ambulance\\Crane\\Route Patrol" + "\n");
            textData.append("Toll Plaza at Km-278.00" + "\n");
            textData.append("Emergency Contact-8129255666" + "\n");
            textData.append("(From Km-270.00 ro 342.00)" + "\n");

            method = "addText";
            printer.addText(textData.toString());
            textData.delete(0, textData.length());
/*
            textData.append("SUBTOTAL                160.38\n");
            textData.append("TAX                      14.43\n");
            method = "addText";
            printer.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            printer.addTextSize(2, 2);
            method = "addText";
            printer.addText("TOTAL    174.81\n");
            method = "addTextSize";
            printer.addTextSize(1, 1);
            method = "addFeedLine";
            printer.addFeedLine(1);

            textData.append("CASH                    200.00\n");
            textData.append("CHANGE                   25.19\n");*/
/*
            textData.append("Wishing you a Happy Journey\n");

            method = "addText";
            printer.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addFeedLine";
            printer.addFeedLine(2);*/

            method = "addBarcode";
            printer.addBarcode(entry.getColumnNumber(),
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);

            method = "addCut";
            printer.addCut(Printer.CUT_FEED);

        } catch (Exception e) {
            printerCallBacks.onError(method + e.toString());
            return false;
        }
        textData = null;
        return true;
    }


    private boolean printData() {
        if (printer == null) return false;

        PrinterStatusInfo status = printer.getStatus();

        if (BuildConfig.DEBUG) {
            Log.e(TAG, PrinterUtils.makeWarningMessage(context, status));
        }

        if (!isPrintable(status)) {
            printerCallBacks.onError(PrinterUtils.makeErrorMessage(context, status));
            disconnect();
            return false;
        }

        try {
            printer.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            printerCallBacks.onError("Unable to print.");
            disconnect();
            return false;
        }

        return true;
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (printer == null) return;
                try {
                    printer.endTransaction();
                } catch (final Exception e) {
                    printerCallBacks.onError("Error ending active tasks.");
                }
                try {
                    printer.disconnect();
                } catch (final Exception e) {
                    printerCallBacks.onError("Disconnect failure.");
                }
                recycle();
            }
        }).start();
    }

    public boolean connect() {
        if (printer == null) return false;
        boolean isBeginTransaction = false;
        try {
            if (TextUtils.isEmpty(printerTarget)) {
                printerCallBacks.onConnectionFailed();
                return false;
            } else {
                printer.connect(printerTarget, Printer.PARAM_DEFAULT);
            }
        } catch (Exception e) {
            PrinterStatusInfo status = printer.getStatus();
            return false;
        }

        try {
            printer.beginTransaction();
            isBeginTransaction = true;
        } catch (Exception e) {
            printerCallBacks.onError("beginTransaction" + e.toString());
        }

        if (!isBeginTransaction) {
            disconnect();
        }
        return true;
    }

    public void startDiscovery(final OnDetectDeviceListener onDetectDeviceListener) {
        DiscoveryListener discoveryListener = new DiscoveryListener() {
            @Override
            public void onDiscovery(final DeviceInfo deviceInfo) {
                onLog("onDiscovery():" + deviceInfo.getDeviceName());
                onDetectDeviceListener.onDetectDevice(deviceInfo);
                stopDiscovery();
            }
        };
        try {
            Discovery.start(context, generateFilterOption(), discoveryListener);
        } catch (Exception e) {
            onDetectDeviceListener.onDetectError();
            e.printStackTrace();
        }
    }

    public void stopDiscovery() {
        while (true) {
            try {
                Discovery.stop();
                break;
            } catch (Epos2Exception e) {
                if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                    return;
                }
            }
        }
    }

    private static FilterOption generateFilterOption() {
        FilterOption filterOption = new FilterOption();
        filterOption.setDeviceType(Discovery.TYPE_PRINTER);
        filterOption.setEpsonFilter(Discovery.FILTER_NAME);
        return filterOption;
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) return false;
        if (status.getConnection() == Printer.FALSE) {
            return false;
        } else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        return true;
    }

    public void recycle() {
        if (printer == null) return;
        printer.clearCommandBuffer();
        printer.setReceiveEventListener(null);
        printer.setStatusChangeEventListener(null);
        printer.setConnectionEventListener(null);
        printer = null;
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        //makeWarningMessage(printerStatusInfo);
        printerCallBacks.onPrinterReady(code + " " + PrinterUtils.makeErrorMessage(context, status));
    }

    private void onLog(String string) {
        if (BuildConfig.DEBUG) {
            Log.e("TPrinter", string);
        }
    }

    private void onLog(Exception e) {
        onLog(Arrays.toString(e.getStackTrace()));
    }
}
