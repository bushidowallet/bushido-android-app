/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bitcoin.blockchain.api.android.barcode;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.bitcoin.blockchain.api.android.app.FragmentActionListener;
import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.android.app.PayPageState;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.AmbientLightManager;
import com.google.zxing.client.android.DecodeFormatManager;
import com.google.zxing.client.android.FinishListener;
import com.google.zxing.client.android.InactivityTimer;
import com.google.zxing.client.android.ViewfinderView;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.result.ResultHandlerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 * @author Abhinava Srivastava
 */
public final class BarcodeFragment extends Fragment implements
		SurfaceHolder.Callback {

	private static final String TAG = BarcodeFragment.class.getSimpleName();

	private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

	private CameraManager cameraManager;
	private CaptureFragmentHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private AmbientLightManager ambientLightManager;
    private FragmentActionListener listener;

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

    public static BarcodeFragment newInstance(FragmentActionListener listener) {
        BarcodeFragment fragment = new BarcodeFragment();
        fragment.listener = listener;
        return fragment;
    }

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		hasSurface = false;
	}

	private IScanResultHandler resultHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section_scan, container, false);
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.camera_preview);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        surfaceView = new SurfaceView(getActivity());
		surfaceView.setLayoutParams(layoutParams);
		viewfinderView = new ViewfinderView(getActivity());
		viewfinderView.setLayoutParams(layoutParams);
		frameLayout.addView(surfaceView);
		frameLayout.addView(viewfinderView);
		inactivityTimer = new InactivityTimer(this.getActivity());
		ambientLightManager = new AmbientLightManager(this.getActivity());
        Button closeButton = (Button) view.findViewById(R.id.button_close);
        closeButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onButtonClick(PayPageState.PAY_FORM, null);
                        } else {
                            Log.e("", "WTF Listener null");
                        }
                    }
                }
        );
		return view;
	}

	public void setDecodeFor(Collection<BarcodeFormat> decodeFormat) {
		this.decodeFormats = decodeFormat;
	}

	public void setDecodeFor(IScanResultHandler.MODE decodeMode){
		switch (decodeMode) {
		case PRODUCT_MODE:
			this.decodeFormats =  DecodeFormatManager.PRODUCT_FORMATS;
			break;
		case QR_CODE_MODE:
			 this.decodeFormats =  DecodeFormatManager.QR_CODE_FORMATS;
			break;
		case DATA_MATRIX_MODE:
			this.decodeFormats =  DecodeFormatManager.DATA_MATRIX_FORMATS;
			break;
		case ONE_D_MODE:
			 this.decodeFormats =  DecodeFormatManager.ONE_D_FORMATS;
			 break;
		}
	}

	SurfaceView surfaceView;

	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		super.onResume();

		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.
		cameraManager = new CameraManager(this.getActivity(), getView());
		viewfinderView.setCameraManager(cameraManager);

		handler = null;

		resetStatusView();

		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		ambientLightManager.start(cameraManager);
		inactivityTimer.onResume();
		characterSet = null;
	}

	@Override
	public void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = this.surfaceView;
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	public void restart() {
		restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler,
                        IDS.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
                    "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();
		ScanResult resultHandler = ResultHandlerFactory.parseResult(rawResult);

		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			drawResultPoints(barcode, scaleFactor, rawResult);
		}

		handleDecodeInternally(rawResult, resultHandler, barcode);

	}

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of
	 * the barcode.
	 * 
	 * @param barcode
	 *            A bitmap of the captured image.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param rawResult
	 *            The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, float scaleFactor,
			Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(Color.parseColor("#c099cc00"));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
			} else if (points.length == 4
					&& (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult
							.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
				drawLine(canvas, paint, points[2], points[3], scaleFactor);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(scaleFactor * point.getX(), scaleFactor
							* point.getY(), paint);
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b, float scaleFactor) {
		if (a != null && b != null) {
			canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(),
					scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
		}
	}

	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult,
			ScanResult resultHandler, Bitmap barcode) {
		viewfinderView.setVisibility(View.GONE);
        listener.onButtonClick(PayPageState.PAY_FORM, rawResult.getText());
		if (this.resultHandler != null) {
            Log.i("", "Found qr: " + rawResult.getText());
			this.resultHandler.scanResult(resultHandler);
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG,
                    "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureFragmentHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this.getActivity());
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage("Sorry, the Android camera encountered a problem. You may need to restart the device.");
		builder.setPositiveButton("OK", new FinishListener(this.getActivity()));
		builder.setOnCancelListener(new FinishListener(this.getActivity()));
		builder.show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(IDS.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	public IScanResultHandler getScanResultHandler() {
		return resultHandler;
	}

	public void setScanResultHandler(IScanResultHandler resultHandler) {
		this.resultHandler = resultHandler;
	}
}
