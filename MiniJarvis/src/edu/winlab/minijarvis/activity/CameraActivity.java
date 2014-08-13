///*
// * Copyright (C) 2014 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package edu.winlab.minijarvis.activity;
//
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.hardware.Camera;
//import android.hardware.Camera.PictureCallback;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.WindowManager;
//import android.widget.TextView;
//
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.net.URL;
//import java.util.ArrayList;
//
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.ResponseHandler;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.FileBody;
//import org.apache.http.impl.client.BasicResponseHandler;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.params.CoreConnectionPNames;
//import org.apache.http.params.HttpParams;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.google.android.glass.touchpad.Gesture;
//import com.google.android.glass.touchpad.GestureDetector;
//
//import edu.winlab.minijarvis.R;
//import edu.winlab.minijarvis.model.Results;
//import edu.winlab.minijarvis.model.SearchResults;
//
///**
// * Shows a simple camera preview and takes a picture on tap.
// */
//public class CameraActivity extends Activity {
//
//	private static final String TAG = "CameraActivity";
//	private SurfaceView mPreview;
//	private Camera mCamera;
//	public static final int MEDIA_TYPE_IMAGE = 1;
//	private GestureDetector mGestureDetector = null;
//	private String Content;
//	private String ip="192.168.207.81";
//
//	private final PictureCallback mPictureCallback = new PictureCallback() {
//
//		@Override
//		public void onPictureTaken(byte[] data, Camera camera) {
//			Log.d(TAG, "Picture taken!");
//			String fileName = Environment.getExternalStorageDirectory().getPath()+"/picture.jpg";
//			FileOutputStream imageFileOS;
//			try {
//				imageFileOS = new FileOutputStream(fileName);
//				imageFileOS.write(data);
//				imageFileOS.flush();
//				imageFileOS.close();                
//				System.out.println("Saved in :"+fileName);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			new LongOperation().execute(fileName);            
//			mCamera.release();
//
//		}
//	};
//
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		if(mCamera!=null){
//			mCamera.release();
//			mCamera=null;
//		}
//		
//	}
//	private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
//
//		@Override
//		public void surfaceCreated(SurfaceHolder holder) {
//			try {
//				mCamera.setPreviewDisplay(holder);
//				mCamera.startPreview();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public void surfaceDestroyed(SurfaceHolder holder) {
////			if (mCamera != null) {
////				mCamera.stopPreview();
////
////				//Releases the camera
////				mCamera.release();
////				//Restore the camera object to its initial state
////				mCamera = null;
////
////			}
//		}
//
//		@Override
//		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//			// Nothing to do here.
//		}
//	};
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		setContentView(R.layout.activity_main);
//		
//		mPreview = (SurfaceView) findViewById(R.id.preview);
//		TextView txt = (TextView) findViewById(R.id.shortText);
//		txt.setVisibility(View.GONE);
//		TextView txt2 = (TextView) findViewById(R.id.info);
//		txt2.setText("Tap to take picture"); 
//
//		SurfaceHolder holder= mPreview.getHolder();
//		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//		holder.addCallback(mSurfaceHolderCallback);
//		mGestureDetector = createGestureDetector(this);
//		mCamera = getCameraInstance();
//	}
//
//	private GestureDetector createGestureDetector(Context context) {
//		GestureDetector gestureDetector = new GestureDetector(context);
//		//Create a base listener for generic gestures
//		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
//			@Override
//			public boolean onGesture(Gesture gesture) {
//				if (gesture == Gesture.TAP) {
//					mCamera.takePicture(null, null, mPictureCallback);
//					return true;
//				}else if (gesture == Gesture.LONG_PRESS) {
//					Intent intent = new Intent(CameraActivity.this,SearchResultsActivity.class);
//					intent.putExtra("JSON_Object",Content);
//					startActivity(intent);
//				}
//
//				return false;
//			}
//		});
//		return gestureDetector;
//	}
//
//	/*
//	 * Send generic motion events to the gesture detector
//	 */
//	@Override
//	public boolean onGenericMotionEvent(MotionEvent event) {
//		if (mGestureDetector != null) {
//			return mGestureDetector.onMotionEvent(event);
//		}
//		return false;
//	}
//
//
//	private Camera getCameraInstance() {
//		Camera camera = null;
//		try {
//			camera = Camera.open();
//			// Work around for Camera preview issues.
//			Camera.Parameters params = camera.getParameters();
//			params.setPreviewFpsRange(30000, 30000);
//			camera.setParameters(params);
//		} catch (Exception e) {
//			// cannot get camera or does not exist
//		}
//		return camera;
//	}
//	// Class with extends AsyncTask class
//
//	private class LongOperation  extends AsyncTask<String, Void, Void> {
//
//		// Required initialization         
//		private ProgressDialog Dialog = new ProgressDialog(CameraActivity.this);
//
//
//		protected void onPreExecute() { 			
//			Dialog.setMessage("Please wait..");
//			Dialog.show();
//		}
//
//		// Call after onPreExecute method
//		protected Void doInBackground(String... picturePath) {
//
//			System.out.println("Starting web service!!!");	
//
//			File file = new File(picturePath[0]);
//			String fileName = file.getName();
//			Log.v("FileName", fileName);
//			try {
//				HttpClient httpClient = new DefaultHttpClient();
//				HttpPost postRequest = new HttpPost("http://"+ip+":8080/MiniJarvisFaceServer/image");
//				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//				reqEntity.addPart("image", new FileBody(file));
//				postRequest.setEntity(reqEntity);
//				ResponseHandler<String> handler = new BasicResponseHandler();			
//				Content = httpClient.execute(postRequest,handler);
//				Log.d("aaaa", Content);
//				httpClient.getConnectionManager().shutdown();
//			} catch (IOException e) {
//				System.out.println(e.getMessage());
//				e.printStackTrace();
//			}
//			return null;
//		}
//
//		protected void onPostExecute(Void unused) {
//			// NOTE: You can call UI Element here.
//
//			// Close progress dialog
//			Dialog.dismiss();
//			JSONObject jsonResponse;                       
//			try {
//				if( Content!=null && !Content.isEmpty()){
//					jsonResponse = new JSONObject(Content);
//					Results results=new Results();
//					results.setText(jsonResponse.optString("text"));
//					TextView txt = (TextView) findViewById(R.id.shortText);
//					txt.setVisibility(View.VISIBLE);
//					txt.setText(results.getText()); 
//					TextView txt2 = (TextView) findViewById(R.id.info);
//					txt2.setText("Long press for detailed information");  
//
//				}else{
//					TextView txt2 = (TextView) findViewById(R.id.info);
//					txt2.setText("Server error! try later");
//				}
//
//
//			} catch (JSONException e) {
//
//				e.printStackTrace();
//			}
//		}
//
//	}
//}
