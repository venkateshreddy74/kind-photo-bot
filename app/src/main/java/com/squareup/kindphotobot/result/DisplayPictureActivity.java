package com.squareup.kindphotobot.result;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.print.PrintHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.kindphotobot.App;
import com.squareup.kindphotobot.R;
import com.squareup.kindphotobot.charge.ChargeActivity;
import com.squareup.kindphotobot.settings.SettingsStore;
import com.squareup.kindphotobot.snap.SnapActivity;
import com.squareup.kindphotobot.twitter.Tweeter;
import com.squareup.kindphotobot.util.ParentActivity;

public class DisplayPictureActivity extends ParentActivity {

  private static final String GRAPHIC_INDEX_EXTRA = "graphicIndex";
  private static final String PICTURE_RATIO_EXTRA = "pictureRatio";

  public static void start(Activity activity, int graphicIndex, float pictureRatio) {
    Intent intent = new Intent(activity, DisplayPictureActivity.class);
    intent.putExtra(GRAPHIC_INDEX_EXTRA, graphicIndex);
    intent.putExtra(PICTURE_RATIO_EXTRA, pictureRatio);
    activity.startActivity(intent);
  }

  public DisplayPictureActivity() {
    super(true);
  }

  private SettingsStore settingsStore;
  private PictureViewModel pictureViewModel;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    settingsStore = App.from(this).settingsStore();

    setContentView(R.layout.result);

    findViewById(R.id.back).setOnClickListener(v -> finish());
    ImageView pictureView = findViewById(R.id.picture);

    TextView printButton = findViewById(R.id.print);
    printButton.setEnabled(false);

    Tweeter tweeter = new Tweeter();
    if (tweeter.isLoggedIn()) {
      printButton.setText("Print & Tweet");
    } else {
      printButton.setText("Print");
    }

    printButton.setOnClickListener(__ -> print());

    pictureViewModel = ViewModelProviders.of(this).get(PictureViewModel.class);
    if (savedInstanceState == null) {
      Intent intent = getIntent();
      int graphicIndex = intent.getIntExtra(GRAPHIC_INDEX_EXTRA, -1);
      float pictureRatio = intent.getFloatExtra(PICTURE_RATIO_EXTRA, -1f);
      pictureViewModel.renderBitmap(graphicIndex, pictureRatio);
    }

    pictureViewModel.renderedBitmap().observe(this, (bitmap) -> {
      printButton.setEnabled(true);
      pictureView.setImageBitmap(bitmap);
    });
  }

  private void print() {
    boolean printingAsync = pictureViewModel.print();
    if (printingAsync) {
      done();
    } else {
      localPrint();
    }
  }

  private void localPrint() {
    Bitmap bitmap = pictureViewModel.renderedBitmap().getValue();
    PrintHelper photoPrinter = new PrintHelper(this);
    photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
    photoPrinter.printBitmap("droids.jpg", bitmap, this::done);
  }

  private void done() {
    Toast toast = new Toast(this);
    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
    toast.setDuration(Toast.LENGTH_SHORT);
    LayoutInflater inflater = LayoutInflater.from(this);
    toast.setView(inflater.inflate(R.layout.printed, null));
    toast.show();
    Intent intent;
    if (settingsStore.arePaymentsEnabled()) {
      intent = new Intent(this, ChargeActivity.class);
    } else {
      intent = new Intent(this, SnapActivity.class);
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }
}
