package my.SoundBoard;
/*
 * Remember to create a layout for SDK lower than 11.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;


import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class FileBuilderService extends IntentService {

	private int result = Activity.RESULT_CANCELED;
	private NotificationManager mgr = null;
	private static final int NOTIFY_ID = new Random().nextInt();
	static String folderLocation =Environment.getExternalStorageDirectory().getPath()+"/Custom SoundClips/";
	boolean building = true;
	Notification note;
	Notification.Builder builder;
	
	long totalBytes = 0;
	long currentBytesWritten = 0;
	
	static int bytesPerMillisecond = (2 * 44100 * 16 / 8)/1000;
	int BYTESIZE = 4194304;
	int BUFFSIZE = BYTESIZE/2; //4MB should be good enough for the standard buffer
	long BUFFSIZEMS = BUFFSIZE/bytesPerMillisecond;
	static int HEADERSIZE = 44;
	
	public FileBuilderService() {
		super("FileBuilderService");
		
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		
		Log.v("Version",""+Build.VERSION.SDK_INT);
		
		
		result = Activity.RESULT_OK;
		
		Bundle extras = intent.getExtras();
		if(extras != null){
			Messenger messenger = (Messenger) extras.get("MESSENGER");
			final int duration = (int) extras.getLong("DURATION");
			final String fileName = extras.getString("FILENAME");
			Bundle data = extras.getBundle("DATA");
			final ArrayList<ClipEvent> eventLogs = (ArrayList<ClipEvent>) data.getSerializable("DATA");
			//ArrayList<ClipEvent> events = (ArrayList<ClipEvent>) extras.get("Sounds");
			Message msg = Message.obtain();
			msg.arg1 = result;
			msg.obj = "Path results";
			

			//Find out how many MB need to be processed before completion
			//This will help the user visually see how much progress has been achieved
			for(int p = 0; p < eventLogs.size(); p++){
				
				if(eventLogs.get(p).type == ClipEvent.EventType.PLAY){
					for(int j = p; j< eventLogs.size(); j++){
						if(eventLogs.get(j).type == ClipEvent.EventType.STOP &&
								eventLogs.get(j).buttonName.equals(eventLogs.get(p).buttonName)){
							totalBytes = totalBytes + (eventLogs.get(j).stop - eventLogs.get(p).start)*bytesPerMillisecond;
							break;
						}
					}
				}
				
			}

			//Begin creating the mixed file
			new Thread(new Runnable() {
	            public void run() {
	            	
	            	
	            	int clipCount = 0;
	    			
	    			for(ClipEvent clip : eventLogs){
	    				if(clip.type == ClipEvent.EventType.PLAY){
	    					clipCount++;
	    				}
	    			}
	            	
	            	int i = 0;
	            	updateProgress(0,"Preparing file "+fileName);
	            	boolean success = prepareFile(duration,fileName);
	            	
	            	if(success == false){
	            		updateProgress(100,"Failed to prepare file!");
	            		building = false;
	            		stopSelf();
	            		return;
	            	}
	            	
	            	updateProgress(1,"Mixing tracks...");
	            	
	            	int startEventIndex = 0;
	            	int endEventIndex = 0;
	            	
	            	long mainFileTime = 0;
	            	long startSecondaryFileTime = 0;
	            	long endSecondaryFileTime = 0;
	            	int secondaryFileVolume = 50;
	            	String secondFileLocation;
	            	
	                while (building == true) {
	                    
	                	//Find the next Play event
	                	//If all play events are gone, we have finished mixing tracks
	                	while(eventLogs.get(startEventIndex).type != ClipEvent.EventType.PLAY){
	                		
	                		startEventIndex++;
	                		if(startEventIndex >= eventLogs.size()){
	                			building = false;
	                			break;
	                		}
	                	}
	                	if(building == false){
	                		i=100;
	                		break;
	                	}
	                	endEventIndex = startEventIndex;
	                	//Find related Stop event
	                	for(endEventIndex = startEventIndex; endEventIndex < eventLogs.size(); endEventIndex++){
	                		if(eventLogs.get(endEventIndex).type == ClipEvent.EventType.STOP){
	                			if(eventLogs.get(endEventIndex).buttonName.equalsIgnoreCase(eventLogs.get(startEventIndex).buttonName))
	                				break;
	                		}
	                	}
	                	
	                	//Get important information before getting data from primary and secondary file
	                	mainFileTime = eventLogs.get(startEventIndex).time;
	                	startSecondaryFileTime = eventLogs.get(startEventIndex).start;
	                	endSecondaryFileTime = eventLogs.get(endEventIndex).stop;
	                	secondaryFileVolume = eventLogs.get(startEventIndex).volume;
	                	secondFileLocation = eventLogs.get(startEventIndex).fileLocation;
	                	
	                	try {
							mixFiles(fileName,mainFileTime,secondFileLocation,startSecondaryFileTime,endSecondaryFileTime,secondaryFileVolume);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                	
	                	startEventIndex++;
	                	if(startEventIndex >= eventLogs.size()){
                			building = false;
                			break;
                		}
	                	i++;
	                }
	                try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                updateProgress(i,"Completed Mixing");
	                stopSelf();
	            }
	        }).start();
			
			
			try{
				messenger.send(msg);
			}catch(android.os.RemoteException e1){
				Log.v("Message Event","Error sending message");
			}
		}
		
	}
	

	protected void mixFiles(String fileName, long mainFileTimeMS,
			String secondFileLocation, long startSecondaryFileTimeMS,
			long endSecondaryFileTimeMS, int secondaryFileVolume) throws IOException {
		// TODO Auto-generated method stub
		File mainFile = new File(folderLocation+fileName);
		RandomAccessFile mainFileRAF = new RandomAccessFile(mainFile,"rw");
		File clipFile = new File(secondFileLocation);
		
		long totalTimeRemaining = endSecondaryFileTimeMS - startSecondaryFileTimeMS;
		long duration = totalTimeRemaining;
		long currentMainTime = mainFileTimeMS;
		long currentSecondaryTime = startSecondaryFileTimeMS;
		
		short[] clipData;
		short[] primaryData;
		
		while(totalTimeRemaining > 0){
			//Read clipFile into a buffer to append to the primary file
			
			long size = (endSecondaryFileTimeMS - currentSecondaryTime)*bytesPerMillisecond;
			
			if(size < BUFFSIZE){
				//Get left over PCM data
				
				//Determine if the file in question is a WAV or MP3 file
				if(secondFileLocation.contains(".mp3"))
					clipData = getMP3PCM(secondFileLocation, currentSecondaryTime, endSecondaryFileTimeMS);
				else
					clipData = getWAVPCM(secondFileLocation,currentSecondaryTime,endSecondaryFileTimeMS);
				
				//Combine the clip with the primary file
				primaryData = getWAVPCM(mainFileRAF,currentMainTime,mainFileTimeMS+duration);
				
				//Combine the clip with the primary file
				primaryData = addPCM(primaryData,clipData,secondaryFileVolume);
				currentBytesWritten = currentBytesWritten+(primaryData.length*2);
				
				//Remix the combined data
				writePCMToWAV(mainFileRAF,currentMainTime,mainFileTimeMS+duration,primaryData);
				
				double completion = ((double)currentBytesWritten/totalBytes)*100;
				updateProgress((int)completion,null);
				currentMainTime = currentMainTime+duration;
				totalTimeRemaining = 0;
				mainFileRAF.close();
			}else{
				//Get BUFFSIZE of short data
				
				//Determine if the file in question is a WAV or MP3 file
				if(secondFileLocation.contains(".mp3"))
					clipData = getMP3PCM(secondFileLocation, currentSecondaryTime, currentSecondaryTime+BUFFSIZEMS);
				else
					clipData = getWAVPCM(secondFileLocation, currentSecondaryTime, currentSecondaryTime+BUFFSIZEMS);
				
				//grab the section of data from the primary file to mix on top
				primaryData = getWAVPCM(mainFileRAF,currentMainTime,currentMainTime+BUFFSIZEMS);
				
				//Combine the clip with the primary file
				primaryData = addPCM(primaryData,clipData,secondaryFileVolume);
				currentBytesWritten = currentBytesWritten+(primaryData.length*2);
				//Remix the combined data
				writePCMToWAV(mainFileRAF,currentMainTime,currentMainTime+BUFFSIZEMS,primaryData);
				
				double completion = ((double)currentBytesWritten/totalBytes)*100;
				updateProgress((int)completion,null);
				currentSecondaryTime = currentSecondaryTime+BUFFSIZEMS;
				currentMainTime = currentMainTime+BUFFSIZEMS;
			}
			
			
		}
		
		
	}

	public void updateProgress(int progress , String text){
		
		
		boolean onGoing = true;
		if(progress >= 100)
			onGoing = false;
		
		if(text != null){
			if(Build.VERSION.SDK_INT >= 11){
				builder = new Notification.Builder(this);
				builder.setContentTitle("AudioMashup Blending")
					.setContentText(text)
					.setOngoing(onGoing)
					.setSmallIcon(R.drawable.soundboardicon)
					.setProgress(100, progress, false);
					
				note = builder.getNotification();
				
			}else{
				note = new Notification(R.drawable.soundboardicon,text,System.currentTimeMillis());
				note.flags = note.flags | Notification.FLAG_ONGOING_EVENT;
			}
		}
		else {
			if(Build.VERSION.SDK_INT >= 11){
				builder.setOngoing(onGoing)
					.setProgress(100, progress, false);
					
				note = builder.getNotification();
				
			}else{
				note = new Notification(R.drawable.soundboardicon,"Mixing recorded tracks..",System.currentTimeMillis());
				note.flags = note.flags | Notification.FLAG_ONGOING_EVENT;
			}
		}
		
		mgr.notify(NOTIFY_ID, note);
	}
	
	//Combines two short arrays of PCM data at the new data volume
	public static short[] addPCM (short[] mainData, short[] newData, int volume){
		
		int flow = 0;
		int j = 0;
		
		int length = 0;
		
		if(mainData.length > newData.length)
			length = newData.length;
		else
			length = mainData.length;
		
		
		double percentage = (double)volume/100;
		
		for(int i = 0; i< length; i++){
			
		    flow = (int) (mainData[i] + ((double)newData[j]*percentage));
            if(flow > 32767){
                flow = 32767;
            }
            else if (flow < -32768){
                flow = -32768;
            }
            mainData[i] = (short) flow;
            j++;
		}
		
		
		return mainData;
		
	}

	public static void writePCMToWAV(RandomAccessFile raf, long start_MS,long end_MS,short[] primaryData){
		
		try {
			byte[] rawBytes = shortsToBytes(primaryData);
			raf.seek(HEADERSIZE+(start_MS*bytesPerMillisecond));
			raf.write(rawBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Get PCM data from the primary file while holding onto the file pointer
	//Return a short array of the selected section
	public static short[] getWAVPCM(RandomAccessFile raf, long start_MS, long end_MS){
		int byteBuffer = (int) ((end_MS - start_MS)*bytesPerMillisecond);
		byte[] rawDATA = new byte[byteBuffer];
		
		try {
			raf.seek((long) (HEADERSIZE+start_MS*bytesPerMillisecond));
			raf.read(rawDATA);
			short[] pcmData = bytesToShorts(rawDATA);
			
			return pcmData;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	//Get PCM data from the secondary clip.
	//Overloaded function that takes the file location instead of a RandomAccessFile pointer
	public static short[] getWAVPCM(String file, long start_MS, long end_MS){
		
		int byteBuffer = (int) ((end_MS - start_MS)*bytesPerMillisecond);
		byte[] rawDATA = new byte[byteBuffer];
		
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(file),"rw");
			raf.seek((long) (HEADERSIZE+start_MS*bytesPerMillisecond));
			raf.read(rawDATA);
			short[] pcmData = bytesToShorts(rawDATA);
			raf.close();
			return pcmData;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/*
	 * Returns pcm data between a selected number of milliseconds
	 * into a short array
	 */
	public static short[] getMP3PCM(String file, float start_MS, float end_MS){
			
		//Open the mp3 file
		InputStream mp3Stream = null;
		int frameCount = 0;
		CheapMP3 mp3 = new CheapMP3();
		
		try {
			
			mp3.ReadFile(new File(file));
			mp3Stream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		
		//Library used to get Raw PCM data from mp3 stream
		Decoder d = new Decoder();
		Bitstream stream = new Bitstream(mp3Stream);
		
		int frame = 0;
		frameCount = mp3.getNumFrames();
		//Begin getting PCM data from mp3 frames
		//The goal is to only collect frames we need to be added to eachother
		
		
		short[] fullList = null;
		
		try {
			
			
			Header header = stream.readFrame();
			
			float frameMS = header.ms_per_frame();
			
			int frame_Start = (int)(start_MS/frameMS); // Determine the frame at which we want to start converting
			int frame_End = (int)(end_MS/frameMS); // Determine the end frame at which we want to stop converting
			
			for ( ; frame < frameCount; frame++){
				
				if(header == null || frame > frame_End)
					break;
								
				if(frame >= frame_Start && frame < frame_End){
					SampleBuffer decoderOutput = (SampleBuffer)d.decodeFrame(header, stream);
					short[] pcm = decoderOutput.getBuffer();
					fullList = append(fullList,pcm);
				}
				
				stream.closeFrame();
				header = stream.readFrame();
			}
			stream.close();
		} catch (BitstreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JavaLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fullList;
	}
	
	public static short[] append(short[] a, short[] b){
		
		if(a == null)
			return b;
		else{
			short[] newShort = new short[a.length + b.length];
			newShort = Arrays.copyOf(a, a.length + b.length);
			int length = a.length + b.length;
			int indexB = 0;
			
			for(int i =a.length; i < length; i++){
				newShort[i] = b[indexB];
				indexB++;
			}
			
			return newShort;
		}

	}
	
	public static byte[] toByteArray(char[] array) {
	    return toByteArray(array, Charset.defaultCharset());
	}

	public static byte[] toByteArray(char[] array, Charset charset) {
	    CharBuffer cbuf = CharBuffer.wrap(array);
	    ByteBuffer bbuf = charset.encode(cbuf);
	    return bbuf.array();
	}
	
	/*Creates a WAV file from PCM data
	 * This should be possibly considered to be done differently.
	 * Currently this only writes files of a specific freq as well as other hardcoded info
	 * Consider changing this.
	 * 
	 * Also consider making this function write portions instead of the whole thing at once
	 */
	static public boolean prepareFile(int duration, String file){

		
		try {

			OutputStream out = new FileOutputStream(folderLocation+file);
			
			
			writeId(out, "RIFF");
	        writeInt(out, 36 + bytesPerMillisecond*duration);
	        writeId(out, "WAVE");

	        /* fmt chunk */
	        writeId(out, "fmt ");
	        writeInt(out, 16);
	        writeShort(out, (short) 1);
	        writeShort(out, (short) 2);
	        writeInt(out, 44100);
	        writeInt(out, 2 * 44100 * 16 / 8);
	        writeShort(out, (short)(2 * 16 / 8));
	        writeShort(out, (short) 16);

	        /* data chunk */
	        writeId(out, "data");
	        writeInt(out, bytesPerMillisecond*duration);
	        
	        int bytesLeft = bytesPerMillisecond*duration;
	        byte[] bytes = new byte[4194304];
	        int writeAmount = 4194304;
	        
	        while(bytesLeft != 0){
	        	
	        	if(bytesLeft < 4194304){
	        		writeAmount = bytesLeft;
	        		bytes = new byte[writeAmount];
	        	}
	        	
	        	out.write(bytes);
	        	
	        	bytesLeft = bytesLeft - writeAmount;
	        	//Log.v("Bytes", " Bytes left  = "+ bytesLeft);
	        }
	        
	        return true;
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		} catch (IOException e2){
			e2.printStackTrace();
			return false;
		}
		
		
	}
	
	private static void writeId(OutputStream out, String id) throws IOException {
        for (int i = 0; i < id.length(); i++) out.write(id.charAt(i));
    }

    private static void writeInt(OutputStream out, int val) throws IOException {
        out.write(val >> 0);
        out.write(val >> 8);
        out.write(val >> 16);
        out.write(val >> 24);
    }

    private static void writeShort(OutputStream out, short val) throws IOException {
        out.write(val >> 0);
        out.write(val >> 8);
    }
    
    public static byte[] shortsToBytes(short[] array) throws IOException{
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
		
		for (short s : array) {
            outStream.write(s & 0xff);
            outStream.write((s >> 8 ) & 0xff);
        }
		
		
		byte[] finalData = outStream.toByteArray();
		
		
		return finalData;
	}
    
    /*
	 * Converts a byte array into a short array BigEndian
	 * Do this to prevent more IO.
	 */
	public static short[] bytesToShorts(byte[] array){
		short[] shorts = new short[array.length/2];
		
		
		for(int i = 0; i < shorts.length; i++){
			shorts[i] = ( (short)( ( array[i*2] & 0xff )|( array[i*2 + 1] << 8 ) ) );
		}
		
		return shorts;
		
	}

}
