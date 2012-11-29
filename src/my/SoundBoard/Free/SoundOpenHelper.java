package my.SoundBoard.Free;
/*Date: 04/03/2012
 * Author:Jordan Brobyn
 * Description: Database assister library
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SoundOpenHelper extends SQLiteOpenHelper{

	public static final int DATABASE_VERSION = 3;
	public static final String COLUMN_ID = "_id";
	public static final String PATH = "path";
	public static final String DATABASE_NAME = "soundBoard.db";
	public static final String BOARD_TABLE_NAME = "soundBoard";
	public static final String BOARD_TABLE_CREATE = "CREATE TABLE " + BOARD_TABLE_NAME
			+ " (_id TEXT PRIMARY KEY, path TEXT);";
	public SQLiteDatabase database;
	
	SoundOpenHelper(Context context){
		super(context,DATABASE_NAME,null,DATABASE_VERSION);
	}	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(BOARD_TABLE_CREATE);
    }

	 @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BOARD_TABLE_NAME);
        onCreate(db);
     }
	
}
