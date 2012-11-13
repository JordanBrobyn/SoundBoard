package my.SoundBoard;
/*Date: 04/03/2012
 * Author:Jordan Brobyn
 * Description: Main Activity display to orient the user
 */
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
//import com.google.ads.*;
//import com.bugsense.trace.BugSenseHandler;


public class SoundBoardActivity extends Activity implements OnTouchListener{
    /** Called when the activity is first created. */
	boolean customTitleSupported;
	
	ImageView start;
	ImageView edit;
	ImageView about;
	ImageView snip;
	ImageView record;
	private static final String TAG = "Touch" ;
	Vibrator vib;
	//API Key d1cefcd7
	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	       // BugSenseHandler.setup(this, "d1cefcd7");
	        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	        
	        //Setting touch listeners for each image on the home screen
	        start = (ImageView) findViewById(R.id.Start);
	        start.setOnTouchListener(this);
	        
	        edit = (ImageView) findViewById(R.id.Edit);
	        edit.setOnTouchListener(this);
	        
	        about = (ImageView) findViewById(R.id.About);
	        about.setOnTouchListener(this);
	        
	        snip = (ImageView) findViewById(R.id.Snipper);
	        snip.setOnTouchListener(this);
	         
	        record = (ImageView) findViewById(R.id.recorder);
	        record.setOnTouchListener(this);
	        vib = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
	      // AdView adView = (AdView)findViewById(R.id.adView1);

	       // AdRequest adRequest = new AdRequest();
	       // adRequest.setTesting(true);
	        
	       // adView.loadAd(adRequest);

	}
	
	public void edit_Mode(View v){ //Allow users to edit songs that each button holds
		Intent intent = new Intent(getApplicationContext(), EditActivity.class);
		vib.vibrate(30);
		startActivity(intent);
		
	}
	public void start(View v){ //Start playing
		Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
		vib.vibrate(30);
		startActivity(intent);
		
	}
	public void record(View v){//Allows the user to record voice or other audio through the mic.
		final Dialog dialog = new Dialog(SoundBoardActivity.this);
		dialog.setContentView(R.layout.recorder);
		dialog.setTitle("Recorder");
		dialog.setCancelable(true);
		dialog.show();
		Recorder newRecorder = new Recorder();
		View view = dialog.findViewById(R.id.recorderView);
		
		 
		// Vibrate for 300 milliseconds
		vib.vibrate(30);
		newRecorder.tonCreate(view,vib);//Call the recorder functions
		
	}
	public void about(View v){//Information about the application
		final Dialog dialog = new Dialog(SoundBoardActivity.this);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("About Custom SoundBoard");
        dialog.setCancelable(true);
        Button button = (Button) dialog.findViewById(R.id.Button01);
        button.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
                dialog.cancel();
            }
        });
        vib.vibrate(30);
        dialog.show();
	}
	@Override
	public void onBackPressed(){
		System.gc();
		System.runFinalizersOnExit(true);
		moveTaskToBack(false);
		finish();
	}
	public void snip_it(View v){
		Intent intent = new Intent(getApplicationContext(), SnipperActivity.class);
		vib.vibrate(30);
		startActivity(intent);
		
	}

	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView)v;
		dumpEvent(event);
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			if(v.getId() == R.id.About){
				view.setImageResource(R.drawable.about1);
			}else if(v.getId() == R.id.Start){
				view.setImageResource(R.drawable.start1);
			}else if(v.getId() == R.id.Edit){
				view.setImageResource(R.drawable.edit1);
			}else if(v.getId() == R.id.Snipper){
				view.setImageResource(R.drawable.snip1);
			}else if(v.getId() == R.id.recorder){
				view.setImageResource(R.drawable.recorder1);
			}

		}else if(event.getAction() == MotionEvent.ACTION_UP){
			if(v.getId() == R.id.About){
				view.setImageResource(R.drawable.about0);
				about(v);
			}else if(v.getId() == R.id.Start){
				view.setImageResource(R.drawable.start0);
				start(v);
			}else if(v.getId() == R.id.Edit){
				view.setImageResource(R.drawable.edit0);
				edit_Mode(v);
			}else if(v.getId() == R.id.Snipper){
				view.setImageResource(R.drawable.snip0);
				snip_it(v);
			}else if(v.getId() == R.id.recorder){
				view.setImageResource(R.drawable.recorder0);
				record(v);
			}
		}

		return true;
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
}