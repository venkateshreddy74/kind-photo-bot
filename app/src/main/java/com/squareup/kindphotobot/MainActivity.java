package com.squareup.kindphotobot;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import com.squareup.kindphotobot.charge.ChargeActivity;
import com.squareup.kindphotobot.printer.PrinterSetupActivity;
import com.squareup.kindphotobot.settings.SettingsActivity;
import com.squareup.kindphotobot.settings.SettingsStore;
import com.squareup.kindphotobot.snap.SnapActivity;
import com.squareup.kindphotobot.util.ParentActivity;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class MainActivity extends ParentActivity {

  private SettingsStore settingsStore;

  public MainActivity() {
    super(false);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    settingsStore = App.from(this).settingsStore();

    setContentView(R.layout.main);

    findViewById(R.id.photobooth_button).setOnClickListener(v -> startPhotobooth());

    findViewById(R.id.printers_button).setOnClickListener(
        v -> startActivity(new Intent(MainActivity.this, PrinterSetupActivity.class)));

    findViewById(R.id.settings_button).setOnClickListener(
        v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
  }

  @Override protected void onResume() {
    super.onResume();

    if (settingsStore.shouldLockTask()) {
      if (SDK_INT >= LOLLIPOP) {
        if (!isAppInLockTaskMode()) {
          // This shows a dialog to confirm that we want to go in lock task mode.
          startLockTask();
        }
      } else {
        snack("Kiosk not supported on API " + SDK_INT);
      }
    }
  }

  private void startPhotobooth() {
    if (settingsStore.arePaymentsEnabled()) {
      startActivity(new Intent(MainActivity.this, ChargeActivity.class));
    } else {
      startActivity(new Intent(this, SnapActivity.class));
    }
  }

  private void snack(String message) {
    Snackbar.make(findViewById(R.id.photobooth_button), message, Snackbar.LENGTH_SHORT).show();
  }
}
