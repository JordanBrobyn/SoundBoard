package my.SoundBoard;
/*Date: 04/03/2012
 * Author:Jordan Brobyn
 * Description: Used to play sounds predefined by the user
 * Multiple boards and options like loop and repeat allow for variations of playback
 * Drumkit is currently commented out due to issues with reading from compressed memory
 * 
 * Complete: Start working on the application service for filebuilder etc.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import my.SoundBoard.ClipEvent.EventType;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.Toast;


public class PlayActivity extends TabActivity implements OnTouchListener, SeekBar.OnSeekBarChangeListener, OnCompletionListener, OnCancelListener, OnClickListener{

	boolean loaded = false;
	private static final String TAG = "Touch" ;
	ArrayList<SoundInfo> soundButtons = new ArrayList<SoundInfo>();
	SoundDataSource soundDB;
	Map <String,SoundInfo> hash;
	private View parent;
	long startTime;
	long endTime;
	int recent_Session;
	private Handler handler = new Handler();
	
	boolean recording = false;
	ArrayList<ClipEvent> eventLogs = new ArrayList<ClipEvent>();
	
	Dialog dialog;
	int loop = 0;
	int volume = 50;
	SeekBar mSeekBar;
	
	boolean pauser = false;
	boolean looper = false;
	boolean repeater = false;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);
        TabHost tabs = getTabHost();
        soundDB = new SoundDataSource(getApplicationContext()); 
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
       
        tabs.addTab(tabs.newTabSpec("tab1").setIndicator("Board 1").setContent(R.id.tabview1)); //Create tab view
        tabs.addTab(tabs.newTabSpec("tab2").setIndicator("Board 2").setContent(R.id.tabview2));
        //tabs.addTab(tabs.newTabSpec("tab3").setIndicator("Drum Kit").setContent(R.id.tabview3));
        tabs.setCurrentTab(0);
       
        mSeekBar = (SeekBar) findViewById(R.id.volumeBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(100);
        mSeekBar.setProgress(50);
        parent = findViewById(R.id.tabview1).getRootView();
        parent.setOnTouchListener(this);

        
        soundDB.open(); //Open db to grab data
        populateBoard(); //Display board with active keys and populate hashmap with ids and mediaplayers
        
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.playmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//Option Menu Selections
		
		if (item.getItemId() == R.id.RecordP){
			Toast.makeText(this, "Start Mixing!", Toast.LENGTH_SHORT).show();
			
			
			if(recording == false){
				
				eventLogs = new ArrayList<ClipEvent>();
				stopAll(false);
				recording = true;
				startTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
			
			}
			else{

				stopAll(false);
				recording = false;
				endTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
				dialog = new Dialog(PlayActivity.this);
				dialog.setContentView(R.layout.replay);
				dialog.setTitle("Recorder");
				dialog.setCancelable(true);
				dialog.show();
				
				View view = dialog.findViewById(R.id.builderView);
				Button save = (Button) view.findViewById(R.id.replaySave);
				save.setOnClickListener(this);
				long duration = endTime - startTime;
				ReplayTracks builder = new ReplayTracks();
				builder.setup(view, eventLogs,duration,hash,dialog);

			}
			
			
		}else if(item.getItemId() == R.id.EditM){ //Move to edit Screen
			Toast.makeText(this, "Edit Board", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(getApplicationContext(),EditActivity.class);
			
			stopAll(true);
						
			finish();
			startActivity(intent);
		}
		
		return true;
	}
	
	@Override
	public void onBackPressed(){ // Release audio data to not overflow memory
			
		    stopAll(true);
			soundDB.close();
			finish();
	}
	
	public void stopAll(boolean leaving){
		
		Collection<SoundInfo> c = hash.values();
		Iterator<SoundInfo> it = c.iterator();
	    MediaPlayer mp;
	    SoundInfo info;
        View view = findViewById(R.id.playWindow);
        
	    while(it.hasNext()){  //Remove mediaplayer data to release memory. This is not always done automatically      
           info = (SoundInfo) it.next();
           mp = info.returnMp();
           
           if(mp.isPlaying()){
        	    if(recording == true){
					long currTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - startTime;
					ClipEvent evnt = new ClipEvent(info.id,EventType.STOP,currTime,info.song,volume,0,mp.getCurrentPosition());
					eventLogs.add(evnt);
				}
        	    
        	    ImageView v = (ImageView)view.findViewWithTag(info.id);
        	    v.setImageResource(info.pause);
        	    v.invalidate();
        	    mp.stop();   
           }
           if(leaving == true)
        	   mp.release();
           else
        	   mp.reset();
        }
	}
	
	public boolean onTouch(View v, MotionEvent event){ //Listen for on touch events
			for(int ptrIndex = 0; ptrIndex < event.getPointerCount(); ptrIndex++){
				int actionPointerIndex = event.getActionIndex();
				int actionResolved = event.getAction() & MotionEvent.ACTION_MASK;
				//Log.v("ActionResolved"," = "+ actionResolved);
				if(actionResolved < 7 && actionResolved > 4){ //convert the action to up-down or move
					actionResolved = actionResolved -5;
				}
				
				if(actionResolved == MotionEvent.ACTION_DOWN){
					long starting = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
					dealEvent(ptrIndex,event,v,actionResolved);
					long ending = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
					Log.v("Time taken to perform touch",""+(ending-starting));
					//Log.v("tag","move"+ptrIndex+" "+v.toString());
					//Log.v("event.getPointerCount","="+event.getPointerCount());
				}
				
			}
		
		return false;
	}
	
	 private void dealEvent(int actionPointerIndex, MotionEvent event,
		        View eventView, int actionresolved) {
		    int rawX, rawY;
		    int location[] = { 0, 0 };
		    eventView.getLocationOnScreen(location);
		    // Log.v("tag", location + "");
		    rawX = (int) event.getX(actionPointerIndex) + location[0];
		    rawY = (int) event.getY(actionPointerIndex) + location[1];
		 
		    ArrayList<View> views = getTouchedViews(rawX, rawY, actionresolved);
		 
		    // dumpEvent(event);
		    for (View view : views) {
		        int x, y;
		        view.getLocationOnScreen(location);
		        x = rawX - location[0];
		        y = rawY - location[1];

		        MotionEvent me = MotionEvent.obtain(event.getDownTime(),
		            event.getEventTime(), actionresolved, x, y,
		            event.getPressure(actionPointerIndex),
		            event.getPressure(actionPointerIndex),
		            event.getMetaState(), event.getXPrecision(),
		            event.getYPrecision(), event.getDeviceId(),
		            event.getEdgeFlags());
		        me.setLocation(x, y);
		 
		       if (actionresolved == MotionEvent.ACTION_DOWN) {
		    	   
		    	   if(view instanceof ImageView && y > 0){    
		    		   playSound((ImageView)view);
		    	   }
		       }
		    }
		 
	}
	 private ArrayList<View> getTouchedViews(int x, int y, int action) {
		 
		    int moveGap = 0;
		 
		    if (action == MotionEvent.ACTION_MOVE) {
		        moveGap = 0;
		    }
		 
		    ArrayList<View> touchedViews = new ArrayList<View>();
		    ArrayList<View> possibleViews = new ArrayList<View>();
		 
			if (parent instanceof ViewGroup) {
			    possibleViews.add(parent);
			    for (int i = 0; i < possibleViews.size(); i++) {
					View view = (View)possibleViews.get(i);
	
					int location[] = { 0, 0 };
					view.getLocationOnScreen(location);
	
					if (((view.getHeight() + location[1] + moveGap >= y)
						&& (view.getWidth() + location[0] + moveGap >= x)
						&& (view.getLeft() - moveGap <= x) & (view.getTop()
						- moveGap <= y))
						|| view instanceof FrameLayout) {
	
					    touchedViews.add(view);
					    possibleViews.addAll(getChildViews(view));
					}

			    }
			    for(int i = 0; i< touchedViews.size(); i++){
			    	View view = (View)touchedViews.get(i);
			    	if(!(view instanceof ImageView)){
			    		touchedViews.remove(i);
			    	}
			    }
			}

			return touchedViews;

	 }
	 private ArrayList<View> getChildViews(View view) {
			
		 ArrayList<View> views = new ArrayList<View>();
			if (view instanceof ViewGroup) {
			    ViewGroup v = ((ViewGroup) view);
			    if (v.getChildCount() > 0) {
					for (int i = 0; i < v.getChildCount(); i++) {
						if(v.getChildAt(i).getVisibility() == View.VISIBLE) //Only add children that are visible
							views.add(v.getChildAt(i));
					}
			    }
			}
			return views;
	 }

	 
	public void populateBoard(){ //Display which buttons have a song and create hashmap to use later
		
		Cursor cursor = soundDB.retrieveDB();
		hash = new HashMap<String,SoundInfo>();
		View view = findViewById(R.id.playWindow);
		//prepareDrums pD = new prepareDrums();
		
		//hash = pD.prepareDrums(hash,getApplicationContext());//Add DrumKit
		
		
		cursor.moveToFirst(); // Point to the head of the db list	
		
		while(!cursor.isAfterLast()){ //Log Table to verify
			SoundInfo info = new SoundInfo(cursor.getString(0),cursor.getString(1),0,0,R.drawable.orange3,R.drawable.red1);
			hash.put(cursor.getString(0), info);
					
			ImageView iView = (ImageView) view.findViewWithTag(cursor.getString(0)); //Change icon to signify a song is available
			iView.setImageResource(info.pause);
			iView.setOnTouchListener(this);
			iView.invalidate();
			cursor.moveToNext();
		}
		cursor.close();
	}
	
	public void playSound(ImageView v){ //Play the sound associated with the button
		
		SoundInfo index = null;
		index = hash.get(v.getTag());
		
		if(index != null){
			MediaPlayer mp = index.returnMp();
			
			
			if(mp.isPlaying()){
				/*With multi touch one action is pressed in rapid succession
				 * Since the tabs are layered, multiple buttons are being selected
				 * The timer will prevent the buttons behind to get activated
				 */
				if(System.currentTimeMillis() - index.getTime() < 100)
					return;
				
				if(pauser){
					if(recording == true){
						long currTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - startTime;
						ClipEvent evnt = new ClipEvent(index.id,EventType.STOP,currTime,index.song,volume,0,mp.getCurrentPosition());
						eventLogs.add(evnt);
					}
					mp.pause();
					hash.get(v.getTag()).setTime(System.currentTimeMillis());
					hash.get(v.getTag()).setPaused(true);
					
				}
				else if(looper){
					
					//Log the event occuring for a loop event
					if(recording == true){
						long currTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - startTime;
						ClipEvent evnt = new ClipEvent(index.id,EventType.STOP,currTime,index.song,volume,0,mp.getCurrentPosition());
						eventLogs.add(evnt);
						evnt = new ClipEvent(index.id,EventType.PLAY,currTime,index.song,volume,0,0);
						eventLogs.add(evnt);
						
					}
					mp.seekTo(0);
					hash.get(v.getTag()).setTime(System.currentTimeMillis());
				}else{
					
					//Log events for a stop
					if(recording == true){
						long currTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - startTime;
						ClipEvent evnt = new ClipEvent(index.id,EventType.STOP,currTime,index.song,volume,0,mp.getCurrentPosition());
						eventLogs.add(evnt);
					}
					
					mp.stop();
					v.setImageResource(index.pause);
					v.invalidate();
					mp.reset();
					hash.get(v.getTag()).setTime(System.currentTimeMillis());
				}
			}else{
				try {
					//Screen overlap will hit the button mutliple times, this will prevent activation
					if(System.currentTimeMillis() - index.getTime() < 100) 
						return;
					
					//The song was previously paused. Continue from last position
					if(index.isPaused()){
						mp.start();
						if(recording == true){
							long currTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - startTime;
							ClipEvent evnt = new ClipEvent(index.id,EventType.PLAY,currTime,index.song,volume,mp.getCurrentPosition(),0);
							eventLogs.add(evnt);
						}
						hash.get(v.getTag()).setTime(System.currentTimeMillis());
						hash.get(v.getTag()).setPaused(false);
						return;
					}
					
					/*if(index.song.charAt(0) == 'd'){ //Determine if the song is an asset from drumset

							AssetFileDescriptor afd = getApplicationContext().getAssets().openFd(index.song);
							mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
					}else{*/
						mp.setDataSource(index.song);
					//}
					mp.setOnCompletionListener(this);
					
					//Log events for a stop
					if(recording == true){
						long currTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - startTime;
						ClipEvent evnt = new ClipEvent(index.id,EventType.PLAY,currTime,index.song,volume,mp.getCurrentPosition(),0);
						eventLogs.add(evnt);
					}
					
					v.setImageResource(index.play);
					v.invalidate();
					mp.prepare();
					mp.start();
					mp.setVolume(((float)volume)/100, ((float)volume)/100);
					hash.get(v.getTag()).setTime(System.currentTimeMillis());
					hash.get(v.getTag()).setRepeat(repeater);
					
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void dumpEvent(MotionEvent event) { // Dumb touch events to LogCat for debugging
		   String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
		      "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
		   StringBuilder sb = new StringBuilder();
		   int action = event.getAction();
		   int actionCode = action & MotionEvent.ACTION_MASK;
		   sb.append("event ACTION_" ).append(names[actionCode]);
		   
		   if (actionCode == MotionEvent.ACTION_POINTER_DOWN
		         || actionCode == MotionEvent.ACTION_POINTER_UP) {
		      sb.append("(pid " ).append(
		      action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
		      sb.append(")" );
		   }
		   sb.append("[" );
		   for (int i = 0; i < event.getPointerCount(); i++) {
		      sb.append("#" ).append(i);
		      sb.append("(pid " ).append(event.getPointerId(i));
		      sb.append(")=" ).append((int) event.getX(i));
		      sb.append("," ).append((int) event.getY(i));
		      if (i + 1 < event.getPointerCount())
		         sb.append(";" );
		   }
		   sb.append("]" );
		   Log.d(TAG, sb.toString());
		}

	
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
		volume = progress;
		
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		mSeekBar.setProgress(volume);
		
	}
	
	public void alterOptions(View v){
		
		switch (v.getId()){
			case R.id.restart_button: 
				if(repeater == false){
					ImageView iView = (ImageView) v;
					iView.setImageResource(R.drawable.repeat_restart_on);
					iView.invalidate();
					repeater = true;
				}else{
					ImageView iView = (ImageView) v;
					iView.setImageResource(R.drawable.repeat_restart);
					iView.invalidate();
					repeater = false;
				}
				break;
			case R.id.continuous_button:
				if(looper == false){
					ImageView iView = (ImageView) v;
					iView.setImageResource(R.drawable.repeat_on);
					iView.invalidate();
					looper = true;
				}else{
					ImageView iView = (ImageView) v;
					iView.setImageResource(R.drawable.repeat);
					iView.invalidate();
					looper = false;
				}
				break;
			case R.id.pause_button:
				if(pauser == false){
					ImageView iView = (ImageView) v;
					iView.setImageResource(R.drawable.pause_on);
					iView.invalidate();
					pauser = true;
				}else{
					ImageView iView = (ImageView) v;
					iView.setImageResource(R.drawable.pause_off);
					iView.invalidate();
					pauser = false;
				}
				break;
		}
		
	}

	public void onCompletion(MediaPlayer finished) {
		Collection<SoundInfo> c = hash.values();
		Iterator<SoundInfo> it = c.iterator();
	    MediaPlayer mp;
	    SoundInfo info;
        View view = findViewById(R.id.playWindow);
        ImageView icon;
	    while(it.hasNext()){  //Remove mediaplayer data to release memory. This is not always done automatically
           info = (SoundInfo) it.next();
           mp = info.returnMp();
           
           if(finished == mp){//Reset the mp object to be replayed later
        	 //Log events for a stop
				if(recording == true){
					long currTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - startTime;
					ClipEvent evnt = new ClipEvent(info.id,EventType.STOP,currTime,info.song,volume,0,mp.getDuration());
					eventLogs.add(evnt);
				}
				
				if(info.repeat){
					//mp.seekTo(0);
					mp.start();
					if(recording == true){
						long currTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - startTime;
						ClipEvent evnt = new ClipEvent(info.id,EventType.PLAY,currTime,info.song,volume,0,0);
						eventLogs.add(evnt);
					}
					
				}else{
	        	   icon = (ImageView)view.findViewWithTag(info.id);
	        	   finished.stop();
	        	   finished.reset();
	        	   icon.setImageResource(info.pause); //Notify user that the song stopped by changing the appearance
				}
           }
        }
		
	}
	
	public void handleMessage(Message message){
		if(message.arg1 == RESULT_OK){
			Log.v("Handle Message","Message recieved ok");
		}else{
			Log.v("Message Results", "Save canceled");
		}
	
	}

	public void onCancel(DialogInterface arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		SharedPreferences remaining = getSharedPreferences("Attempts", 0);
		Editor editor = remaining.edit();

		if(remaining.contains("Count")){
			int counter = remaining.getInt("Count",0);
			
			if(counter <= 0){
				Toast.makeText(this, "Recording Limit(10) Reached.", Toast.LENGTH_LONG).show();
				Toast.makeText(this, "Purchase the full version to keep Mixing!", Toast.LENGTH_LONG).show();
				dialog.dismiss();
				return;
			}else{
				System.out.println("Decrementing counter, Counter is currently at "+counter);
				editor.putInt("Count", counter-1);
				editor.commit();
			}
		}else{
			System.out.println("Does not contain count");
			editor.putInt("Count", 10);
			editor.commit();
		}
		
		EditText text = (EditText) dialog.findViewById(R.id.recorderFileName);
		String fileName = text.getText().toString();
		
		if(eventLogs.size() <= 0){
			Toast.makeText(this, "ERROR: No songs were selected to mix!", Toast.LENGTH_LONG).show();
			return;
		}
		for(ClipEvent event: eventLogs){
			if(!(event.fileLocation.endsWith(".mp3") || event.fileLocation.endsWith(".wav"))){
				Toast.makeText(this, "OOPS! Recordings can only save MP3 or WAV files", Toast.LENGTH_LONG).show();
				return;
			}
		}
		
		Log.v("FileName",""+fileName);
		dialog.dismiss();
		Log.v("Dismissing","Dismissed event");
		Intent intent = new Intent(this,FileBuilderService.class);
		Messenger messenger = new Messenger(handler);
		Bundle data = new Bundle();
		data.putSerializable("DATA", eventLogs);
		intent.putExtra("DURATION", (endTime - startTime));
		intent.putExtra("FILENAME",fileName+".wav");
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("DATA", data);
		startService(intent);
	}

}
