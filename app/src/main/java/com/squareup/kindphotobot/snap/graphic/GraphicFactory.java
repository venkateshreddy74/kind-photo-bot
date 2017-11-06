package com.squareup.kindphotobot.snap.graphic;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.squareup.kindphotobot.R;

public enum GraphicFactory {

  OREO(R.drawable.oreo) {
    @Override public FaceGraphic create(Context context) {
      return new OreoGraphic(context);
    }
  },

  ROBOT(R.drawable.robot) {
    @Override public FaceGraphic create(Context context) {
      return new RobotGraphic(context);
    }
  },

  LEAK_CANARY(R.drawable.leakcanary) {
    @Override public FaceGraphic create(Context context) {
      return new LeakCanaryGraphic(context);
    }
  },

  JAKE(R.drawable.jake) {
    @Override public FaceGraphic create(Context context) {
      return new JakeGraphic(context);
    }
  },

  JAKE_FULL(R.drawable.jake_full) {
    @Override public FaceGraphic create(Context context) {
      return new JakeFullGraphic(context);
    }
  };

  public final @DrawableRes int drawableResId;

  GraphicFactory(@DrawableRes int drawableResId) {
    this.drawableResId = drawableResId;
  }

  public abstract FaceGraphic create(Context context);

}
