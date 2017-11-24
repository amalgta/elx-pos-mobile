package styx.mobile.elxpos.application.printer;

/**
 * Created by amalg on 24-11-2017.
 */

public interface PrinterCallBacks {
    void onPrinterConnected(String status);

    void onError(String sendData);

    void onPrintCompleted();
}
