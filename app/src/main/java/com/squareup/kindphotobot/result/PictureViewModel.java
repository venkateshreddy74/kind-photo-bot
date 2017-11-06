package com.squareup.kindphotobot.result;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import com.birbit.android.jobqueue.JobManager;
import com.squareup.kindphotobot.App;
import com.squareup.kindphotobot.printer.CloudPrinter;
import com.squareup.kindphotobot.settings.SettingsStore;
import com.squareup.kindphotobot.snap.BitmapBytesHolder;
import com.squareup.kindphotobot.snap.PictureRenderer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PictureViewModel extends AndroidViewModel {

  private final CloudPrinter cloudPrinter;
  private final MutableLiveData<Bitmap> renderedBitmap = new MutableLiveData<>();
  private final App app;
  private final BitmapBytesHolder bitmapBytesHolder;
  private final PictureRenderer pictureRenderer;
  private final Executor backgroundExecutor;
  private final Handler mainHandler;
  private final JobManager jobManager;
  private final SettingsStore settingsStore;

  public PictureViewModel(Application application) {
    super(application);
    app = App.from(application);
    this.cloudPrinter = app.cloudPrinter();
    bitmapBytesHolder = app.bitmapHolder();
    pictureRenderer = app.pictureRenderer();
    settingsStore = app.settingsStore();
    jobManager = app.jobManager();
    backgroundExecutor = Executors.newSingleThreadExecutor();
    mainHandler = new Handler(Looper.getMainLooper());
  }

  public LiveData<Bitmap> renderedBitmap() {
    return renderedBitmap;
  }

  public void renderBitmap(int graphicIndex, float pictureRatio) {
    if (renderedBitmap.getValue() != null) {
      throw new IllegalStateException("Can only render bitmap once per view model");
    }
    byte[] bitmapBytes = bitmapBytesHolder.release();
    backgroundExecutor.execute(() -> renderInBackground(bitmapBytes, graphicIndex, pictureRatio));
  }

  private void renderInBackground(byte[] bitmapBytes, int graphicIndex, float pictureRatio) {
    Bitmap bitmap = pictureRenderer.render(bitmapBytes, graphicIndex, pictureRatio);
    mainHandler.post(() -> renderedBitmap.setValue(bitmap));
  }

  public boolean print() {
    Bitmap bitmap = renderedBitmap.getValue();
    if (bitmap == null) {
      throw new NullPointerException("bitmap should not be null");
    }

    boolean cloudPrint = cloudPrinter.isCloudPrintSetUp();
    String tweetMessage = settingsStore.getTweetMessage();
    SavePictureJob job = new SavePictureJob(app, bitmap, tweetMessage, cloudPrint);
    jobManager.addJobInBackground(job);

    return cloudPrint;
  }
}
