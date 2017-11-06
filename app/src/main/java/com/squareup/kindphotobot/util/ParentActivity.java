package com.squareup.kindphotobot.util;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

public abstract class ParentActivity extends AppCompatActivity {

  private final boolean immersive;

  public ParentActivity(boolean immersive) {
    this.immersive = immersive;
  }

  @Override public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (immersive && hasFocus) {
      getWindow().getDecorView()
          .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
  }

  public boolean isAppInLockTaskMode() {
    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

    if (SDK_INT >= M) {
      // For SDK version 23 and above.
      return activityManager.getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_NONE;
    }

    if (SDK_INT >= LOLLIPOP) {
      // When SDK version >= 21. This API is deprecated in 23.
      return activityManager.isInLockTaskMode();
    }
    return false;
  }
}
