package styx.mobile.elxpos.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.gson.Gson;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.printer.PrinterCallBacks;
import styx.mobile.elxpos.application.ShowError;
import styx.mobile.elxpos.application.printer.TPrinter;
import styx.mobile.elxpos.application.Utils;
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

        tPrinter = new TPrinter(this, this);

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
                .translationY(buttonCapture.getHeight())
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
                    runPrintReceiptSequence(entry);
                    bottomDialog.dismiss();
                }
            });
        }
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
        if (!tPrinter.initializeObject()) {
            return false;
        }
        if (!tPrinter.printReceipt(entry)) {
            tPrinter.finalizeObject();
            return false;
        }
        return true;
    }

    public void onError(String message) {

    }

    @Override
    public void onClickedButton(RadioRealButton button, int position) {
        Utils.hideKeyboard(this);
    }

    @Override
    public void onPrinterConnected(String status) {
        updateButtonState(true);
        tPrinter.disconnectPrinter();
    }

    @Override
    public void onPrintCompleted() {

    }
}
