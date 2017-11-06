package com.squareup.kindphotobot.snap.graphic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import com.google.android.gms.vision.face.Face;
import com.squareup.kindphotobot.R;
import com.squareup.kindphotobot.snap.Scaler;

public class JakeFullGraphic implements FaceGraphic {

  private final Bitmap bitmap;
  private final Matrix matrix;

  public JakeFullGraphic(Context context) {
    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.jake_full);
    matrix = new Matrix();
  }

  @Override public void updateFace(Face face) {
  }

  @Override public void draw(Canvas canvas, Scaler translator) {
    float jakeHeight = (2f / 3) * canvas.getHeight();
    float jakeWidth = (bitmap.getWidth() * jakeHeight) / bitmap.getHeight();

    float jakeX = canvas.getWidth() / 2 - jakeWidth / 2;

    float jakeY = canvas.getHeight() - jakeHeight;

    matrix.reset();
    float scaleX = (jakeWidth * 1f) / bitmap.getWidth();
    float scaleY = (jakeHeight * 1f) / bitmap.getHeight();
    matrix.postScale(scaleX, scaleY);
    matrix.postTranslate(jakeX, jakeY);
    if (translator.isFrontFacing()) {
      matrix.postScale(-1, 1, jakeX + jakeWidth / 2, jakeY + jakeHeight / 2);
    }

    canvas.drawBitmap(bitmap, matrix, null);
  }

  @Override public void forgetFace() {
  }
}
