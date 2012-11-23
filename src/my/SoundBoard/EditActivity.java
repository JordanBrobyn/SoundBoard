package my.SoundBoard;
/*Date: 04/03/2012
 * Author:Jordan Brobyn
 * Description: Activity used to give the user access to editing the soundboard
 * Each button will add or change the value of the database stored on the device
 * 
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

public class EditActivity extends TabActivity{

	SoundDataSource soundDB;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        TabHost tabs = getTabHost();
        tabs.addTab(tabs.newTabSpec("tab1").setIndicator("Board 1").setContent(R.id.tabview1));
        tabs.addTab(tabs.newTabSpec("tab2").setIndicator("Board 2").setContent(R.id.tabview2));
        tabs.setCurrentTab(0);
        soundDB = new SoundDataSource(getApplicationContext());
		soundDB.open();
		
		prepareTable();
		//soundDB.deleteAll();
	}
	
	@Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) // Return selected data from file browser
    {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null){
			String file = data.getStringExtra("file"); //Find selected file
			String id = data.getStringExtra("id"); // Keep track of button pressed
			View view = findViewById(R.id.editWindow);
			ImageView iView = (ImageView) view.findViewWithTag(id);
			iView.setImageResource(R.drawable.orange3);
			iView.invalidate();
			editPreferences(file,id);
		}
 		
    }
	
	public void prepareTable(){ //Alert the user which buttons have a current song
		Cursor cursor = soundDB.retrieveDB();
		
		cursor.moveToFirst();
		View view = findViewById(R.id.editWindow);
		
		while(!cursor.isAfterLast()){ //Log Table to verify 
			
			ImageView iView = (ImageView) view.findViewWithTag(cursor.getString(0));
			iView.setImageResource(R.drawable.orange3);
			iView.invalidate();
			cursor.moveToNext();
		}
		
		cursor.close();
	}
	
	public void editPreferences(String file, String id){
		soundDB.addSound(id, file);		
	}
	
	public void editSound(View v){ //Select which files should be used with each button
		Toast.makeText(getApplicationContext(),"Find Your Sound File",5).show();
		Intent intent = new Intent(getApplicationContext(), sdBrowser.class);
		intent.putExtra("id",v.getTag().toString());
		startActivityForResult(intent,1);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.editmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.save){
			Toast.makeText(this, "Not Available in Trial Version", Toast.LENGTH_SHORT).show();
		}
		else if (item.getItemId() == R.id.load){
			Toast.makeText(this, "Not Available in Trial Version", Toast.LENGTH_SHORT).show();
		}else if(item.getItemId() == R.id.start){
			Toast.makeText(this, "Play Board", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(getApplicationContext(),PlayActivity.class);
			soundDB.close();
			finish();
			startActivity(intent);
		}
		
		return true;
	}
	
	@Override
	public void onBackPressed(){ // Release audio data to not overflow memory
		
			soundDB.close();
			finish();
	}
}
