package styx.mobile.elxpos.application.printer;

/**
 * Created by amalg on 24-11-2017.
 */

public interface PrinterCallBacks {
    void onPrinterReady(String status);

    void onError(Exception errorMessage, String message);

    void onMessage(String message);
}