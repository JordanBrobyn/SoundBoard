package my.SoundBoard.Free;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.os.Environment;


public class FileAccessor {

	
	static String folderLocation =Environment.getExternalStorageDirectory().getPath()+"Custom SoundClips/";
	
	static public byte[] openFile(String file){
		
		RandomAccessFile f;
		byte[] contents = null;
		try {
			
			f = new RandomAccessFile(file,"r");
			contents = new byte[(int)f.length()];
			f.read(contents);
			f.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return contents;
	}
	
	/*Creates a WAV file from PCM data
	 * This should be possibly considered to be done differently.
	 * Currently this only writes files of a specific freq as well as other hardcoded info
	 * Consider changing this.
	 * 
	 * Also consider making this function write portions instead of the whole thing at once
	 */
	static public void writeFile(short[] data, String file){
		//RandomAccessFile f;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
		try {
			
			for (short s : data) {
	            outStream.write(s & 0xff);
	            outStream.write((s >> 8 ) & 0xff);
	        }
			
			
			byte[] finalData = outStream.toByteArray();
			OutputStream out = new FileOutputStream(file);
			
			
			writeId(out, "RIFF");
	        writeInt(out, 36 + finalData.length);
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
	        writeInt(out, finalData.length);
	        out.write(finalData);
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2){
			e2.printStackTrace();
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
}
