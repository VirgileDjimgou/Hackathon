package com.zeiss.cloudcam;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

public class CaptureFragment extends CameraPreviewFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "CaptureFragment";

    //private static final String BUCKET_NAME = "z0001-bucket";
    private static final String BUCKET_NAME = "zoafrotest";

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");

    private int IMAGE_FORMAT = ImageFormat.JPEG;

    private static final int DISPLAY_CONTROLS_NONE = 0;

    private static final int DISPLAY_CONTROLS_ISO = 1;

    private static final int DISPLAY_CONTROLS_FOCUS = 2;

    private static final int DISPLAY_CONTROLS_EXPOSURE_TIME = 3;

    private static final int DISPLAY_CONTROLS_CAPTURE = 4;

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_FOR_FOCUS_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure precapture sequence to complete.
     */
    private static final int STATE_WAITING_FOR_PRECAPTURE_SEQUENCE_COMPLETE = 2;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 3;

    private View mControlGrid;

    private ToggleButton mIsoButton;
    private SeekBar mIsoSeekBar;
    private TextView mIsoLabel;

    private ToggleButton mFocusButton;
    private ToggleButton mAutoFocusButton;
    private SeekBar mFocusDistanceSeekBar;
    private TextView mFocusDistanceLabel;

    private ToggleButton mExposureTimeButton;
    private SeekBar mExposureTimeSeekBar;
    private TextView mExposureTimeLabel;

    private Button mCaptureButton;
    private ProgressBar mCaptureProgressBar;

    private Button mUploadButton;

    private File mImageDirectory;

    private File mImageFilePattern;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;

    private float mBestFocusDistance;

    private long mBestExposureTime;

    private List<CaptureRequest> mCaptureRequests = new ArrayList<CaptureRequest>();

    private List<CaptureResult> mCaptureResults = new ArrayList<CaptureResult>();

    private ArrayList<Image> mCapturedImages = new ArrayList<Image>();

    private int mImagesCaptured;

    private int mImagesAvailable;

    private int mImagesToCapture = 12;

    private long mImageCaptureStart;

    private File mLastImageFile;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility mTransferUtility;

    // A List of all transfers
    private List<TransferObserver> mTransferObservers;

    /**
     * This is a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mImagesAvailable++;
            Image image = reader.acquireNextImage();

            Log.d(TAG, "ImageReader.OnImageAvailableListener.onImageAvailable(" + mImagesAvailable + ", " + image.getWidth() + " x " + image.getHeight() + ")");

            // image 0 is dummy image => just close
            if (mImagesAvailable > 0) {
                File imageFile = new File(String.format(mImageFilePattern.getAbsolutePath(), DATE_TIME_FORMAT.format(new Date()), "jpg"));
                Log.d(TAG, "Saving image '" + imageFile.getAbsolutePath() + "'");
                mBackgroundHandler.post(new JpegImageSaver(image, imageFile));
                mLastImageFile = imageFile;
            } else {
                // image 0 is dummy image => just close
                image.close();
            }
        }

    };

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    protected CameraCaptureSession.CaptureCallback mPreCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            onPreviewCapture(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            onPreviewCapture(session, request, result);
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImageDirectory = getActivity().getExternalFilesDir(null);
        mImageFilePattern = new File(mImageDirectory, "Image_%s.%s");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capture, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mControlGrid = view.findViewById(R.id.controlGrid);

        // iso
        mIsoButton = (ToggleButton)view.findViewById(R.id.isoButton);
        mIsoButton.setOnClickListener(this);

        mIsoSeekBar = (SeekBar)view.findViewById(R.id.isoSeekBar);
        mIsoSeekBar.setMax(8);
        mIsoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCameraSettings.Iso = seekBarValueToIso(progress);
                mIsoLabel.setText(isoToString(mCameraSettings.Iso));
                onIsoChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        });

        mIsoLabel = (TextView)view.findViewById(R.id.isoLabel);

        // focus
        mFocusButton = (ToggleButton)view.findViewById(R.id.focusButton);
        mFocusButton.setOnClickListener(this);

        mAutoFocusButton = (ToggleButton)view.findViewById(R.id.autoFocusButton);
        mAutoFocusButton.setOnCheckedChangeListener(this);

        mFocusDistanceSeekBar = (SeekBar)view.findViewById(R.id.focusDistanceSeekBar);
        mFocusDistanceSeekBar.setMax(100);
        mFocusDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCameraSettings.FocusDistance = seekBarValueToFocusDistance(progress);
                mFocusDistanceLabel.setText(focusDistanceToString(mCameraSettings.AutoFocus, mCameraSettings.FocusDistance));
                onFocusDistanceChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        });

        mFocusDistanceLabel = (TextView)view.findViewById(R.id.focusDistanceLabel);

        // exposure time
        mExposureTimeButton = (ToggleButton)view.findViewById(R.id.exposureTimeButton);
        mExposureTimeButton.setOnClickListener(this);

        mExposureTimeSeekBar = (SeekBar)view.findViewById(R.id.exposureTimeSeekBar);
        mExposureTimeSeekBar.setMax(100);
        mExposureTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCameraSettings.ExposureTime = seekBarValueToExposureTime(progress);
                mExposureTimeLabel.setText(exposureTimeToString(mCameraSettings.ExposureTime));
                onExposureTimeChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        });

        mExposureTimeLabel = (TextView)view.findViewById(R.id.exposureTimeLabel);

        mCaptureButton = (Button)view.findViewById(R.id.captureButton);
        mCaptureButton.setOnClickListener(this);

        mCaptureProgressBar = (ProgressBar)view.findViewById(R.id.captureProgressBar);

        mUploadButton = (Button)view.findViewById(R.id.uploadButton);
        mUploadButton.setOnClickListener(this);

        mIsoSeekBar.setProgress(isoToSeekBarValue(mCameraSettings.Iso));
        mIsoLabel.setText(isoToString(mCameraSettings.Iso));
        mAutoFocusButton.setChecked(mCameraSettings.AutoFocus);
        mFocusDistanceSeekBar.setEnabled(!mCameraSettings.AutoFocus);
        mFocusDistanceSeekBar.setProgress(focusDistanceToSeekBarValue(mCameraSettings.FocusDistance));
        mFocusDistanceLabel.setText(focusDistanceToString(mCameraSettings.AutoFocus, mCameraSettings.FocusDistance));
        mExposureTimeSeekBar.setProgress(exposureTimeToSeekBarValue(mCameraSettings.ExposureTime));
        mExposureTimeLabel.setText(exposureTimeToString(mCameraSettings.ExposureTime));

        showSettingsControls(DISPLAY_CONTROLS_NONE);

        mTransferUtility = AwsUtility.getTransferUtility(mTextureView.getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        initAwsTransferData();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Clear transfer listeners to prevent memory leak, or
        // else this activity won't be garbage collected.
        if (mTransferObservers != null && !mTransferObservers.isEmpty()) {
            for (TransferObserver observer : mTransferObservers) {
                observer.cleanTransferListener();
            }
        }

        MainActivity.Instance.saveApplicationSettings();
    }

    /**
     * Gets all relevant transfers from the Transfer Service for populating the
     * UI
     */
    private void initAwsTransferData() {
        // Use TransferUtility to get all upload transfers.
        mTransferObservers = mTransferUtility.getTransfersWithType(TransferType.UPLOAD);
        TransferListener listener = new UploadListener();
        for (TransferObserver observer : mTransferObservers) {
            // Sets listeners to in progress transfers
            if (TransferState.WAITING.equals(observer.getState())
                    || TransferState.WAITING_FOR_NETWORK.equals(observer.getState())
                    || TransferState.IN_PROGRESS.equals(observer.getState())) {
                observer.setTransferListener(listener);
            }
        }
    }

    @Override
    protected void onCameraOpened() {
        Activity activity = getActivity();

        try {
            // get the camera characteristics
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());

            // get the configurations
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // for still image captures, we use the largest available size.
            Size largestSize = Collections.max(Arrays.asList(map.getOutputSizes(IMAGE_FORMAT)), new CompareSizesByArea());

            mImageReader = ImageReader.newInstance(largestSize.getWidth(), largestSize.getHeight(), IMAGE_FORMAT, /*maxImages*/ 12);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

            addCaptureSessionTargetOutput(mImageReader.getSurface());
        } catch (CameraAccessException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCameraClosed() {
        if (null != mImageReader) {
            //removeCaptureSessionTargetOutput(mImageReader.getSurface());
            mImageReader.close();
            mImageReader = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.isoButton: {
                if (mIsoSeekBar.getVisibility() == View.INVISIBLE) {
                    showSettingsControls(DISPLAY_CONTROLS_ISO);
                } else {
                    ((ToggleButton)view.findViewById(R.id.isoButton)).setChecked(false);
                    showSettingsControls(DISPLAY_CONTROLS_NONE);
                }
                break;
            }
            case R.id.focusButton: {
                if (mFocusDistanceSeekBar.getVisibility() == View.INVISIBLE) {
                    showSettingsControls(DISPLAY_CONTROLS_FOCUS);
                } else {
                    showSettingsControls(DISPLAY_CONTROLS_NONE);
                }
                break;
            }
            case R.id.exposureTimeButton: {
                if (mExposureTimeSeekBar.getVisibility() == View.INVISIBLE) {
                    showSettingsControls(DISPLAY_CONTROLS_EXPOSURE_TIME);
                } else {
                    showSettingsControls(DISPLAY_CONTROLS_NONE);
                }
                break;
            }
            case R.id.captureButton: {
                showSettingsControls(DISPLAY_CONTROLS_CAPTURE);
                enableButtons(false);
                startCapture();
                break;
            }
            case R.id.uploadButton: {
                startUpload();
                break;
            }
        }
    }

    public void startCapture() {
        if (mCameraSettings.AutoFocus) {
            lockFocus();
        } else if (mCameraSettings.ExposureTime == 0L) {
            runPrecaptureSequence();
        } else {
            capture();
        }
    }

    public void startUpload() {
        /*mLastImageFile = new File(getActivity().getExternalFilesDir(null), "Test.txt");
        try {
            java.io.FileWriter fw = new java.io.FileWriter(mLastImageFile);
            fw.write("Test\n");
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }*/
        Log.d("Test", "Uploaden");
        Log.e(TAG, "Uploading '" + mLastImageFile.getAbsolutePath() + "' ...");
        TransferObserver observer = mTransferUtility.upload(BUCKET_NAME, mLastImageFile.getName(), mLastImageFile);
        observer.setTransferListener(new UploadListener());
    }

    /*
     * A TransferListener class that can listen to a upload task and be notified
     * when the status changes.
     */
    // schlussel function  Funktion  zum hochladen die Bilder , die wir  mit Smartphone Kamera  hochgeladen haben ..
    private class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "Error during upload: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d(TAG, "onStateChanged: " + id + ", " + newState);
            if (newState == TransferState.COMPLETED) {
                // wenn den Upload zu ende ist und show
                showToast(" dim prinzipe ist der Photo im ");
                showToast("Upload finished.");
            } else {
                showToast("Upload error.");
            }

        }
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_FOR_FOCUS_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mPreCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mPreCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_FOR_PRECAPTURE_SEQUENCE_COMPLETE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mPreCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mPreCaptureCallback} from both {@link #lockFocus()}.
     */
    private void capture() {
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(mImageReader.getSurface());

            captureRequestBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)100);
            captureRequestBuilder.set(CaptureRequest.JPEG_THUMBNAIL_QUALITY, (byte)100);
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(rotation));

            captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);

            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, mCameraSettings.Iso);

            if (mCameraSettings.AutoFocus) {
                //captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                captureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mBestFocusDistance);
            } else {
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                captureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mCameraSettings.FocusDistance);
            }

            if (mCameraSettings.ExposureTime == 0L) {
                //captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, mBestExposureTime);
            } else {
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, mCameraSettings.ExposureTime);
            }

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    mImagesCaptured++;

                    Log.d(TAG, "onCaptureCompleted(" + mImagesCaptured + ")");

                    // image 0 is dummy image => don't add, use or save
                    if (mImagesCaptured > 0) {
                        mCaptureProgressBar.setProgress(mImagesCaptured);

                        int requestedSensorSensitivity = request.get(CaptureRequest.SENSOR_SENSITIVITY);
                        int sensorSensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY);
                        Log.d(TAG, "Sensor Sensitivity: " + sensorSensitivity + " (Requested: " + requestedSensorSensitivity + ")");

                        float lensAperture = result.get(CaptureResult.LENS_APERTURE);
                        Log.d(TAG, "Lens Apterture: " + lensAperture);

                        float lensFocusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);
                        Log.d(TAG, "Lens Focus Distance: " + lensFocusDistance);

                        long exposureTimeNs = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                        Log.d(TAG, "Exposure Time: " + (exposureTimeNs / 1e6) + " ms");

                        int flashState = result.get(CaptureResult.FLASH_STATE);
                        Log.d(TAG, "Flash State: " + CameraKeyUtility.flashStateToString(flashState));

                        int flashMode = result.get(CaptureResult.FLASH_MODE);
                        Log.d(TAG, "Flash Mode: " + CameraKeyUtility.flashModeToString(flashMode));

                        mCaptureResults.add(result);
                    }

                    if (mImagesCaptured >= mImagesToCapture) {
                        long imageCaptureEnd = System.currentTimeMillis();
                        long imageCaptureTime = imageCaptureEnd - mImageCaptureStart;
                        //showToast("Image capture took " + imageCaptureTime + " ms.");
                        Log.d(TAG, "Image capture took " + imageCaptureTime + " ms.");

                        unlockFocus();
                    } else {
                        try {
                            mCaptureSession.capture(mCaptureRequests.get(mImagesCaptured), this, null);
                            Log.d(TAG, "Capture request " + (mImagesCaptured + 1) + " sent.");
                        } catch (CameraAccessException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            };

            // image 0 will be dummy image
            mImagesAvailable = -1;
            mImagesCaptured = -1;
            mImagesToCapture = 1;
            mCaptureRequests.clear();
            mCaptureResults.clear();
            mCapturedImages.clear();

            mCaptureRequests.clear();
            for (int i = 0; i <= mImagesToCapture; i++) {
                mCaptureRequests.add(captureRequestBuilder.build());
            }

            stopPreview();

            mCaptureProgressBar.setMax(mImagesToCapture);
            mCaptureProgressBar.setProgress(0);

            mCaptureSession.capture(mCaptureRequests.get(0), CaptureCallback, null);
            Log.d(TAG, "Capture request " + (mImagesCaptured + 1) + " sent.");

            mImageCaptureStart = System.currentTimeMillis();
        } catch (CameraAccessException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // reset the auto-focus trigger
            // commented out solved the problem of yellow image after capturing
            //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mPreCaptureCallback, mBackgroundHandler);

            // after this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            startPreview();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        finally
        {
            captureFinished();
        }
    }

    public void captureFinished()
    {
        enableButtons(true);
    }

    @Override
    protected void onPreviewCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
        onPreviewCapture(session, request, partialResult);
    }

    @Override
    protected void onPreviewCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
        onPreviewCapture(session, request, result);
    }

    protected void onPreviewCapture(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult result) {
        if (mState != STATE_PREVIEW) {
            Log.d(TAG, "State = " + StateToString(mState));
        }

        /*
        int requestedSensorSensitivity = request.get(CaptureRequest.SENSOR_SENSITIVITY);
        int sensorSensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY);
        Log.d(TAG, "Sensor Sensitivity: " + sensorSensitivity + " (Requested: " + requestedSensorSensitivity + ")");

        float lensAperture = result.get(CaptureResult.LENS_APERTURE);
        Log.d(TAG, "Lens Apterture: " + lensAperture);

        float lensFocusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);
        Log.d(TAG, "Lens Focus Distance: " + lensFocusDistance);

        long exposureTimeNs = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
        Log.d(TAG, "Exposure Time: " + (exposureTimeNs / 1e6) + " ms");

        int awbMode = result.get(CaptureResult.CONTROL_AWB_MODE);
        Log.d(TAG, "Control AWB Mode: " + CameraKeyUtility.controlAwbModeToString(awbMode));
        */

        switch (mState) {
            case STATE_PREVIEW: {
                // We have nothing to do when the camera preview is working normally.
                break;
            }
            case STATE_WAITING_FOR_FOCUS_LOCK: {
                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                Log.d(TAG, "AfState = " + CameraKeyUtility.afStateToString(afState));
                if (afState == null) {
                    mBestFocusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);
                    Log.d(TAG, "Best Lens Focus Distance: " + mBestFocusDistance);

                    mState = STATE_PICTURE_TAKEN;
                    capture();
                } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                    mBestFocusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);
                    Log.d(TAG, "Best Lens Focus Distance: " + mBestFocusDistance);

                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    Log.d(TAG, "AeState = " + CameraKeyUtility.aeStateToString(aeState));
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        mBestExposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                        Log.d(TAG, "Best Exposure Time: " + (int)Math.round(mBestExposureTime / 1e6) + " ms");

                        mState = STATE_PICTURE_TAKEN;
                        capture();
                    } else {
                        if (mCameraSettings.ExposureTime == 0L) {
                            runPrecaptureSequence();
                        } else {
                            capture();
                        }
                    }
                }
                break;
            }
            case STATE_WAITING_FOR_PRECAPTURE_SEQUENCE_COMPLETE: {
                // CONTROL_AE_STATE can be null on some devices
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                Log.d(TAG, "AeState = " + CameraKeyUtility.aeStateToString(aeState));
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    mBestExposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                    Log.d(TAG, "Best Exposure Time: " + (int)Math.round(mBestExposureTime / 1e6) + " ms");

                    mState = STATE_PICTURE_TAKEN;
                    capture();
                }
                break;
            }
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.autoFocusButton: {
                mCameraSettings.AutoFocus = isChecked;
                mFocusDistanceSeekBar.setEnabled(!mCameraSettings.AutoFocus);
                mFocusDistanceLabel.setText(focusDistanceToString(mCameraSettings.AutoFocus, mCameraSettings.FocusDistance));
                onFocusDistanceChanged();
                break;
            }
        }
    }

    private int seekBarValueToIso(int value) {
        return value * 50;
    }

    private int isoToSeekBarValue(int iso) {
        return iso / 50;
    }

    private String isoToString(int iso) {
        return iso == 0 ? "Auto" : Integer.toString(iso);
    }

    public void onIsoChanged() {
        if (mCaptureSession != null) {
            try {
                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, mCameraSettings.Iso);

                // restart preview
                stopPreview();
                startPreview();
            } catch (CameraAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    private float seekBarValueToFocusDistance(int value) {
        return value / 10.0f;
    }

    private int focusDistanceToSeekBarValue(float focusDistance) {
        return (int)(focusDistance * 10);
    }

    private String focusDistanceToString(boolean autoFocus, float focusDistance) {
        if (autoFocus) {
            return "Auto";
        } else {
            return focusDistance == 0.0f ? "Infinity" : String.format("%.1f", focusDistance);
        }
    }

    public void onFocusDistanceChanged() {
        if (mCaptureSession != null) {
            try {
                if (mCameraSettings.AutoFocus) {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                } else {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                    mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mCameraSettings.FocusDistance);
                }

                // restart preview
                stopPreview();
                startPreview();
            } catch (CameraAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    private long seekBarValueToExposureTime(int value) {
        return value * 1000000;
    }

    private int exposureTimeToSeekBarValue(long exposureTime) {
        return (int)Math.round(exposureTime / 1e6);
    }

    private String exposureTimeToString(long exposureTime) {
        return exposureTime == 0L ? "Auto" : String.format("%d ms", (int)Math.round(exposureTime / 1e6));
    }

    public void onExposureTimeChanged() {
        if (mCaptureSession != null) {
            try {
                if (mCameraSettings.ExposureTime == 0L) {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                } else {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                    mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, mCameraSettings.ExposureTime);
                }

                // restart preview
                stopPreview();
                startPreview();
            } catch (CameraAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showSettingsControls(int settingsControls) {
        boolean controlGridVisible = settingsControls == DISPLAY_CONTROLS_ISO || settingsControls == DISPLAY_CONTROLS_FOCUS || settingsControls == DISPLAY_CONTROLS_EXPOSURE_TIME;
        mControlGrid.setVisibility(controlGridVisible ? View.VISIBLE : View.INVISIBLE);

        mIsoButton.setChecked(settingsControls == DISPLAY_CONTROLS_ISO);
        mIsoSeekBar.setVisibility(settingsControls == DISPLAY_CONTROLS_ISO ? View.VISIBLE : View.INVISIBLE);

        mFocusButton.setChecked(settingsControls == DISPLAY_CONTROLS_FOCUS);
        mAutoFocusButton.setVisibility(settingsControls == DISPLAY_CONTROLS_FOCUS ? View.VISIBLE : View.INVISIBLE);
        mFocusDistanceSeekBar.setVisibility(settingsControls == DISPLAY_CONTROLS_FOCUS ? View.VISIBLE : View.INVISIBLE);

        mExposureTimeButton.setChecked(settingsControls == DISPLAY_CONTROLS_EXPOSURE_TIME);
        mExposureTimeSeekBar.setVisibility(settingsControls == DISPLAY_CONTROLS_EXPOSURE_TIME ? View.VISIBLE : View.INVISIBLE);

        mCaptureProgressBar.setVisibility(settingsControls == DISPLAY_CONTROLS_CAPTURE ? View.INVISIBLE : View.INVISIBLE);
    }

    public void enableButtons(final boolean enable) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    mIsoButton.setEnabled(enable);
                    mFocusButton.setEnabled(enable);
                    mExposureTimeButton.setEnabled(enable);
                    mCaptureButton.setEnabled(enable);
                    mUploadButton.setEnabled(enable);
                }
            });
        }
    }

    protected String StateToString(int state) {
        switch (state) {
            case STATE_PREVIEW:
                return "STATE_PREVIEW";
            case STATE_WAITING_FOR_FOCUS_LOCK:
                return "STATE_WAITING_FOR_FOCUS_LOCK";
            case STATE_WAITING_FOR_PRECAPTURE_SEQUENCE_COMPLETE:
                return "STATE_WAITING_FOR_PRECAPTURE_SEQUENCE_COMPLETE";
            case STATE_PICTURE_TAKEN:
                return "STATE_PICTURE_TAKEN";
            default:
                return null;
        }
    }
}
