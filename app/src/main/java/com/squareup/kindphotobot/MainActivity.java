package com.squareup.kindphotobot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.squareup.kindphotobot.snap.SnapActivity;
import com.squareup.sdk.pos.ChargeRequest;
import com.squareup.sdk.pos.PosClient;
import com.squareup.sdk.pos.PosSdk;

import static com.squareup.sdk.pos.CurrencyCode.USD;

public class MainActivity extends AppCompatActivity {

  private static final int CHARGE_REQUEST_CODE = 1;
  private PosClient posClient;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);
    // Replace YOUR_CLIENT_ID with your Square-assigned client application ID,
    // available from the Application Dashboard.

    posClient = PosSdk.createClient(this, "sq0idp-C27kk-ElqJsW-gYk03FoyA");

    findViewById(R.id.photobooth_button).setOnClickListener(v -> startTransaction());
  }

  private void startPhotobooth() {

    startActivity(new Intent(this, SnapActivity.class));
  }

  public void startTransaction() {
    ChargeRequest request = new ChargeRequest.Builder(1_00, USD).build();
    try {
      Intent intent = posClient.createChargeIntent(request);
      startActivityForResult(intent, CHARGE_REQUEST_CODE);
    } catch (ActivityNotFoundException e) {
      showDialog("Error", "Square Point of Sale is not installed", null);
      posClient.openPointOfSalePlayStoreListing();
    }
  }

  private void showDialog(String title, String message, DialogInterface.OnClickListener listener) {
    Log.d("MainActivity", title + " " + message);
    new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, listener)
            .show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CHARGE_REQUEST_CODE) {
      if (data == null) {
        showDialog("Error", "Square Point of Sale was uninstalled or crashed", null);
        return;
      }

      if (resultCode == Activity.RESULT_OK) {
        ChargeRequest.Success success = posClient.parseChargeSuccess(data);
        String message = "Client transaction id: " + success.clientTransactionId;
        showDialog("Success!", message, null);

        //TODO Ensure
        startPhotobooth();

      } else {
        ChargeRequest.Error error = posClient.parseChargeError(data);

        if (error.code == ChargeRequest.ErrorCode.TRANSACTION_ALREADY_IN_PROGRESS) {
          String title = "A transaction is already in progress";
          String message = "Please complete the current transaction in Point of Sale.";

          showDialog(title, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // Some errors can only be fixed by launching Point of Sale
              // from the Home screen.
              posClient.launchPointOfSale();
            }
          });
        } else {
          showDialog("Error: " + error.code, error.debugDescription, null);
        }
      }
    }
  }
}
