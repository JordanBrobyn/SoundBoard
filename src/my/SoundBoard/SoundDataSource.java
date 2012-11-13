package my.SoundBoard;
/*Date: 04/03/2012
 * Author:Jordan Brobyn
 * Description: Database set up
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SoundDataSource {

	private SQLiteDatabase database;
	public static final String QUERY = "_id,path";
	private SoundOpenHelper dbHelper;
	private String[] allSounds = { SoundOpenHelper.COLUMN_ID,
			SoundOpenHelper.PATH };
	
	public SoundDataSource(Context context) {
		dbHelper = new SoundOpenHelper(context);
	}
	
	public void open() throws SQLException {
		
		database = dbHelper.getWritableDatabase();
		//dbHelper.onCreate(database);
	}

	public void close() {
		dbHelper.close();
	}
	
	public void deleteAll() {
	      database.delete(SoundOpenHelper.BOARD_TABLE_NAME, null, null);
	}
	
	public String addSound(String id, String path){
		
		ContentValues values = new ContentValues();
		values.put(SoundOpenHelper.COLUMN_ID, id);
		values.put(SoundOpenHelper.PATH, path);
		long insertId = database.replace(SoundOpenHelper.BOARD_TABLE_NAME, null, values);
		if(insertId == -1){
			return "done";
		}
		Cursor cursor = database.query(SoundOpenHelper.BOARD_TABLE_NAME,
				allSounds,null,null,null,null,null);
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){ //Log Table
			cursor.moveToNext();
		}
		cursor.close();
		
		return "done";
	}
	
	public Cursor retrieveDB(){
		Cursor cursor = database.query(SoundOpenHelper.BOARD_TABLE_NAME,
				allSounds,null,null,null,null,null);
		
		return cursor; 
	}
	
	
}
