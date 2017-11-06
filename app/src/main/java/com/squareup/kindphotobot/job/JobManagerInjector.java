package com.squareup.kindphotobot.job;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.di.DependencyInjector;
import com.squareup.kindphotobot.App;

public class JobManagerInjector implements DependencyInjector {

  private final App app;

  public JobManagerInjector(App app) {
    this.app = app;
  }

  @Override public void inject(Job job) {
    if (job instanceof InjectedJob) {
      ((InjectedJob) job).inject(app);
    }
  }
}
