package styx.mobile.elxpos.application.printer;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.eposprint.Builder;

import styx.mobile.elxpos.R;
import styx.mobile.elxpos.activity.PreferencesActivity;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.model.Entry;

/**
 * Created by amalg on 24-11-2017.
 */

public class PrinterUtils {
    public static final String TAG = "PrinterUtils";

    static String getCodeText(int state) {
        String return_text = "";
        switch (state) {
            case Epos2CallbackCode.CODE_SUCCESS:
                return_text = "PRINT_SUCCESS";
                break;
            case Epos2CallbackCode.CODE_PRINTING:
                return_text = "PRINTING";
                break;
            case Epos2CallbackCode.CODE_ERR_AUTORECOVER:
                return_text = "ERR_AUTORECOVER";
                break;
            case Epos2CallbackCode.CODE_ERR_COVER_OPEN:
                return_text = "ERR_COVER_OPEN";
                break;
            case Epos2CallbackCode.CODE_ERR_CUTTER:
                return_text = "ERR_CUTTER";
                break;
            case Epos2CallbackCode.CODE_ERR_MECHANICAL:
                return_text = "ERR_MECHANICAL";
                break;
            case Epos2CallbackCode.CODE_ERR_EMPTY:
                return_text = "ERR_EMPTY";
                break;
            case Epos2CallbackCode.CODE_ERR_UNRECOVERABLE:
                return_text = "ERR_UNRECOVERABLE";
                break;
            case Epos2CallbackCode.CODE_ERR_FAILURE:
                return_text = "ERR_FAILURE";
                break;
            case Epos2CallbackCode.CODE_ERR_NOT_FOUND:
                return_text = "ERR_NOT_FOUND";
                break;
            case Epos2CallbackCode.CODE_ERR_SYSTEM:
                return_text = "ERR_SYSTEM";
                break;
            case Epos2CallbackCode.CODE_ERR_PORT:
                return_text = "ERR_PORT";
                break;
            case Epos2CallbackCode.CODE_ERR_TIMEOUT:
                return_text = "ERR_TIMEOUT";
                break;
            case Epos2CallbackCode.CODE_ERR_JOB_NOT_FOUND:
                return_text = "ERR_JOB_NOT_FOUND";
                break;
            case Epos2CallbackCode.CODE_ERR_SPOOLER:
                return_text = "ERR_SPOOLER";
                break;
            case Epos2CallbackCode.CODE_ERR_BATTERY_LOW:
                return_text = "ERR_BATTERY_LOW";
                break;
            case Epos2CallbackCode.CODE_ERR_TOO_MANY_REQUESTS:
                return_text = "ERR_TOO_MANY_REQUESTS";
                break;
            case Epos2CallbackCode.CODE_ERR_REQUEST_ENTITY_TOO_LARGE:
                return_text = "ERR_REQUEST_ENTITY_TOO_LARGE";
                break;
            default:
                return_text = String.format("%d", state);
                break;
        }
        return return_text;
    }

    static String makeErrorMessage(Activity activity, PrinterStatusInfo status) {
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

    static void displayPrinterStatus(Context context, PrinterStatusInfo status) {

        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += context.getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += context.getString(R.string.handlingmsg_warn_battery_near_end);
        }

        Log.e(TAG, warningsMsg);

    }

    static boolean isPrintable(PrinterStatusInfo status) {
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

    static boolean generateReceiptData(Context activity, Printer mPrinter, Entry entry) {
        boolean isDebug = false;

        if (mPrinter == null) return false;

        String format = ("    %1$s %2$s\n");

        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        String method = "";

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addTextFont(Printer.FONT_A);

            mPrinter.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED, Builder.TRUE, Builder.PARAM_UNSPECIFIED);
            mPrinter.addText(String.format(format, Utils.getPersistData(activity, Constants.DataBaseStorageKeys.inputTitle1), ""));
            mPrinter.addText(String.format(format, Utils.getPersistData(activity, Constants.DataBaseStorageKeys.inputTitle2), ""));
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
                mPrinter.addText(String.format(format, "Reg.No          :", entry.getRegistrationNumber()));
                mPrinter.addText(String.format(format, "Amount Paid     :", "Rs." + entry.getAmountPaid()));
                mPrinter.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED, Builder.FALSE, Builder.PARAM_UNSPECIFIED);

                mPrinter.addText(String.format(format, entry.getColumnNumber(), ""));
                mPrinter.addText(String.format(format, "===========================================", ""));
                mPrinter.addText(String.format(format, Utils.getPersistData(activity, Constants.DataBaseStorageKeys.inputFooter1), ""));
                mPrinter.addText(String.format(format, Utils.getPersistData(activity, Constants.DataBaseStorageKeys.inputFooter2), ""));
                mPrinter.addText(String.format(format, Utils.getPersistData(activity, Constants.DataBaseStorageKeys.inputFooter3), ""));
                mPrinter.addText(String.format(format, Utils.getPersistData(activity, Constants.DataBaseStorageKeys.inputFooter4), ""));
                mPrinter.addText(String.format(format, "Emergency Contact-", Utils.getPersistData(activity, Constants.DataBaseStorageKeys.ContactNumber)));
                mPrinter.addText(String.format(format, Utils.getPersistData(activity, Constants.DataBaseStorageKeys.inputFooter5), ""));

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

    public static void generateDefaults(Context context) {
        Utils.persistData(context, Constants.DataBaseStorageKeys.ContactNumber, "8129255666");
        Utils.persistData(context, Constants.DataBaseStorageKeys.inputTitle1, "GIPL TOLL PLAZA - NH47.");
        Utils.persistData(context, Constants.DataBaseStorageKeys.inputTitle2, "Thrishur-Edapally");
        Utils.persistData(context, Constants.DataBaseStorageKeys.inputFooter1, "GIPL WISHES YOU");
        Utils.persistData(context, Constants.DataBaseStorageKeys.inputFooter2, "*HAPPY JOURNEY*. Free Services");
        Utils.persistData(context, Constants.DataBaseStorageKeys.inputFooter3, "Ambulance\\Crane\\Route Patrol");
        Utils.persistData(context, Constants.DataBaseStorageKeys.inputFooter4, "Toll Plaza at Km-278.00");
        Utils.persistData(context, Constants.DataBaseStorageKeys.inputFooter5, "(From Km-270.00 to 342.00))");
    }
}
