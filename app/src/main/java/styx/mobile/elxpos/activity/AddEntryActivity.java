package styx.mobile.elxpos.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.application.printer.DiscoverCallBacks;
import styx.mobile.elxpos.application.printer.PrinterCallBacks;
import styx.mobile.elxpos.application.printer.TPrinter;
import styx.mobile.elxpos.model.Entry;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddEntryActivity extends AppCompatActivity implements PrinterCallBacks, View.OnClickListener, RadioRealButtonGroup.OnClickedButtonListener {

    private View buttonCapture;
    private EditText inputTransactionNumber, inputRegistrationNumber, inputColumnNumber, inputAmountPaid;
    private RadioRealButtonGroup radioGroupVehicleClass, radioGroupPaymentMethod, radioGroupPassType, radioGroupLane;
    private Entry entry;
    private TPrinter tPrinter;
    private MaterialDialog materialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry_activity);

        Utils.setTitleColor(this, ContextCompat.getColor(this, R.color.blue));

        buttonCapture = findViewById(R.id.buttonSave);
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
            case R.id.buttonSave:
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

    private void showProgress(String message) {
        stopProgress();
        materialDialog = new MaterialDialog.Builder(this)
                //.title("Loading")
                .content(message)
                .cancelable(false)
                .progress(true, 0)
                .show();
    }

    private void stopProgress() {
        if (materialDialog == null) return;
        materialDialog.dismiss();
        materialDialog = null;
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

        if (isValid(entry)) {
            showProgress("Scanning for device.");
            tPrinter.startDiscovery(new DiscoverCallBacks() {
                @Override
                public void onDeviceDetected(DeviceInfo deviceInfo) {
                    tPrinter.setTarget(deviceInfo.getTarget());
                    showProgress("Printing on progress.");
                    if (!tPrinter.runPrintReceiptSequence(entry)) {
                        //TODO
                    }else {
                        setEntry(new Entry());
                    }
                    stopProgress();
                }
            });
        }

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

    @Override
    public void onPrinterReady(String status) {
        stopProgress();
    }

    @Override
    public void onError(Exception errorMessage, String message) {
        stopProgress();
        new BottomDialog.Builder(this)
                .setCancelable(false)
                .setContent(message)
                .setPositiveText("Retry")
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {

                    }
                })
                .setNegativeText("CANCEL")
                .build()
                .show();
        //ShowMsg.showException(errorMessage, message);
    }

    @Override
    public void onMessage(String message) {
        stopProgress();
        new BottomDialog.Builder(this)
                .setCancelable(false)
                .setContent(message)
                .setPositiveText("Retry")
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {

                    }
                })
                .setNegativeText("CANCEL")
                .build()
                .show();
    }

    public class BackgroundTask extends AsyncTask<String, Integer, String > {
        private ProgressDialog mProgressDialog;
        int progress;
        public BackgroundTask() {
            mProgressDialog = new ProgressDialog(AddEntryActivity.this);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(0);
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog =ProgressDialog.show(AddEntryActivity.this, "", "Loading...",true,false);
            super.onPreExecute();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... params) {

            return "";
        }
        @Override
        protected void onPostExecute(String  result) {
            Toast.makeText(AddEntryActivity.this, result, Toast.LENGTH_LONG).show();
            mProgressDialog.dismiss();
            super.onPostExecute(result);
        }
    }
}
