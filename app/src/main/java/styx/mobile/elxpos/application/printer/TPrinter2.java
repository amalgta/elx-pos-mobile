package styx.mobile.elxpos.application.printer;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.model.Entry;

/**
 * Created by amalg on 24-11-2017.
 */

public class TPrinter2 implements ReceiveListener {
    private static final String TAG = "PrinterLog";

    private Printer printer;
    private Context context;
    private String printerTarget;
    private PrinterCallBacks printerCallBacks;

    public TPrinter2(Context context, PrinterCallBacks printerCallBacks) {
        this(context, printerCallBacks, "");
    }

    public TPrinter2(Context context, PrinterCallBacks printerCallBacks, String printerTarget) {
        this.context = context;
        this.printerTarget = printerTarget;
        this.printerCallBacks = printerCallBacks;
    }

    public void setPrinterTarget(String printerTarget) {
        this.printerTarget = printerTarget;
    }

    public boolean isTargetSet() {
        return TextUtils.isEmpty(printerTarget );

    }

    public boolean initiatePrinter() {
        if (TextUtils.isEmpty(printerTarget)) {
            //printerCallBacks.onPrinterNotConnected();
            return false;
        } else {
            try {
                if (printer == null)
                    printer = new Printer(0, 0, context);
            } catch (Exception e) {
                onLog(e);
                return false;
            }
            printer.setReceiveEventListener(this);
            return true;
        }
    }

    public boolean generateDataBuffer(Entry entry) {
        if (printer == null) return false;

        final int barcodeWidth = 6;
        final int barcodeHeight = 100;

        String method = "";

        try {
            method = "addTextAlign";
            printer.addTextAlign(Printer.ALIGN_LEFT);

            printer.addTextSize(2, 1);

            printer.addTextFont(Printer.FONT_A);
            printer.addText("GIPL TOLL PLAZA - NH47.\n");
            printer.addTextSize(1, 1);


            StringBuilder textData = new StringBuilder();
            textData.append("Thrishur-Edapally\n");
            textData.append("===========================================\n");
            textData.append("Tran. Number   :     " + entry.getTransactionNumber() + "\n");
            textData.append("Date           :     " + Utils.getToday() + "\n");
            textData.append("Lane           :     " + entry.getLane() + "\n");
            textData.append("Operator       :     " + "cksgh" + "\n");
            textData.append("Vehicle Class  :     " + entry.getVehicleClass() + "\n");
            textData.append("Payment Method :     " + entry.getPaymentMethod() + "\n");
            printer.addText(textData.toString());


            printer.addText("Pass Type      :     " + entry.getPassType() + "\n");


            textData = new StringBuilder();
            textData.append("Expiry         :     " + Utils.getTomorrow() + "\n");
            printer.addText(textData.toString());

            printer.addText("Pass Type      :     " + entry.getPassType() + "\n");
            printer.addText("Reg.No         :     " + "3820" + "\n");
            printer.addText("Amount Paid    :     Rs." + entry.getAmountPaid() + "\n");

            textData = new StringBuilder();
            textData.append(entry.getColumnNumber() + "\n");
            textData.append("===========================================\n");
            textData.append("GIPL WISHES YOU" + "\n");
            textData.append("*HAPPY JOURNEY*. Free Services" + "\n");
            textData.append("Ambulance\\Crane\\Route Patrol" + "\n");
            textData.append("Toll Plaza at Km-278.00" + "\n");
            textData.append("Emergency Contact-8129255666" + "\n");
            textData.append("(From Km-270.00 ro 342.00)" + "\n");
            printer.addText(textData.toString());


            method = "addText";
            printer.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addBarcode";
            printer.addBarcode(entry.getColumnNumber(),
                    Printer.BARCODE_CODE128,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);

            method = "addCut";
            printer.addCut(Printer.CUT_FEED);

        } catch (Exception e) {
            Log.e(TAG, method + ":" + e.toString());
            return false;
        }
        return true;
    }

    public boolean printData() {
        if (printer == null) return false;

        PrinterStatusInfo status = printer.getStatus();

        if (BuildConfig.DEBUG) {
            Log.e(TAG, PrinterUtils.makeWarningMessage(context, status));
        }

        if (!isPrintable(status)) {
           // printerCallBacks.onError(PrinterUtils.makeErrorMessage(context, status));
            disconnect();
            return false;
        }

        try {
            printer.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            //printerCallBacks.onError("Unable to print.");
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
                    //printerCallBacks.onError("Error ending active tasks.");
                }
                try {
                    printer.disconnect();
                } catch (final Exception e) {
                 //   printerCallBacks.onError("Disconnect failure.");
                }
                recycle();
            }
        }).start();
    }

    public boolean connect() {
        if (printer == null) return false;
        boolean isBeginTransaction = false;
        try {
            printer.connect(printerTarget, Printer.PARAM_DEFAULT);
        } catch (Exception e) {
           // printerCallBacks.onError(PrinterUtils.makeErrorMessage(context, printer.getStatus()));
            return false;
        }

        try {
            printer.beginTransaction();
            isBeginTransaction = true;
        } catch (Exception e) {
          //  printerCallBacks.onError("beginTransaction" + e.toString());
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stopDiscovery();
                    }
                }).start();
            }
        };
        try {
            Discovery.start(context, generateFilterOption(), discoveryListener);
        } catch (Exception e) {
            onDetectDeviceListener.onDetectError();
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
            Log.e("TPrinter2", string);
        }
    }

    private void onLog(Exception e) {
        onLog(Arrays.toString(e.getStackTrace()));
    }
}
