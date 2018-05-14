package com.gametime.quadrant.Network;

import com.gametime.quadrant.Models.AreaCheck;
import com.gametime.quadrant.Models.AreaDetails;
import com.gametime.quadrant.Models.CommonFriends;
import com.gametime.quadrant.Models.CreateGroup;
import com.gametime.quadrant.Models.CreateInvitation;
import com.gametime.quadrant.Models.CreatedGroups;
import com.gametime.quadrant.Models.CurrUsers;
import com.gametime.quadrant.Models.FbToken;
import com.gametime.quadrant.Models.GetMutualFriends;
import com.gametime.quadrant.Models.GroupImageUpload;
import com.gametime.quadrant.Models.GroupInfo;
import com.gametime.quadrant.Models.GroupMemberList;
import com.gametime.quadrant.Models.Invites;
import com.gametime.quadrant.Models.JoinGroup;
import com.gametime.quadrant.Models.JoinedGroups;
import com.gametime.quadrant.Models.MemberInfo;
import com.gametime.quadrant.Models.MemberListParams;
import com.gametime.quadrant.Models.MemberProfile;
import com.gametime.quadrant.Models.MessageParams;
import com.gametime.quadrant.Models.Messages;
import com.gametime.quadrant.Models.NearbyGroups;
import com.gametime.quadrant.Models.PrivateMessagesList;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Models.ReportUser;
import com.gametime.quadrant.Models.Requests;
import com.gametime.quadrant.Models.UserProfile;
import com.gametime.quadrant.Models.UserProfileLocation;
import com.gametime.quadrant.Models.UserProfileStatus;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by Akansh on 09-11-2017.
 */

public interface APIInterface {
    @POST("auth/login/fb")
    Call<QuadrantLoginDetails> postFbAccessToken(@Body FbToken accessToken);

    @GET("groups/nearby/{latLng}")
    Call<NearbyGroups> getNearbyGroups(@Path("latLng") String latLng);

    @POST("groups")
    Call<ResponseBody> postCreateGroup(@Body CreateGroup createGroup);

    @POST("area/user")
    Call<AreaCheck> postArea(@Body AreaDetails areaDetails);

    @POST("groups/join/{groupid}")
    Call<ResponseBody> joinGroup(@Path("groupid") String groupid, @Body JoinGroup joinGroup);

    @GET("group/info/{groupid}")
    Call<GroupInfo> getGroupInfo(@Path("groupid") String groupid);

    @GET("groups/joined")
    Call<JoinedGroups> fetchJoinedGroups();

    @DELETE("groups/joined/{id}")
    Call<ResponseBody> leaveJoinedGroup(@Path("id") String id);

    @GET("groups")
    Call<CreatedGroups> fetchCreatedGroups();

    @DELETE("groups/{groupid}")
    Call<ResponseBody> deleteGroup(@Path("groupid") Integer groupid);

    @GET("groups/requests")
    Call<Requests> fetchAllRequests();

    @POST("groups/requests/{action}/{reqid}")
    Call<ResponseBody> AcceptRejectRequests(@Path("action") String action,
                                            @Path("reqid") Integer reqid);

    @POST("group/history/msg")
    Call<Messages> getAllMessages(@Body MessageParams messageParams);

    @POST("group/members/list")
    Call<GroupMemberList> getAllMembersOfGrp(@Body MemberListParams listParams);

    @GET("user/friend/list")
    Call<PrivateMessagesList> getAllPrivateMessages();

    @POST("group/member/view")
    Call<MemberProfile> getMemberProfileInfo(@Body MemberInfo memberInfo);

    @GET("groups/room/{status}/{id}")
    Call<ResponseBody> getJabberRoomStatus(@Path("status") String status,
                                           @Path("id") String id);

    @POST("user/add/friend/{id}")
    Call<ResponseBody> addFriendsToPrivateMessaging(@Path("id") String id);

    @POST("block/user/{id}")
    Call<ResponseBody> blockUserFromPM(@Path("id") String id);

    @POST("unblock/user/{id}")
    Call<ResponseBody> unblockUserFromPM(@Path("id") String id);

    @GET("user/profile")
    Call<UserProfile> getUserProfile();

    @POST("user/update/profile/{field}")
    Call<ResponseBody> sendStatusInfo(@Path("field") String field,
                                      @Body UserProfileStatus status);

    @POST("user/update/profile/{field}")
    Call<ResponseBody> sendLocationInfo(@Path("field") String field,
                                        @Body UserProfileLocation location);

    @GET("groups/invitation")
    Call<Invites> getInvitesList();

    @POST("reporting/user")
    Call<ResponseBody> reportUser(@Body ReportUser userReporting);

    @GET("user/friend/list")
    Call<CurrUsers> getCurrentUsersList();

    @POST("group/create/invitation")
    Call<ResponseBody> createInvite(@Body CreateInvitation createInvitation);

    @Multipart
    @POST("group/upload")
    Call<GroupImageUpload> grpImgUpload(@Part("gid") RequestBody gid, @Part MultipartBody.Part imagePart, @Part("name") RequestBody name);

    @Multipart
    @POST("user/chat/image/upload")
    Call<GroupImageUpload> personalUpload(@Part MultipartBody.Part imagePart, @Part("name") RequestBody name);

    @POST("mutual/friends")
    Call<CommonFriends> allMutualFriends(@Body GetMutualFriends getMutualFriends);
}
