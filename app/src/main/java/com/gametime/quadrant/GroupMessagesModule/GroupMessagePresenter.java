package com.gametime.quadrant.GroupMessagesModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.gametime.quadrant.Adapters.ChatsAdapter;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Models.GroupImageSuccess;
import com.gametime.quadrant.Models.GroupImageUpload;
import com.gametime.quadrant.Models.GroupMessageRealm;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Akansh on 17-11-2017.
 */

public class GroupMessagePresenter implements GroupMessagesContract.GroupMessageActions {
    public GroupMessagesContract.GroupMessageView view;
    public static final String TAG= "GroupMessagePresenter";
    public List<GroupImageSuccess> groupImages = new ArrayList<>();
    public GroupImageSuccess groupImgs;
    private ChatsAdapter chatsAdapter;
    private Boolean uploaded = false;
    private Context context;
    private Realm realm;
    private List<GroupMessageRealm> msgRealm;
    private ChatsAdapter messagesAdapter;

    public GroupMessagePresenter(GroupMessagesContract.GroupMessageView view, Context context, Realm realm, List<GroupMessageRealm> msgRealm, ChatsAdapter messagesAdapter) {
        this.context = context;
        this.view = view;
        this.realm = realm;
        this.msgRealm = msgRealm;
        this.messagesAdapter = messagesAdapter;
    }

    @Override
    public void updateAdapter(List<GroupMessageRealm> messageList, Context context, RecyclerView recyclerView) {
        ChatsAdapter chatsAdapter = new ChatsAdapter(context,
                messageList, false, "");
        recyclerView.setAdapter(chatsAdapter);

        view.successfulMessage("view updated");
    }

    @Override
    public void sendMessage(List<GroupMessageRealm> messageList, Context context) {
        ChatsAdapter chatsAdapter = new ChatsAdapter(context,
                messageList, false, "");
        chatsAdapter.notifyDataSetChanged();

        view.successfulMessage("Message sent");
    }

