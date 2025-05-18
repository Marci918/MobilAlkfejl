package com.example.zeneblog;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class BlogSyncJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("JobService", "Blog szinkron elindult");

        new Thread(() -> {
            jobFinished(params, false);
        }).start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}

