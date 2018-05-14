package com.gametime.quadrant.GroupMessagesModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.gametime.quadrant.Models.GroupMessageRealm;
import com.gametime.quadrant.Models.QuadrantLoginDetails;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.File;
import java.util.List;

/**
 * Created by Akansh on 17-11-2017.
 */

public class GroupMessagesContract {
    public interface GroupMessageView {
        void successfulMessage(String msg);
    }

    public interface GroupMessageActions {
        void updateAdapter(List<GroupMessageRealm> messageList, Context context, RecyclerView recyclerView);
        Boolean uploadImage(Context context, File image, String gid, String date, RecyclerView recyclerView, XMPPTCPConnection conn1, String groupJid, QuadrantLoginDetails login, List<GroupMessageRealm> messageList);
        void reUploadImage(Context context, String image, String gid, String date, RecyclerView recyclerView, XMPPTCPConnection conn1, String groupJid, QuadrantLoginDetails login, List<GroupMessageRealm> messageList);
        void sendMessage(List<GroupMessageRealm> messageList, Context context);
    }
}
