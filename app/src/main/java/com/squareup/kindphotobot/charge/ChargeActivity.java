package com.squareup.kindphotobot.charge;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.squareup.kindphotobot.R;
import com.squareup.kindphotobot.snap.SnapActivity;
import com.squareup.kindphotobot.util.ParentActivity;
import com.squareup.sdk.pos.ChargeRequest;
import com.squareup.sdk.pos.PosClient;
import com.squareup.sdk.pos.PosSdk;
import timber.log.Timber;

import static com.squareup.sdk.pos.ChargeRequest.TenderType.CARD;
import static com.squareup.sdk.pos.CurrencyCode.USD;
import static com.squareup.sdk.pos.PosApi.AUTO_RETURN_TIMEOUT_MIN_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ChargeActivity extends ParentActivity {

  // Go to https://connect.squareup.com/apps to create an app.
  private static final String CLIENT_ID = "TODO Update ChargeActivity.CLIENT_ID";
  private static final int CHARGE_REQUEST_CODE = 0xF00D;
  private PosClient posClient;

  public ChargeActivity() {
    super(true);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.landing_layout);
    posClient = PosSdk.createClient(this, CLIENT_ID);
    findViewById(R.id.tap).setOnClickListener(v -> chargePosApi());
  }

  private void chargePosApi() {
    ChargeRequest.Builder requestBuilder = new ChargeRequest.Builder(1_00, USD) //
        .restrictTendersTo(CARD) //
        .note(getString(R.string.pos_api_note)) //
        .autoReturn(AUTO_RETURN_TIMEOUT_MIN_MILLIS, MILLISECONDS);
    Intent chargeIntent = posClient.createChargeIntent(requestBuilder.build());
    startActivityForResult(chargeIntent, CHARGE_REQUEST_CODE);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CHARGE_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        startActivity(new Intent(this, SnapActivity.class));
      } else {
        ChargeRequest.Error error = posClient.parseChargeError(data);
        if (error.code != ChargeRequest.ErrorCode.TRANSACTION_CANCELED) {
          displayDialog(error);
          Timber.d(error.debugDescription);
        }
      }
    }
  }

  @Override public void onBackPressed() {
    // Ignore back when app is in lock task mode.
    if (isAppInLockTaskMode()) {
      Toast.makeText(this, R.string.app_task_locked, Toast.LENGTH_SHORT).show();
      return;
    }
    super.onBackPressed();
  }

  private void displayDialog(ChargeRequest.Error error) {
    Timber.d(error.debugDescription);
    new AlertDialog.Builder(this).setTitle(R.string.transaction_error)
        .setMessage(error.debugDescription)
        .show();
  }
}
