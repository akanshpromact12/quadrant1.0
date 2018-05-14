package com.gametime.quadrant.Adapters;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.GroupMessagesModule.GroupMessageActivity;
import com.gametime.quadrant.ImageUploadStatus;
import com.gametime.quadrant.Models.PrivateMessageRealm;
import com.gametime.quadrant.Models.PrivateMessagesList;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Models.ReportUser;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.PrivateMessagesModule.PrivateMessagesActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.AccessVariables;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.CreatedGroupsModule.CreatedGroupsFragment.TAG;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

public class PrivateMessagesAdapter extends RecyclerView.Adapter<PrivateMessagesAdapter.PrivateMessagesViewHolder> {
    private final QuadrantLoginDetails login;
    private Context context;
    private ArrayList<PrivateMessageRealm> messageList;
    private int status;
    private PopupWindow window;
    private android.support.v7.view.ActionMode actionMode;
    private PrivateMessagesList.Success privateMessagesLocal;

    public PrivateMessagesAdapter(Context context, ArrayList<PrivateMessageRealm> messageList, PrivateMessagesList.Success privateMessages) {
        this.messageList = messageList;
        this.context = context;
        this.status = status;
        String APICreds = context.getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);
        this.privateMessagesLocal = privateMessages;
    }

    @Override
    public PrivateMessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_messages_display, parent,
                        false);

        return new PrivateMessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PrivateMessagesViewHolder holder, final int position) {
        final PrivateMessageRealm privateMessages = messageList.get(position);
        final String imageName = privateMessages.getImage_url().split("/")[privateMessages.getImage_url().split("/").length-1];
        File imageFile = new File(AccessVariables.root+"/"+imageName);

        Log.d(TAG, "onBindViewHolder: -> text: "+privateMessages.getMessage()+" multi id: " + privateMessages.getMultimedia_identifier());
        if (privateMessages.getMultimedia_identifier()) {
            holder.chatMsg.setVisibility(View.GONE);
            holder.imageMsgRelLayout.setVisibility(View.VISIBLE);

            if (imageFile.exists()) {
                holder.imageProgress.setVisibility(View.GONE);
                Uri imageUri = Uri.fromFile(imageFile);
                Glide.with(context).load(imageUri)
                        .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                        .into(holder.imageMsg);
                holder.download.setVisibility(View.GONE);
            } else if (privateMessages.getThumb_url().equalsIgnoreCase("") && privateMessages.getMultimedia_identifier()) {
                Log.d("::::PMAdapter", "file: " + privateMessages.getImage_url());
                holder.imageProgress.setVisibility(View.GONE);
                Glide.with(context).load(privateMessages.getImage_url())
                        .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                        .into(holder.imageMsg);
                holder.download.setVisibility(View.GONE);
            } else {
                holder.imageProgress.setVisibility(View.GONE);
                Glide.with(context).load(privateMessages.getThumb_url())
                        .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                        .into(holder.imageMsg);
                holder.download.setVisibility(View.VISIBLE);
            }

            holder.imageMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupWindow(privateMessages.getImage_url(), privateMessages.getMessage_sender(), privateMessages.getMessage_datetime());
                }
            });

            if (privateMessages.getStatus() == ImageUploadStatus.NONE.getValue()) {
                holder.uploadFailedRelLayout.setVisibility(View.GONE);
                holder.imageProgress.setVisibility(View.GONE);
            } else if (privateMessages.getStatus() == ImageUploadStatus.INPROGRESS.getValue()) {
                holder.uploadFailedRelLayout.setVisibility(View.GONE);
                holder.imageProgress.setVisibility(View.VISIBLE);
            } else if (privateMessages.getStatus() == ImageUploadStatus.SUCCESS.getValue()) {
                holder.uploadFailedRelLayout.setVisibility(View.GONE);
                holder.imageProgress.setVisibility(View.GONE);
            } else if (privateMessages.getStatus() == ImageUploadStatus.FAILED.getValue()) {
                holder.uploadFailedRelLayout.setVisibility(View.VISIBLE);
                holder.imageProgress.setVisibility(View.GONE);
            }

            holder.uploadFailed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file = new File(privateMessages.getImage_url());
                    ((PrivateMessagesActivity) context).uploadImageChat(file, position);
                }
            });
        } else {
            holder.chatMsg.setVisibility(View.VISIBLE);
            holder.imageMsgRelLayout.setVisibility(View.GONE);
            holder.download.setVisibility(View.GONE);
            holder.chatMsg.setText(privateMessages.getMessage());
            holder.imageProgress.setVisibility(View.GONE);
        }

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (privateMessages.getImage_url() != null) {
                    holder.imageProgress.setVisibility(View.VISIBLE);
                    holder.download.setVisibility(View.GONE);
                    Log.d("::::PM", "image url: " + privateMessages.getImage_url());
                    try {
                        Glide.with(context).asBitmap()
                                .load(privateMessages.getImage_url())
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                        String path = saveImage(resource, imageName);
                                        File imageFile = new File(path);

                                        if (imageFile.exists()) {
                                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                                            holder.imageMsg.setImageBitmap(bitmap);
                                            holder.imageProgress.setVisibility(View.GONE);
                                        } else {
                                            if (privateMessages.getThumb_url() != null) {
                                                Glide.with(context)
                                                        .load(login.getResourceHostDomain()+
                                                                privateMessages.getThumb_url())
                                                        .into(holder.imageMsg);
                                                holder.download.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                });
                    } catch (Exception ex) {
                        holder.imageProgress.setVisibility(View.GONE);
                        GenExceptions.logException(ex);
                    }
                }
            }
        });

        holder.msgSent.setText(privateMessages.getMessage_datetime());
        holder.senderName.setText(privateMessages.getMessage_sender());


        holder.PMWholeDisplay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!holder.senderName.getText().equals(login.getXmppUserDetails().getNick())) {
                    LayoutInflater inflater = ((GroupMessageActivity) context).getLayoutInflater();
                    View alertView = inflater.inflate(R.layout.custom_alert, null);
                    final EditText reportString = alertView.findViewById(R.id.alertReportMessage);

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context)
                            .setTitle(R.string.report_message_title)
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
                                        ReportUser reportUser = new ReportUser(login.getId().toString(), reportString.getText().toString(), "message", newDate, "", newDate, login.getXmppUserDetails().getJid(), privateMessagesLocal.getJid());
                                        Call<ResponseBody> userReport = apiInterface.reportUser(reportUser);
                                        userReport.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                try {
                                                    Log.d(ContentValues.TAG, "Reporting user: " + response.body().string());
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Log.e(ContentValues.TAG, "Something went wrong. Please try again later");
                                            }
                                        });
                                        Toast.makeText(context, "The user has been reported....", Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                }
                            }).setCancelable(false);
                    final AlertDialog dialog = alertBuilder.create();
                    dialog.show();

                    return true;
                } else {
                    return false;
                }
            }
        });

        holder.PMWholeDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMode = ((GroupMessageActivity) context).startSupportActionMode(new ActionMode.Callback() {
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
                                //Toast.makeText(context, "Copy option selected...", Toast.LENGTH_SHORT).show();
                                String getText = holder.chatMsg.getText().toString();
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
                    public void onDestroyActionMode(ActionMode mode) {
                        actionMode = null;
                    }
                });
            }
        });

        if (holder.senderName.getText().equals(login.getXmppUserDetails().getNick())) {
            holder.PMWholeDisplay.setBackgroundResource(R.color.senderAsSelfColor);
        } else {
            holder.PMWholeDisplay.setBackgroundResource(R.color.senderColor);
        }
    }

    private void showPopupWindow(String imageUrl, String sender, String dateTime) {
        try {
            ImageView enlargedImage;
            TextView senderName, date;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_window, null);
            window = new PopupWindow(layout, context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels, true);

            window.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.colorPrimary)));
            window.setOutsideTouchable(true);
            window.showAtLocation(layout, Gravity.FILL, 40, 60);

            enlargedImage = layout.findViewById(R.id.enlargedImage);
            senderName = layout.findViewById(R.id.senderName);
            date = layout.findViewById(R.id.dateTime);

            DateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
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

            Glide.with(context).load(imageUrl)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                    .into(enlargedImage);
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

        File storageDir = new File(AccessVariables.root);
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
        return messageList.size();
    }

    static class PrivateMessagesViewHolder extends RecyclerView.ViewHolder {
        TextView senderName, msgSent, chatMsg;
        ImageView imageMsg, download, uploadFailed;
        RelativeLayout PMWholeDisplay, imageMsgRelLayout, uploadFailedRelLayout;
        View imageProgress;

        PrivateMessagesViewHolder(View itemView) {
            super(itemView);

            senderName = itemView.findViewById(R.id.nameOfPMSender);
            msgSent = itemView.findViewById(R.id.dateTimePMMsgSent);
            chatMsg = itemView.findViewById(R.id.textPMChatMessage);
            imageMsg = itemView.findViewById(R.id.imagePMChatMessage);
            imageMsgRelLayout = itemView.findViewById(R.id.imagePMChatMessageRelLayout);
            download = itemView.findViewById(R.id.downloadPM);
            PMWholeDisplay = itemView.findViewById(R.id.pmWholeDisplay);
            imageProgress = itemView.findViewById(R.id.imageProgress);
            uploadFailed = itemView.findViewById(R.id.uploadFailed);
            uploadFailedRelLayout = itemView.findViewById(R.id.uploadFailedRelLayout);
        }
    }
}
