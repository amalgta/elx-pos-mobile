package styx.mobile.elxpos.application.printer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.model.Entry;

/**
 * Created by amalg on 25-11-2017.
 */

public class TPrinter implements ReceiveListener {
    public static String TAG = "TPRINTER";
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
        if (mPrinter == null) return false;

        final int barcodeWidth = 6;
        final int barcodeHeight = 100;

        String method = "";

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            mPrinter.addTextSize(2, 1);

            mPrinter.addTextFont(Printer.FONT_A);
            mPrinter.addText("GIPL TOLL PLAZA - NH47.\n");
            mPrinter.addTextSize(1, 1);


            StringBuilder textData = new StringBuilder();
            textData.append("Thrishur-Edapally\n");
            textData.append("===========================================\n");
            textData.append("Tran. Number   :     " + entry.getTransactionNumber() + "\n");
            textData.append("Date           :     " + Utils.getToday() + "\n");
            textData.append("Lane           :     " + entry.getLane() + "\n");
            textData.append("Operator       :     " + "cksgh" + "\n");
            textData.append("Vehicle Class  :     " + entry.getVehicleClass() + "\n");
            textData.append("Payment Method :     " + entry.getPaymentMethod() + "\n");
            mPrinter.addText(textData.toString());


            mPrinter.addText("Pass Type      :     " + entry.getPassType() + "\n");


            textData = new StringBuilder();
            textData.append("Expiry         :     " + Utils.getTomorrow() + "\n");
            mPrinter.addText(textData.toString());

            mPrinter.addText("Pass Type      :     " + entry.getPassType() + "\n");
            mPrinter.addText("Reg.No         :     " + "3820" + "\n");
            mPrinter.addText("Amount Paid    :     Rs." + entry.getAmountPaid() + "\n");

            textData = new StringBuilder();
            textData.append(entry.getColumnNumber() + "\n");
            textData.append("===========================================\n");
            textData.append("GIPL WISHES YOU" + "\n");
            textData.append("*HAPPY JOURNEY*. Free Services" + "\n");
            textData.append("Ambulance\\Crane\\Route Patrol" + "\n");
            textData.append("Toll Plaza at Km-278.00" + "\n");
            textData.append("Emergency Contact-8129255666" + "\n");
            textData.append("(From Km-270.00 ro 342.00)" + "\n");
            mPrinter.addText(textData.toString());


            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addBarcode";
            mPrinter.addBarcode(entry.getColumnNumber(),
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);

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

        dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            printerCallBacks.onMessage(makeErrorMessage(activity, status));
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

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            } catch (Epos2Exception e) {
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

    public static String makeErrorMessage(Activity activity, PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += activity.getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += activity.getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += activity.getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += activity.getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += activity.getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += activity.getString(R.string.handlingmsg_err_autocutter);
            msg += activity.getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += activity.getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += activity.getString(R.string.handlingmsg_err_overheat);
                msg += activity.getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += activity.getString(R.string.handlingmsg_err_overheat);
                msg += activity.getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += activity.getString(R.string.handlingmsg_err_overheat);
                msg += activity.getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += activity.getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += activity.getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    public void dispPrinterWarnings(PrinterStatusInfo status) {
        /*
        EditText edtWarnings = (EditText) findViewById(R.id.edtWarnings);
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
        }

        edtWarnings.setText(warningsMsg);
        */
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                //ShowMsg.showResult(code, makeErrorMessage(activity, status), activity);
                String errMsg = makeErrorMessage(activity, status);

                String msg = "";
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
                        stopDiscover();
                    }
                });
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

}
