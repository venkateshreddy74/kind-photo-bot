package com.squareup.kindphotobot;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.squareup.kindphotobot.snap.SnapActivity;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);

    findViewById(R.id.photobooth_button).setOnClickListener(v -> startPhotobooth());
  }

  private void startPhotobooth() {
    startActivity(new Intent(this, SnapActivity.class));
  }
}
