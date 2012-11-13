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

import android.os.Environment;
import android.util.Log;

public class mp3Writter{

	String path;
	int start;
	int finish;
	int duration;
	String save_Name;
	
	public int save(String _path,int _start, int _finish, int _duration, String Name){
		path = _path;
		start = _start;
		finish = _finish;
		duration = _duration;
		save_Name = Name;
		try {
			int value = process_MP3();
			return value;
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
		
		int BPM = (int) (length/duration);
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

}
