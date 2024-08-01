package org.tensorflow.lite.examples.objectdetection.fragments;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u00ca\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\r\u0018\u0000 ^2\u00020\u00012\u00020\u0002:\u0002]^B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010*\u001a\u00020+H\u0003J\b\u0010,\u001a\u00020%H\u0002J\u0010\u0010-\u001a\u00020+2\u0006\u0010.\u001a\u00020/H\u0002J\b\u00100\u001a\u00020\u0005H\u0002J\n\u00101\u001a\u0004\u0018\u000102H\u0002J\u0012\u00103\u001a\u0004\u0018\u0001042\u0006\u00105\u001a\u00020\u0005H\u0002J\b\u00106\u001a\u00020+H\u0002J\u0010\u00107\u001a\u00020+2\u0006\u00108\u001a\u000209H\u0016J$\u0010:\u001a\u00020;2\u0006\u0010<\u001a\u00020=2\b\u0010>\u001a\u0004\u0018\u00010?2\b\u0010@\u001a\u0004\u0018\u00010AH\u0016J\b\u0010B\u001a\u00020+H\u0016J\u0010\u0010C\u001a\u00020+2\u0006\u0010D\u001a\u00020\u0005H\u0016J-\u0010E\u001a\u00020+2\u0006\u0010F\u001a\u00020G2\u000e\u0010H\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00050I2\u0006\u0010J\u001a\u00020KH\u0016\u00a2\u0006\u0002\u0010LJ8\u0010M\u001a\u00020+2\u0006\u0010.\u001a\u00020\n2\u000e\u0010N\u001a\n\u0012\u0004\u0012\u00020P\u0018\u00010O2\u0006\u0010Q\u001a\u00020R2\u0006\u0010S\u001a\u00020G2\u0006\u0010T\u001a\u00020GH\u0016J\b\u0010U\u001a\u00020+H\u0016J\u001a\u0010V\u001a\u00020+2\u0006\u0010W\u001a\u00020;2\b\u0010@\u001a\u0004\u0018\u00010AH\u0017J\b\u0010X\u001a\u00020+H\u0002J\b\u0010Y\u001a\u00020+H\u0002J \u0010Z\u001a\u00020+2\u0006\u0010[\u001a\u00020\n2\u000e\u0010N\u001a\n\u0012\u0004\u0012\u00020P\u0018\u00010OH\u0002J\b\u0010\\\u001a\u00020+H\u0002R\u0010\u0010\u0004\u001a\u00020\u0005X\u0082D\u00a2\u0006\u0004\n\u0002\b\u0006R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\u00020\b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u0019\"\u0004\b\u001a\u0010\u001bR\u000e\u0010\u001c\u001a\u00020\u001dX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010 \u001a\u0010\u0012\f\u0012\n #*\u0004\u0018\u00010\"0\"0!X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010$\u001a\u00020%X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b&\u0010\'\"\u0004\b(\u0010)\u00a8\u0006_"}, d2 = {"Lorg/tensorflow/lite/examples/objectdetection/fragments/CameraFragment;", "Landroidx/fragment/app/Fragment;", "Lorg/tensorflow/lite/examples/objectdetection/ObjectDetectorHelper$DetectorListener;", "()V", "TAG", "", "TAG$1", "_fragmentCameraBinding", "Lorg/tensorflow/lite/examples/objectdetection/databinding/FragmentCameraBinding;", "bitmapBuffer", "Landroid/graphics/Bitmap;", "camera", "Landroidx/camera/core/Camera;", "cameraExecutor", "Ljava/util/concurrent/ExecutorService;", "cameraProvider", "Landroidx/camera/lifecycle/ProcessCameraProvider;", "fragmentCameraBinding", "getFragmentCameraBinding", "()Lorg/tensorflow/lite/examples/objectdetection/databinding/FragmentCameraBinding;", "imageAnalyzer", "Landroidx/camera/core/ImageAnalysis;", "mLocationManager", "Landroid/location/LocationManager;", "getMLocationManager", "()Landroid/location/LocationManager;", "setMLocationManager", "(Landroid/location/LocationManager;)V", "objectDetectorHelper", "Lorg/tensorflow/lite/examples/objectdetection/ObjectDetectorHelper;", "preview", "Landroidx/camera/core/Preview;", "storageActivityResultLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "kotlin.jvm.PlatformType", "switchlocn", "", "getSwitchlocn", "()Z", "setSwitchlocn", "(Z)V", "bindCameraUseCases", "", "checkPermission", "detectObjects", "image", "Landroidx/camera/core/ImageProxy;", "getDeviceLocation", "getLastKnownLocation", "Landroid/location/Location;", "getOutputMediaFile", "Ljava/io/File;", "name", "initBottomSheetControls", "onConfigurationChanged", "newConfig", "Landroid/content/res/Configuration;", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onError", "error", "onRequestPermissionsResult", "requestCode", "", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "onResults", "results", "", "Lorg/tensorflow/lite/task/vision/detector/Detection;", "inferenceTime", "", "imageHeight", "imageWidth", "onResume", "onViewCreated", "view", "requestPermission", "setUpCamera", "showAndSaveImagePopup", "bitmap", "updateControlsUi", "API", "Companion", "app_debug"})
public final class CameraFragment extends androidx.fragment.app.Fragment implements org.tensorflow.lite.examples.objectdetection.ObjectDetectorHelper.DetectorListener {
    private boolean switchlocn = false;
    private final java.lang.String TAG$1 = "ObjectDetection";
    private org.tensorflow.lite.examples.objectdetection.databinding.FragmentCameraBinding _fragmentCameraBinding;
    private org.tensorflow.lite.examples.objectdetection.ObjectDetectorHelper objectDetectorHelper;
    private android.graphics.Bitmap bitmapBuffer;
    private androidx.camera.core.Preview preview;
    private androidx.camera.core.ImageAnalysis imageAnalyzer;
    private androidx.camera.core.Camera camera;
    private androidx.camera.lifecycle.ProcessCameraProvider cameraProvider;
    
