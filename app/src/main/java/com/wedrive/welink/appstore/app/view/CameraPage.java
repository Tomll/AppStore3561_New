/**
 * Created by wangzhichao on 2015年12月3日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */
package com.wedrive.welink.appstore.app.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.model.FilterObj;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.util.ImageUtil;
import com.wedrive.welink.appstore.app.widget.SquareCameraPreview;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("unused") 
@SuppressLint("NewApi")
public class CameraPage extends BasePage implements SurfaceHolder.Callback, Camera.PictureCallback{

	private final static String TAG = "CameraPage";
	private Context mContext;
	private ActivityInterface mAif;
	private View mView;

	public static final String CAMERA_ID_KEY = "camera_id";
	public static final String CAMERA_FLASH_KEY = "flash_mode";
	public static final String PREVIEW_HEIGHT_KEY = "preview_height";

	private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
	private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

	private int mCameraID;
	private String mFlashMode;
	private Camera mCamera;
	private SquareCameraPreview mPreviewView;
	private SurfaceHolder mSurfaceHolder;

	private int mDisplayOrientation;
	private int mLayoutOrientation;

	private int mCoverHeight;
	private int mPreviewHeight;
	
	private CameraOrientationListener mOrientationListener;


	public CameraPage(Context context, View view, ActivityInterface aif) {
		super(context, view, aif);
		mContext = context;
		mView = view;
		mAif = aif;
		initView(view);
	}
	
	@Override
	public void viewWillAppear(int arg0) {
		super.viewWillAppear(arg0);
		 MainActivity.mMainActivity.setFirstAndSecondTitle("拍照", "管理");
		 MainActivity.mMainActivity.setTitleDividerVisibile(true);
	}