    @Override
    public Boolean uploadImage(final Context context, final File image, final String gid, final String date, final RecyclerView recyclerView, final XMPPTCPConnection conn1, final String groupJid, final QuadrantLoginDetails login, final List<GroupMessageRealm> messageList) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), image);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", image.getName(), requestFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");
        RequestBody gidReq = RequestBody.create(MediaType.parse("text/plain"), gid);
        //MultipartBody.Part part = MultipartBody.Part.create()
        //GrpImageUpload grpImageUpload = new GrpImageUpload(gid);
        Call<GroupImageUpload> callImgUpload = apiInterface.grpImgUpload(gidReq, body, name);
        callImgUpload.enqueue(new Callback<GroupImageUpload>() {
            @Override
            public void onResponse(Call<GroupImageUpload> call, Response<GroupImageUpload> response) {
                final String imageUrl = response.body().getSuccess().getImageUri();
                final String thumbnailUrl = response.body().getSuccess().getThumbUri();

                conn1.getUser();
                try {
                    MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(conn1);
                    EntityBareJid bareJid = JidCreate.entityBareFrom(groupJid);

                    Jid ownJid = JidCreate.entityBareFrom(login.getXmppUserDetails().getJid());

                    MultiUserChat muc = mucManager.getMultiUserChat(bareJid);
                    Resourcepart resourcepart = Resourcepart.from(login
                            .getXmppUserDetails().getNick());

                    final Message message = new Message();
                    message.setStanzaId(UUID.randomUUID().toString());
                    message.setType(Message.Type.groupchat);
                    message.setBody("imageUpload");
                    message.setTo(bareJid);
                    message.setFrom(ownJid);

                    Map<String, String> attributes = new HashMap<>();
                    attributes.put("image", imageUrl);
                    attributes.put("thumbnail", thumbnailUrl);

                    ((GroupMessageActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            realm.beginTransaction();
                            GroupMessageRealm groupMessageRealm = realm.createObject(GroupMessageRealm.class, UUID.randomUUID().toString());
                            groupMessageRealm.setNick(login.getXmppUserDetails().getNick());
                            groupMessageRealm.setBody("imageUpload");
                            groupMessageRealm.setStanzId(message.getStanzaId());
                            groupMessageRealm.setTime(date);
                            groupMessageRealm.setPeer(login.getXmppUserDetails().getJid().toString());
                            groupMessageRealm.setXml(message.toXML().toString());
                            groupMessageRealm.setText("imageUpload");
                            groupMessageRealm.setThumbnailUrl(image.getPath());
                            groupMessageRealm.setImageUrl(image.getPath());
                            groupMessageRealm.setImageName(image.getName());
                            groupMessageRealm.setGroupJid(groupJid);
                            realm.commitTransaction();
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<GroupMessageRealm> realmResults = realm.where(GroupMessageRealm.class)
                                            .equalTo("groupJid", groupJid)
                                            .findAll();

                                    msgRealm.add(0, realmResults.last());
                                    messagesAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });

                    StandardExtensionElement sendImage = StandardExtensionElement.builder("image", "urn:xmpp:image").addAttribute("name", imageUrl).build();
                    StandardExtensionElement sendThumbnail = StandardExtensionElement.builder("thumbnail", "urn:xmpp:thumbnail").addAttribute("thumbnail", thumbnailUrl).build();

                    Collection<ExtensionElement> sendImages = new ArrayList<>();
                    sendImages.add(sendImage);
                    sendImages.add(sendThumbnail);

                    message.addExtensions(sendImages);
                    muc.sendMessage(message);

                    uploaded = true;
                } catch (Exception ex) {
                    GenExceptions.fireException(ex);
                    uploaded = false;
                }
            }

            @Override
            public void onFailure(Call<GroupImageUpload> call, Throwable t) {
                GenExceptions.fireException(t);
                new ChatsAdapter(context, messageList, true);

                uploaded = false;
            }
        });

        return uploaded;
    }

    @Override
    public void reUploadImage(final Context context, String image, String gid, String date, RecyclerView recyclerView, final XMPPTCPConnection conn1, final String groupJid, final QuadrantLoginDetails login, final List<GroupMessageRealm> messageList) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), image);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", image, requestFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");
        RequestBody gidReq = RequestBody.create(MediaType.parse("text/plain"), gid);
        //MultipartBody.Part part = MultipartBody.Part.create()
        //GrpImageUpload grpImageUpload = new GrpImageUpload(gid);
        Call<GroupImageUpload> callImgUpload = apiInterface.grpImgUpload(gidReq, body, name);
        callImgUpload.enqueue(new Callback<GroupImageUpload>() {
            @Override
            public void onResponse(Call<GroupImageUpload> call, Response<GroupImageUpload> response) {
                String imageUrl = response.body().getSuccess().getImageUri();
                String thumbnailUrl = response.body().getSuccess().getThumbUri();


                conn1.getUser();
                try {
                    MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(conn1);
                    EntityBareJid bareJid = JidCreate.entityBareFrom(groupJid);

                    Jid ownJid = JidCreate.entityBareFrom(login.getXmppUserDetails().getJid());

                    MultiUserChat muc = mucManager.getMultiUserChat(bareJid);
                    Resourcepart resourcepart = Resourcepart.from(login
                            .getXmppUserDetails().getNick());

                    Message message = new Message();
                    message.setStanzaId(UUID.randomUUID().toString());
                    message.setType(Message.Type.groupchat);
                    message.setBody("imageUpload");
                    message.setTo(bareJid);
                    message.setFrom(ownJid);

                    Map<String, String> attributes = new HashMap<>();
                    attributes.put("image", imageUrl);
                    attributes.put("thumbnail", thumbnailUrl);

                    StandardExtensionElement sendImage = StandardExtensionElement.builder("image", "urn:xmpp:image").addAttribute("name", imageUrl).build();
                    StandardExtensionElement sendThumbnail = StandardExtensionElement.builder("thumbnail", "urn:xmpp:thumbnail").addAttribute("thumbnail", thumbnailUrl).build();

                    Collection<ExtensionElement> sendImages = new ArrayList<>();
                    sendImages.add(sendImage);
                    sendImages.add(sendThumbnail);

                    message.addExtensions(sendImages);
                    muc.sendMessage(message);
                } catch (Exception ex) {
                    GenExceptions.fireException(ex);
                }
            }

            @Override
            public void onFailure(Call<GroupImageUpload> call, Throwable t) {
                GenExceptions.fireException(t);
                new ChatsAdapter(context, messageList, true);
            }
        });
    }
}
