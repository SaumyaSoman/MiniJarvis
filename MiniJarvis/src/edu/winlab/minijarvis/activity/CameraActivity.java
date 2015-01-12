/*
 * Copyright (C) 2014 The Android Open Source Project
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

package edu.winlab.minijarvis.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Paint.Align;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import edu.winlab.minijarvis.R;

/**
 * This is the main activity. It opens camera and takes images every 10 seconds. Each image is sent to server.
 * The server does object/face recognition and sends back annotation to glassware. If faces are recognized, the
 * annotation is displayed on the glass for approx 5s.
 * @author Saumya
 *
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback{
	
	private static final String TAG = "CameraActivity";
	private SurfaceView mPreview;
	private Camera mCamera;
	private GestureDetector mGestureDetector = null;
	private String ip="192.168.1.1"; // ip of my server
	static JSONObject jsonResponse; 
	boolean stop=false;
	public final Handler handler = new Handler();
	SurfaceHolder holder;
	private String fileName;
	TextView mCounter;
	ImageView imgView;
	private Paint rectPaint;
	private Paint textPaint;
	private Paint background;
	Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //to prevent screen from dimming
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.activity_main);

		mPreview = (SurfaceView) findViewById(R.id.preview);
		mCounter = (TextView) findViewById(R.id.counter);
		imgView=(ImageView) findViewById(R.id.image);

		holder = mPreview.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this);
		
		//Set Paint objects
		setRectPaint();
		setTextPaint();
		setBackground();
		//to detect gestures
		mGestureDetector = createGestureDetector(this);
		
		//To safely open camera
		
		// Start the main thread. Image is captured when preview starts.
		Sync sync = new Sync(call,500);

	}	
	
	@Override
	protected void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.release();
			mCamera = null; 
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCamera != null) {
			mCamera.release();
			mCamera = null; 
		}
	}
	
	public class Sync {
		Runnable task;
		public Sync(Runnable task, long time) {
			this.task = task;
			for(int i=0; i < 3; i++)
			{
				mCamera = getCameraInstance();
				if(mCamera != null) break;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(mCamera == null)
			{
				debug("Camera cannot be locked");
			}
			handler.removeCallbacks(task);
			handler.postDelayed(task, time);
		}
	}
	
	/**
	 * Thread to capture image every 10s
	 */
	final private Runnable call = new Runnable() {
		@Override
		public void run() {
			
			//stop variable is false when user requests further info. No more iages are taken.
			if (stop==false) {	
				
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// Timer to take images every 10s
				new CountDownTimer(10000, 1000) {
					
					//Countdown is displayed on the view
					public void onTick(long millisUntilFinished) {

						mCamera.startPreview();
						mCounter.setVisibility(View.VISIBLE);
						mCounter.setText(Long.toString(millisUntilFinished / 1000));
						jsonResponse=null;
						imgView.setVisibility(View.GONE);
					}
					
					//Image is captured after countdown and sent to server for recognition. The timer restarts after 8s
					public void onFinish() {
						mCounter.setVisibility(View.GONE);
						mCamera.takePicture(null, null, mPictureCallback);
						//Current timer thread is cancelled.
						this.cancel();		
						handler.postDelayed(call,7*1000);
										
					}
				}.start();
			}
		}
	};
	
	/**
	 * To get camera instance
	 * @return Camera
	 */
	private static Camera getCameraInstance() {
		Camera camera = null;
		try {
			camera = Camera.open();
			
			// Work around for Camera preview issues
			Camera.Parameters params = camera.getParameters();
			params.setPreviewFpsRange(30000, 30000);
			camera.setParameters(params);
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		return camera;
	}

	/*
	 * Sets Paint object to draw rectangles.
	 */
	public void setRectPaint() {
		rectPaint=new Paint();
		rectPaint.setColor(Color.GREEN);
		rectPaint.setStyle(Paint.Style.STROKE);
		rectPaint.setStrokeWidth(5);
	}
	
	/*
	 * Sets Paint object for annotations
	 */
	public void setTextPaint() {
		textPaint=new Paint();
		textPaint.setColor(Color.GREEN);
		textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(60);
		textPaint.setTextAlign(Align.CENTER);
	}
	
	/*
	 * Sets Paint object to draw rectangles for text background.
	 */
	public void setBackground() {
		background=new Paint();
		background.setColor(Color.BLACK);
		background.setStyle(Paint.Style.FILL);
		background.setStrokeWidth(5);
	}
	
	

	/*
	 * Callback when the picture is taken
	 */
	private final PictureCallback mPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Log.d(TAG, "Picture taken!");
			mCounter.setVisibility(View.GONE);
			fileName = Environment.getExternalStorageDirectory().getPath()+"/picture.jpg"; //saved with filename picture
			FileOutputStream imageFileOS;
			try {
				imageFileOS = new FileOutputStream(fileName);
				imageFileOS.write(data);
				imageFileOS.flush();
				imageFileOS.close();                
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/picture.jpg")
					.copy(Bitmap.Config.ARGB_8888, true);
			//AsyncTask method to access server.
			new LongOperation().execute(fileName);  

		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		try {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
			mCamera = getCameraInstance();
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	//To detect gestures
	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		//Create a base listener for generic gestures
		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.LONG_PRESS) {
					if(jsonResponse.optJSONArray("responses")!=null && jsonResponse.optJSONArray("responses").length()>0){
						stop=true;
						mCamera.release();
						mCamera=null;
						Intent intent = new Intent(CameraActivity.this,SearchResultsActivity.class);
						intent.putExtra("JSON_Object",jsonResponse.toString());
						startActivity(intent);
					}						
				}

				return false;
			}
		});
		return gestureDetector;
	}

	/*
	 * Send generic motion events to the gesture detector
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

	// Class for server call. Better to do server call with AsyncTask
	public class LongOperation  extends AsyncTask<String, String, String> {

		public LongOperation(){
		}

		//Send image to server using http post
		protected String doInBackground(String... picturePath) {

			File file = new File(picturePath[0]);
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost postRequest = new HttpPost("http://"+ip+":8080/MJFaceServer/image");
				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("image", new FileBody(file));
				postRequest.setEntity(reqEntity);
				ResponseHandler<String> handler = new BasicResponseHandler();			
				String content = httpClient.execute(postRequest,handler);
				jsonResponse = new JSONObject(content);
				httpClient.getConnectionManager().shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return picturePath[0];
		}

		// Get results from server and display
		@Override
		protected void onPostExecute(String filePath) {
			imgView.setVisibility(View.VISIBLE);					
			Canvas canvas=new Canvas(bitmap);
			canvas.drawBitmap(bitmap,0, 0, null);
			if(jsonResponse!=null){
				Log.d(TAG, jsonResponse.toString());	
				JSONArray annotations=jsonResponse.optJSONArray("annotations");
				if(annotations!=null && annotations.length()>0){					
					
					for (int i=0;i<annotations.length();i++) {
						JSONObject annotation = null;
						try {
							annotation = (JSONObject) annotations.get(i);
						} catch (JSONException e) {
							e.printStackTrace();
						}						
						int left=annotation.optInt("x");
						int top =annotation.optInt("y");
						int right= annotation.optInt("x")+annotation.optInt("width");
						int bottom=annotation.optInt("y")+annotation.optInt("height");
						canvas.drawRect(left, top,right, bottom, rectPaint);
						canvas.drawText(annotation.optString("text"), annotation.optInt("x")+5,annotation.optInt("y")-5, textPaint);
					}					
				}else{	
					String s="No faces found or unknown faces";
					canvas.drawRect(canvas.getWidth()/2-textPaint.measureText(s)/2, canvas.getHeight()/2 - textPaint.getTextSize(), canvas.getWidth()/2 + textPaint.measureText(s)/2, canvas.getHeight()/2, background);
					canvas.drawText(s,canvas.getWidth()/2 ,canvas.getHeight()/2, textPaint);
				}
//
//				if(jsonResponse.optJSONArray("responses")!=null && jsonResponse.optJSONArray("responses").length()>0){
//					canvas.drawRect(0, canvas.getHeight()/2,canvas.getWidth(), canvas.getHeight()/2+70, background);
//					canvas.drawText("Long press for detailed information", canvas.getWidth()/2,canvas.getHeight()/2, textPaint);
//				}
			}else{
				String s="Server error! try later";
				canvas.drawRect(canvas.getWidth()/2-textPaint.measureText(s)/2, canvas.getHeight()/2 - textPaint.getTextSize(), canvas.getWidth()/2 + textPaint.measureText(s)/2, canvas.getHeight()/2, background);
				canvas.drawText(s, canvas.getWidth()/2,canvas.getHeight()/2, textPaint);
			}
			imgView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, imgView.getWidth(), imgView.getHeight(), false));
									
		}
	}
	
	private void debug(String message){
		Log.d(TAG, message);
	}
}
