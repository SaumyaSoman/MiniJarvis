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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
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
import android.view.View.OnClickListener;
import android.widget.TextView;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import edu.winlab.minijarvis.R;
import edu.winlab.minijarvis.model.Results;
import edu.winlab.minijarvis.model.SearchResults;

/**
 * Shows a simple camera preview and takes a picture on tap.
 */
public class CopyOfCameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	private SurfaceView mPreview;
	private Camera mCamera;
	public static final int MEDIA_TYPE_IMAGE = 1;
	private GestureDetector mGestureDetector = null;
	private String Content;
	private String ip="192.168.207.81";
	private int i=0;
	JSONObject jsonResponse; 
	
	private final PictureCallback mPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "Picture taken!");
			String fileName = Environment.getExternalStorageDirectory().getPath()+"/picture.jpg";
			FileOutputStream imageFileOS;
			try {
				imageFileOS = new FileOutputStream(fileName);
				imageFileOS.write(data);
				imageFileOS.flush();
				imageFileOS.close();                
				System.out.println("Saved in :"+fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			new LongOperation().execute(fileName);  
			mCamera.startPreview();
		}
	};

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
//		    if (mCamera != null) {
//		        mCamera.stopPreview();
//		        mCamera.release();
//		        mCamera = null; 
//		    }
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//			try {
//				mCamera.setPreviewDisplay(holder);
//				mCamera.startPreview();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		mPreview = (SurfaceView) findViewById(R.id.preview);
		SurfaceHolder holder= mPreview.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(mSurfaceHolderCallback);
		mGestureDetector = createGestureDetector(this);
		mCamera = getCameraInstance();
		Sync sync = new Sync(call,3*1000);
	}
    public final Handler handler = new Handler();
	  final private Runnable call = new Runnable() {
	        public void run() {
    		
	        Log.v("test","this will run every minute" + i); i++;
	        Content=null;
	        jsonResponse=null;
	        mCamera.takePicture(null, null, mPictureCallback);
	        handler.postDelayed(call,7*1000);
	        
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
	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		//Create a base listener for generic gestures
		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.LONG_PRESS) {
					try {
						jsonResponse = new JSONObject(Content);
						if(jsonResponse.optJSONArray("responses")!=null && jsonResponse.optJSONArray("responses").length()>0){
							Intent intent = new Intent(CopyOfCameraActivity.this,SearchResultsActivity.class);
							intent.putExtra("JSON_Object",Content);
							startActivity(intent);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
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
	// Class with extends AsyncTask class

	private class LongOperation  extends AsyncTask<String, Void, Void> {

	
		// Call after onPreExecute method
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
				System.out.println("Content "+Content);
				httpClient.getConnectionManager().shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Void unused) {
				                      
				try {
					System.out.println("Content...in post"+Content);
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
