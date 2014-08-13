package edu.winlab.minijarvis.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import edu.winlab.minijarvis.R;
import edu.winlab.minijarvis.model.SearchResults;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class SearchResultsActivity extends Activity{

	private GestureDetector mGestureDetector = null;
	private ListView lv1 =null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.livecard_camerademo);
		Intent intent = getIntent();
		String content=intent.getStringExtra("JSON_Object");
		ArrayList<SearchResults> responses=extractResults(content);		
		lv1 = (ListView) findViewById(R.id.listV_main);
		lv1.setAdapter(new ResponseListBaseAdapter(this, responses));
		if(lv1 != null){
			lv1.setAdapter(new ResponseListBaseAdapter(this, responses));
			lv1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			lv1.setClickable(true);
			lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		         public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		        	 Log.d("MY_LOG", "click at position " + position);
		         }
		    });
		}

		mGestureDetector = createGestureDetector(this);
	}
	
	private GestureDetector createGestureDetector(Context context) {
		  GestureDetector gestureDetector = new GestureDetector(context);
		    //Create a base listener for generic gestures
		    gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
		        @Override
		        public boolean onGesture(Gesture gesture) {
		            if (gesture == Gesture.TAP) { // On Tap, generate a new number
		                return true;
		            } else if (gesture == Gesture.LONG_PRESS) {
						Object object = lv1.getSelectedItem();
					    SearchResults selectedResult = (SearchResults)object;
					    System.out.println(selectedResult.toString());
		            	//System.out.println(selectedResult.getLink());
		            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedResult.getLink()));
		            	startActivity(browserIntent);
		                return true;
		            } else if (gesture == Gesture.SWIPE_RIGHT) {
		                // do something on right (forward) swipe
		            	System.out.println("here"+lv1.getSelectedItemPosition());
		                lv1.setSelection(lv1.getSelectedItemPosition()+1);
		                return true;
		            } else if (gesture == Gesture.SWIPE_LEFT) {
		                // do something on left (backwards) swipe
		            	System.out.println("right"+lv1.getSelectedItemPosition());
		                lv1.setSelection(lv1.getSelectedItemPosition()-1);
		                return true;
		            }
		            return false;
		        }
		    });
		    gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
		        @Override
		        public void onFingerCountChanged(int previousCount, int currentCount) {
		          // do something on finger count changes
		        }
		    });
		    gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
		        @Override
		        public boolean onScroll(float displacement, float delta, float velocity) {
		            // do something on scrolling

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
		
	public ArrayList<SearchResults> extractResults(String content){
		JSONObject jsonResponse;
		try {
			jsonResponse = new JSONObject(content);
		    JSONArray jsonMainNode = jsonResponse.optJSONArray("responses");		            
		    ArrayList<SearchResults> searchResults=new ArrayList<SearchResults>();
		    for(int i=0; i<jsonMainNode.length(); i++){
				 						JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
				 						SearchResults data=new SearchResults();
				 						/******* Fetch node values **********/
				 						data.setTitle(jsonChildNode.optString("title"));
				 						data.setLink(jsonChildNode.optString("link"));
				 						data.setSnippet(jsonChildNode.optString("snippet"));
				 						searchResults.add(data);
		    }	
		    return searchResults;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	

}
