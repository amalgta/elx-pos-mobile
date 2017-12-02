package styx.mobile.elxpos.application.printer;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import styx.mobile.elxpos.model.Entry;

/**
 * Created by amalg on 25-11-2017.
 */

public class TPrinter implements ReceiveListener {
    public static String TAG = "TPrinter";

    private Printer mPrinter;
    private String target;

    private Activity activity;
    private PrinterCallBacks printerCallBacks;

    public TPrinter(Activity activity, PrinterCallBacks printerCallBacks) {
        this.activity = activity;
        this.printerCallBacks = printerCallBacks;
    }

    private boolean runPrintReceiptSequence(Entry entry) {
        if (!initializeObject()) {
            return false;
        }

        if (!PrinterUtils.generateReceiptData(activity, mPrinter, entry)) {
            finalizeObject();
            return false;
        }

        if (!printData()) {
            finalizeObject();
            return false;
        }

        return true;
    }

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();
        PrinterUtils.displayPrinterStatus(activity, status);

        if (!PrinterUtils.isPrintable(status)) {
            printerCallBacks.onError(new Exception("NotPrintableException"), PrinterUtils.makeErrorMessage(activity, status));
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

    private boolean initializeObject() {
        try {
            mPrinter = new Printer(0, 0, activity);
        } catch (Exception e) {
            printerCallBacks.onError(e, "Printer");
            return false;
        }
        mPrinter.setReceiveEventListener(this);
        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();
        mPrinter.setReceiveEventListener(null);
        mPrinter = null;
    }

    private boolean connectPrinter() {
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

                //displayPrinterStatus(status);

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
                stopDiscovery();
            }
        };

        try {
            Discovery.start(activity, mFilterOption, mDiscoveryListener);
        } catch (Exception e) {
            printerCallBacks.onError(e, "start");
        }
    }

    public void stopDiscovery() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    private static class PrintTask extends AsyncTask<Entry, String, Boolean> {
        PrinterCallBacks printerCallBacks;
        TPrinter tPrinter;

        private PrintTask(TPrinter tPrinter, PrinterCallBacks printerCallBacks) {
            this.printerCallBacks = printerCallBacks;
            this.tPrinter = tPrinter;
        }

        protected Boolean doInBackground(Entry... entries) {
            return tPrinter.runPrintReceiptSequence(entries[0]);
        }

        protected void onPostExecute(Boolean success) {
            printerCallBacks.stopProgress();
            printerCallBacks.onMessage(success ? "Print successful" : "Print successful");
        }
    }

    public void startPrint(Entry entry) {
        new PrintTask(this, printerCallBacks).execute(entry);
    }

    public void setTarget(String target) {
        this.target = target;
    }

}
