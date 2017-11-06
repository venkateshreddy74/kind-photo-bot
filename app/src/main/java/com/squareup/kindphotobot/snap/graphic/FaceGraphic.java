package com.squareup.kindphotobot.snap.graphic;

import android.graphics.Canvas;
import com.google.android.gms.vision.face.Face;
import com.squareup.kindphotobot.snap.Scaler;

public interface FaceGraphic {
  void draw(Canvas canvas, Scaler translator);

  void updateFace(Face face);

  void forgetFace();
}
