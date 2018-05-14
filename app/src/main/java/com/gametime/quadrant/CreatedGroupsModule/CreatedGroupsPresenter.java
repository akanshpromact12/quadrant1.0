package com.gametime.quadrant.CreatedGroupsModule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gametime.quadrant.Adapters.CreatedGroupsAdapter;
import com.gametime.quadrant.CreateGroupModule.CreateGroupActivity;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Models.CreatedGroups;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.Swipe.SwipeController;
import com.gametime.quadrant.Swipe.SwipeControllerActions;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatedGroupsPresenter implements CreatedGroupsContract.CreatedGroupsActions {
    private static final String TAG = CreateGroupActivity.class.getName();
    private CreatedGroupsContract.CreatedGroupsView view;
    private CreatedGroupsAdapter adapter;
    private ArrayList<CreatedGroups.Groupsd> createdGroups;
    private SwipeController swipeController;
    private Context context;

    CreatedGroupsPresenter(CreatedGroupsContract.CreatedGroupsView view, Context context) {
        this.view = view;
        this.context = context;
    }

    @Override
    public void showCreatedGroups(final RecyclerView recyclerView,
                                  final Context context) {
        view.progressBarVisibility(View.VISIBLE);
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<CreatedGroups> call = apiInterface.fetchCreatedGroups();
        call.enqueue(new Callback<CreatedGroups>() {
            @Override
            public void onResponse(Call<CreatedGroups> call, final Response<CreatedGroups> response) {
                if (response.code() == 200) {
                    view.noCreateGrpsVisibility(View.GONE);
                    createdGroups = new ArrayList<>();

                    adapter = new CreatedGroupsAdapter(context,
                            createdGroups);

                    createdGroups.addAll(response.body().getGroups());
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);

                    swipeController = new SwipeController(new SwipeControllerActions() {
                        @Override
                        public void onRightClicked(final int position) {
                            AlertDialog.Builder alert = new AlertDialog
                                    .Builder(context).setTitle("Delete Group")
                                    .setMessage("Are you sure you want to delete " +
                                            "the group?")
                                    .setPositiveButton(android.R.string.yes,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int i) {
                                                    view.progressBarVisibility(View.VISIBLE);
                                                    deleteGroup(createdGroups.get(position).getId(), createdGroups, adapter, position);

                                                    dialog.dismiss();
                                                }
                                            }).setNegativeButton(android.R.string.no,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int i) {
                                                    dialog.dismiss();
                                                }
                                            });
                            alert.show();
                        }
                    });

                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
                    itemTouchHelper.attachToRecyclerView(recyclerView);

                    recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                        @Override
                        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                            swipeController.onDraw(c, "created");
                        }
                    });

                    recyclerView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
                        }
                    });

                    view.CreatedGroupsFetchSuccessView("All Created groups");
                } else if (response.code() != 200) {
                    Log.e(TAG, "There was some problem.....");
                    ((Activity) context).finish();
                } /*else if (response.body().getGroups().size() == 0) {
                    Log.d(TAG, "size is 0");
                    view.noCreateGrpsVisibility(View.VISIBLE);
                }*/
                view.progressBarVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<CreatedGroups> call, Throwable t) {
                view.CreatedGroupsFetchSuccessView(GenExceptions.fireException(t));
                view.progressBarVisibility(View.GONE);
            }
        });
    }

    private void deleteGroup(Integer id, final ArrayList<CreatedGroups.Groupsd> createdGroups, final CreatedGroupsAdapter adapter, final int position) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.deleteGroup(id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                view.progressBarVisibility(View.GONE);
                createdGroups.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, adapter.getItemCount());

                view.CreatedGroupsFetchSuccessView("Group was deleted successfully");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                view.progressBarVisibility(View.GONE);
                view.CreatedGroupsFetchSuccessView("Some problem occured in deleting the group");
            }
        });
    }
}
