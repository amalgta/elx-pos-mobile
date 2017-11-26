package styx.mobile.elxpos.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PreferencesActivity extends AppCompatActivity implements View.OnClickListener {

    private View buttonSave;
    private EditText inputPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Utils.setTitleColor(this, ContextCompat.getColor(this, R.color.blue));

        inputPhoneNumber = findViewById(R.id.inputPhoneNumber);
        buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this);

        bindUI();
    }

    private void bindUI() {
        String contactNumber = Utils.getPersistData(this, Constants.DataBaseStorageKeys.ContactNumber);
        inputPhoneNumber.setText(TextUtils.isEmpty(contactNumber) ? "" : contactNumber);
        inputPhoneNumber.setSelection(inputPhoneNumber.getText().length());
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

    private void doSaveEntry() {
        String contactNumber = inputPhoneNumber.getText().toString();
        Utils.persistData(PreferencesActivity.this, Constants.DataBaseStorageKeys.ContactNumber, contactNumber);
        finish();
    }
}
