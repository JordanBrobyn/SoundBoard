package my.SoundBoard;
/*Date: 04/03/2012
 * Author:Jordan Brobyn
 * Description: Individual sound set up.
 */
import java.io.IOException;

import android.media.MediaPlayer;
import android.util.Log;
import android.widget.ImageView;

public class SoundInfo {

	String id;
	String song;
	int status;
	int pause; // Icons for pause and play
	int play; // Icons for pause and play
	int playType;
	int start;
	int stop;
	ImageView view;
	MediaPlayer mp;
	long time;
	boolean just_ON;
	boolean repeat = false;
	boolean paused = false;
	
	public SoundInfo(String _id, String _song, int _status, int _playType,int _pause,int _play) {
		id = _id;
		song = _song;
		status = _status;
		playType = _playType;
		mp = new MediaPlayer();
		pause = _pause;
		play = _play;
	}
	
	public void setRepeat(boolean _repeat){
		repeat = _repeat;
	}
	
	public boolean getRepeat(){
		return repeat;
	}
	
	public void setTime(long _time){
		time = _time;
	}
	
	public long getTime(){
		return time;
	}
	
	public void setPaused(boolean pause){
		paused = pause;
	}
	
	public boolean isPaused(){
		return paused;
	}
	
	public void setOn(){
		just_ON = true;
	}
	
	public void setOff(){
		just_ON = false;
	}
	
	public boolean getState(){
		return just_ON;
	}
	
	public MediaPlayer returnMp(){
		return mp;
	}
	public void changeMpState(MediaPlayer _mp){
		mp = _mp;
	}

}
