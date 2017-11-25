package styx.mobile.elxpos.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.epson.epos2.discovery.DeviceInfo;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.gson.Gson;
import com.wang.avi.AVLoadingIndicatorView;

import styx.mobile.elxpos.R;
import styx.mobile.elxpos.adapter.PrinterRecyclerAdapter;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.Utils;
import styx.mobile.elxpos.application.printer.OnDetectDeviceListener;
import styx.mobile.elxpos.application.printer.PrinterCallBacks;
import styx.mobile.elxpos.application.printer.TPrinter2;
import styx.mobile.elxpos.model.Device;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DiscoverDeviceActivity extends AppCompatActivity implements View.OnClickListener, PrinterRecyclerAdapter.OnDeviceSelectedListener {
    Toolbar toolbar;
    RecyclerView recyclerViewPrinterList;
    PrinterRecyclerAdapter adapter;
    AVLoadingIndicatorView progressView;
    View buttonRestartDiscovery;
    TPrinter2 tPrinter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_device);
        Utils.setTitleColor(this, ContextCompat.getColor(this, R.color.colorPrimary));

        toolbar = findViewById(R.id.toolbar);
        progressView = findViewById(R.id.progressView);
        recyclerViewPrinterList = findViewById(R.id.recyclerViewPrinterList);
        buttonRestartDiscovery = findViewById(R.id.buttonRestartDiscovery);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new PrinterRecyclerAdapter(this);
        recyclerViewPrinterList.setHasFixedSize(true);
        recyclerViewPrinterList.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPrinterList.setAdapter(adapter);

        buttonRestartDiscovery.setOnClickListener(this);

        tPrinter2 = new TPrinter2(this, new PrinterCallBacks() {
            @Override
            public void onPrinterReady(String status) {

            }

            @Override
            public void onError(Exception errorMessage, String message) {

            }

            @Override
            public void onMessage(String message) {

            }

        });
        tPrinter2.startDiscovery(new OnDetectDeviceListener() {
            @Override
            public void onDetectDevice(final DeviceInfo deviceInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter == null) return;
                        progressView.smoothToHide();
                        adapter.add(deviceInfo);
                    }
                });
            }

            @Override
            public void onDetectError() {

            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    private void restartDiscovery() {
        progressView.smoothToHide();
        tPrinter2.stopDiscovery();

        adapter.clear();
        progressView.smoothToShow();

        tPrinter2.startDiscovery(new OnDetectDeviceListener() {
            @Override
            public void onDetectDevice(final DeviceInfo deviceInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter == null) return;
                        progressView.smoothToHide();
                        adapter.add(deviceInfo);
                    }
                });
            }

            @Override
            public void onDetectError() {

            }
        });
    }

    public void onError() {
        new BottomDialog.Builder(this)
                .setTitle("Error")
                .setContent("We hit an error while processing the request. Please try again.")
                .setNegativeText("Dismiss")
                .setNegativeTextColorResource(R.color.colorAccent)
                .setNegativeTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setPositiveText("Retry")
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        restartDiscovery();
                    }
                }).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tPrinter2.stopDiscovery();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonRestartDiscovery:
                restartDiscovery();
                break;

            default:
                break;
        }
    }

    @Override
    public void onDeviceSelected(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            onError();
            return;
        }

        Utils.persistData(this, Constants.DataBaseStorageKeys.Device, new Gson().toJson(new Device(deviceInfo)));
        Intent intent = new Intent();
        intent.putExtra(Constants.BundleKeys.DeviceName, deviceInfo.getDeviceName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
