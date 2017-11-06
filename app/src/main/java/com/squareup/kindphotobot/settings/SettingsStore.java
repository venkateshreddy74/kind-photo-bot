package com.squareup.kindphotobot.settings;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingsStore {

  private static final String LOCK_TASK_KEY = "lockTask";
  private static final String PAYMENTS_ENABLED_KEY = "paymentsEnabled";
  private static final String TWEET_MESSAGE_KEY = "tweetMessage";
  private final SharedPreferences preferences;

  public SettingsStore(Context context) {
    Context appContext = context.getApplicationContext();
    preferences = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
  }

  public void setLockTask(boolean lockTask) {
    preferences.edit().putBoolean(LOCK_TASK_KEY, lockTask).apply();
  }

  public boolean shouldLockTask() {
    return preferences.getBoolean(LOCK_TASK_KEY, true);
  }

  public void setPaymentsEnabled(boolean paymentsEnabled) {
    preferences.edit().putBoolean(PAYMENTS_ENABLED_KEY, paymentsEnabled).apply();
  }

  public boolean arePaymentsEnabled() {
    return preferences.getBoolean(PAYMENTS_ENABLED_KEY, true);
  }

  public void setTweetMessage(String message) {
    preferences.edit().putString(TWEET_MESSAGE_KEY, message).apply();
  }

  public String getTweetMessage() {
    return preferences.getString(TWEET_MESSAGE_KEY, "Snap!");
  }
}
