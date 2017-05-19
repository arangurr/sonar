package com.arangurr.sonar.ui;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.arangurr.sonar.Constants;
import com.arangurr.sonar.R;

public class IntroActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_intro);

    final TextInputLayout usernameTextInput =
        (TextInputLayout) findViewById(R.id.textinputlayout_intro);
    final EditText usernameEditText = (EditText) findViewById(R.id.edittext_intro_username);
    final FloatingActionButton doneFab = (FloatingActionButton) findViewById(R.id.fab_intro_done);

    doneFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int editTextLength = usernameEditText.getText().length();
        if (editTextLength < 4) {
          if (editTextLength == 0) {
            usernameTextInput.setError(getString(R.string.intro_error_empty_name));
          } else {
            usernameTextInput.setError(getString(R.string.intro_error_short_name));
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

    usernameEditText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == IME_ACTION_DONE) {
          doneFab.performClick();
          return true;
        }
        return false;
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
