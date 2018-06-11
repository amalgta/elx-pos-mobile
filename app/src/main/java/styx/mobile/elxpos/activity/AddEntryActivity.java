package styx.mobile.elxpos.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.epson.epos2.discovery.DeviceInfo;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.gson.Gson;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.application.printer.DiscoverCallBacks;
import styx.mobile.elxpos.application.printer.PrinterCallBacks;
import styx.mobile.elxpos.application.printer.TPrinter;
import styx.mobile.elxpos.model.Entry;

public class AddEntryActivity extends AppCompatActivity implements PrinterCallBacks, View.OnClickListener, RadioRealButtonGroup.OnClickedButtonListener {

    private View buttonSave, containerDatePicker, containerTimePicker;
    private TextView labelReminderDate, labelReminderMinute, labelReminderHour, labelReminderPeriod;
    private EditText inputTransactionNumber, inputRegistrationNumber, inputColumnNumber, inputAmountPaid, inputOperator;
    private RadioRealButtonGroup radioGroupVehicleClass, radioGroupPaymentMethod, radioGroupPassType, radioGroupLane;
    private Entry entry;
    private TPrinter tPrinter;
    private MaterialDialog materialDialog;

    Calendar calendarReminderTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry_activity);

        Utils.setTitleColor(this, ContextCompat.getColor(this, R.color.blue));

        buttonSave = findViewById(R.id.buttonSave);
        inputTransactionNumber = findViewById(R.id.inputTransactionNumber);
        inputRegistrationNumber = findViewById(R.id.inputRegistrationNumber);
        inputColumnNumber = findViewById(R.id.inputColumnNumber);
        inputAmountPaid = findViewById(R.id.inputAmountPaid);
        inputOperator = findViewById(R.id.inputOperator);
        radioGroupVehicleClass = findViewById(R.id.radioGroupVehicleClass);
        radioGroupPaymentMethod = findViewById(R.id.radioGroupPaymentMethod);
        radioGroupPassType = findViewById(R.id.radioGroupPassType);
        radioGroupLane = findViewById(R.id.radioGroupLane);
        labelReminderDate = findViewById(R.id.labelReminderDate);
        labelReminderMinute = findViewById(R.id.labelReminderMinute);
        labelReminderHour = findViewById(R.id.labelReminderHour);
        labelReminderPeriod = findViewById(R.id.labelReminderPeriod);
        containerDatePicker = findViewById(R.id.containerDatePicker);
        containerTimePicker = findViewById(R.id.containerTimePicker);

        containerTimePicker.setOnClickListener(this);
        containerDatePicker.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tPrinter.disconnectPrinter();
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
        inputOperator.setText(TextUtils.isEmpty(entry.getOperator()) ? "" : entry.getOperator());

        if (TextUtils.isEmpty(entry.getVehicleClass())) {
            radioGroupVehicleClass.setPosition(0);
        } else {
            for (int i = 0; i < radioGroupVehicleClass.getButtons().size(); i++) {
                radioGroupVehicleClass.getButtons().get(i).setTypeface(ResourcesCompat.getFont(this, R.font.roboto_thin));
                if (radioGroupVehicleClass.getButtons().get(i).getText().contentEquals(entry.getVehicleClass()))
                    radioGroupVehicleClass.setPosition(i);
            }
        }

        if (TextUtils.isEmpty(entry.getPaymentMethod())) {
            radioGroupPaymentMethod.setPosition(0);
        } else {
            for (int i = 0; i < radioGroupPaymentMethod.getButtons().size(); i++) {
                radioGroupPaymentMethod.getButtons().get(i).setTypeface(ResourcesCompat.getFont(this, R.font.roboto_thin));
                if (radioGroupPaymentMethod.getButtons().get(i).getText().contentEquals(entry.getPaymentMethod()))
                    radioGroupPaymentMethod.setPosition(i);
            }
        }

        if (TextUtils.isEmpty(entry.getPassType())) {
            radioGroupPassType.setPosition(0);
        } else {
            for (int i = 0; i < radioGroupPassType.getButtons().size(); i++) {
                radioGroupPassType.getButtons().get(i).setTypeface(ResourcesCompat.getFont(this, R.font.roboto_thin));
                if (radioGroupPassType.getButtons().get(i).getText().contentEquals(entry.getPassType()))
                    radioGroupPassType.setPosition(i);
            }
        }

        if (TextUtils.isEmpty(entry.getLane())) {
            radioGroupLane.setPosition(0);
        } else {
            for (int i = 0; i < radioGroupLane.getButtons().size(); i++) {
                radioGroupLane.getButtons().get(i).setTypeface(ResourcesCompat.getFont(this, R.font.roboto_thin));
                if (radioGroupLane.getButtons().get(i).getText().contentEquals(entry.getLane()))
                    radioGroupLane.setPosition(i);
            }
        }

        try {
            if (TextUtils.isEmpty(entry.getStartTime())) throw new Exception();
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(new SimpleDateFormat(Constants.DateFormat.PrintDate, Locale.ENGLISH).parse(entry.getStartTime()));
            calendarReminderTime = timeCalendar;
        } catch (Exception e) {
            calendarReminderTime = Calendar.getInstance();
        } finally {
            onTimeSelected();
        }
    }

    private void onTimeSelected() {
        labelReminderDate.setText(
                new SimpleDateFormat("MMMM d", Locale.ENGLISH).format(calendarReminderTime.getTime())
                        .concat(Utils.getDateOrdinal(calendarReminderTime.get(Calendar.DAY_OF_MONTH)))
                        .concat(", ")
                        .concat(String.valueOf(calendarReminderTime.get(Calendar.YEAR))));

        labelReminderHour.setText(new SimpleDateFormat("h", Locale.ENGLISH).format(calendarReminderTime.getTime()));
        labelReminderMinute.setText(new SimpleDateFormat("mm", Locale.ENGLISH).format(calendarReminderTime.getTime()));
        labelReminderPeriod.setText(String.valueOf(calendarReminderTime.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSave:
                doSaveEntry();
                break;
            case R.id.containerDatePicker:
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                calendarReminderTime.set(Calendar.YEAR, year);
                                calendarReminderTime.set(Calendar.MONTH, monthOfYear);
                                calendarReminderTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                onTimeSelected();
                            }
                        },
                        calendarReminderTime.get(Calendar.YEAR),
                        calendarReminderTime.get(Calendar.MONTH),
                        calendarReminderTime.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            case R.id.containerTimePicker:
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                calendarReminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendarReminderTime.set(Calendar.MINUTE, minute);
                                onTimeSelected();
                            }
                        },
                        calendarReminderTime.get(Calendar.HOUR_OF_DAY),
                        calendarReminderTime.get(Calendar.MINUTE), false);
                timePickerDialog.show();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.BundleKeys.PersistedEntry, entry);
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
        String operatorName = inputOperator.getText().toString();

        String vehicleClass = (radioGroupVehicleClass.getPosition() >= 0) ? radioGroupVehicleClass.getButtons().get(radioGroupVehicleClass.getPosition()).getText() : "";
        String paymentMethod = (radioGroupPaymentMethod.getPosition() >= 0) ? radioGroupPaymentMethod.getButtons().get(radioGroupPaymentMethod.getPosition()).getText() : "";
        String passType = (radioGroupPassType.getPosition() >= 0) ? radioGroupPassType.getButtons().get(radioGroupPassType.getPosition()).getText() : "";
        String lane = (radioGroupLane.getPosition() >= 0) ? radioGroupLane.getButtons().get(radioGroupLane.getPosition()).getText() : "";

        String startTime = Utils.getParsedCalendar(calendarReminderTime);
        String endTime = Utils.getParsedCalendar(Utils.getTomorrow(calendarReminderTime));

        entry = new Entry(transactionNumber, registrationNumber, columnNumber, amountPaid, vehicleClass, paymentMethod, passType, lane, startTime, endTime, operatorName);
        Utils.persistData(AddEntryActivity.this, Constants.DataBaseStorageKeys.LastPrintedReceipt, new Gson().toJson(entry));

        if (isValid(entry)) {
            doScanAndPrint();
        }
    }

    private void doScanAndPrint() {
        if (isValid(entry)) {
            startProgress("Establishing connection");
            tPrinter.startDiscovery(new DiscoverCallBacks() {
                @Override
                public void onDeviceDetected(DeviceInfo deviceInfo) {
                    tPrinter.setTarget(deviceInfo.getTarget());
                    tPrinter.startPrint(entry);
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
        if (TextUtils.isEmpty(entry.getOperator())) {
            StyleableToast.makeText(this, "Please enter the operator name.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
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

        if (!isDateValid(calendarReminderTime)) {
            StyleableToast.makeText(this, "Please select a date of future.", Toast.LENGTH_SHORT, R.style.ErrorToast).show();
            return false;
        }
        return true;
    }

    private boolean isDateValid(Calendar dateTime) {
        return dateTime != null;
    }

    @Override
    public void onPrinterReady(String status) {
        stopProgress();
        setEntry(new Entry());
    }

    @Override
    public void onError(Exception errorMessage, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    stopProgress();
                    new BottomDialog.Builder(AddEntryActivity.this)
                            .setCancelable(false)
                            .setContent(message)
                            .setPositiveText("Retry")
                            .onPositive(new BottomDialog.ButtonCallback() {
                                @Override
                                public void onClick(@NonNull BottomDialog bottomDialog) {
                                    doScanAndPrint();
                                }
                            })
                            .setNegativeText("CANCEL")
                            .build()
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    public void onMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    StyleableToast.makeText(AddEntryActivity.this, message, Toast.LENGTH_SHORT, R.style.ErrorToast).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void startProgress(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    stopProgress();
                    materialDialog = new MaterialDialog.Builder(AddEntryActivity.this)
                            .content(message)
                            .cancelable(true)
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    tPrinter.stopDiscovery();
                                }
                            })
                            .progress(true, 0)
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void stopProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (materialDialog == null) return;
                    materialDialog.dismiss();
                    materialDialog = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