	private void initView(View view){		
		 mOrientationListener=new CameraOrientationListener(mContext);
		 mCameraID = getBackCameraID();
         mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;      
         mOrientationListener.enable();
         mPreviewView = (SquareCameraPreview) view.findViewById(R.id.camera_preview_view);
         mPreviewView.getHolder().addCallback(CameraPage.this);
         final View topCoverView = view.findViewById(R.id.cover_top_view);
         final View btnCoverView = view.findViewById(R.id.cover_bottom_view);

         if (mCoverHeight == 0) {
             ViewTreeObserver observer = mPreviewView.getViewTreeObserver();
             observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {              
				@Override
                 public void onGlobalLayout() {
                     int width = mPreviewView.getWidth();
                     mPreviewHeight = mPreviewView.getHeight();
                     mCoverHeight = (mPreviewHeight - width) / 2;
                     topCoverView.getLayoutParams().height = mCoverHeight;
                     btnCoverView.getLayoutParams().height = mCoverHeight;

                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                         mPreviewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                     } else {
                         mPreviewView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                     }
                 }
             });
         } else {
             topCoverView.getLayoutParams().height = mCoverHeight;
             btnCoverView.getLayoutParams().height = mCoverHeight;
         }

         view.findViewById(R.id.change_camera).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
                     mCameraID = getBackCameraID();
                 } else {
                     mCameraID = getFrontCameraID();
                 }
                 restartPreview();
             }
         });

         final TextView autoFlashIcon = (TextView) view.findViewById(R.id.auto_flash_icon);
         view.findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
                     mFlashMode = Camera.Parameters.FLASH_MODE_ON;
                     autoFlashIcon.setText("On");
                 } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
                     mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
                     autoFlashIcon.setText("Off");
                 } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
                     mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
                     autoFlashIcon.setText("Auto");
                 }

                 setupCamera();
             }
         });

         view.findViewById(R.id.capture_image_button).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 takePicture();
             }
         });

	}

	@Override
	public void onResume() {
		super.onResume();
		startPreview();
	}

	private void getCamera(int cameraID) {
        Log.d(TAG, "get camera with id " + cameraID);
        try {
            mCamera = Camera.open(cameraID);
            mPreviewView.setCamera(mCamera);
        } catch (Exception e) {
            Log.d(TAG, "Can't open camera with id " + cameraID);
            e.printStackTrace();
        }
    }
	
	private void startPreview() {
		getCamera(mCameraID);
		startCameraPreview();
	}

	private void restartPreview() {
		stopCameraPreview();
		mCamera.release();
		getCamera(mCameraID);
		startCameraPreview();
	}
	
	 /**
     * Start the camera preview
     */
    private void startCameraPreview() {
        determineDisplayOrientation();
        setupCamera();
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Can't start camera preview due to IOException " + e);
            e.printStackTrace();
        }
    }

    /**
     * Stop the camera preview
     */
    private void stopCameraPreview() {
        mCamera.stopPreview();
        mPreviewView.setCamera(null);
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        int rotation = mAif.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;

        // Camera direction
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mDisplayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        mLayoutOrientation = degrees;
        Log.e("message","mCamera:"+mCamera);
        mCamera.setDisplayOrientation(displayOrientation);
    }

    /**
     * Setup the camera parameters
     */
    @SuppressLint("InlinedApi")
	private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);


        // Set continuous picture focus, if it's supported
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        final View changeCameraFlashModeBtn = mView.findViewById(R.id.flash);
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(mFlashMode)) {
            parameters.setFlashMode(mFlashMode);
            changeCameraFlashModeBtn.setVisibility(View.VISIBLE);
        } else {
            changeCameraFlashModeBtn.setVisibility(View.INVISIBLE);
        }

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
    }

    private Size determineBestSize(List<Size> sizes, int widthThreshold) {
        Size bestSize = null;
        Size size;
        int numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);
            boolean isDesireRatio = (size.width / 4) == (size.height / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            Log.d(TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    private int getFrontCameraID() {
        PackageManager pm = mContext.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    private int getBackCameraID() {
        return CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * Take a picture
     */
    private void takePicture() {
        mOrientationListener.rememberOrientation();
        Camera.ShutterCallback shutterCallback = null;
        Camera.PictureCallback raw = null;
        Camera.PictureCallback postView = null;
        mCamera.takePicture(shutterCallback, raw, postView, this);
    }

    public void onStop() {
        mOrientationListener.disable();
        stopCameraPreview();
        mCamera.release();
    }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		  mSurfaceHolder = holder;
		  getCamera(mCameraID);
	      startCameraPreview();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		int rotation = (mDisplayOrientation + mOrientationListener.getRememberedNormalOrientation() + mLayoutOrientation) % 360;
		Drawable drawable = ImageUtil.rotateDrawable(mContext, rotation, data);
		onStop();
		FilterObj filter = new FilterObj();
		filter.setTag(drawable);
		mAif.showPage(Configs.VIEW_POSITION_MANNGER_CAMERA, Configs.VIEW_POSITION_MANNGER_CROP, filter,true,null,null);
		
	}
	
	
	/**
	 * 
	 * <p>功能描述</p>编辑选择照片
	 * @param uri
	 * @author wangzhichao
	 * @date 2015年11月10日
	 */
	
	private void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", true);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG);
		mAif.startActivityForResult(intent, 1);
	}
	
	private static class CameraOrientationListener extends OrientationEventListener {

		private int mCurrentNormalizedOrientation;
		private int mRememberedNormalOrientation;

		public CameraOrientationListener(Context context) {
			super(context, SensorManager.SENSOR_DELAY_NORMAL);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			if (orientation != ORIENTATION_UNKNOWN) {
				mCurrentNormalizedOrientation = normalize(orientation);
			}
		}

		private int normalize(int degrees) {
			if (degrees > 315 || degrees <= 45) {
				return 0;
			}

			if (degrees > 45 && degrees <= 135) {
				return 90;
			}

			if (degrees > 135 && degrees <= 225) {
				return 180;
			}

			if (degrees > 225 && degrees <= 315) {
				return 270;
			}

			throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
		}

		public void rememberOrientation() {
			mRememberedNormalOrientation = mCurrentNormalizedOrientation;
		}

		public int getRememberedNormalOrientation() {
			return mRememberedNormalOrientation;
		}
	}
	
	@Override
	public void goBack() {
		onStop();
		mAif.showPrevious(null);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            goBack();
        }
		return true;
	}

}