    /**
     * Blocking camera operations are performed using this executor
     */
    private java.util.concurrent.ExecutorService cameraExecutor;
    @org.jetbrains.annotations.NotNull()
    private static final org.tensorflow.lite.examples.objectdetection.fragments.CameraFragment.Companion Companion = null;
    @java.lang.Deprecated()
    private static final int STORAGE_PERMISSION_CODE = 100;
    @java.lang.Deprecated()
    private static final java.lang.String TAG = "PERMISSION_TAG";
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> storageActivityResultLauncher = null;
    @org.jetbrains.annotations.Nullable()
    private android.location.LocationManager mLocationManager;
    
    public CameraFragment() {
        super();
    }
    
    public final boolean getSwitchlocn() {
        return false;
    }
    
    public final void setSwitchlocn(boolean p0) {
    }
    
    private final org.tensorflow.lite.examples.objectdetection.databinding.FragmentCameraBinding getFragmentCameraBinding() {
        return null;
    }
    
    @java.lang.Override()
    public void onResume() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initBottomSheetControls() {
    }
    
    private final void updateControlsUi() {
    }
    
    private final void setUpCamera() {
    }
    
    @android.annotation.SuppressLint(value = {"UnsafeOptInUsageError"})
    private final void bindCameraUseCases() {
    }
    
    private final void detectObjects(androidx.camera.core.ImageProxy image) {
    }
    
    @java.lang.Override()
    public void onConfigurationChanged(@org.jetbrains.annotations.NotNull()
    android.content.res.Configuration newConfig) {
    }
    
    @java.lang.Override()
    public void onResults(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap image, @org.jetbrains.annotations.Nullable()
    java.util.List<org.tensorflow.lite.task.vision.detector.Detection> results, long inferenceTime, int imageHeight, int imageWidth) {
    }
    
    private final void requestPermission() {
    }
    
    private final boolean checkPermission() {
        return false;
    }
    
    @java.lang.Override()
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults) {
    }
    
    private final void showAndSaveImagePopup(android.graphics.Bitmap bitmap, java.util.List<org.tensorflow.lite.task.vision.detector.Detection> results) {
    }
    
    private final java.lang.String getDeviceLocation() {
        return null;
    }
    
    private final java.io.File getOutputMediaFile(java.lang.String name) {
        return null;
    }
    
    @java.lang.Override()
    public void onError(@org.jetbrains.annotations.NotNull()
    java.lang.String error) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.location.LocationManager getMLocationManager() {
        return null;
    }
    
    public final void setMLocationManager(@org.jetbrains.annotations.Nullable()
    android.location.LocationManager p0) {
    }
    
    private final android.location.Location getLastKnownLocation() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\"\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\bH\'\u00a8\u0006\t"}, d2 = {"Lorg/tensorflow/lite/examples/objectdetection/fragments/CameraFragment$API;", "", "sendData", "Lretrofit2/Call;", "Lokhttp3/ResponseBody;", "String", "Lokhttp3/RequestBody;", "image", "Lokhttp3/MultipartBody$Part;", "app_debug"})
    public static abstract interface API {
        
        @org.jetbrains.annotations.NotNull()
        @retrofit2.http.POST(value = "predict")
        @retrofit2.http.Multipart()
        public abstract retrofit2.Call<okhttp3.ResponseBody> sendData(@org.jetbrains.annotations.NotNull()
        @retrofit2.http.Part(value = "ltlg")
        okhttp3.RequestBody String, @org.jetbrains.annotations.NotNull()
        @retrofit2.http.Part()
        okhttp3.MultipartBody.Part image);
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lorg/tensorflow/lite/examples/objectdetection/fragments/CameraFragment$Companion;", "", "()V", "STORAGE_PERMISSION_CODE", "", "TAG", "", "app_debug"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
}