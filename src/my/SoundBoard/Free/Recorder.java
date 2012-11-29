package my.SoundBoard.Free;
import java.io.IOException;

import my.SoundBoard.Free.R;

import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Recorder implements Runnable, OnCompletionListener, OnTouchListener {

	int recording = 0;
	int buffersize;
	Chronometer start;
	ProgressBar mSeekBar;
	MediaPlayer mp;
	int progress;
	RehearsalAudioRecorder RAR;
	String filename;
	
	ImageView Player;
	ImageView Recorder;
	
	EditText textView;
	View active;
	Vibrator vib;
	
	public void tonCreate(View dialog,Vibrator _vib) { //Prepare the recorder

        RAR = new RehearsalAudioRecorder(true, MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);
        start = (Chronometer) dialog.findViewById(R.id.chronoTime);
        mSeekBar = (ProgressBar) dialog.findViewById(R.id.progressBar1);
        
        Player = (ImageView) dialog.findViewById(R.id.player2);
        Player.setOnTouchListener(this);
        
        Recorder = (ImageView) dialog.findViewById(R.id.recorder2);
        Recorder.setOnTouchListener(this);
        
        textView = (EditText)dialog.findViewById(R.id.recordText);
        active = dialog;
        vib = _vib;
	}
	
	public void record(View v){ //Record the users voice
		filename = textView.getText().toString();
		ImageView view = (ImageView)v ;
		if(recording == 0){
			start.setBase(SystemClock.elapsedRealtime());
			Toast.makeText(active.getContext(), "Now Recording!", Toast.LENGTH_SHORT).show();
			RAR.setOutputFile(Environment.getExternalStorageDirectory().getPath()+"/Custom SoundClips/"+filename+".wav");
			RAR.prepare();
			RAR.start();
			recording = 1;
			start.start();
			view.setImageResource(R.drawable.record3);
		}else{
			RAR.stop();
			RAR.reset();
			RAR.release();
			prepareRAR();
			recording = 0;
			start.stop();
			view.setImageResource(R.drawable.record2);
			Toast.makeText(active.getContext(), "Recording Saved to (Custom SoundClips)", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void replay(View v){ //Allows the user to replay the recording with the name requested
		
		filename = textView.getText().toString();
		
		if(mp.isPlaying()){
			mp.stop();
			mp.reset();
			v.setBackgroundResource(R.drawable.pause1);
		}else{
			try {
				
				mp.setDataSource(Environment.getExternalStorageDirectory().getPath()+"/Custom SoundClips/"+filename+".wav");
				mp.prepare();
				mSeekBar.setMax(mp.getDuration());
				mp.start();
				mSeekBar.setProgress(0);
				v.setBackgroundResource(R.drawable.play1);
				Thread run = new Thread((Runnable)this);
				run.start();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void prepareRAR(){
		RAR = new RehearsalAudioRecorder(true, MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
	}
	
	public void run() {
		int currentPosition = 0;
        int total = mp.getDuration();
        while (mp.isPlaying()) {
        	mSeekBar.setProgress(mp.getCurrentPosition());
        	
            try {
            	currentPosition = mp.getCurrentPosition();
                Thread.sleep(500); 
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
        }

	}

	public void onCompletion(MediaPlayer arg0) {
		mp.stop();
		mp.reset();
		ImageView view = (ImageView)active.findViewById(R.id.player2);
		view.setBackgroundResource(R.drawable.pause1);
	}

	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			if(v.getId() == R.id.player2){
				vib.vibrate(20);
				replay(v);
			}else if(v.getId() == R.id.recorder2){
				vib.vibrate(20);
				record(v);
			}
		}
		return true;
	}
}
