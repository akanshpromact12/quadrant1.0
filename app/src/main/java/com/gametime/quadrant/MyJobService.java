package com.gametime.quadrant;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Akansh on 26-04-2018.
 */

class MyJobService extends JobService {
    private static final String TAG = "MyJobService";

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d(TAG, "Performing long running task in scheduled job");

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
