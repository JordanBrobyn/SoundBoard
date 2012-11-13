package my.SoundBoard;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
public class prepareDrums {

	Map<String,SoundInfo> prepareDrums(Map<String,SoundInfo> hash, Context context){
		
		AssetManager Am = context.getAssets();
		SoundInfo info;
		info = new SoundInfo("Button31","drums/cymbal5.wav",0,0,R.drawable.cymbol0,R.drawable.cymbol1);
		hash.put("Button31", info);
		
		info = new SoundInfo("Button32","drums/hihat20.wav",0,0,R.drawable.hihat0,R.drawable.hihat1);
		hash.put("Button32", info);

		info = new SoundInfo("Button33","drums/cymbal3.wav",0,0,R.drawable.cymbol0,R.drawable.cymbol1);
		hash.put("Button33", info);

		info = new SoundInfo("Button34","drums/snare.mp3",0,0,R.drawable.snare0,R.drawable.snare1);
		hash.put("Button34", info);
		
		info = new SoundInfo("Button35","drums/snaredrum79.wav",0,0,R.drawable.snare0,R.drawable.snare1);
		hash.put("Button35", info);
		
		info = new SoundInfo("Button36","drums/tomtomdrum5.wav",0,0,R.drawable.tomtom0,R.drawable.tomtom1);
		hash.put("Button36", info);
		
		info = new SoundInfo("Button37","drums/tomtomdrum6.wav",0,0,R.drawable.tomtom0,R.drawable.tomtom1);
		hash.put("Button37", info);
		
		info = new SoundInfo("Button38","drums/tomtomdrum7.wav",0,0,R.drawable.tomtom0,R.drawable.tomtom1);
		hash.put("Button38", info);
		
		info = new SoundInfo("Button39","drums/bassdrum6.wav",0,0,R.drawable.bass0,R.drawable.bass1);
		hash.put("Button39", info);

		return hash;
	}
	
}
