package com.squareup.kindphotobot.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.squareup.kindphotobot.App;
import com.squareup.kindphotobot.R;
import com.squareup.kindphotobot.util.ParentActivity;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class SettingsActivity extends ParentActivity {

  private TwitterLoginButton twitterLoginButton;
  private View twitterLogoutButton;
  private TextView twitterStateView;
  private EditText tweetMessageView;
  private SettingsStore settingsStore;

  public SettingsActivity() {
    super(false);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.settings);

    App app = App.from(this);
    settingsStore = app.settingsStore();

    twitterLoginButton = findViewById(R.id.twitter_login_button);
    twitterLogoutButton = findViewById(R.id.twitter_logout_button);
    twitterLoginButton.setCallback(new Callback<TwitterSession>() {
      @Override public void success(Result<TwitterSession> result) {
        snack("Twitter login success");
        updateTwitterState();
      }

      @Override public void failure(TwitterException exception) {
        snack("Twitter login failure");
        updateTwitterState();
      }
    });
    twitterLogoutButton.setOnClickListener(view -> {
      TwitterCore.getInstance().getSessionManager().clearActiveSession();
      updateTwitterState();
    });

    CheckBox kioskCheckbox = findViewById(R.id.kiosk_check);
    kioskCheckbox.setChecked(settingsStore.shouldLockTask());
    kioskCheckbox.setOnCheckedChangeListener(
        (compoundButton, checked) -> settingsStore.setLockTask(checked));

    CheckBox paymentsCheckbox = findViewById(R.id.payments_check);
    paymentsCheckbox.setChecked(settingsStore.arePaymentsEnabled());
    paymentsCheckbox.setOnCheckedChangeListener(
        (compoundButton, checked) -> settingsStore.setPaymentsEnabled(checked));

    tweetMessageView = findViewById(R.id.tweet_message);
    tweetMessageView.setText(settingsStore.getTweetMessage());
    twitterStateView = findViewById(R.id.twitter_state);
    updateTwitterState();
  }

  @Override protected void onPause() {
    super.onPause();
    settingsStore.setTweetMessage(tweetMessageView.getText().toString());
  }

  private void snack(String message) {
    Snackbar.make(twitterLoginButton, message, Snackbar.LENGTH_SHORT).show();
  }

  private void updateTwitterState() {
    TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
    if (session == null) {
      twitterStateView.setText("Twitter: not logged in.");
      twitterLoginButton.setVisibility(View.VISIBLE);
      twitterLogoutButton.setVisibility(View.GONE);
    } else {
      twitterStateView.setText("Twitter: logged in as " + session.getUserName());
      twitterLoginButton.setVisibility(View.GONE);
      twitterLogoutButton.setVisibility(View.VISIBLE);
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    twitterLoginButton.onActivityResult(requestCode, resultCode, data);
  }
}
