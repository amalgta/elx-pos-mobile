package styx.mobile.elxpos.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import styx.mobile.elxpos.R;
import styx.mobile.elxpos.application.Constants;
import styx.mobile.elxpos.application.Utils;

public class PreferencesActivity extends AppCompatActivity implements View.OnClickListener {

    private View buttonSave, iconBack;
    private EditText inputPhoneNumber;
    private EditText inputTitle1;
    private EditText inputTitle2;
    private EditText inputFooter1;
    private EditText inputFooter2;
    private EditText inputFooter3;
    private EditText inputFooter4;
    private EditText inputFooter5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Utils.setTitleColor(this, ContextCompat.getColor(this, R.color.blue));

        inputPhoneNumber = findViewById(R.id.inputPhoneNumber);
        inputTitle1 = findViewById(R.id.inputTitle1);
        inputTitle2 = findViewById(R.id.inputTitle2);
        inputFooter1 = findViewById(R.id.inputFooter1);
        inputFooter2 = findViewById(R.id.inputFooter2);
        inputFooter3 = findViewById(R.id.inputFooter3);
        inputFooter4 = findViewById(R.id.inputFooter4);
        inputFooter5 = findViewById(R.id.inputFooter5);

        iconBack = findViewById(R.id.iconBack);
        buttonSave = findViewById(R.id.buttonSave);

        iconBack.setOnClickListener(this);
        buttonSave.setOnClickListener(this);

        bindUI();
    }

    private void bindUI() {
        inputPhoneNumber.setText(Utils.getPersistData(this, Constants.DataBaseStorageKeys.ContactNumber));
        inputTitle1.setText(Utils.getPersistData(this, Constants.DataBaseStorageKeys.inputTitle1));
        inputTitle2.setText(Utils.getPersistData(this, Constants.DataBaseStorageKeys.inputTitle2));
        inputFooter1.setText(Utils.getPersistData(this, Constants.DataBaseStorageKeys.inputFooter1));
        inputFooter2.setText(Utils.getPersistData(this, Constants.DataBaseStorageKeys.inputFooter2));
        inputFooter3.setText(Utils.getPersistData(this, Constants.DataBaseStorageKeys.inputFooter3));
        inputFooter4.setText(Utils.getPersistData(this, Constants.DataBaseStorageKeys.inputFooter4));
        inputFooter5.setText(Utils.getPersistData(this, Constants.DataBaseStorageKeys.inputFooter5));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iconBack:
                onBackPressed();
                break;
            case R.id.buttonSave:
                doSaveEntry();
                break;
        }
    }

    private void doSaveEntry() {
        Utils.persistData(PreferencesActivity.this, Constants.DataBaseStorageKeys.ContactNumber, inputPhoneNumber.getText().toString());
        Utils.persistData(PreferencesActivity.this, Constants.DataBaseStorageKeys.inputTitle1, inputTitle1.getText().toString());
        Utils.persistData(PreferencesActivity.this, Constants.DataBaseStorageKeys.inputTitle2, inputTitle2.getText().toString());
        Utils.persistData(PreferencesActivity.this, Constants.DataBaseStorageKeys.inputFooter1, inputFooter1.getText().toString());
        Utils.persistData(PreferencesActivity.this, Constants.DataBaseStorageKeys.inputFooter2, inputFooter2.getText().toString());
        Utils.persistData(PreferencesActivity.this, Constants.DataBaseStorageKeys.inputFooter3, inputFooter3.getText().toString());
        Utils.persistData(PreferencesActivity.this, Constants.DataBaseStorageKeys.inputFooter4, inputFooter4.getText().toString());
        Utils.persistData(PreferencesActivity.this, Constants.DataBaseStorageKeys.inputFooter5, inputFooter5.getText().toString());
        finish();
    }
}
