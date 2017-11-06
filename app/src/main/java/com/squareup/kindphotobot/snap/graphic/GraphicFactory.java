package com.squareup.kindphotobot.snap.graphic;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.squareup.kindphotobot.R;

public enum GraphicFactory {

  OREO(R.drawable.oreo) {
    @Override public FaceGraphic create(Context context) {
      return new OreoGraphic(context);
    }
  }, //

  LEAK_CANARY(R.drawable.leakcanary) {
    @Override public FaceGraphic create(Context context) {
      return new LeakCanaryGraphic(context);
    }
  }, //

  // Add more effects here.

  ;

  public final @DrawableRes int drawableResId;

  GraphicFactory(@DrawableRes int drawableResId) {
    this.drawableResId = drawableResId;
  }

  public abstract FaceGraphic create(Context context);

}
