package org.tensorflow.lite.examples.objectdetection;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\n\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0002J\u0012\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0014J\b\u0010\u0011\u001a\u00020\u0012H\u0016R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lorg/tensorflow/lite/examples/objectdetection/NearbyService;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "mLocationManager", "Landroid/location/LocationManager;", "getMLocationManager", "()Landroid/location/LocationManager;", "setMLocationManager", "(Landroid/location/LocationManager;)V", "progressBar", "Landroid/widget/ProgressBar;", "getLastKnownLocation", "Landroid/location/Location;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onSupportNavigateUp", "", "app_debug"})
public final class NearbyService extends androidx.appcompat.app.AppCompatActivity {
    private android.widget.ProgressBar progressBar;
    @org.jetbrains.annotations.Nullable()
    private android.location.LocationManager mLocationManager;
    
    public NearbyService() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    public boolean onSupportNavigateUp() {
        return false;
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
}