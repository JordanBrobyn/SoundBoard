package my.SoundBoard;
/*
 * Note: Start working on playing the songs in the time they are required to be played.
 * Finish getting pause working in Play.
 * Then work on getting the actual file being built.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import my.SoundBoard.ClipEvent.EventType;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ReplayTracks implements Runnable, OnCompletionListener, OnTouchListener,  OnDismissListener {

	ImageView play;
	Button save;
	View active;
	ArrayList<ClipEvent> clipEvnts;
	ProgressBar mSeekBar;
	Map<String,SoundInfo> hash;
	Dialog dialog;
	long time;
	Thread runner;
	boolean playing = false;
	
	public void setup(View view, ArrayList<ClipEvent> clipEvnt,long duration, Map<String,SoundInfo> _hash,Dialog _dialog){
		
		play = (ImageView) view.findViewById(R.id.playerImage);
		play.setOnTouchListener(this);
		//save = (Button) view.findViewById(R.id.replaySave);
		//save.setOnClickListener(this);
		active = view;
		clipEvnts = clipEvnt;
		hash = _hash;
		time = duration;
		dialog = _dialog;
		dialog.setOnDismissListener(this);
	}
	
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		if(arg1.getAction() == MotionEvent.ACTION_DOWN){
			if(playing == false){
				runner = new Thread(this);
				runner.start();
				playing = true;
			}else{
				playing = false;
			}
		}
		
		return true;
	}

	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	public void run() {

		Log.v("Thread","Running");
		long startTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
		int index = 0;
		int secondCheck = 0;
		long currentTime = 0;
		
		/*
		 * This while loop is used to allow the user to playback
		 * their recently used audio clips to hear it for themselves.
		 * 
		 * Currently does not support pausing, only stop and play
		 */
		
		if(clipEvnts.size() == 0){
			playing = false;
			return;
		}
		
		while(playing == true){
			
			 currentTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
			
			if(currentTime - startTime >= clipEvnts.get(index).getTime()){
				try{
					performAction(clipEvnts.get(index));
				}
				catch (IOException e){
					Log.v("Error","Could not perform action");
				}
				secondCheck = index + 1;
				index++;
				
				while(secondCheck < clipEvnts.size()){
					currentTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
					
					if(currentTime - startTime >= clipEvnts.get(secondCheck).getTime()){
						try{
							performAction(clipEvnts.get(secondCheck));
						}
						catch (IOException e){
							Log.v("Error","Could not perform action");
						}
						secondCheck++;
					}else{
						index = secondCheck;
						break;
					}
				}
					
			}
			
			if (index >= clipEvnts.size() || currentTime - startTime >= time){
				Log.v("Thread","Finished Thread");
				break;
			}
					
			
		}
		stopAll(false);
		
		playing = false;
		Log.v("Thread","Stopped");
	}
	
	public void performAction(ClipEvent event) throws IOException{
		
		SoundInfo info = hash.get(event.buttonName);
		MediaPlayer mp = info.returnMp();
		
		Log.v("Thread","About to perform action "+event.type +"on "+event.buttonName);
		switch(event.type){
		
			case PLAY:
				mp.setDataSource(info.song);
				mp.setOnCompletionListener(this);
				mp.prepare();
				mp.setVolume((float) event.volume/100, (float)event.volume/100);
				mp.seekTo((int)event.start);
				mp.start();
				break;
			case STOP:
				mp.stop();
				mp.reset();
				break;
		}
		
	}

	/*public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Log.v("CLick Event","Button Clicked");
		
		if(arg0.getId() == R.id.replaySave){
			playing = false;
			dialog.dismiss();
		}
		
	}*/
	
	//Stops all playing audio associated with the hashmap.
	public void stopAll(boolean leaving){
		
		Collection<SoundInfo> c = hash.values();
		Iterator<SoundInfo> it = c.iterator();
	    MediaPlayer mp;
	    SoundInfo info;
        
	    while(it.hasNext()){  //Remove mediaplayer data to release memory. This is not always done automatically      
           info = (SoundInfo) it.next();
           mp = info.returnMp();
           
           if(mp.isPlaying()){
        	    mp.stop();   
           }
           if(leaving == true)
        	   mp.release();
           else
        	   mp.reset();
        }
	}

	public void onDismiss(DialogInterface arg0) {
		// TODO Auto-generated method stub
		playing = false;
	}

}
