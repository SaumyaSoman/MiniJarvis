package edu.winlab.minijarvis.activity;
//package edu.winlab.minijarvis;
//
//import com.google.android.glass.timeline.LiveCard;
//import com.google.android.glass.timeline.LiveCard.PublishMode;
//
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Intent;
//import android.hardware.Camera;
//import android.os.Binder;
//import android.os.IBinder;
//
//import android.os.StrictMode;
//import android.widget.RemoteViews;
//
//public class InformService extends Service{
//
//	
//    private static final String LIVE_CARD_TAG = "informMe";
//    private LiveCard mLiveCard;
////    private RemoteViews mLiveCardView;
//////    StrictMode.ThreadPolicy defaultThreadPolicy = null;
//////    
//////    public class LocalBinder extends Binder {
//////        public InformService getService() {
//////            return InformService.this;
//////        }
//////    }
//////    private final IBinder mBinder = new LocalBinder();
////
////@Override
////public void onCreate() {
////    super.onCreate();
////    //defaultThreadPolicy = StrictMode.getThreadPolicy();
////}
////
////@Override
////public IBinder onBind(Intent intent) {
//////	onServiceStart();
//////    return mBinder;
////	return null;
////}
////
////@Override
////public int onStartCommand(Intent intent, int flags, int startId) {
////	
////	onServiceStart();
////	return START_STICKY;
////}
////
////private void onServiceStart()
////{
////
////	//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(defaultThreadPolicy).permitAll().build());
////	
////	Camera.open();
//////	if(mLiveCard==null){
//////		
//////		//create a new live card
//////		mLiveCard=new LiveCard(this, LIVE_CARD_TAG);
//////		
//////		//set the view. View has the message hello world
//////		mLiveCardView = new RemoteViews(getPackageName(),R.layout.livecard_camerademo);
//////		mLiveCard.setViews(mLiveCardView);
//////		
//////		//This indicates the action when the user taps to select the card
//////		Intent menuIntent = new Intent(this, CameraActivity.class);
//////        mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
//////        
//////        // Publish the live card
//////        mLiveCard.publish(PublishMode.REVEAL);
//////	}
////}
////
////
////@Override
////public void onDestroy() {
////	if (mLiveCard != null && mLiveCard.isPublished()) {
////	        mLiveCard.unpublish();
////	        mLiveCard = null;
////	    }
////	    super.onDestroy();
////}
//
//    
//  //  <--------itexico----------------->
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO Auto-generated method
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//    	mLiveCard=new LiveCard(this, LIVE_CARD_TAG);
//        Intent i = new Intent(this, CameraActivity1.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(i);
//
//        return START_STICKY;
//    }
//}