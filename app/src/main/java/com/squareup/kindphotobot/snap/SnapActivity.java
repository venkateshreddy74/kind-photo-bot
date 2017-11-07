package com.squareup.kindphotobot.snap;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.squareup.kindphotobot.R;
import com.squareup.kindphotobot.snap.camera.CameraSourcePreview;
import com.squareup.kindphotobot.snap.camera.GraphicOverlay;
import com.squareup.kindphotobot.snap.graphic.FaceGraphic;
import com.squareup.kindphotobot.snap.graphic.GraphicFactory;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.squareup.kindphotobot.util.RecyclerSnaps.centerSnap;

public class SnapActivity extends AppCompatActivity {

  private static final int PRINTED_WIDTH_INCH = 86;
  private static final int PRINTED_HEIGHT_INCH = 54;
  /** Ratio of the KC-36IP cards, 54x86mm */
  public static final float PRINTED_CARD_RATIO = (PRINTED_WIDTH_INCH * 1.0f) / PRINTED_HEIGHT_INCH;
  private static final int PRINTER_DPI = 300;
  // If you get a dark preview, try lowering the frame rate to 15 fps.
  // https://github.com/googlesamples/android-vision/issues/162#issuecomment-271508706
  private static final float MAX_FRAME_RATE = 30f;
  private static final int RC_HANDLE_GMS = 9001;
  private static final int PERMISSION_REQUEST_CODE = 1;
  private final List<GraphicFaceTracker> faceTrackers = new ArrayList<>();
  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private volatile int graphicIndex;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.snap);

    findViewById(R.id.take_picture).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (cameraSource != null) {
          cameraSource.takePicture(new CameraSource.ShutterCallback() {

            @Override
            public void onShutter() {

            }
          }, new CameraSource.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] bytes) {

              BitmapFactory.Options options = new BitmapFactory.Options();
              options.inMutable = true;


              Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

              FaceDetector faceDetector = new FaceDetector.Builder(getBaseContext()).setMode(FaceDetector.ACCURATE_MODE).build();

             /* FaceDetector detector = new FaceDetector.Builder(getBaseContext())
                      .setProminentFaceOnly(true)
                      .build();*/

              Frame frame = new Frame.Builder().setBitmap(bitmap).build();


              SparseArray<Face> faceSparseArray = faceDetector.detect(frame);

              //  FaceGraphic faceGraphic = (FaceGraphic) GraphicFactory.values();

              FaceGraphic faceGraphic = GraphicFactory.values()[graphicIndex].create(getBaseContext());

              faceGraphic.draw(new Canvas(), Scaler.ISO);

              faceDetector.release();


              Toast toast = new Toast(getBaseContext());
              toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
              toast.setDuration(Toast.LENGTH_LONG);
              LayoutInflater inflater = LayoutInflater.from(getBaseContext());
              View toastView = inflater.inflate(R.layout.toasts, null);
              ImageView pictureView = toastView.findViewById(R.id.picture_view);
              pictureView.setImageBitmap(bitmap);
              toast.setView(toastView);
              toast.show();

              print(bitmap);


            }
          });
        }
      }
    });

    preview = findViewById(R.id.preview);
    graphicOverlay = preview.getGraphicOverlay();

    if (hasPermission(CAMERA) && hasPermission(WRITE_EXTERNAL_STORAGE)) {
      createCameraSource();
    } else {
      requestRequiredPermissions();
    }

    RecyclerView recyclerView = findViewById(R.id.graphic_recycler_view);

    centerSnap(recyclerView, (position) -> updateGraphicFactory(GraphicFactory.values()[position]));
  }

  private void print(Bitmap bitmap) {
    PrintHelper photoPrinter = new PrintHelper(getBaseContext());
    photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
    photoPrinter.printBitmap("awesome_photo.jpg", bitmap, () -> this.finish());
  }

  private boolean hasPermission(String permission) {
    return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED;
  }

  public void updateGraphicFactory(GraphicFactory graphicFactory) {
    graphicIndex = graphicFactory.ordinal();
    for (GraphicFaceTracker faceTracker : faceTrackers) {
      faceTracker.faceGraphic = createFaceGraphic();
    }
    graphicOverlay.postInvalidate();
  }

  private FaceGraphic createFaceGraphic() {
    return GraphicFactory.values()[graphicIndex].create(this);
  }

  private void requestRequiredPermissions() {
    String[] permissions = new String[] { CAMERA, WRITE_EXTERNAL_STORAGE };

    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)
        && !ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
      ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
      return;
    }

    View.OnClickListener listener =
        view -> ActivityCompat.requestPermissions(SnapActivity.this, permissions,
            PERMISSION_REQUEST_CODE);

    Snackbar.make(graphicOverlay, "Access to camera and external storage permissions required.",
        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, listener).show();
  }

  private void createCameraSource() {
    Context context = getApplicationContext();
    FaceDetector detector = createFaceDetector(context);

    detector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());

    if (!detector.isOperational()) {
      Timber.d("Face detector dependencies are not yet available.");
    }

    cameraSource = new CameraSource.Builder(context, detector) //
        // Camera will decide actual size, and we'll crop accordingly in layout.
        .setRequestedPreviewSize(640, 480)
        .setFacing(CameraSource.CAMERA_FACING_FRONT)
        .setRequestedFps(MAX_FRAME_RATE)
        .setAutoFocusEnabled(true)
        .build();
  }

  private FaceDetector createFaceDetector(Context context) {
    return new FaceDetector.Builder(context) //
        .setLandmarkType(FaceDetector.ALL_LANDMARKS) //
        .setClassificationType(FaceDetector.NO_CLASSIFICATIONS) //
        .setTrackingEnabled(true).setMode(FaceDetector.FAST_MODE).build();
  }

  @Override protected void onResume() {
    super.onResume();
    startCameraSource();
  }

  @Override protected void onPause() {
    super.onPause();
    preview.stop();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    if (requestCode != PERMISSION_REQUEST_CODE) {
      Timber.d("Got unexpected permission result: " + requestCode);
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      return;
    }

    if (grantResults.length == 2
        && grantResults[0] == PERMISSION_GRANTED
        && grantResults[1] == PERMISSION_GRANTED) {
      Timber.d("Permissions granted - initialize the camera source");
      createCameraSource();
      return;
    }

    new AlertDialog.Builder(this).setTitle("Camera or storage permission missing")
        .setMessage("The camera and storage permissions are required to take pictures.")
        .setPositiveButton(R.string.ok, (dialog, id) -> finish())
        .show();
  }

  private void startCameraSource() {
    // check that the device has play services available.
    int code =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
    if (code != ConnectionResult.SUCCESS) {
      GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS).show();
    }
    if (cameraSource != null) {
      preview.start(cameraSource, graphicOverlay, PRINTED_CARD_RATIO);
    }
  }

  private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
    @Override public Tracker<Face> create(Face face) {
      return new GraphicFaceTracker();
    }
  }

  private class GraphicFaceTracker extends Tracker<Face> implements Graphic {
    // Updated when changing graphic.
    private FaceGraphic faceGraphic;

    @Override public void onNewItem(int i, Face face) {
      faceGraphic = createFaceGraphic();
      faceTrackers.add(this);
    }

    @Override public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
      faceGraphic.updateFace(face);
      graphicOverlay.update(this);
    }

    @Override public void onMissing(FaceDetector.Detections<Face> detectionResults) {
      graphicOverlay.forget(this);
      faceGraphic.forgetFace();
    }

    @Override public void onDone() {
      graphicOverlay.forget(this);
      faceGraphic.forgetFace();
      faceTrackers.remove(this);
    }

    @Override public void draw(Canvas canvas) {
      faceGraphic.draw(canvas, graphicOverlay);
    }
  }
}
