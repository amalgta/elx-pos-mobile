package styx.mobile.elxpos.application.printer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;

import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.model.Entry;

/**
 * Created by amalg on 25-11-2017.
 */

public class TPrinter implements ReceiveListener {
    public static String TAG = "TPrinter";
    private Printer mPrinter = null;
    private Activity activity;
    private String target;
    private PrinterCallBacks printerCallBacks;

    public TPrinter(Activity activity, PrinterCallBacks printerCallBacks) {
        this.activity = activity;
        this.printerCallBacks = printerCallBacks;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean createReceiptData(Entry entry) {
        boolean isDebug = true;

        if (mPrinter == null) return false;

        String contactNumber = Utils.getPersistData(activity, Constants.DataBaseStorageKeys.ContactNumber);
        String format = ("    %1$s %2$s\n");

        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        String method = "";

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addTextFont(Printer.FONT_A);

            mPrinter.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED, Builder.TRUE, Builder.PARAM_UNSPECIFIED);
            mPrinter.addText(String.format(format, "GIPL TOLL PLAZA - NH47.", ""));
            mPrinter.addText(String.format(format, "Thrishur-Edapally", ""));
            mPrinter.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED, Builder.FALSE, Builder.PARAM_UNSPECIFIED);

            mPrinter.addText(String.format(format, "===========================================", ""));
            mPrinter.addText(String.format(format, "Tran. Number    :", entry.getTransactionNumber()));
            mPrinter.addText(String.format(format, "Date            :", Utils.getToday()));
            mPrinter.addText(String.format(format, "Lane            :", entry.getLane()));
            mPrinter.addText(String.format(format, "Operator        :", "cksgh"));
            mPrinter.addText(String.format(format, "Vehicle Class   :", entry.getVehicleClass()));
            mPrinter.addText(String.format(format, "Payment Method  :", entry.getPaymentMethod()));

            if (!isDebug) {

                mPrinter.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED, Builder.TRUE, Builder.PARAM_UNSPECIFIED);
                mPrinter.addText(String.format(format, "Pass Type       :", entry.getPassType()));
                mPrinter.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED, Builder.FALSE, Builder.PARAM_UNSPECIFIED);

                mPrinter.addText(String.format(format, "Expiry          :", Utils.getTomorrow()));

                mPrinter.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED, Builder.TRUE, Builder.PARAM_UNSPECIFIED);
                mPrinter.addText(String.format(format, "Reg.No          :", "3820"));
                mPrinter.addText(String.format(format, "Amount Paid     :", "Rs." + entry.getAmountPaid()));
                mPrinter.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED, Builder.FALSE, Builder.PARAM_UNSPECIFIED);

                mPrinter.addText(String.format(format, entry.getColumnNumber(), ""));
                mPrinter.addText(String.format(format, "===========================================", ""));
                mPrinter.addText(String.format(format, "GIPL WISHES YOU", ""));
                mPrinter.addText(String.format(format, "*HAPPY JOURNEY*. Free Services", ""));
                mPrinter.addText(String.format(format, "Ambulance\\Crane\\Route Patrol", ""));
                mPrinter.addText(String.format(format, "Toll Plaza at Km-278.00", ""));
                mPrinter.addText(String.format(format, "Emergency Contact-", TextUtils.isEmpty(contactNumber) ? "" : contactNumber));
                mPrinter.addText(String.format(format, "(From Km-270.00 to 342.00)", ""));

                mPrinter.addTextAlign(Printer.ALIGN_CENTER);
                method = "addBarcode";
                mPrinter.addBarcode(Utils.leadingZeros(entry.getColumnNumber(), 24), Builder.BARCODE_ITF,
                        Builder.HRI_BELOW,
                        Printer.FONT_A,
                        barcodeWidth, barcodeHeight);

                mPrinter.addText("\n");

                method = "addCut";
                mPrinter.addCut(Printer.CUT_FEED);
            }
        } catch (Exception e) {
            Log.e(TAG, method + ":" + e.toString());
            return false;
        }
        return true;
    }

    public boolean runPrintReceiptSequence(Entry entry) {
        if (!initializeObject()) {
            return false;
        }

        if (!createReceiptData(entry)) {
            finalizeObject();
            return false;
        }

        if (!printData()) {
            finalizeObject();
            return false;
        }

        return true;
    }

    public boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();
        PrinterUtils.dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            printerCallBacks.onMessage(PrinterUtils.makeErrorMessage(activity, status));
            try {
                mPrinter.disconnect();
            } catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            printerCallBacks.onError(e, "sendData");
            try {
                mPrinter.disconnect();
            } catch (Exception ex) {
                // Do nothing
            }
            return false;
        }
        return true;
    }

    public boolean initializeObject() {
        try {
            mPrinter = new Printer(0, 0, activity);
        } catch (Exception e) {
            printerCallBacks.onError(e, "Printer");
            return false;
        }
        mPrinter.setReceiveEventListener(this);
        return true;
    }

    public void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();
        mPrinter.setReceiveEventListener(null);
        mPrinter = null;
    }

    public boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            if (TextUtils.isEmpty(target)) throw new Exception("TargetEmptyException");
            mPrinter.connect(target, Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            printerCallBacks.onError(e, "connect");
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        } catch (Exception e) {
            printerCallBacks.onError(e, "beginTransaction");
        }

        if (!isBeginTransaction) {
            try {
                mPrinter.disconnect();
            } catch (Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }

    public void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        } catch (final Exception e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    printerCallBacks.onError(e, "endTransaction");
                }
            });
        }

        try {
            mPrinter.disconnect();
        } catch (final Exception e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    printerCallBacks.onError(e, "disconnect");
                }
            });
        }

        finalizeObject();
    }

    public boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        } else if (status.getOnline() == Printer.FALSE) {
            return false;
        } else {
            ;//print available
        }

        return true;
    }


    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                //ShowMsg.showResult(code, makeErrorMessage(activity, status), activity);
                String errMsg = PrinterUtils.makeErrorMessage(activity, status);

                String msg;
                if (errMsg.isEmpty()) {
                    msg = String.format(
                            "\t%s\n\t%s\n",
                            "Result",
                            PrinterUtils.getCodeText(code));
                } else {
                    msg = String.format(
                            "\t%s\n\t%s\n\n\t%s\n\t%s\n",
                            "Title",
                            PrinterUtils.getCodeText(code),
                            "Description",
                            errMsg);
                }

                //dispPrinterWarnings(status);

                printerCallBacks.onPrinterReady(msg);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }

    public void startDiscovery(final DiscoverCallBacks discoverCallBacks) {
        final FilterOption mFilterOption = new FilterOption();
        mFilterOption.setDeviceType(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NAME);

        final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
            @Override
            public void onDiscovery(final DeviceInfo deviceInfo) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public synchronized void run() {
                        discoverCallBacks.onDeviceDetected(deviceInfo);
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stopDiscover();
                    }
                }).start();
            }
        };

        try {
            Discovery.start(activity, mFilterOption, mDiscoveryListener);
        } catch (Exception e) {
            printerCallBacks.onError(e, "start");
        }
    }

    public void stopDiscover() {
        while (true) {
            try {
                Discovery.stop();
                break;
            } catch (Epos2Exception e) {
                if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                    break;
                }
            }
        }
    }

    public String getTarget() {
        return target;
    }
}
