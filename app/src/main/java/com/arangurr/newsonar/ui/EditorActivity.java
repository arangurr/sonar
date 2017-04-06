package com.arangurr.newsonar.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.BinaryQuestion;
import com.arangurr.newsonar.data.Poll;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    //    private TextView mDurationHeader;
//    private ImageView mWarningImage;
//    private SeekBar mDurationSeekBar;
    private Switch mPasswordSwitch;
    private EditText mPasswordEditText;
    private TextView mPrivacyTextView;
    private Spinner mPrivacySpinner;
    private LinearLayout mSettingsHeader;
    private LinearLayout mSettingsContent;
    private ImageButton mAddButton;
    private LinearLayout mContainer;

    private Poll mPoll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_editor);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // If creating a new Poll.
        mPoll = new Poll();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        mDurationHeader = (TextView) findViewById(R.id.textview_editor_duration_header);
//        mWarningImage = (ImageView) findViewById(R.id.image_editor_warning);
//        mDurationSeekBar = (SeekBar) findViewById(R.id.seekbar_editor_duration);
        mPasswordSwitch = (Switch) findViewById(R.id.switch_editor_password);
        mPasswordEditText = (EditText) findViewById(R.id.edittext_editor_password);
        mPrivacyTextView = (TextView) findViewById(R.id.textview_editor_privacy_explanation);
        mPrivacySpinner = (Spinner) findViewById(R.id.spinner_editor_privacy_selector);
        mSettingsHeader = (LinearLayout) findViewById(R.id.linearlayout_editor_header_settings);
        mSettingsContent = (LinearLayout) findViewById(R.id.linearlayout_editor_content_settings);
        mAddButton = (ImageButton) findViewById(R.id.button_editor_add);
        mContainer = (LinearLayout) findViewById(R.id.linearlayout_editor_container);

        mAddButton.setOnClickListener(this);

//        mDurationSeekBar.setProgress(3);
//
//        mDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                String[] array = getResources().getStringArray(R.array.array_publish_durations);
//                mDurationHeader.setText(String.format("Duration: %s", array[progress]));
//
//                mWarningImage.setVisibility(progress ==
//                        seekBar.getMax() ? View.VISIBLE : View.INVISIBLE);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
        mPasswordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPasswordEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        mPrivacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] privacyStrings = getResources().getStringArray(R.array
                        .array_privacy_explained);

                mPrivacyTextView.setText(privacyStrings[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSettingsHeader.setOnClickListener(this);

        setDefaults();
    }

    private void setDefaults() {
        mPrivacySpinner.setSelection(1); // By default all votes will be anonymous
        mPrivacyTextView.setText(getResources().getStringArray(R.array.array_privacy_explained)[1]);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linearlayout_editor_header_settings:
                boolean newState = !v.isActivated();
                v.setActivated(newState);
                mSettingsContent.setVisibility(newState ? View.GONE : View.VISIBLE);
                break;
            case R.id.button_editor_add:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final String[] array = getResources().getStringArray(R.array
                        .array_binaryquestion_modes);
                builder.setTitle("Binary Question").setItems(array, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BinaryQuestion q;
                        switch (which) {
                            case 0:
                                q = new BinaryQuestion("Test Title",
                                        Constants.BINARY_MODE_YESNO);
                                break;
                            case 1:
                                q = new BinaryQuestion("Test Title",
                                        Constants.BINARY_MODE_TRUEFALSE);
                                break;
                            case 2:
                                q = new BinaryQuestion("Test Title",
                                        Constants.BINARY_MODE_UPDOWNVOTE);
                                break;
                            case 3:
                                q = new BinaryQuestion("Test Title",
                                        Constants.BINARY_MODE_CUSTOM);
                                // TODO: 05/04/2017 add custom options
                                break;
                        }
                    }
                });
                builder.show();
                break;
            default:
        }
    }
}
