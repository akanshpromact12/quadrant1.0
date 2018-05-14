package com.gametime.quadrant.HomeModule;

import android.content.Context;

import java.util.List;

/**
 * Created by Akansh on 10-11-2017.
 */

public class HomeContract {
    public interface homeView {
        void homeSuccessView(String msg);
    }

    public interface homeActions {
        void displayLoggedInUsername(String username);
        void addGroup (Context context);
        void addFragment(android.support.v4.app.Fragment fragment, String title, Context context, List<android.support.v4.app.Fragment> mFragmentList, List<String> mFragmentTitleList);
    }
}
