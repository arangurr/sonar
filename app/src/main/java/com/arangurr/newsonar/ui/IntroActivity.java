package com.arangurr.newsonar.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.R;

import java.util.UUID;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);


        final TextInputLayout usernameTextInput =
                (TextInputLayout) findViewById(R.id.textinputlayout_intro);
        final EditText usernameEditText = (EditText) findViewById(R.id.edittext_intro_username);
        FloatingActionButton doneFab = (FloatingActionButton) findViewById(R.id.fab_intro_done);

        doneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int editTextLength = usernameEditText.getText().length();
                if (editTextLength < 4) {
                    if (editTextLength == 0) {
                        usernameTextInput.setError("You need to enter a name");
                    } else {
                        usernameTextInput.setError("You need to enter a longer name");
                    }
                } else {
                    SharedPreferences preferences = getDefaultSharedPreferences
                            (getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();

                    @SuppressLint("HardwareIds") String uuid = Settings.Secure.getString
                            (getContentResolver(), Settings.Secure.ANDROID_ID);
                    editor.putString(Constants.KEY_UUID, uuid);

                    editor.putString(Constants.KEY_USERNAME, usernameEditText.getText().toString());

                    editor.apply();

                    startActivity(new Intent(IntroActivity.this, DashboardActivity.class));
                    finish();
                }
            }
        });

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (usernameTextInput.getError() != null && s.length() >= 4) {
                    usernameTextInput.setError(null);
                }

            }
        });
    }
}
