package com.gametime.quadrant.PrivateGroupModule;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gametime.quadrant.BaseActivity;
import com.gametime.quadrant.CreateGroupSelectAreaModule.CreateGroupSelectAreaActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Network;

import java.util.Calendar;

import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_DESCRIPTION_KEY;

public class PrivateGroupActivity extends BaseActivity implements PrivateGroupContract.PrivateGroupView {
    private static final String TAG = "PrivateGroupActivity";
    private String strLatLng;
    private String desc;
    private TextView groupName;
    private TextView groupNameDesc;
    private Button reqOrPwd;
    private Button reqOnly;
    private Button pwdOnly;
    private EditText expiryDate;
    private EditText password;
    private String buttonSelected;
    private TextView next, title;
    private PrivateGroupPresenter presenter;
    private ProgressBar progressBar;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_group);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        groupName = (TextView) findViewById(R.id.txtViewGrpName);
        groupNameDesc = (TextView) findViewById(R.id.txtviewGrpNameDesc);
        reqOrPwd = (Button) findViewById(R.id.btnRequestOrPwd);
        reqOnly = (Button) findViewById(R.id.btnRequestOnly);
        pwdOnly = (Button) findViewById(R.id.btnPwdOnly);
        expiryDate = (EditText) findViewById(R.id.ExpiryDt);
        password = (EditText) findViewById(R.id.enterPwd);
        next = (TextView) findViewById(R.id.nextButtonLink);
        back = findViewById(R.id.backButtonPrivateGroup);
        title = findViewById(R.id.titlePrivateGroup);

        title.setText("Create New Group");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrivateGroupActivity.this, CreateGroupSelectAreaActivity.class);
                intent.putExtra("activityClose", true);

                startActivity(intent);
                finish();
            }
        });
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        desc = getIntent().getStringExtra(EXTRA_GROUP_DESCRIPTION_KEY);
        strLatLng = getIntent().getStringExtra("strLatLng");
        groupName.setText(getIntent().getStringExtra("groupName"));
        groupNameDesc.setText("As the Group Administrator,\n" +
                "do you wish to accept requests\n" +
                "for access to this group?");
        presenter = new PrivateGroupPresenter(this);

        expiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int yr = calendar.get(Calendar.YEAR);
                int mnt = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(PrivateGroupActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                expiryDate.setText(dayOfMonth+"/"+monthOfYear+"/"+year);
                            }
                        }, yr, mnt, day);
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePicker.show();
                datePicker.setCanceledOnTouchOutside(true);
            }
        });
        reqOrPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable left = PrivateGroupActivity.this
                        .getResources().getDrawable(R.drawable
                                .ic_done_white_24dp);
                left.setBounds(0, 0, left.getIntrinsicWidth(),
                        left.getIntrinsicHeight());

                reqOrPwd.setCompoundDrawables(left, null,
                        null, null);
                reqOrPwd.setPadding(20, 0, 0, 0);
                reqOnly.setCompoundDrawables(null, null, null,
                        null);
                pwdOnly.setCompoundDrawables(null, null, null,
                        null);
                buttonSelected = "reqOrPwd";
                expiryDate.setText("");
                expiryDate.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
            }
        });

        reqOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable left = PrivateGroupActivity.this
                        .getResources().getDrawable(R.drawable
                                .ic_done_white_24dp);
                left.setBounds(0, 0, left.getIntrinsicWidth(),
                        left.getIntrinsicHeight());

                reqOnly.setCompoundDrawables(left, null, null,
                        null);
                reqOnly.setPadding(20, 0, 0, 0);
                reqOrPwd.setCompoundDrawables(null, null, null,
                        null);
                pwdOnly.setCompoundDrawables(null, null, null,
                        null);
                buttonSelected = "reqOnly";
                expiryDate.setText("");
                expiryDate.setVisibility(View.VISIBLE);
                password.setVisibility(View.GONE);
            }
        });

        pwdOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable left = PrivateGroupActivity.this
                        .getResources().getDrawable(R.drawable
                                .ic_done_white_24dp);
                left.setBounds(0, 0, left.getIntrinsicWidth(),
                        left.getIntrinsicHeight());

                pwdOnly.setCompoundDrawables(left,
                        null, null, null);
                pwdOnly.setPadding(20, 0, 0, 0);
                reqOrPwd.setCompoundDrawables(null, null, null,
                        null);
                reqOnly.setCompoundDrawables(null, null, null,
                        null);
                buttonSelected = "pwdOnly";
                expiryDate.setVisibility(View.GONE);
                password.setVisibility(View.VISIBLE);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                presenter.createPrivateGroup(buttonSelected, PrivateGroupActivity.this, desc, groupName.getText().toString(), password.getText().toString(), strLatLng);
                /*CreateGroup createGroup = new CreateGroup("PRIVATE",
                        "POLYGON", desc, 1, 0, getIntent()
                        .getStringExtra("groupName"),
                        password.getText().toString(), strLatLng,
                        "MANUAL");

                APIInterface apiInterface = APIClient
                        .getClient()
                        .create(APIInterface.class);
                Call<ResponseBody> callPostGroupInfo = apiInterface
                        .postCreateGroup(XAccessToken, createGroup);
                callPostGroupInfo.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Toast.makeText(PrivateGroupActivity.this,
                                "Group " + groupName.getText().toString() +
                                        " successfully created",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(PrivateGroupActivity.this,
                                HomeActivity.class);
                        intent.putExtra("X-Access-Token", XAccessToken);

                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d(TAG, "onFailure: -> error: " +
                                t.getText());
                    }
                });*/
            }
        });
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
        Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void progressBarVisibility(Integer visibility) {
        progressBar.setVisibility(visibility);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(PrivateGroupActivity.this, CreateGroupSelectAreaActivity.class);
        intent.putExtra("activityClose", true);

        startActivity(intent);
        finish();
    }
}
