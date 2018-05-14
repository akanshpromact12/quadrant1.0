package com.gametime.quadrant.JoinedGroupsModule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.gametime.quadrant.Adapters.JoinedGroupsAdapter;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Models.JoinedGroups;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.Swipe.SwipeController;
import com.gametime.quadrant.Swipe.SwipeControllerActions;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinedGroupPresenter implements JoinedGroupContract.joinGroupActions {
    private JoinedGroupContract.joinedGroupView view;
    private SwipeController swipeController;
    private ArrayList<JoinedGroups.Groups> groups;
    private static final String TAG = "JoinedGroupsActivity";

    JoinedGroupPresenter(JoinedGroupContract.joinedGroupView view) {
        this.view = view;
    }

    @Override
    public void showJoinedGroups(final Context context, final RecyclerView recyclerView, final RelativeLayout relativeLayout) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        view.progressBarVisible(View.VISIBLE);
        Call<JoinedGroups> call = apiInterface.fetchJoinedGroups();
        call.enqueue(new Callback<JoinedGroups>() {
            @Override
            public void onResponse(Call<JoinedGroups> call, Response<JoinedGroups> response) {
                if (/*response.body().getGroups() != null*/response.code() == 200) {
                    view.noJoinedGroupsVisible(View.GONE);
                    groups = new ArrayList<>();
                    final JoinedGroupsAdapter adapter = new
                            JoinedGroupsAdapter(context, groups);
                    groups.addAll(response.body().getGroups());
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);

                    /*ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                            return false;
                        }

                        static final float ALPHA_FULL = 1.0f;

                        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                View itemView = viewHolder.itemView;

                                Paint p = new Paint();
                                Bitmap icon;

                                if (dX > 0) {

                                    //color : left side (swiping towards right)
                                    p.setARGB(255, 255, 0, 0);
                                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                            (float) itemView.getBottom(), p);

                                    // icon : left side (swiping towards right)
                                    icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_delete_black_24dp);
                                    c.drawBitmap(icon,
                                            (float) itemView.getLeft() + convertDpToPx(16),
                                            (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                            p);
                                }*//* else {

                                    //color : right side (swiping towards left)
                                    p.setARGB(255, 0, 255, 0);

                                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                            (float) itemView.getRight(), (float) itemView.getBottom(), p);

                                    //icon : left side (swiping towards right)
                                    icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_delete_black_24dp);
                                    c.drawBitmap(icon,
                                            (float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                                            (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                            p);
                                }*//*

                                // Fade out the view when it is swiped out of the parent
                                final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                                viewHolder.itemView.setAlpha(alpha);
                                viewHolder.itemView.setTranslationX(dX);

                            } else {
                                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                            }
                        }

                        private int convertDpToPx(int dp){
                            return Math.round(dp * (context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
                        }
                        @Override
                        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                            final int position = viewHolder.getAdapterPosition(); //swiped position

                            if (direction == ItemTouchHelper.RIGHT) { //swipe left

                                groups.remove(position);
                                adapter.notifyItemRemoved(position);

                                Toast.makeText(context,"Swipped to right",Toast.LENGTH_SHORT).show();

                            }*//*else if(direction == ItemTouchHelper.RIGHT){//swipe right

                                yourarraylist.remove(position);
                                youradapter.notifyItemRemoved(position);

                                Toast.makeText(getApplicationContext(),"Swipped to right",Toast.LENGTH_SHORT).show();

                            }*//*

                        }
                    };
                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                    itemTouchHelper.attachToRecyclerView(recyclerView);*/

                    swipeController = new SwipeController(new SwipeControllerActions() {
                        @Override
                        public void onRightClicked(final int position) {
                            AlertDialog.Builder leaveGrpAlert = new AlertDialog.Builder(context);
                            leaveGrpAlert.setTitle("Leave Group");
                            leaveGrpAlert.setMessage("Are you sure you want to leave the group?");
                            leaveGrpAlert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    leaveGroup(String.valueOf(groups.get(position).getId()), context, groups, adapter, position);
                                    dialogInterface.dismiss();
                                }
                            });
                            leaveGrpAlert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            AlertDialog dialog = leaveGrpAlert.create();
                            dialog.show();
                        }
                    });

                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
                    itemTouchHelper.attachToRecyclerView(recyclerView);

                    recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                        @Override
                        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                            swipeController.onDraw(c, "joined");
                        }
                    });

                    view.progressBarVisible(View.GONE);
                    view.joinedGroupSuccess("Showing joined groups");
                    view.progressBarVisible(View.GONE);
                } else if (response.code() != 200) {
                    view.progressBarVisible(View.GONE);
                    Log.e(TAG, "There was some problem.....");
                    ((Activity) context).finish();
                } else {
                    view.noJoinedGroupsVisible(View.VISIBLE);
                }
                view.progressBarVisible(View.GONE);
            }

            @Override
            public void onFailure(Call<JoinedGroups> call, Throwable t) {
                view.joinedGroupSuccess(GenExceptions.fireException(t));
                view.progressBarVisible(View.GONE);
            }
        });
    }

    private void leaveGroup(final String id, final Context context, final ArrayList<JoinedGroups.Groups> groups, final JoinedGroupsAdapter adapter, final int position) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<ResponseBody> leaveJoinedGrp = apiInterface.leaveJoinedGroup(id);
        leaveJoinedGrp.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                groups.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, adapter.getItemCount());

                view.joinedGroupSuccess("Group was left successfully");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                view.joinedGroupSuccess("Some problem occured while leaving group. Please try again later");
                GenExceptions.fireException(t);
            }
        });
    }
}
