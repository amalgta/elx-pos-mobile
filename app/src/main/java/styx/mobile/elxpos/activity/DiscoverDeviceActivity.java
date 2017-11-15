package styx.mobile.elxpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Printer;
import android.view.View;

import styx.mobile.elxpos.Constants;
import styx.mobile.elxpos.R;
import styx.mobile.elxpos.adapter.PrinterRecyclerAdapter;

import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.Epos2Exception;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class DiscoverDeviceActivity extends AppCompatActivity implements View.OnClickListener, PrinterRecyclerAdapter.OnDeviceSelectedListener {
    Toolbar toolbar;
    RecyclerView recyclerViewPrinterList;
    PrinterRecyclerAdapter adapter;
    AVLoadingIndicatorView progressView;
    View buttonRestartDiscovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_device);
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

        startDiscovery();
    }

    private static FilterOption generateFilterOption() {
        FilterOption filterOption = new FilterOption();
        filterOption.setDeviceType(Discovery.TYPE_PRINTER);
        filterOption.setEpsonFilter(Discovery.FILTER_NAME);
        return filterOption;
    }

    private void startDiscovery() {
        try {
            Discovery.start(this, generateFilterOption(), mDiscoveryListener);
        } catch (Exception e) {
            onError();
            e.printStackTrace();
        }
    }

    private void stopDiscovery() {
        while (true) {
            try {
                Discovery.stop();
                break;
            } catch (Epos2Exception e) {
                if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                    onError();
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void restartDiscovery() {
        progressView.smoothToHide();
        stopDiscovery();

        adapter.clear();
        progressView.smoothToShow();

        try {
            Discovery.start(this, generateFilterOption(), mDiscoveryListener);
        } catch (Exception e) {
            onError();
            e.printStackTrace();
        }
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
        stopDiscovery();
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

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    if (adapter == null) return;
                    progressView.smoothToHide();
                    adapter.add(deviceInfo);
                }
            });
        }
    };

    @Override
    public void onDeviceSelected(DeviceInfo deviceInfo) {
        Intent intent = new Intent();
        intent.putExtra(Constants.BundleKeys.DeviceTarget, deviceInfo.getTarget());
        setResult(RESULT_OK, intent);
        finish();
    }
}
