package com.gametime.quadrant.EnterPasswordModule;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gametime.quadrant.Models.Group;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Network;

import static com.gametime.quadrant.Utils.Constants.EXTRA_NEARBY_GROUP_OBJ;

public class EnterPasswordActivity extends AppCompatActivity implements EnterPasswordContract.EnterPasswordView {
    TextView done;
    EditText password;
    ImageView backButton, membersOfGrp, cancelPwd;
    TextView adminOfGroup, groupNamePartOfGrp, NoOfMembers;
    EnterPasswordPresenter presenter;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        NoOfMembers = findViewById(R.id.NoOfMembers);
        NoOfMembers.setVisibility(View.GONE);
        membersOfGrp.setVisibility(View.GONE);
        adminOfGroup.setVisibility(View.GONE);
        cancelPwd = findViewById(R.id.cancelPwd);
        password = findViewById(R.id.enterPrivGrpPwd);
        done = findViewById(R.id.sendPwd);
        backButton = findViewById(R.id.backButton);
        membersOfGrp = findViewById(R.id.membersOfGrp);
        adminOfGroup = findViewById(R.id.adminOfGroup);
        groupNamePartOfGrp = findViewById(R.id.groupNamePartOfGrp);
        presenter = new EnterPasswordPresenter(this);
        group = (Group) getIntent().getSerializableExtra(EXTRA_NEARBY_GROUP_OBJ);

        cancelPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.fileMembershipToGrp(String.valueOf(group.getId()), EnterPasswordActivity.this, password.getText().toString(), group.getRequestAcceptType());
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void showMessage(String msg) {
        Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }
}
