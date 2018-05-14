package com.gametime.quadrant.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.gametime.quadrant.GroupMessagesModule.GroupMessageActivity;
import com.gametime.quadrant.Models.GroupMessageRealm;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Models.ReportUser;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.AccessVariables;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {
    private Context context;
    private List<GroupMessageRealm> messages;
    private QuadrantLoginDetails login;
    private boolean adminMsg;
    private String admin;
    private final String root = AccessVariables.root;
    private Boolean uploadFailed = false;
    private Boolean imageUpload = false;
    private PopupWindow window;
    private boolean fileExists = false;
    private android.support.v7.view.ActionMode actionMode;
    private int count = 0;

    public ChatsAdapter(Context context, List<GroupMessageRealm> messages, boolean adminMsg, String admin) {
        this.context = context;
        this.messages = messages;
        this.adminMsg = adminMsg;
        this.admin = admin;
        String loginDetails = context.getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(loginDetails, QuadrantLoginDetails.class);
    }

    public ChatsAdapter(Context context, List<GroupMessageRealm> messages, Boolean uploadFailed) {
        this.context = context;
        this.messages = messages;
        this.uploadFailed = uploadFailed;
    }

    public ChatsAdapter(Context context, List<GroupMessageRealm> messages, Boolean imageUpload, String s, int i) {
        this.context = context;
        this.messages = messages;
        this.imageUpload = imageUpload;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.chats_display,null);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChatViewHolder holder, final int position) {
        final GroupMessageRealm messagesItem = messages.get(position);
        holder.uploadImage.setVisibility(View.GONE);
        if (imageUpload) {
            holder.uploadImage.setVisibility(View.VISIBLE);
            holder.download.setVisibility(View.GONE);

            Glide.with(context)
                    .load(messagesItem.getImageUrl())
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                    .into(holder.imageMessage);
        } else {
            holder.uploadImage.setVisibility(View.GONE);
        }

        if (adminMsg) {
            if (messagesItem.getNick().equalsIgnoreCase(admin)) {
                String dateTime = messagesItem.getTime();
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                try {
                    Date date = dt.parse(dateTime);
                    SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy, hh:mm a", Locale.getDefault());
                    String dtt = format.format(date);

                    holder.dateTimeMsgSent.setText(dtt);
                    Log.d(TAG, "onBindViewHolder: date -> " + dtt + "\n" + messagesItem.getNick()+"\n"+messagesItem.getBody());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (uploadFailed) {
                    holder.download.setVisibility(View.GONE);
                    holder.upload.setVisibility(View.VISIBLE);
                    holder.upload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.uploadImage.setVisibility(View.VISIBLE);
                            int size = messagesItem.getImageUrl().split("/").length;
                            //((GroupMessageActivity) context).reUploadImage(messagesItem.getImageUrl().split("/")[size-1], messagesItem.getTime());
                        }
                    });
                }

                holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url;
                        File file = new File(root+"/"+messagesItem.getImageName());
                        if (file.exists()) {
                            Uri imageUri = Uri.fromFile(file);
                            url = imageUri.toString();
                            fileExists = true;
                        } else {
                            url = login.getResourceHostDomain() + messagesItem.getImageUrl();
                            fileExists = false;
                        }
                        showPopupWindow(url, messagesItem.getNick(), messagesItem.getTime(), fileExists);
                    }
                });

                holder.nameOfSender.setText(messagesItem.getNick());
                if (messagesItem.getImageUrl() == null
                        || messagesItem.getImageUrl().isEmpty()) {
                    holder.imageRelLayout.setVisibility(View.GONE);
                    holder.textMessage.setVisibility(View.VISIBLE);
                    holder.download.setVisibility(View.GONE);
                    holder.textMessage.setText(messagesItem.getText());
                } else {
                    holder.imageRelLayout.setVisibility(View.VISIBLE);
                    holder.textMessage.setVisibility(View.GONE);
                    holder.imageMessage.setBackgroundResource(R.drawable.placeholder);

                    File dir = new File(root);
                    if (!dir.exists()) {
                        if (!dir.mkdirs()) {
                            Log.e(TAG, "onBindViewHolder: -> there was some problem creating dir", new Exception("Some problem occured while creating directory"));
                        }
                    }

                    File file = new File(root+"/"+messagesItem.getImageName());
                    if (file.exists()) {
                        Uri imageUri = Uri.fromFile(file);
                        Glide.with(context)
                                .load(imageUri)
                                .into(holder.imageMessage);
                        holder.download.setVisibility(View.GONE);
                    } else {
                        if (messagesItem.getThumbnailUrl() != null) {
                            holder.uploadImage.setVisibility(View.VISIBLE);
                            Glide.with(context).load(login.getResourceHostDomain() + messagesItem.getThumbnailUrl())
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            holder.uploadImage.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            holder.uploadImage.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(holder.imageMessage);
                            holder.download.setVisibility(View.VISIBLE);
                        }
                    }
                }

                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.uploadImage.setVisibility(View.VISIBLE);
                        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
                        if (messagesItem.getImageUrl() != null) {
                            Glide.with(context).asBitmap().load(login.getResourceHostDomain() + messagesItem.getImageUrl())
                                    .listener(new RequestListener<Bitmap>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                            holder.uploadImage.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                            holder.uploadImage.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                    String path = saveImage(resource, messagesItem.getImageName());
                                    File parent = new File(path);

                                    File dir = new File(parent.getParent());
                                    if (!dir.exists()) {
                                        if (!dir.mkdirs()) {
                                            Log.e(TAG, "onBindViewHolder: -> there was some problem creating dir", new Exception("Some problem occured while creating directory"));
                                        }
                                    }

                                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                                    holder.imageMessage.setImageBitmap(bitmap);
                                    holder.download.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });

                holder.groupDisplay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        count++;
                        actionMode = ((GroupMessageActivity) context).startSupportActionMode(new android.support.v7.view.ActionMode.Callback() {
                            @Override
                            public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
                                mode.getMenuInflater().inflate(R.menu.action_menu, menu);
                                mode.setTitle("Choose your action");

                                return true;
                            }

                            @Override
                            public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
                                return false;
                            }

                            @Override
                            public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.action_menu_copy:
                                        //Toast.makeText(context, "Copy option selected...", Toast.LENGTH_SHORT).show();
                                        String getText = holder.textMessage.getText().toString();
                                        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clipData = ClipData.newPlainText("Clip Text" , getText);
                                        clipboardManager.setPrimaryClip(clipData);
                                        mode.finish();

                                        return true;
                                    default:
                                        return false;
                                }
                            }

                            @Override
                            public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
                                actionMode = null;
                            }
                        });

                        if (count > 1) {
                            if (actionMode != null)
                                actionMode.finish();
                            count = 0;
                        }
                    }
                });

                holder.groupDisplay.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!holder.nameOfSender.getText().equals(login.getXmppUserDetails().getNick())) {
                            LayoutInflater inflater = ((GroupMessageActivity) context).getLayoutInflater();
                            View alertView = inflater.inflate(R.layout.custom_alert, null);
                            final EditText reportString = alertView.findViewById(R.id.alertReportMessage);

                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context)
                                    .setTitle(context.getString(R.string.report_message_title))
                                    .setView(alertView)
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                                            if (!TextUtils.isEmpty(reportString.getText().toString())) {
                                                APIInterface apiInterface = APIClient.getClientWithAuth(context)
                                                        .create(APIInterface.class);
                                                ReportUser reportUser = new ReportUser(login.getId().toString(), reportString.getText().toString(), "message", newDate, messagesItem.getXml(), newDate, login.getXmppUserDetails().getJid(), messagesItem.getGroupJid());
                                                Call<ResponseBody> userReport = apiInterface.reportUser(reportUser);
                                                userReport.enqueue(new Callback<ResponseBody>() {
                                                    @Override
                                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                        try {
                                                            Log.d(TAG, "Reporting user: " + response.body().string());
                                                        } catch (Exception ex) {
                                                            ex.printStackTrace();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                        Log.e(TAG, "Something went wrong. Please try again later");
                                                    }
                                                });
                                                Toast.makeText(context, "The user has been reported....", Toast.LENGTH_SHORT).show();
                                            }

                                            dialog.dismiss();
                                        }
                                    })
                                    .setCancelable(false);
                            final AlertDialog dialog = alertBuilder.create();
                            dialog.show();

                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                if (holder.nameOfSender.getText().equals(login.getXmppUserDetails().getNick())) {
                    holder.groupDisplay.setBackgroundResource(R.color.senderAsSelfColor);
                } else {
                    holder.groupDisplay.setBackgroundResource(R.color.senderColor);
                }
            } else {
                holder.groupDisplay.setVisibility(View.GONE);
            }
        } else {
            String dateTime = messagesItem.getTime();
            Log.d("::::TAG", "date: " + dateTime);
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            try {
                Date date = dt.parse(dateTime);
                SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy, hh:mm a", Locale.getDefault());
                String dtt = format.format(date);

                holder.dateTimeMsgSent.setText(dtt);
                Log.d(TAG, "onBindViewHolder: date -> " + dtt + "\n" + messagesItem.getNick()+"\n"+messagesItem.getBody());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (uploadFailed) {
                holder.download.setVisibility(View.GONE);
                holder.upload.setVisibility(View.VISIBLE);
                holder.upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.uploadImage.setVisibility(View.VISIBLE);
                        int size = messagesItem.getImageUrl().split("/").length;
                        //((GroupMessageActivity) context).reUploadImage(messagesItem.getImageUrl().split("/")[size-1], messagesItem.getTime());
                    }
                });
            }

            holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url;
                    File file = new File(root+"/"+messagesItem.getImageName());
                    if (file.exists()) {
                        Uri imageUri = Uri.fromFile(file);
                        url = imageUri.toString();
                        fileExists = true;
                    } else {
                        url = login.getResourceHostDomain() + messagesItem.getImageUrl();
                        fileExists = false;
                    }
                    showPopupWindow(url, messagesItem.getNick(), holder.dateTimeMsgSent.getText().toString(), fileExists);
                }
            });

            holder.groupDisplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionMode = ((GroupMessageActivity) context).startSupportActionMode(new android.support.v7.view.ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
                            mode.getMenuInflater().inflate(R.menu.action_menu, menu);
                            mode.setTitle("Choose your action");

                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
                            return false;
                        }

                        @Override
                        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_menu_copy:
                                    //Toast.makeText(context, "Copy option selected...", Toast.LENGTH_SHORT).show();
                                    String getText = holder.textMessage.getText().toString();
                                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clipData = ClipData.newPlainText("Clip Text" , getText);
                                    clipboardManager.setPrimaryClip(clipData);
                                    mode.finish();
                                    return true;
                                default:
                                    return false;
                            }
                        }

                        @Override
                        public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
                            actionMode = null;
                        }
                    });
                }
            });

            holder.groupDisplay.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!holder.nameOfSender.getText().equals(login.getXmppUserDetails().getNick())) {
                        LayoutInflater inflater = ((GroupMessageActivity) context).getLayoutInflater();
                        View alertView = inflater.inflate(R.layout.custom_alert, null);
                        final EditText reportString = alertView.findViewById(R.id.alertReportMessage);

                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.report_message_title))
                                .setView(alertView)
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        String newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                                        if (!TextUtils.isEmpty(reportString.getText().toString())) {
                                            APIInterface apiInterface = APIClient.getClientWithAuth(context)
                                                    .create(APIInterface.class);
                                            ReportUser reportUser = new ReportUser(login.getId().toString(), reportString.getText().toString(), "message", newDate, messagesItem.getXml(), newDate, login.getXmppUserDetails().getJid(), messagesItem.getGroupJid());
                                            Call<ResponseBody> userReport = apiInterface.reportUser(reportUser);
                                            userReport.enqueue(new Callback<ResponseBody>() {
                                                @Override
                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                    try {
                                                        Log.d(TAG, "Reporting user: " + response.body().string());
                                                        dialog.dismiss();
                                                    } catch (Exception ex) {
                                                        ex.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                    Log.e(TAG, "Something went wrong. Please try again later");
                                                    dialog.dismiss();
                                                }
                                            });
                                            Toast.makeText(context, "The user has been reported....", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setCancelable(false);
                        final AlertDialog dialog = alertBuilder.create();
                        dialog.show();

                        return true;
                    } else {
                        return false;
                    }
                }
            });

            holder.nameOfSender.setText(messagesItem.getNick());
            /*if (!(messagesItem.getImageUrl().equals("")
                    && messagesItem.getThumbnailUrl().equals(""))) {
                holder.imageRelLayout.setVisibility(View.VISIBLE);
                holder.textMessage.setVisibility(View.GONE);
                holder.imageMessage.setBackgroundResource(R.drawable.placeholder);
                holder.uploadImage.setVisibility(View.VISIBLE);

                Glide.with(context).load(messagesItem.getThumbnailUrl())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.uploadImage.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.uploadImage.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(holder.imageMessage);
                holder.download.setVisibility(View.GONE);
            } else */if (messagesItem.getImageUrl() == null
                    || messagesItem.getImageUrl().isEmpty()) {
                holder.imageRelLayout.setVisibility(View.GONE);
                holder.textMessage.setVisibility(View.VISIBLE);
                holder.download.setVisibility(View.GONE);
                holder.textMessage.setText(messagesItem.getText());
            } else {
                holder.imageRelLayout.setVisibility(View.VISIBLE);
                holder.textMessage.setVisibility(View.GONE);
                holder.imageMessage.setBackgroundResource(R.drawable.placeholder);

                File dir = new File(root);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        Log.e(TAG, "onBindViewHolder: -> there was some problem creating dir", new Exception("Some problem occured while creating directory"));
                    }
                }


                File file = new File(root+"/"+messagesItem.getImageName());
                if (file.exists()) {
                    Uri imageUri = Uri.fromFile(file);
                    Glide.with(context)
                            .load(imageUri)
                            .into(holder.imageMessage);
                    holder.download.setVisibility(View.GONE);
                } else {
                    if (messagesItem.getThumbnailUrl() != null) {
                        holder.uploadImage.setVisibility(View.VISIBLE);
                        Glide.with(context).load(login.getResourceHostDomain() + messagesItem.getThumbnailUrl())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        holder.uploadImage.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        holder.uploadImage.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .into(holder.imageMessage);
                        holder.download.setVisibility(View.VISIBLE);
                    }
                }
            }

            holder.download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
                    Log.d(":::Chats", "image url: " + messagesItem.getImageUrl());
                    if (messagesItem.getImageUrl() != null) {
                        holder.uploadImage.setVisibility(View.VISIBLE);
                        Glide.with(context).asBitmap().load(login.getResourceHostDomain() + messagesItem.getImageUrl())
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        holder.uploadImage.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        holder.uploadImage.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                String path = saveImage(resource, messagesItem.getImageName());
                                File parent = new File(path);

                                File dir = new File(parent.getParent());
                                if (!dir.exists()) {
                                    if (!dir.mkdirs()) {
                                        Log.e(TAG, "onBindViewHolder: -> there was some problem creating dir", new Exception("Some problem occured while creating directory"));
                                    }
                                }

                                Bitmap bitmap = BitmapFactory.decodeFile(path);
                                holder.imageMessage.setImageBitmap(bitmap);
                                holder.download.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });

            if (holder.nameOfSender.getText().equals(login.getXmppUserDetails().getNick())) {
                holder.groupDisplay.setBackgroundResource(R.color.senderAsSelfColor);
            } else {
                holder.groupDisplay.setBackgroundResource(R.color.senderColor);
            }
        }
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_menu, menu);
            mode.setTitle("Choose your action");

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_menu_copy:
                    Toast.makeText(context, "Copy option selected...", Toast.LENGTH_SHORT).show();
                    mode.finish();

                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };

    private void showPopupWindow(String imageUrl, String sender, String dateTime, boolean fileExists) {
        try {
            ImageView enlargedImage;
            TextView senderName, date;
            final RelativeLayout progressBarPopup;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_window, null);
            window = new PopupWindow(layout, context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels, true);

            window.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.colorPrimary)));
            window.setOutsideTouchable(true);
            window.showAtLocation(layout, Gravity.FILL, 40, 60);

            enlargedImage = layout.findViewById(R.id.enlargedImage);
            senderName = layout.findViewById(R.id.senderName);
            date = layout.findViewById(R.id.dateTime);
            progressBarPopup = layout.findViewById(R.id.imageProgress);
            progressBarPopup.setVisibility(View.VISIBLE);

            DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterday = dateFormat.format(cal.getTime());
            String today = dateFormat.format(new Date());
            String formattedDate;
            if (dateTime.split(",")[0].equalsIgnoreCase(today)) {
                formattedDate = "Today, " + dateTime.split(",")[1];
            } else if (dateTime.split(",")[0].equalsIgnoreCase(yesterday)) {
                formattedDate = "Yesterday, " + dateTime.split(",")[1];
            } else {
                formattedDate = dateTime;
            }

            if (fileExists) {
                Glide.with(context).load(imageUrl)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBarPopup.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBarPopup.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(enlargedImage);
            } else {
                Glide.with(context).load(imageUrl)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBarPopup.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBarPopup.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                        .into(enlargedImage);
            }
            enlargedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                }
            });
            senderName.setText(sender);
            date.setText(formattedDate);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String saveImage(Bitmap image, String imageFileName) {
        String savedImagePath = null;

        File storageDir = new File(root);
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return savedImagePath;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView nameOfSender, dateTimeMsgSent, textMessage;
        ImageView imageMessage, download, upload;
        RelativeLayout groupDisplay, imageRelLayout;
        ProgressBar uploadImage;

        ChatViewHolder(View itemView) {
            super(itemView);

            nameOfSender = itemView.findViewById(R.id.nameOfSender);
            dateTimeMsgSent = itemView.findViewById(R.id
                    .dateTimeMsgSent);
            textMessage = itemView.findViewById(R.id.textChatMessage);
            imageMessage = itemView.findViewById(R.id.imageChatMessage);
            download = itemView.findViewById(R.id.downloadImg);
            groupDisplay = itemView.findViewById(R.id.groupWholeDisplay);
            imageRelLayout = itemView.findViewById(R.id.imageRelLayout);
            upload = itemView.findViewById(R.id.uploadImg);
            uploadImage = itemView.findViewById(R.id.uploadImage);
        }
    }
}