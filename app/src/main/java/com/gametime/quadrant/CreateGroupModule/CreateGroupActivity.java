package com.gametime.quadrant.CreateGroupModule;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gametime.quadrant.CreateGroupSelectAreaModule.CreateGroupSelectAreaActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Network;

import static com.gametime.quadrant.Utils.Constants.EXTRA_LAT_LNG_KEY;

public class CreateGroupActivity extends AppCompatActivity implements CreateGroupContract.CreateGroupView {
    private EditText groupName;
    private EditText groupDesc;
    private String strLatLng;
    private CreateGroupPresenter presenter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        progressBar = findViewById(R.id.progressBar);
        progressBar.bringToFront();
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        groupName = findViewById(R.id.txtBoxGroupName);
        groupDesc = findViewById(R.id.txtBoxGroupDesc);
        Button publicButton = findViewById(R.id.btnPublic);
        Button privateButton = findViewById(R.id.btnPrivate);
        strLatLng = getIntent().getStringExtra(EXTRA_LAT_LNG_KEY);
        presenter = new CreateGroupPresenter(this, this);
        ImageView backButton = findViewById(R.id.backButtonCreateGroup);
        TextView groupNamePartOfGrp = findViewById(R.id.titleCreateGroup);

        groupNamePartOfGrp.setText(R.string.create_group_title);
        publicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                new CreateGroupSelectAreaActivity().finish();
                presenter.createPublicGroup(groupName.getText().toString(),
                        CreateGroupActivity.this,
                        groupDesc.getText().toString(),
                        strLatLng);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        privateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.createPrivateGroup(groupName.getText().toString(), groupDesc.getText().toString(), strLatLng, CreateGroupActivity.this);
            }
        });
    }

    @Override
    public void showSuccessMsg(String msg) {
        Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }

    @Override
    public void progressBarVisibility(Integer visibility) {
        progressBar.setVisibility(visibility);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }
}
