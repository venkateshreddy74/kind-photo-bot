package com.squareup.kindphotobot.job;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.squareup.kindphotobot.App;

public class JobService extends FrameworkJobSchedulerService {
  @Override protected JobManager getJobManager() {
    return App.from(this).jobManager();
  }
}