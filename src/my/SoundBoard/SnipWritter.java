package my.SoundBoard;
/*Date: 04/03/2012
 * Author:Jordan Brobyn
 * Description: Uses the snipping created by the user to save this portion of the mp3 to a new file
 * The file is then saved to Custom SoundClips
 * 
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.os.Environment;
import android.util.Log;

public class SnipWritter{

	String path;
	int start;
	int finish;
	int duration;
	String save_Name;
	
	static int chunkSize = 16;
	static int channels = 2;
	static int sampleRate = 44100;
	static int bytesPerMillisecond = (channels * sampleRate * chunkSize / 8)/1000;
	int BYTESIZE = 4194304;
	int BUFFSIZE = BYTESIZE/2; //4MB should be good enough for the standard buffer
	long BUFFSIZEMS = BUFFSIZE/bytesPerMillisecond;
	static int HEADERSIZE = 44;
	
	public int save(String _path,int _start, int _finish, int _duration, String Name){
		path = _path;
		start = _start;
		finish = _finish;
		duration = _duration;
		save_Name = Name;
		try {
			if(path.toString().endsWith(".mp3")){
				int value = process_MP3();
				return value;
			}else if(path.toString().endsWith(".wav")){
				int value = process_WAV();
				return value;
			}else
				return 0;
			
		} catch (Exception e) {
			e.printStackTrace();
			return 0; // Failed
		}
	}
	
	public int process_MP3() throws IOException{
				
		File file = new File(path);
		long length;
		byte[] mp3; //Store the mp3 file to byte array
		FileInputStream is = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(is);
		
		if(!file.toString().endsWith(".mp3")){
			bis.close();
			return 0; // Failed
		}
		
		length = file.length();
		
		int BPM = (int) ((length-255+32)/duration);
		int bytes_S = start*BPM+255+32;
		int bytes_E = finish*BPM+255+32;
		
		int new_Size = bytes_E - bytes_S;
		mp3 = new byte[BPM];
		File new_dir = new File(Environment.getExternalStorageDirectory().getPath()+"/Custom SoundClips/");
		new_dir.mkdir();
		if(save_Name.equals("")){
			save_Name = "test.mp3";
		}else{
			if(save_Name.contains(".")){
				save_Name = save_Name.substring(0,save_Name.lastIndexOf("."));
			}
			save_Name = save_Name+".mp3";
		}
		File file2 = new File(new_dir,save_Name);
		FileOutputStream os = new FileOutputStream(file2);
		
		bis.skip(bytes_S); //Start reading the mp3 file from this point
		
		while(bis.available() > (length-bytes_E)){
			
			if(bis.available() < BPM){
				mp3 = new byte[bis.available()];
			}
			
			bis.read(mp3);
			os.write(mp3);
		}
		bis.close();
		is.close();//Close input stream
		os.write(mp3);
		os.close();
		
		return 1;//Success
	}
	
	public int process_WAV() throws IOException{
		
		File soundFile = new File(path);
		FileInputStream is = new FileInputStream(soundFile);
		BufferedInputStream bis = new BufferedInputStream(is);
		
		int length = (int) soundFile.length();
		
		File new_dir = new File(Environment.getExternalStorageDirectory().getPath()+"/Custom SoundClips/");
		new_dir.mkdir();
		
		if(save_Name.equals("")){
			save_Name = "test.wav";
		}else{
			if(save_Name.contains(".")){
				save_Name = save_Name.substring(0,save_Name.lastIndexOf("."));
			}
			save_Name = save_Name+".wav";
		}
		File newFile = new File(Environment.getExternalStorageDirectory().getPath()+"/Custom SoundClips/"+save_Name);
		//Creates a file with the proper header information
		FileBuilderService.prepareFile(finish-start, save_Name);
		
		
		int bytes_S = bytesPerMillisecond*start+HEADERSIZE;
		int bytes_E = bytesPerMillisecond*finish+HEADERSIZE;
		
		//Skip to where we need data
		bis.skip(bytes_S);
		
		RandomAccessFile ra = new RandomAccessFile(newFile,"rw");
		ra.seek(HEADERSIZE);
		int buffer = 1048576;
		
		byte[] wav = new byte[buffer];
		
		//Read and then write portions of the original file to the new clipped file
		while(bis.available() > (length-bytes_E)){
			
			if(bis.available() < buffer){
				wav = new byte[bis.available()];
			}
			
			bis.read(wav);
			ra.write(wav);
		}
		
		bis.close();
		is.close();
		ra.close();
		
		return 1;
	}

}
