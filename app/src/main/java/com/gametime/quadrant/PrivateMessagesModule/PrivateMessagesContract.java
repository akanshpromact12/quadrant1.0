package com.gametime.quadrant.PrivateMessagesModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Akansh on 05-12-2017.
 */

public class PrivateMessagesContract {
    public interface PrivateMessagesView {
        void successfulMessage(String msg);
        void makeProgreessBarVisible();
        void makeProgreessBarInvisible();
        void uploadImageChat(File image, int position);
    }

    public interface PrivateMessagesActions {
        void sendMessage(ArrayList<String> messages, String textToSend, ArrayList<String> names, String senderName, ArrayList<String> times, String date, Context context, RecyclerView recyclerView);
        void blockUser(String userId, Context context);
        void unBlockUser(String userId, Context context);
    }
}
