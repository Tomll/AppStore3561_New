package com.wedrive.welink.appstore.app.widget;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class MSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback, Camera.PreviewCallback
{
	private Camera mCamera;
//	private boolean getOnePic = false;
	private Parameters parameters;
//	private MediaRecorder mMediaRecorder;
	private Size bestSize;
//	private String videoName;
	private SurfaceHolder mSurfaceHolder;

	public MSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initSurface(context);
	}

	public MSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSurface(context);
	}

	public MSurfaceView(Context context) {
		super(context);
		initSurface(context);
	}

	@SuppressWarnings("deprecation")
	private void initSurface(Context context) {
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceHolder.setKeepScreenOn(true);
	}

	@Override
	public void onPreviewFrame(final byte[] data, final Camera camera) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2,
			int arg3) {
		if (mCamera != null) {
			parameters = mCamera.getParameters();
			List<Size> sizes = parameters.getSupportedPreviewSizes();
			if (sizes != null) {
				bestSize = sizes.get(0);
				for (int i = 1; i < sizes.size(); i++) {
					if (bestSize.width * bestSize.height < sizes.get(i).width
							* sizes.get(i).height) {
						bestSize = sizes.get(i);
					}
				}
			}

			parameters.setPreviewSize(bestSize.width, bestSize.height);
			mCamera.setPreviewCallback(this);
			mCamera.setParameters(parameters);

			try {
				if (sizes != null) {
					mCamera.setPreviewDisplay(holder);
					mCamera.startPreview();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getContext(), "检测不到摄像头，请您检查摄像头是否正常连接",
						Toast.LENGTH_SHORT).show();
				if (mCamera != null) {
					mCamera.release();
					mCamera = null;
				}
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		try {
			if (mCamera == null) {
				mCamera = Camera.open();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		try {
			if (mCamera != null) {
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
