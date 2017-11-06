package com.squareup.kindphotobot;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.kindphotobot.job.GcmJobService;
import com.squareup.kindphotobot.job.JobManagerInjector;
import com.squareup.kindphotobot.job.JobService;
import com.squareup.kindphotobot.job.TimberJobLogger;
import com.squareup.kindphotobot.oauth.OAuthStore;
import com.squareup.kindphotobot.printer.CloudPrinter;
import com.squareup.kindphotobot.settings.SettingsStore;
import com.squareup.kindphotobot.snap.BitmapBytesHolder;
import com.squareup.kindphotobot.snap.PictureRenderer;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterConfig;
import timber.log.Timber;

public class App extends Application {

  private OAuthStore oAuthStore;
  private CloudPrinter cloudPrinter;
  private PictureRenderer pictureRenderer;
  private BitmapBytesHolder bitmapBytesHolder;
  private JobManager jobManager;
  private SettingsStore settingsStore;

  public static App from(Context context) {
    return (App) context.getApplicationContext();
  }

  @Override public void onCreate() {
    super.onCreate();
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
    TwitterConfig config =
        new TwitterConfig.Builder(this).logger(new DefaultLogger(Log.DEBUG)).debug(true).build();
    Twitter.initialize(config);
    oAuthStore = new OAuthStore(this);
    cloudPrinter = CloudPrinter.create(oAuthStore, this);
    pictureRenderer = new PictureRenderer(this);
    bitmapBytesHolder = new BitmapBytesHolder();
    settingsStore = new SettingsStore(this);

    Configuration.Builder builder = new Configuration.Builder(this) //
        .minConsumerCount(1) //
        .maxConsumerCount(3) //
        .loadFactor(3) //
        .customLogger(new TimberJobLogger()).injector(new JobManagerInjector(this)) //
        .consumerKeepAlive(120);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(this, JobService.class),
          true);
    } else {
      int enableGcm = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
      if (enableGcm == ConnectionResult.SUCCESS) {
        builder.scheduler(GcmJobSchedulerService.createSchedulerFor(this, GcmJobService.class),
            true);
      }
    }
    jobManager = new JobManager(builder.build());
  }

  public OAuthStore oAuthStore() {
    return oAuthStore;
  }

  public CloudPrinter cloudPrinter() {
    return cloudPrinter;
  }

  public PictureRenderer pictureRenderer() {
    return pictureRenderer;
  }

  public BitmapBytesHolder bitmapHolder() {
    return bitmapBytesHolder;
  }

  public JobManager jobManager() {
    return jobManager;
  }

  public SettingsStore settingsStore() {
    return settingsStore;
  }
}
