package com.gametime.quadrant.GroupMemberListModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.gametime.quadrant.Adapters.GroupMemberListAdapter;
import com.gametime.quadrant.Models.GroupMemberList;
import com.gametime.quadrant.Models.MemberListParams;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupMemberListPresenter implements GroupMemberListContract.GroupMemberListActions {
    private List<GroupMemberList.Success> memberList;
    private GroupMemberListAdapter listAdapter;

    @Override
    public void getAllMemberDetails(final String groupId, final RecyclerView recyclerView, final Context context) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        final MemberListParams memberListParams = new MemberListParams(groupId);

        Call<GroupMemberList> call = apiInterface.getAllMembersOfGrp(memberListParams);
        call.enqueue(new Callback<GroupMemberList>() {
            @Override
            public void onResponse(Call<GroupMemberList> call, Response<GroupMemberList> response) {
                if (response.body().getSuccess() != null) {
                    memberList = new ArrayList<>();
                    response.body().getSuccess().get(0).getJid();

                    listAdapter = new GroupMemberListAdapter(context, memberList, groupId);
                    memberList.addAll(response.body().getSuccess());
                    listAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(listAdapter);
                } else {
                    Toast.makeText(context, "some Error occured..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupMemberList> call, Throwable t) {

            }
        });
    }

    GroupMemberListPresenter(GroupMemberListContract.GroupMemberListView view) {
        GroupMemberListContract.GroupMemberListView view1 = view;
    }
}
