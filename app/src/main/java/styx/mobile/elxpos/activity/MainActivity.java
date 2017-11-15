package styx.mobile.elxpos.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import styx.mobile.elxpos.R;
import styx.mobile.elxpos.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    View buttonConfigurePrinter, buttonViewLastReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonConfigurePrinter = findViewById(R.id.buttonConfigurePrinter);
        buttonViewLastReceipt = findViewById(R.id.buttonViewLastReceipt);

        Utils.setTitleColor(this, ContextCompat.getColor(this, R.color.blue));

        buttonConfigurePrinter.setOnClickListener(this);
        buttonViewLastReceipt.setOnClickListener(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.buttonConfigurePrinter:
                intent = new Intent(MainActivity.this, DiscoverDeviceActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonViewLastReceipt:
                intent = new Intent(MainActivity.this, DiscoverDeviceActivity.class);
                startActivity(intent);
                break;
        }
    }
}
