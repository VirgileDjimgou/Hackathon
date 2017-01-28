package com.zeiss.cloudcam;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;

/**
 * Created by zoafro on 12.12.2016.
 */
public class CameraKeyUtility {
    public static String imageFormatToString(int imageFormat) {
        switch(imageFormat) {
            case ImageFormat.DEPTH16:
                return "DEPTH16";
            case ImageFormat.DEPTH_POINT_CLOUD:
                return "DEPTH_POINT_CLOUD";
            case ImageFormat.FLEX_RGBA_8888:
                return "FLEX_RGBA_8888";
            case ImageFormat.FLEX_RGB_888:
                return "FLEX_RGB_888";
            case ImageFormat.JPEG:
                return "JPEG";
            case ImageFormat.NV16:
                return "NV16";
            case ImageFormat.NV21:
                return "NV21";
            case ImageFormat.PRIVATE:
                return "PRIVATE";
            case ImageFormat.RAW10:
                return "RAW10";
            case ImageFormat.RAW12:
                return "RAW12";
            case ImageFormat.RAW_SENSOR:
                return "RAW_SENSOR";
            case ImageFormat.RGB_565:
                return "RGB_565";
            case ImageFormat.UNKNOWN:
                return "UNKNOWN";
            case ImageFormat.YUV_420_888:
                return "YUV_420_888";
            case ImageFormat.YUV_422_888:
                return "YUV_422_888";
            case ImageFormat.YUV_444_888:
                return "YUV_444_888";
            case ImageFormat.YUY2:
                return "YUY2";
            case ImageFormat.YV12:
                return "YV12";
            case PixelFormat.A_8:
                return "A_8";
            case PixelFormat.LA_88:
                return "LA_88";
            case PixelFormat.L_8:
                return "L_8";
            case PixelFormat.OPAQUE:
                return "OPAQUE";
            case PixelFormat.RGBA_4444:
                return "RGBA_4444";
            case PixelFormat.RGBA_5551:
                return "RGBA_5551";
            case PixelFormat.RGBA_8888:
                return "RGBA_8888";
            case PixelFormat.RGBX_8888:
                return "RGBX_8888";
            case PixelFormat.RGB_332:
                return "RGB_332";
            case PixelFormat.RGB_888:
                return "RGB_888";
            case PixelFormat.TRANSLUCENT:
                return "TRANSLUCENT";
            case PixelFormat.TRANSPARENT:
                return "TRANSPARENT";
            default:
                return null;
        }
    }

    public static String flashModeToString(int flashMode) {
        switch (flashMode) {
            case CameraCharacteristics.FLASH_MODE_OFF:
                return "FLASH_MODE_OFF";
            case CameraCharacteristics.FLASH_MODE_SINGLE:
                return "FLASH_MODE_SINGLE";
            case CameraCharacteristics.FLASH_MODE_TORCH:
                return "FLASH_MODE_TORCH";
            default:
                return null;
        }
    }

    public static String flashStateToString(int flashState) {
        switch (flashState) {
            case CameraCharacteristics.FLASH_STATE_UNAVAILABLE:
                return "FLASH_STATE_UNAVAILABLE";
            case CameraCharacteristics.FLASH_STATE_CHARGING:
                return "FLASH_STATE_CHARGING";
            case CameraCharacteristics.FLASH_STATE_READY:
                return "FLASH_STATE_READY";
            case CameraCharacteristics.FLASH_STATE_FIRED:
                return "FLASH_STATE_FIRED";
            case CameraCharacteristics.FLASH_STATE_PARTIAL:
                return "FLASH_STATE_PARTIAL";
            default:
                return null;
        }
    }

    public static String afStateToString(int afState) {
        switch (afState) {
            case CameraCharacteristics.CONTROL_AF_STATE_INACTIVE:
                return "CONTROL_AF_STATE_INACTIVE";
            case CameraCharacteristics.CONTROL_AF_STATE_PASSIVE_SCAN:
                return "CONTROL_AF_STATE_PASSIVE_SCAN";
            case CameraCharacteristics.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                return "CONTROL_AF_STATE_PASSIVE_FOCUSED";
            case CameraCharacteristics.CONTROL_AF_STATE_ACTIVE_SCAN:
                return "CONTROL_AF_STATE_ACTIVE_SCAN";
            case CameraCharacteristics.CONTROL_AF_STATE_FOCUSED_LOCKED:
                return "CONTROL_AF_STATE_FOCUSED_LOCKED";
            case CameraCharacteristics.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                return "CONTROL_AF_STATE_NOT_FOCUSED_LOCKED";
            case CameraCharacteristics.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                return "CONTROL_AF_STATE_PASSIVE_UNFOCUSED";
            default:
                return null;
        }
    }

    public static String aeStateToString(int aeState) {
        switch (aeState) {
            case CameraCharacteristics.CONTROL_AE_STATE_INACTIVE:
                return "CONTROL_AE_STATE_INACTIVE";
            case CameraCharacteristics.CONTROL_AE_STATE_SEARCHING:
                return "CONTROL_AE_STATE_SEARCHING";
            case CameraCharacteristics.CONTROL_AE_STATE_CONVERGED:
                return "CONTROL_AE_STATE_CONVERGED";
            case CameraCharacteristics.CONTROL_AE_STATE_LOCKED:
                return "CONTROL_AE_STATE_LOCKED";
            case CameraCharacteristics.CONTROL_AE_STATE_FLASH_REQUIRED:
                return "CONTROL_AE_STATE_FLASH_REQUIRED";
            case CameraCharacteristics.CONTROL_AE_STATE_PRECAPTURE:
                return "CONTROL_AE_STATE_PRECAPTURE";
            default:
                return null;
        }
    }

    public static String controlAwbModeToString(int controlAwbMode)
    {
        switch(controlAwbMode) {

            case CameraMetadata.CONTROL_AWB_MODE_OFF:
                return "Off";
            case CameraMetadata.CONTROL_AWB_MODE_AUTO:
                return "Auto";
            case CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT:
                return "Incandescent";
            case CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT:
                return "Fluorescent";
            case CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT:
                return "Warm Fluorescent";
            case CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT:
                return "Daylight";
            case CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT:
                return "Cloudy Daylight";
            case CameraMetadata.CONTROL_AWB_MODE_TWILIGHT:
                return "Twilight";
            case CameraMetadata.CONTROL_AWB_MODE_SHADE:
                return "Shade";
            default:
                return "Unknown";
        }
    }
}
