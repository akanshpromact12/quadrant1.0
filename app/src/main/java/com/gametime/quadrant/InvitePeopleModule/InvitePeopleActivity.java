package com.gametime.quadrant.InvitePeopleModule;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gametime.quadrant.CreateGroupSelectAreaModule.CreateGroupSelectAreaActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Network;

public class InvitePeopleActivity extends AppCompatActivity implements InvitePeopleContract.InvitePeopleView {
    Button InvitePeople, ReturnToGroups;
    ImageView back;
    private boolean hideText;
    private TextView Excellent, grpCreated;
    private String groupType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_people);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        back = findViewById(R.id.backButtonInvitePeople);
        InvitePeople = findViewById(R.id.btnInvitePeople);
        ReturnToGroups = findViewById(R.id.btnReturnToGrps);
        Excellent = findViewById(R.id.txtViewExcellent);
        grpCreated = findViewById(R.id.txtViewGrpCreated);

        if (getIntent().hasExtra("hideText")) {
            hideText = getIntent().getBooleanExtra("hideText", false);

            if (hideText) {
                Toast.makeText(this, "hideText ", Toast.LENGTH_SHORT).show();

                Excellent.setVisibility(View.GONE);
                grpCreated.setVisibility(View.GONE);
            }
        }

        if (getIntent().hasExtra("groupType")) {
            groupType = getIntent().getStringExtra("groupType");

            if (groupType.equals("public")) {
                Snackbar.make(getWindow().getDecorView().getRootView(), "public group created successfully created", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView(), "private group created successfully created", Snackbar.LENGTH_LONG).show();
            }
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InvitePeopleActivity.this, CreateGroupSelectAreaActivity.class);
                intent.putExtra("activityClose", true);

                startActivity(intent);
                finish();
            }
        });
        InvitePeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String body = "Hey, I've created a group. Join me at ...";

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(shareIntent, "Share using..."));
            }
        });

        ReturnToGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (getIntent().hasExtra("hideText")) {
                    /*hideText = getIntent().getBooleanExtra("hideText", false);

                    if (hideText) {*/
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    //}
                /*} else {
                    Intent intent = new Intent(InvitePeopleActivity.this, CreateGroupSelectAreaActivity.class);
                    intent.putExtra("activityClose", true);

                    startActivity(intent);
                    finish();
                    CreateGroupSelectAreaActivity activity =  new CreateGroupSelectAreaActivity();
                    activity.finish();
                }*/
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(InvitePeopleActivity.this, CreateGroupSelectAreaActivity.class);
        intent.putExtra("activityClose", true);

        startActivity(intent);
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
    public void showMessage(String msg) {

    }
}
