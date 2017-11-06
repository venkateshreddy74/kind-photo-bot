# Build Your Own Cash flow Positive Photobooth!

Feel free to ask us for help at any time during this workshop session! 

## Step 1: Set up

1. Clone this repository: `git clone git@github.com:square/kind-photo-bot.git` 
2. Open the project with Android Studio (3.0)
3. Connect a device and run the app

The provided code contains `MainActivity`, which has just one button which starts the `SnapActivity`. `SnapActivity` displays a preview of the front-facing camera and renders some effects on top. Take a look at the code!

## Step 2: Add Payments

The button in `MainActivity` currently launches the `SnapActivity`. Instead, we want the photobooth app require a $1 payment before starting `SnapActivity`. To do that, we'll integrate with the Square Point of Sale API.

To get started with the API:

1. Download the [Square Point of Sale app](https://play.google.com/store/apps/details?id=com.squareup) on your device.
2. Open the app and log in or sign up for a Square merchant account. Don't worry about linking your bank account for now, and you can skip the step about getting a Square reader shipped (we're providing those).
3. Sign in with your new Square account and register your new application on Square's [developer portal](https://connect.squareup.com/apps). 
  * To register your application, click **New Application**, enter a name, and click **Create App**. This is your developer portal, and you'll need to come back here later. For now, take note of your **Application ID** under the **Credentials** tab
4. Follow the instructions for getting started with the Point of Sale API for Android [here](https://docs.connect.squareup.com/articles/point-of-sale-api-android) NOTE: you can start with the second step (Obtain your app's SHA-1 fingerprint) since we've already registered our Application name.
5. Following the instructions in the documentation, link up the button in `MainActivity` to a POS API call in the `MainActivity`'s `onCreate` method. Receive the result from the Point of Sale transaction in the `onActivityResult` method.
6. If the transaction result was a success, start the Photobooth activity.
7. Try it out! You can test by taking cash payments, or use the provided Square readers to take card payments and refund yourself later.

## Step 2: Take Pictures

1. Add a button in `SnapActivity` to take a picture using <a href="https://developers.google.com/android/reference/com/google/android/gms/vision/CameraSource.html#takePicture(com.google.android.gms.vision.CameraSource.ShutterCallback, com.google.android.gms.vision.CameraSource.PictureCallback)">CameraSource#takePicture</a>
2. In the picture callback, you're provided with a `byte[]` containing the captured photo. Here's how we turn it into the final bitmap to print.
  * Use <a href="https://developer.android.com/reference/android/graphics/BitmapFactory.html#decodeByteArray(byte[], int, int, android.graphics.BitmapFactory.Options)">BitmapFactory#decodeByteArray</a> to turn the `byte[]` into a `Bitmap`. Don't forget to use add the `inMutable` option so that we can draw on the `Bitmap`. 
  * Create a new <a href="https://developers.google.com/android/reference/com/google/android/gms/vision/face/FaceDetector">FaceDetector</a>. Set the mode to `FaceDetector.ACCURATE_MODE` for better results. 
  * Create a <a href="https://developers.google.com/android/reference/com/google/android/gms/vision/Frame">Frame</a> wrapping the `Bitmap` and <a href="https://developers.google.com/android/reference/com/google/android/gms/vision/face/FaceDetector.html#detect(com.google.android.gms.vision.Frame)">FaceDetector#detect</a> to find all the faces. 
  * Create a `Canvas` around the `Bitmap` to draw on it. 
  * Find the selected `FaceGraphic` by calling `GraphicFactory.values()[SnapActivity.graphicIndex]` and draw the graphic on the `Canvas`. Use `Scaler.ISO` when drawing (we're not drawing on the scaled-down camera preview any more)
  * Release the `FaceDetector`
  * Display the photo in a `Toast`. Create a `toast.xml` layout that contains an `ImageView` with id `picture_view`, then:

  ```
Toast toast = new Toast(activity);
toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
toast.setDuration(Toast.LENGTH_SHORT);
LayoutInflater inflater = LayoutInflater.from(activity);
View toastView = inflater.inflate(R.layout.toast, null);
ImageView pictureView = content.findViewById(R.id.picture_view);
pictureView.setImageBitmap(bitmap);
toast.setView(toastView);
toast.show();

  ```

## Step 3: Add printing

1. Visit [this Google Cloud Print link](https://www.google.com/cloudprint/addpublicprinter.html?printerid=e2289732-f1e8-7440-c1ce-1a6eb16882c3&key=783088520) and sign in with the Google account on your device. This will make our printer available on your device.
2. Display the printer selection activity using `PrintHelper`:
```
  PrintHelper photoPrinter = new PrintHelper(activity);
  photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
  photoPrinter.printBitmap("awesome_photo.jpg", bitmap, () -> activity.finish());
```

Congrats, you're done!!!

## If you have more time:

* Add more filters. Use our existing `FaceGraphic` classes as examples.
* Use the shutter callback to add feedback when the photo gets taken.
* Add a countdown to the camera snap on the `SnapActivity`
* Print using [Google Cloud Printing](https://developers.google.com/cloud-print/docs/appInterfaces) 
* Tweet the finished photos using [Twitter Kit](https://dev.twitter.com/twitterkit/android/overview)
* Enable Kiosk mode and prevent users from existing using <a href="https://developer.android.com/reference/android/app/Activity.html#startLockTask()">Activity#startLockTask</a>

