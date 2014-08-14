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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
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
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import edu.winlab.minijarvis.R;
import edu.winlab.minijarvis.model.Results;


/**
 * This is the main activity. It opens camera and takes images every 10 seconds. Each image is sent to server.
 * The server does object/face recognition and sends back annotation to glassware. On long press, SearchResultsActivity is opened
 * @author Saumya
 *
 */
public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	private SurfaceView mPreview;
	private Camera mCamera;
	public static final int MEDIA_TYPE_IMAGE = 1;
	private GestureDetector mGestureDetector = null;
	private String Content;
	private String ip="192.168.207.81"; // ip of my server
	JSONObject jsonResponse; 
	boolean stop=false;
	public final Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //to prevent screen from dimming
		setContentView(R.layout.activity_main);
		mPreview = (SurfaceView) findViewById(R.id.preview);
		SurfaceHolder holder= mPreview.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(mSurfaceHolderCallback);
		
		//to detect gestures
		mGestureDetector = createGestureDetector(this);
		mCamera = getCameraInstance();
		
		// the first image is taken in 3s
		Sync sync = new Sync(call,3*1000);
	}
	
	final private Runnable call = new Runnable() {
		public void run() {
			
			//if stop==false, it means user did long press for an annotation, so no more images required
			if(stop==false){
				//picture is taken every taken 10s after the first time
				mCamera.takePicture(null, null, mPictureCallback);
				handler.postDelayed(call,10*1000);
			}


		}
	};

	
	public class Sync {
		Runnable task;
		public Sync(Runnable task, long time) {
			this.task = task;
			handler.removeCallbacks(task);
			handler.postDelayed(task, time);
		}
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
	
	/*
	 * Callback when the picture is taken
	 */
	private final PictureCallback mPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "Picture taken!");
			String fileName = Environment.getExternalStorageDirectory().getPath()+"/picture.jpg"; //saved with filename picture
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
			new LongOperation().execute(fileName);  
			mCamera.startPreview();
		}
	};

	private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {

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
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}
	};
	
	//To detect gestures
	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		//Create a base listener for generic gestures
		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.LONG_PRESS) {
					try {
						if(Content!=null){
							jsonResponse = new JSONObject(Content);
							if(jsonResponse.optJSONArray("responses")!=null && jsonResponse.optJSONArray("responses").length()>0){
								stop=true;
								mCamera.release();
								mCamera=null;
								Intent intent = new Intent(CameraActivity.this,SearchResultsActivity.class);
								intent.putExtra("JSON_Object",Content);
								startActivity(intent);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
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


	private Camera getCameraInstance() {
		Camera camera = null;
		try {
			camera = Camera.open();
			// Work around for Camera preview issues.
			Camera.Parameters params = camera.getParameters();
			params.setPreviewFpsRange(30000, 30000);
			camera.setParameters(params);
		} catch (Exception e) {
			// cannot get camera or does not exist
		}
		return camera;
	}
	
	
	// Class for server call. Better to do server call with AsyncTask
	private class LongOperation  extends AsyncTask<String, Void, Void> {
		
		//Send image to server using http post
		protected Void doInBackground(String... picturePath) {
			
			File file = new File(picturePath[0]);
			String fileName = file.getName();
			Log.v("FileName", fileName);
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost postRequest = new HttpPost("http://"+ip+":8080/MiniJarvisFaceServer/image");
				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("image", new FileBody(file));
				postRequest.setEntity(reqEntity);
				ResponseHandler<String> handler = new BasicResponseHandler();			
				Content = httpClient.execute(postRequest,handler);
				httpClient.getConnectionManager().shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		// Get results from server and display
		protected void onPostExecute(Void unused) {

			try {
				if(Content!=null){
					jsonResponse = new JSONObject(Content);
					Results results=new Results();
					results.setText(jsonResponse.optString("text"));
					TextView txt = (TextView) findViewById(R.id.shortText);
					txt.setVisibility(View.VISIBLE);
					txt.setText(results.getText()); 

					if(jsonResponse.optJSONArray("responses")!=null && jsonResponse.optJSONArray("responses").length()>0){
						TextView txt2 = (TextView) findViewById(R.id.info);
						txt2.setVisibility(View.VISIBLE);
						txt2.setText("Long press for detailed information");
					}else{
						TextView txt2 = (TextView) findViewById(R.id.info);
						txt2.setVisibility(View.GONE);
					}
				}else{
					TextView txt2 = (TextView) findViewById(R.id.info);
					txt2.setVisibility(View.VISIBLE);
					txt2.setText("Server error! try later");
					TextView txt = (TextView) findViewById(R.id.shortText);
					txt.setVisibility(View.GONE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
