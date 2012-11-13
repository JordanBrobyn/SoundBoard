package my.SoundBoard;

/*Date: 04/03/2012
 * Author:Jordan Brobyn
 * Description: Activity to cut mp3 files
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class SnipperActivity extends Activity implements SeekBar.OnSeekBarChangeListener, Runnable{

	SeekBar mSeekBar;
	SeekBar mSeekBar2;
	SeekBar mSeekBar3;
    TextView mProgressText;
    TextView mTrackingText;
    MediaPlayer mp;
    int start_position;
    int end_position;
    String filePath;
    int start_Time;
    int end_Time;
    int song_Duration;
    Chronometer start;
    Chronometer end;
    ProgressDialog loading;
    Chronometer current;
    
    String save_Name;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snipper);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mSeekBar = (SeekBar)findViewById(R.id.seeker);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar2 = (SeekBar)findViewById(R.id.seeker2);
        mSeekBar2.setOnSeekBarChangeListener(this);
        mSeekBar3 = (SeekBar)findViewById(R.id.seeker3);
        mSeekBar3.setOnSeekBarChangeListener(this);
        start = (Chronometer) findViewById(R.id.chronoStart);
        end = (Chronometer) findViewById(R.id.chronoEnd);
        current = (Chronometer) findViewById(R.id.chronoCurrent);
        mp = new MediaPlayer();
        filePath = null;
        start_Time = 0;
        song_Duration = 0;
        

	}
	
	public void precision(View v){
		final int selection;
		int value = 0;
		if(v.getId() == R.id.precision1)
			value = 1;
		else
			value = 0;
		
		selection = value;
		
		final Dialog dialog = new Dialog(SnipperActivity.this);
        dialog.setContentView(R.layout.precision);
        dialog.setTitle("Set Exact Time");
        dialog.setCancelable(true);
        Button button = (Button) dialog.findViewById(R.id.submit);
        
        button.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
        		EditText minutes = (EditText)dialog.findViewById(R.id.minutes);
        		EditText seconds = (EditText)dialog.findViewById(R.id.seconds);
        		EditText milliseconds = (EditText)dialog.findViewById(R.id.milliseconds);
        		long min;
        		long sec;
        		long milli;
        		
        		if(minutes.getText().toString().equals(""))
        			min = 0;
        		else
        			min = Integer.parseInt(minutes.getText().toString());
        		if(seconds.getText().toString().equals(""))
        			sec = 0;
        		else
        			sec = Integer.parseInt(seconds.getText().toString());
        		if(milliseconds.getText().toString().equals(""))
        			milli = 0;
        		else
        			milli = Integer.parseInt(milliseconds.getText().toString());
        		
        		long totalTime = (min*60000)+(sec*1000)+milli;
        		
        		
        		if(selection == 1){
        			start.setText(min+":"+sec+":"+milli);
        			start_Time = (int) totalTime;
        			start_position = (int)totalTime;
        			mSeekBar.setProgress((int)totalTime);
        			if(start_Time > end_Time){ // reset end time to ensure it is > start
        				end.setText(min+":"+sec+":"+milli);
        				end_Time = (int) totalTime;
            			end_position = (int)totalTime;
            			mSeekBar2.setProgress((int)totalTime);
        			}
        		}else{
        			end.setText(min+":"+sec+":"+milli);
        			end_Time = (int) totalTime;
        			end_position = (int) totalTime;
        			mSeekBar2.setProgress((int)totalTime);
        			if(end_Time < start_Time){ // reset start time to be < end time 
        				start.setText(min+":"+sec+":"+milli);
        				start_Time = (int) totalTime;
            			start_position = (int)totalTime;
            			mSeekBar.setProgress((int)totalTime);
        			}
        		}
                dialog.cancel(); 
            }
        });
        dialog.show();	
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if(seekBar.getId() == mSeekBar.getId()){
        	start_position = progress;
        }else if (seekBar.getId() == mSeekBar2.getId()){
        	end_position = progress;
        }
    }

	
	@Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) // Return selected data from file browser
    {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null){
			String file = data.getStringExtra("file"); //Find selected file
			EditText file_Path = (EditText) findViewById(R.id.file_Path);
			String file_Name = file.substring(file.lastIndexOf("/")+1);
			file_Path.setText(file_Name);
			filePath = file;
			
			try {
				mp.setDataSource(file);
				mp.prepare();
				song_Duration = mp.getDuration();
				mSeekBar.setMax(song_Duration);
				mSeekBar2.setMax(song_Duration);
				mSeekBar3.setMax(song_Duration);
				mp.reset();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
 		
    }
	
	public void playSound(View v){
		if(filePath == null){
			Toast.makeText(getApplicationContext(),"Please Select a file first",Toast.LENGTH_LONG).show();
		}else{
			if(mp.isPlaying()){
				mp.stop();
				mp.reset();
				ImageButton ib = (ImageButton)findViewById(R.id.snipPlay);
				ib.setImageResource(R.drawable.play1);
			}else{
				try {
					
					mp.setDataSource(filePath);
					mp.prepare();
					mp.start();
					mp.seekTo(start_Time);
					mSeekBar3.setProgress(start_Time);
					Thread run = new Thread((Runnable)this);
					ImageButton ib = (ImageButton)findViewById(R.id.snipPlay);
					ib.setImageResource(R.drawable.pause1);
					run.start();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void startBrowseActivity(View v){ // File browser
		Intent intent = new Intent(getApplicationContext(),sdBrowser.class);
		startActivityForResult(intent,1);
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		if(seekBar.getId() == mSeekBar.getId()){
			int seconds = (int)(start_position/1000)%60;
			int minutes = (int)(start_position/1000/60);
			start.setText(minutes+":"+seconds);
			start_Time = start_position;
			
			if(start_Time > end_Time){
				end_Time = start_Time;
				end.setText(minutes+":"+seconds);
				mSeekBar2.setProgress(start_Time);
			}
			
			if(mp.isPlaying()){
				mp.seekTo(start_Time);
			}
		}else if(seekBar.getId() == mSeekBar2.getId()){
			int seconds = (int)(end_position/1000)%60;
			int minutes = (int)(end_position/1000/60);
			end.setText(minutes+":"+seconds);
			end_Time = end_position;
			
			if(end_Time < start_Time){
				start_Time = end_Time;
				start.setText(minutes+":"+seconds);
				mSeekBar.setProgress(end_Time);
				
				if(mp.isPlaying()){
					mp.seekTo(start_Time);
				}
			}
		}
	}
	
	private class saveMP3 extends AsyncTask<String, Void, String> {
		int value=0;
		@Override
		protected String doInBackground(String... params) {
			String result = null;
			mp3Writter writter = new mp3Writter();
			value = writter.save(filePath, start_Time, end_Time, song_Duration,save_Name);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			loading.dismiss();
			if(value == 1)
				Toast.makeText(getApplicationContext(),"File Snipping Success!",Toast.LENGTH_LONG).show();
			else
				Toast.makeText(getApplicationContext(),"Failed to read MP3!",Toast.LENGTH_LONG).show();
		}
	}
	
	public void save(View v){ // Save the file
		
		final Dialog dialog = new Dialog(SnipperActivity.this);
        dialog.setContentView(R.layout.savedialog);
        dialog.setTitle("Last Step!");
        dialog.setCancelable(true);
        Button button = (Button) dialog.findViewById(R.id.doneSave);
        button.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
        		EditText field = (EditText)dialog.findViewById(R.id.saveName2);
        		save_Name = null;
        		save_Name = field.getText().toString();
                dialog.cancel();
                saveMP3 save = new saveMP3(); // Async Task
        		loading = new ProgressDialog(SnipperActivity.this); //Show loading bar
        		loading.show();
        		save.execute(new String[] { "" });
            }
        });
        dialog.show();	
	}
	
	@Override
	public void onBackPressed(){ // Release audio data to not overflow memory
			
			mp.stop();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mp.reset();
			mp.release();
			finish();
	}
	
	public void run() {
		int currentPosition = 0;
        int total = mp.getDuration();
        int seconds;
		int minutes;
        while (mp.isPlaying() && currentPosition <= end_Time) {
        	mSeekBar3.setProgress(mp.getCurrentPosition());
        	seconds = (int)(currentPosition/1000)%60;
			minutes = (int)(currentPosition/1000/60);
        	//current.setText(minutes+":"+seconds);
        	runOnUiThread(new Runnable() {            
			    public void run() {
			    	int seconds = (int)(mp.getCurrentPosition()/1000)%60;
					int minutes = (int)(mp.getCurrentPosition()/1000/60);
			        //this will run on UI thread, so its safe to modify UI views.
			    	current.setText(minutes+":"+seconds);
			    }
			});
            try {
            	currentPosition = mp.getCurrentPosition();
                Thread.sleep(500); 
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
        }
        mp.stop();
        mp.reset();

	}
}
