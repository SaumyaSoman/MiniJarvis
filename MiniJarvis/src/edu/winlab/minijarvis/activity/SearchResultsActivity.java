package edu.winlab.minijarvis.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import edu.winlab.minijarvis.model.SearchResults;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.widget.AdapterView;

/**
 * This activity displays search results from Google in cards
 * @author Saumya
 *
 */

public class SearchResultsActivity extends Activity {

	private ArrayList<Card> mCards;
	private CardScrollView mCardScrollView;
	private GestureDetector mGestureDetector = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String content=intent.getStringExtra("JSON_Object");
		ArrayList<SearchResults> responses=extractResults(content);		

		createCards(responses);

		mCardScrollView = new CardScrollView(this);
		CSAdapter adapter = new CSAdapter();
		mCardScrollView.setAdapter(adapter);
		mCardScrollView.activate();
		setContentView(mCardScrollView);
		mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				Log.d("Click","click at position " + position);
			}
		});
		mGestureDetector = createGestureDetector(this);
	}
	
	/**
	 * Method to create cards for each search result
	 * @param responses ArrayList<SearchResults>
	 */

	private void createCards(ArrayList<SearchResults> responses) {
		mCards = new ArrayList<Card>();

		for (SearchResults searchResults : responses) {
			Card card= new Card(this);
			card.setText(searchResults.getSnippet());
			card.setFootnote(searchResults.getLink());
			mCards.add(card);
		}
	}

	/**
	 * Adaptor for card scroll
	 * @author Saumya
	 *
	 */
	private class CSAdapter extends CardScrollAdapter {

		@Override
		public int getPosition(Object item) {
			return mCards.indexOf(item);
		}

		@Override
		public int getCount() {
			return mCards.size();
		}

		@Override
		public Object getItem(int position) {
			return mCards.get(position);
		}

		@Override
		public int getViewTypeCount() {
			return Card.getViewTypeCount();
		}

		@Override
		public int getItemViewType(int position){
			return mCards.get(position).getItemViewType();
		}

		@Override
		public View getView(int position, View convertView,
				ViewGroup parent) {
			return  mCards.get(position).getView(convertView, parent);
		}
	}

	/**
	 * Method to detect gesture (long press/tap) and opens the url
	 * @param context
	 * @return GestureDetector
	 */
	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		//Create a base listener for generic gestures
		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.LONG_PRESS || gesture == Gesture.TAP) {
					Object object = mCardScrollView.getSelectedItem();
					Card selectedResult = (Card)object;
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedResult.getFootnote().toString()));
					startActivity(browserIntent);
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

	/**
	 * Method to extract search results from JSON response string
	 * @param content JSON response string
	 * @return ArrayList<SearchResults>
	 */
	public ArrayList<SearchResults> extractResults(String content){
		JSONObject jsonResponse;
		try {
			jsonResponse = new JSONObject(content);
			JSONArray jsonMainNode = jsonResponse.optJSONArray("responses");		            
			ArrayList<SearchResults> searchResults=new ArrayList<SearchResults>();
			for(int i=0; i<jsonMainNode.length(); i++){
				JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
				SearchResults data=new SearchResults();
				data.setTitle(jsonChildNode.optString("title"));
				data.setLink(jsonChildNode.optString("link"));
				data.setSnippet(jsonChildNode.optString("snippet"));
				searchResults.add(data);
			}	
			return searchResults;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}
}