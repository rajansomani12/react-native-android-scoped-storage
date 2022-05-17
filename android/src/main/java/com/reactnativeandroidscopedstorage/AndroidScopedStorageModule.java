package com.reactnativeandroidscopedstorage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class AccessFileModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    public static final int MULTIPLE_PERMISSIONS_ABOVE10 = 10;
    public static final int MULTIPLE_PERMISSIONS_BELOW10 = 11;
    public static final int VIDEO_CAPTURE_ABOVE_ANDROID10 = 101;
    public static final int VIDEO_CAPTURE_BELOW_ANDROID10 = 102;
    public static final int VIDEO_PICKER_ABOVE_ANDROID10 = 103;
    public static final int VIDEO_PICKER_BELOW_ANDROID10 = 104;

    public static final int IMAGE_CAPTURE_ABOVE_ANDROID10 = 105;
    public static final int IMAGE_CAPTURE_BELOW_ANDROID10 = 106;
    public static final int IMAGE_PICKER_ABOVE_ANDROID10 = 107;
    public static final int IMAGE_PICKER_BELOW_ANDROID10 = 108;
    public static final String SUB_DIRECTORY_NAME = "Video_Contest/";
    public static final String FILEPATH = "path";
    public static final String DURATION = "duration";
    public static final String IMAGE_EVENT_NAME = "imageData"; // this key will use while you want to pass data android to react native and get data using this key in react native
    public static final String VIDEO_EVENT_NAME = "videoData"; // this key will use while you want to pass data android to react native and get data using this key in react native
    String[] permissionsBelowAndroid10 = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            READ_EXTERNAL_STORAGE};

    String[] permissionsAboveAndroid10 = new String[]{
            Manifest.permission.CAMERA,
            READ_EXTERNAL_STORAGE};
    File photoFile = null;

    public AccessFileModule(@Nullable ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @NonNull
    @Override
    public String getName() {
        return "PermissionFile";
    }

    @ReactMethod
    public void accessStorage(int type) {
        Log.e("Type-->", String.valueOf(type));
//        deleteCache(getReactApplicationContext());
        if (checkPermissions()) {
            if (type == 3) //video Capture
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30); // pass max seconds for video capturing
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getReactApplicationContext().getCurrentActivity().startActivityForResult(intent, VIDEO_CAPTURE_ABOVE_ANDROID10);
                } else {
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30); // pass max seconds for video capturing
                    takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    if (takeVideoIntent.resolveActivity(getReactApplicationContext().getCurrentActivity().getPackageManager()) != null) {
                        getReactApplicationContext().getCurrentActivity().startActivityForResult(takeVideoIntent, VIDEO_CAPTURE_BELOW_ANDROID10);
                    }
                }

            } else if (type == 4) //video Picker
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getReactApplicationContext().getCurrentActivity().startActivityForResult(intent, VIDEO_PICKER_ABOVE_ANDROID10);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    getReactApplicationContext().getCurrentActivity().startActivityForResult(intent, VIDEO_PICKER_BELOW_ANDROID10);

                }

            } else if (type == 1) //Image Capture
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent takeImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takeImageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takeImageIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getReactApplicationContext().getCurrentActivity().startActivityForResult(takeImageIntent, IMAGE_CAPTURE_ABOVE_ANDROID10);
                } else {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    Intent takeImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if (takeImageIntent.resolveActivity(getReactApplicationContext().getCurrentActivity().getPackageManager()) != null) {

                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.i(TAG, "IOException");
                        }
                        if (photoFile != null) {
                            takeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            getReactApplicationContext().getCurrentActivity().startActivityForResult(takeImageIntent, IMAGE_CAPTURE_BELOW_ANDROID10);
                        }

                    }
                }

            } else if (type == 2) //Image Picker
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    getReactApplicationContext().getCurrentActivity().startActivityForResult(intent, IMAGE_PICKER_ABOVE_ANDROID10);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    getReactApplicationContext().getCurrentActivity().startActivityForResult(intent, IMAGE_PICKER_BELOW_ANDROID10);

                }

            }
        } else {
            Toast.makeText(getReactApplicationContext(), "Please give permissions", Toast.LENGTH_SHORT).show();
            // code if permission is not granted
        }

    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        WritableMap writableMap = new WritableNativeMap();
        WritableArray promiseArray = Arguments.createArray();
        if (requestCode == VIDEO_CAPTURE_ABOVE_ANDROID10 && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                writableMap = saveVideoForAndroi10AndAbove(data.getData());
                promiseArray.pushMap(writableMap);
                sendEvent(getReactApplicationContext(), VIDEO_EVENT_NAME, promiseArray);
            }

        } else if (requestCode == VIDEO_PICKER_ABOVE_ANDROID10 && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                String filePath = "";
//                filePath = getPathfromURI (data.getData());
//                File file = new File(filePath);
//                 long duration = 0;
//                 duration = getVideoDurationBelowAndroid10(getReactApplicationContext(),file);
//                if(duration == 0)
//                {
//                    Toast.makeText(getReactApplicationContext(), "This Video file is either not supported or currepted", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                Log.e("duration--->", String.valueOf(duration));
//                Log.e("filePath-->",String.valueOf(file.getAbsolutePath()));
                File videoFile = null;
                try {
                    videoFile = File.createTempFile("Video_Contest", ".mp4");

                    InputStream is = null;
                    OutputStream os = null;
                    try {
                        is = getReactApplicationContext().getCurrentActivity().getContentResolver().openInputStream(data.getData());
                        os = new FileOutputStream(videoFile);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                    } catch (Exception e) {
                    } finally {
                        is.close();
                        os.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /*String filePath = (new MediaAccessClass(BaseApplication.appInstance,getActivity())).getRealPath(getActivity(),uri);*/

                /*String filePath = getPDFPath(uri);*/

                String fileName = videoFile.getAbsolutePath().substring(videoFile.getAbsolutePath().lastIndexOf("/") + 1);
                long duration = 0;
                duration = getVideoDurationBelowAndroid10(getReactApplicationContext(), videoFile);
                writableMap.putString(FILEPATH, videoFile.getAbsolutePath());
                writableMap.putInt(DURATION, (int) duration);
                promiseArray.pushMap(writableMap);
                sendEvent(getReactApplicationContext(), VIDEO_EVENT_NAME, promiseArray);
            }
        } else if (requestCode == VIDEO_CAPTURE_BELOW_ANDROID10 && resultCode == RESULT_OK) {
            Uri vid = data.getData();
            String selectedFilePath = FilePath.getPath(getReactApplicationContext(), vid);
            final File file = new File(selectedFilePath);
            long duration = getVideoDurationBelowAndroid10(getReactApplicationContext(), file);
            if (duration == 0) {
                Toast.makeText(getReactApplicationContext(), "This Video file is either not supported or currepted", Toast.LENGTH_SHORT).show();
                return;
            }
//            File file = saveVideo(vid);
            Log.e("FilePath-->", file.getAbsolutePath());
            Log.e("duration--->", String.valueOf(duration));
            writableMap.putString(FILEPATH, file.getAbsolutePath());
            writableMap.putInt(DURATION, (int) duration);
            promiseArray.pushMap(writableMap);
            sendEvent(getReactApplicationContext(), VIDEO_EVENT_NAME, promiseArray);
        } else if (requestCode == VIDEO_PICKER_BELOW_ANDROID10 && resultCode == RESULT_OK) {
            Uri vid = data.getData();
            String selectedFilePath = FilePath.getPath(getReactApplicationContext(), vid);
            final File file = new File(selectedFilePath);
            long duration = getVideoDurationBelowAndroid10(getReactApplicationContext(), file);

            if (duration == 0) {
                Toast.makeText(getReactApplicationContext(), "This Video file is either not supported or currepted", Toast.LENGTH_SHORT).show();
                return;
            }
//            File file = saveVideo(vid);
            Log.e("FilePath-->", file.getAbsolutePath());
            Log.e("duration--->", String.valueOf(duration));

            writableMap.putString(FILEPATH, file.getAbsolutePath());
            writableMap.putInt(DURATION, (int) duration);
            promiseArray.pushMap(writableMap);
            sendEvent(getReactApplicationContext(), VIDEO_EVENT_NAME, promiseArray);

        } else if (requestCode == IMAGE_CAPTURE_BELOW_ANDROID10 && resultCode == RESULT_OK) {
//            Uri imgUri = data.getData();
//            String selectedFilePath = FilePath.getPath(getReactApplicationContext(), imgUri);
//            final File file = new File(selectedFilePath);
            Log.e("FilePath-->", photoFile.getAbsolutePath());
            writableMap.putString(FILEPATH, photoFile.getAbsolutePath());
            promiseArray.pushMap(writableMap);
            sendEvent(getReactApplicationContext(), IMAGE_EVENT_NAME, promiseArray);

        } else if (requestCode == IMAGE_PICKER_BELOW_ANDROID10 && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            String selectedFilePath = FilePath.getPath(getReactApplicationContext(), imgUri);
            File file = new File(selectedFilePath);
            Log.e("FilePath-->", file.getAbsolutePath());
            writableMap.putString(FILEPATH, file.getAbsolutePath());
            promiseArray.pushMap(writableMap);
            sendEvent(getReactApplicationContext(), IMAGE_EVENT_NAME, promiseArray);
        } else if (requestCode == IMAGE_PICKER_ABOVE_ANDROID10 && resultCode == RESULT_OK) {
//            Uri imgUri = data.getData();
//            String selectedFilePath = FilePath.getPath(getReactApplicationContext(), imgUri);
//            File file = new File(selectedFilePath);
//            Log.e("FilePath-->", file.getAbsolutePath());

            File imageFile = null;
            try{
                imageFile = File.createTempFile("Video_contest" ,".jpg");

                InputStream is = null;
                OutputStream os = null;
                try {
                    is = getReactApplicationContext().getCurrentActivity().getContentResolver().openInputStream(data.getData());
                    os = new FileOutputStream(imageFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                }catch (Exception e){
                } finally {
                    is.close();
                    os.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            /*String filePath = (new MediaAccessClass(BaseApplication.appInstance,getActivity())).getRealPath(getActivity(),uri);*/

            /*String filePath = getPDFPath(uri);*/

            String fileName = imageFile.getAbsolutePath().substring(imageFile.getAbsolutePath().lastIndexOf("/") + 1);

            writableMap.putString(FILEPATH, imageFile.getAbsolutePath());
            promiseArray.pushMap(writableMap);
            sendEvent(getReactApplicationContext(), IMAGE_EVENT_NAME, promiseArray);
        } else if (requestCode == IMAGE_CAPTURE_ABOVE_ANDROID10 && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            try {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                writableMap = saveImageAndroid10AndAbove(bitmap, "VideoContest");
                promiseArray.pushMap(writableMap);
                sendEvent(getReactApplicationContext(), IMAGE_EVENT_NAME, promiseArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //            File file = new File(selectedFilePath);
            //            Log.e("FilePath-->",file.getAbsolutePath());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private WritableMap saveVideoForAndroi10AndAbove(Uri uri3) {
        WritableMap writableMap = new WritableNativeMap();
        Context context = getReactApplicationContext().getCurrentActivity();
        String videoFileName = "video_competition" + System.currentTimeMillis() + ".mp4";

        ContentValues valuesvideos;
        valuesvideos = new ContentValues();
        valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + SUB_DIRECTORY_NAME);
        valuesvideos.put(MediaStore.Video.Media.TITLE, videoFileName);
        valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName);
        valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 1);
        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); //all video files on primary external storage
        Uri uriSavedVideo = resolver.insert(collection, valuesvideos);

        ParcelFileDescriptor pfd;

        try {
            pfd = context.getContentResolver().openFileDescriptor(uriSavedVideo, "w");

            assert pfd != null;
            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

            // Get the already saved video as fileinputstream from here
            InputStream in = getReactApplicationContext().getCurrentActivity().getContentResolver().openInputStream(uri3);


            byte[] buf = new byte[8192];

            int len;
            int progress = 0;
            while ((len = in.read(buf)) > 0) {
                progress = progress + len;

                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            pfd.close();
            valuesvideos.clear();
            valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0);
            valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0); //only your app can see the files until pending is turned into 0

            context.getContentResolver().update(uriSavedVideo, valuesvideos, null, null);
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "/Movies/" + SUB_DIRECTORY_NAME + videoFileName);


            long videoDuration = getVideoDurationBelowAndroid10(context, file);
            Log.e("Duration--->", String.valueOf(videoDuration));
            Log.e("FilePath--->", String.valueOf(file.getPath()));
            writableMap.putString(FILEPATH, String.valueOf(file.getAbsolutePath()));
            writableMap.putInt(DURATION, (int) videoDuration);

        } catch (Exception e) {
            Toast.makeText(context, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return writableMap;
    }

    private WritableMap saveImageAndroid10AndAbove(Bitmap bitmap, @NonNull String name) throws IOException {
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        name = name + "_" + ts;
        boolean saved;
        OutputStream fos = null;
        String selectedFilePath = "";
        Uri imageUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getReactApplicationContext().getCurrentActivity().getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + SUB_DIRECTORY_NAME);
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            selectedFilePath = FilePath.getPath(getReactApplicationContext(), imageUri);

            fos = resolver.openOutputStream(imageUri);
        }
//


        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
        Log.e("Path-->", selectedFilePath);
        WritableMap map = new WritableNativeMap();
        map.putString(FILEPATH, selectedFilePath);


        return map;
    }


    public long getVideoDurationBelowAndroid10(Context context, File file) {
        long timeInMillisec = 0;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
            retriever.setDataSource(context, Uri.fromFile(file));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Long.parseLong(time);
            retriever.release();
            return timeInMillisec;
        } catch (Exception e) {
            Log.e("Exception--->", e.getMessage());
        }
        return timeInMillisec;
    }

    public String getPathfromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getReactApplicationContext().getCurrentActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;

    }

    @SuppressLint("Range")
    public static long checkVideoDurationValidation(Context context, Uri uri) {
        Log.d("CommonHandler", "Uri: " + uri);
        Cursor cursor = MediaStore.Video.query(context.getContentResolver(), uri, new
                String[]{MediaStore.Video.VideoColumns.DURATION});
        long duration = 0;
        if (cursor != null && cursor.moveToFirst()) {
            duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video
                    .VideoColumns.DURATION));
            cursor.close();
        }

        return duration;
    }

    private boolean checkPermissions() {

        boolean returnValue = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int result = 0;
            List<String> listPermissionsNeeded = new ArrayList<>();
            for (String p : permissionsAboveAndroid10) {
                result = ContextCompat.checkSelfPermission(getReactApplicationContext().getCurrentActivity(), p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                    returnValue = false;
                } else {
                    returnValue = true;
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(getReactApplicationContext().getCurrentActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS_ABOVE10);
                return false;
            }
        } else {
            int result = 0;
            List<String> listPermissionsNeeded = new ArrayList<>();
            for (String p : permissionsBelowAndroid10) {
                result = ContextCompat.checkSelfPermission(getReactApplicationContext().getCurrentActivity(), p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                    returnValue = false;
                } else {
                    returnValue = true;
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(getReactApplicationContext().getCurrentActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS_BELOW10);
                return false;
            }

        }

        return returnValue;
    }

    public static boolean checkIsImage(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String type = contentResolver.getType(uri);
        if (type != null) {
            return type.startsWith("image/");
        } else {
            // try to decode as image (bounds only)
            InputStream inputStream = null;
            try {
                inputStream = contentResolver.openInputStream(uri);
                if (inputStream != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    return options.outWidth > 0 && options.outHeight > 0;
                }
            } catch (IOException e) {
                // ignore
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.closeQuietly(inputStream);
                }
            }
        }
        // default outcome if image not confirmed
        return false;
    }

    protected File saveVideo(final Uri uriVideo) {
        boolean success = false;
        // make the directory
        File vidDir = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_MOVIES) + File.separator + "Video Contest");
        vidDir.mkdirs();
        // create unique identifier
        Random generator = new Random();
        int n = 100;
        n = generator.nextInt(n);
        // create file name
        String videoName = "Video_Contest_App" + n + ".mp4";
        File fileVideo = new File(vidDir.getAbsolutePath(), videoName);

        try {
            fileVideo.createNewFile();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (success) {
            Toast.makeText(getReactApplicationContext(), "Video saved!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getReactApplicationContext(),
                    "Error during video saving", Toast.LENGTH_LONG).show();
        }


        return vidDir;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        String mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableArray map) {
        Log.e("sendEvent-->","event has fired");
        reactContext
                .getJSModule(RCTNativeAppEventEmitter.class)
                .emit(eventName, map);
    }
    @ReactMethod
    public void removeListeners(Integer count) {
        // Remove upstream listeners, stop unnecessary background tasks
    }





}
