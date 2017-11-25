package styx.mobile.elxpos.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.epson.epos2.discovery.DeviceInfo;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.gson.Gson;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.ShowError;
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.application.printer.OnDetectDeviceListener;
import styx.mobile.elxpos.application.printer.PrinterCallBacks;
import styx.mobile.elxpos.application.printer.TPrinter;
import styx.mobile.elxpos.model.Entry;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddEntryActivity extends AppCompatActivity implements PrinterCallBacks, View.OnClickListener, RadioRealButtonGroup.OnClickedButtonListener {

    private View buttonCapture;
    private EditText inputTransactionNumber, inputRegistrationNumber, inputColumnNumber, inputAmountPaid;
    private RadioRealButtonGroup radioGroupVehicleClass, radioGroupPaymentMethod, radioGroupPassType, radioGroupLane;
    private Entry entry;
    TPrinter tPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry_activity);

        Utils.setTitleColor(this, ContextCompat.getColor(this, R.color.blue));

        buttonCapture = findViewById(R.id.buttonCapture);
        inputTransactionNumber = findViewById(R.id.inputTransactionNumber);
        inputRegistrationNumber = findViewById(R.id.inputRegistrationNumber);
        inputColumnNumber = findViewById(R.id.inputColumnNumber);
        inputAmountPaid = findViewById(R.id.inputAmountPaid);
        radioGroupVehicleClass = findViewById(R.id.radioGroupVehicleClass);
        radioGroupPaymentMethod = findViewById(R.id.radioGroupPaymentMethod);
        radioGroupPassType = findViewById(R.id.radioGroupPassType);
        radioGroupLane = findViewById(R.id.radioGroupLane);

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

        tPrinter = new TPrinter(this, this);
    }

    private void setEntry(Entry entry) {
        if (entry == null) {
            this.entry = new Entry();
            bindUI(this.entry);
        } else
            bindUI(entry);
    }

    private void bindUI(Entry entry) {
        inputAmountPaid.setText(TextUtils.isEmpty(entry.getAmountPaid()) ? "" : entry.getAmountPaid());
        inputTransactionNumber.setText(TextUtils.isEmpty(entry.getTransactionNumber()) ? "" : entry.getTransactionNumber());
        inputRegistrationNumber.setText(TextUtils.isEmpty(entry.getRegistrationNumber()) ? "" : entry.getRegistrationNumber());
        inputColumnNumber.setText(TextUtils.isEmpty(entry.getColumnNumber()) ? "" : entry.getColumnNumber());

        if (TextUtils.isEmpty(entry.getVehicleClass())) {
            radioGroupVehicleClass.setPosition(-1);
        } else {
            for (int i = 0; i < radioGroupVehicleClass.getButtons().size(); i++) {
                if (radioGroupVehicleClass.getButtons().get(i).getText().contentEquals(entry.getVehicleClass()))
                    radioGroupVehicleClass.setPosition(i);
            }
        }

        if (TextUtils.isEmpty(entry.getPaymentMethod())) {
            radioGroupPaymentMethod.setPosition(-1);
        } else {
            for (int i = 0; i < radioGroupPaymentMethod.getButtons().size(); i++) {
                if (radioGroupPaymentMethod.getButtons().get(i).getText().contentEquals(entry.getPaymentMethod()))
                    radioGroupPaymentMethod.setPosition(i);
            }
        }

        if (TextUtils.isEmpty(entry.getPassType())) {
            radioGroupPassType.setPosition(-1);
        } else {
            for (int i = 0; i < radioGroupPassType.getButtons().size(); i++) {
                if (radioGroupPassType.getButtons().get(i).getText().contentEquals(entry.getPassType()))
                    radioGroupPassType.setPosition(i);
            }
        }

        if (TextUtils.isEmpty(entry.getLane())) {
            radioGroupLane.setPosition(-1);
        } else {
            for (int i = 0; i < radioGroupLane.getButtons().size(); i++) {
                if (radioGroupLane.getButtons().get(i).getText().contentEquals(entry.getLane()))
                    radioGroupLane.setPosition(i);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonCapture:
                doSaveEntry();
                break;
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

    private void updateButtonState(final boolean state) {
        buttonCapture
                .animate()
                .alpha(state ? 1.0f : 0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        buttonCapture.setVisibility(state ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onClickedButton(RadioRealButton button, int position) {
        Utils.hideKeyboard(this);
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

        entry = new Entry(transactionNumber, registrationNumber, columnNumber, amountPaid, vehicleClass, paymentMethod, passType, lane);
        Utils.persistData(AddEntryActivity.this, Constants.DataBaseStorageKeys.LastPrintedReceipt, new Gson().toJson(entry));

        doPrint(entry);
    }

    private void doPrint(final Entry entry) {
        if (isValid(entry)) {
            MaterialDialog bottomDialog = startProgress();
            if (runPrintReceiptSequence(entry)) {
                setEntry(new Entry());
            }
            stopProgress(bottomDialog);
        }
    }

    private MaterialDialog startProgress() {
        return new MaterialDialog.Builder(this)
                //.title("Loading")
                .content("Printing on progress.")
                .cancelable(false)
                .progress(true, 0)
                .show();

    }

    private void stopProgress(MaterialDialog bottomDialog) {
        bottomDialog.dismiss();
    }

    private boolean runPrintReceiptSequence(Entry entry) {
        if (!tPrinter.initiatePrinter()) {
            return false;
        }
        if (!tPrinter.connect()) return false;

        if (!tPrinter.printBuffer(entry)) {
            return false;
        }
        return true;
    }

    public void onError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowError.onError(AddEntryActivity.this, message, "RETRY", new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {
                        doPrint(entry);
                    }
                }, "CANCEL", new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {
                        //startActivity(new Intent(AddEntryActivity.this, AddEntryActivity.class));
                        //finish();
                    }
                });
            }
        });
    }

    @Override
    public void onConnectionFailed() {
        OnDetectDeviceListener onDetectDeviceListener = new OnDetectDeviceListener() {
            int x = 0;

            @Override
            public void onDetectDevice(DeviceInfo deviceInfo) {
                tPrinter.setPrinterTarget(deviceInfo.getTarget());
                doPrint(entry);
            }

            @Override
            public void onDetectError() {
                if (++x < 5)
                    tPrinter.startDiscovery(this);
                else
                    onError("Searching stopped.");
            }
        };
        tPrinter.startDiscovery(onDetectDeviceListener);
    }

    @Override
    public void onPrinterReady(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AddEntryActivity.this, "Ready to print", Toast.LENGTH_SHORT).show();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                tPrinter.disconnect();
            }
        }).start();
    }

    private boolean isValid(Entry entry) {
        if (entry == null) return false;
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
}
