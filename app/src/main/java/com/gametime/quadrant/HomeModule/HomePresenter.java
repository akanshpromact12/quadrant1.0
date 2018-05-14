package com.gametime.quadrant.HomeModule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gametime.quadrant.CreateGroupSelectAreaModule.CreateGroupSelectAreaActivity;

import java.util.List;

/**
 * Created by Akansh on 10-11-2017.
 */

public class HomePresenter implements HomeContract.homeActions {
    public final HomeContract.homeView view;

    public HomePresenter(HomeContract.homeView view) {
        this.view = view;
    }

    @Override
    public void displayLoggedInUsername(String username) {
        view.homeSuccessView("Logged in as: " + username);
    }

    @Override
    public void addGroup(Context context) {
        Intent intent = new Intent(context,
                CreateGroupSelectAreaActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void addFragment(android.support.v4.app.Fragment fragment, String title, Context context, List<android.support.v4.app.Fragment> mFragmentList, List<String> mFragmentTitleList) {
        mFragmentList.add(fragment);
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);
        mFragmentTitleList.add(title);
    }
}
