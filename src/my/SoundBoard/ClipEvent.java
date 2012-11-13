package my.SoundBoard;

import java.io.Serializable;

import android.os.Parcel;

public class ClipEvent implements Serializable{

	
	
	public enum EventType{
		PAUSE,PLAY,STOP
	}
	
	EventType type;
	String buttonName;
	long time;
	String fileLocation;
	int volume=50;
	long stop;
	long start;
	
	public ClipEvent(String _button, EventType _type, long _time, String _file,int _volume , long startTime, long stopTime){
		this.type = _type;
		this.buttonName = _button;
		this.time = _time;
		this.fileLocation = _file;
		this.volume = _volume;
		this.start = startTime;
		this.stop = stopTime;
	}
	
	@Override
	public String toString() {
		return "ClipEvent [buttonName=" + buttonName + ", type=" + type
				+ ", time=" + time + ", volume=" + volume + ", start=" + start
				+ ", stop=" + stop + ", fileLocation=" + fileLocation + "]";
	}

	public long getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}

	public long getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public EventType getType() {
		return type;
	}
	public void setType(EventType type) {
		this.type = type;
	}
	public String getButtonName() {
		return buttonName;
	}
	public void setButtonName(String buttonName) {
		this.buttonName = buttonName;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getFileLocation() {
		return fileLocation;
	}
	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}
	
	
}
