package styx.mobile.elxpos.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.gson.Gson;

import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.application.printer.PrinterUtils;
import styx.mobile.elxpos.model.Entry;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    View buttonViewLastReceipt;
    View buttonAddEntry;
    View buttonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonViewLastReceipt = findViewById(R.id.buttonViewLastReceipt);
        buttonSettings = findViewById(R.id.buttonSettings);
        buttonAddEntry = findViewById(R.id.buttonAddEntry);

        Utils.setTitleColor(this, ContextCompat.getColor(this, R.color.yellow_1));

        buttonViewLastReceipt.setOnClickListener(this);
        buttonAddEntry.setOnClickListener(this);
        buttonSettings.setOnClickListener(this);

        initializePreferences();
    }

    private void initializePreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean(Constants.BundleKeys.firstTime, false)) {
            PrinterUtils.generateDefaults(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Constants.BundleKeys.firstTime, true);
            editor.apply();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.buttonAddEntry:
                intent = new Intent(MainActivity.this, AddEntryActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonSettings:
                intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonViewLastReceipt:
                if (Utils.getPersistData(this, Constants.DataBaseStorageKeys.LastPrintedReceipt) != null) {
                    Entry entry = new Gson().fromJson(Utils.getPersistData(this, Constants.DataBaseStorageKeys.LastPrintedReceipt), Entry.class);
                    intent = new Intent(MainActivity.this, AddEntryActivity.class);
                    intent.putExtra(Constants.BundleKeys.Entry, entry);
                    startActivity(intent);
                } else {
                    new BottomDialog.Builder(this)
                            .setTitle("Error")
                            .setContent("No saved receipts. Please create one.")
                            .setPositiveText("CREATE")
                            .onPositive(new BottomDialog.ButtonCallback() {
                                @Override
                                public void onClick(@NonNull BottomDialog dialog) {
                                    startActivity(new Intent(MainActivity.this, AddEntryActivity.class));
                                }
                            }).setCancelable(false)
                            .setNegativeText("DISMISS")
                            .show();
                }
                break;
        }
    }
}
