package styx.mobile.elxpos.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.gson.Gson;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.PrinterUtils;
import styx.mobile.elxpos.application.ShowError;
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.model.Device;
import styx.mobile.elxpos.model.Entry;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddEntryActivity extends AppCompatActivity implements ReceiveListener, View.OnClickListener, RadioRealButtonGroup.OnClickedButtonListener {

    View buttonCapture;
    EditText inputTransactionNumber, inputRegistrationNumber, inputColumnNumber, inputAmountPaid;
    RadioRealButtonGroup radioGroupVehicleClass, radioGroupPaymentMethod, radioGroupPassType, radioGroupLane;

    private Printer mPrinter = null;
    Entry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry_activity);

        buttonCapture = findViewById(R.id.buttonCapture);
        inputTransactionNumber = findViewById(R.id.inputTransactionNumber);
        inputRegistrationNumber = findViewById(R.id.inputRegistrationNumber);
        inputColumnNumber = findViewById(R.id.inputColumnNumber);
        inputAmountPaid = findViewById(R.id.inputAmountPaid);
        radioGroupVehicleClass = findViewById(R.id.radioGroupVehicleClass);
        radioGroupPaymentMethod = findViewById(R.id.radioGroupPaymentMethod);
        radioGroupPassType = findViewById(R.id.radioGroupPassType);
        radioGroupLane = findViewById(R.id.radioGroupLane);

        Utils.setTitleColor(this, ContextCompat.getColor(this, R.color.blue));

        buttonCapture.setOnClickListener(this);
        radioGroupVehicleClass.setOnClickedButtonListener(this);
        radioGroupPaymentMethod.setOnClickedButtonListener(this);
        radioGroupPassType.setOnClickedButtonListener(this);
        radioGroupLane.setOnClickedButtonListener(this);

        if (savedInstanceState != null) {
            Entry entry = savedInstanceState.getParcelable(Constants.BundleKeys.PersistedEntry);
            setEntry(entry);
            Utils.hideKeyboard(this);
        } else if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(Constants.BundleKeys.Entry)) {
            Entry entry = getIntent().getExtras().getParcelable(Constants.BundleKeys.Entry);
            setEntry(entry);
            Utils.hideKeyboard(this);
        } else {
            setEntry(null);
        }
    }

    private void setEntry(Entry entry) {
        if (entry == null)
            this.entry = new Entry();
        else
            bindUI(entry);
    }

    private void bindUI(Entry entry) {
        inputAmountPaid.setText(entry.getAmountPaid());
        inputTransactionNumber.setText(entry.getTransactionNumber());
        inputRegistrationNumber.setText(entry.getRegistrationNumber());
        inputColumnNumber.setText(entry.getColumnNumber());

        for (int i = 0; i < radioGroupVehicleClass.getButtons().size(); i++) {
            if (radioGroupVehicleClass.getButtons().get(i).getText().contentEquals(entry.getVehicleClass()))
                radioGroupVehicleClass.setPosition(i);

        }
        for (int i = 0; i < radioGroupPaymentMethod.getButtons().size(); i++) {
            if (radioGroupPaymentMethod.getButtons().get(i).getText().contentEquals(entry.getPaymentMethod()))
                radioGroupPaymentMethod.setPosition(i);
        }
        for (int i = 0; i < radioGroupPassType.getButtons().size(); i++) {
            if (radioGroupPassType.getButtons().get(i).getText().contentEquals(entry.getPassType()))
                radioGroupPassType.setPosition(i);
        }
        for (int i = 0; i < radioGroupLane.getButtons().size(); i++) {
            if (radioGroupLane.getButtons().get(i).getText().contentEquals(entry.getLane()))
                radioGroupLane.setPosition(i);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.BundleKeys.PersistedEntry, entry);
    }

    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                onError(PrinterUtils.makeErrorMessage(AddEntryActivity.this, status));

                dispPrinterWarnings(status);

                //updateButtonState(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //disconnectPrinter();
                    }
                }).start();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonCapture:
                doSaveEntry();
                break;
        }
    }

    private void doSaveEntry() {
        String amountPaid = inputAmountPaid.getText().toString();
        String transactionNumber = inputTransactionNumber.getText().toString();
        String registrationNumber = inputRegistrationNumber.getText().toString();
        String columnNumber = inputColumnNumber.getText().toString();

        String vehicleClass = (radioGroupVehicleClass.getPosition() >= 0) ? radioGroupVehicleClass.getButtons().get(radioGroupVehicleClass.getPosition()).getText() : "";
        String paymentMethod = (radioGroupPaymentMethod.getPosition() >= 0) ? radioGroupPaymentMethod.getButtons().get(radioGroupPaymentMethod.getPosition()).getText() : "";
        String passType = (radioGroupPassType.getPosition() >= 0) ? radioGroupPassType.getButtons().get(radioGroupPassType.getPosition()).getText() : "";
        String lane = (radioGroupLane.getPosition() >= 0) ? radioGroupLane.getButtons().get(radioGroupLane.getPosition()).getText() : "";

        final Entry entry = new Entry(transactionNumber, registrationNumber, columnNumber, amountPaid, vehicleClass, paymentMethod, passType, lane);

        if (isValid(entry)) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Utils.persistData(AddEntryActivity.this, Constants.DataBaseStorageKeys.LastPrintedReceipt, new Gson().toJson(entry));
                    BottomDialog bottomDialog = ShowError.showProgress(AddEntryActivity.this);
                    onCompletePrint(runPrintReceiptSequence(entry));
                    bottomDialog.dismiss();
                }
            });
        }
    }

    private void onCompletePrint(boolean success) {
        //TODO showOnCompletePrint
    }

    private boolean isValid(Entry entry) {
        if (TextUtils.isEmpty(entry.getTransactionNumber())) {
            StyleableToast.makeText(this, "Please enter the transaction number.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
            return false;
        }
        if (TextUtils.isEmpty(entry.getRegistrationNumber())) {
            StyleableToast.makeText(this, "Please enter the registration number.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
            return false;
        }
        if (TextUtils.isEmpty(entry.getColumnNumber())) {
            StyleableToast.makeText(this, "Please enter the column number.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
            return false;
        }
        if (TextUtils.isEmpty(entry.getAmountPaid())) {
            StyleableToast.makeText(this, "Please enter the paid amount.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
            return false;
        }
        if (TextUtils.isEmpty(entry.getVehicleClass())) {
            StyleableToast.makeText(this, "Please choose the vehicle class.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
            return false;
        }
        if (TextUtils.isEmpty(entry.getPaymentMethod())) {
            StyleableToast.makeText(this, "Please choose payment method.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
            return false;
        }
        if (TextUtils.isEmpty(entry.getPassType())) {
            StyleableToast.makeText(this, "Please choose pass type.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
            return false;
        }
        if (TextUtils.isEmpty(entry.getLane())) {
            StyleableToast.makeText(this, "Please choose lane.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
            return false;
        }
        return true;
    }

    private boolean runPrintReceiptSequence(Entry entry) {
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

    private boolean initializeObject() {
        try {
            mPrinter = new Printer(0, 0, this);
        } catch (Exception e) {
            onError("Printer");
            return false;
        }
        mPrinter.setReceiveEventListener(this);
        return true;
    }

    private boolean createReceiptData(Entry entry) {
        String method = "";
        Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            method = "addImage";
            mPrinter.addImage(logoData, 0, 0,
                    logoData.getWidth(),
                    logoData.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);

            method = "addFeedLine";
            mPrinter.addFeedLine(1);

/*            textData.append("THE STORE 123 (555) 555 – 5555\n");
            textData.append("STORE DIRECTOR – John Smith\n");
            textData.append("\n");
            textData.append("7/01/07 16:58 6153 05 0191 134\n");
            textData.append("ST# 21 OP# 001 TE# 01 TR# 747\n");*/

            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
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
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
/*
            textData.append("SUBTOTAL                160.38\n");
            textData.append("TAX                      14.43\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(2, 2);
            method = "addText";
            mPrinter.addText("TOTAL    174.81\n");
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            textData.append("CASH                    200.00\n");
            textData.append("CHANGE                   25.19\n");*/
/*
            textData.append("Wishing you a Happy Journey\n");

            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addFeedLine";
            mPrinter.addFeedLine(2);*/

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
            onError(method);
            return false;
        }

        textData = null;

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

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            onError(PrinterUtils.makeErrorMessage(this, status));
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
            onError("sendData");
            try {
                mPrinter.disconnect();
            } catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }

    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            Device device = new Gson().fromJson(Utils.getPersistData(this, Constants.DataBaseStorageKeys.Device), Device.class);
            mPrinter.connect(device.getTarget(), Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            onError("connect");
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        } catch (Exception e) {
            onError("beginTransaction");
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

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        // EditText edtWarnings = (EditText) findViewById(R.id.edtWarnings);
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            //    warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            //warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
        }

        //edtWarnings.setText(warningsMsg);
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        } else if (status.getOnline() == Printer.FALSE) {
            return false;
        } else {
            //print available
        }

        return true;
    }

    private void onError(String message) {

    }

    @Override
    public void onClickedButton(RadioRealButton button, int position) {
        Utils.hideKeyboard(this);
    }
}
