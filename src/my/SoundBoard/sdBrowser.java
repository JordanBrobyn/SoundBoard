package my.SoundBoard;

/*Date: 04/03/2012
 * Author:Jordan Brobyn
 * Description: Browse the sdcard
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class sdBrowser extends ListActivity{

	private ArrayList<String> item = null;
	private List<String> path = null;
	private String root=Environment.getExternalStorageDirectory().getPath();
	private TextView myPath;
	private String buttonId = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searcher);
        myPath = (TextView)findViewById(R.id.path);
        Bundle extras = getIntent().getExtras();
        
        if(extras != null){
        	buttonId = extras.getString("id");
        }
        try{
        	getDir(root);
        }catch(Exception e){
        	Toast.makeText(getApplicationContext(), "Unable to open the SDCARD.\nEnsure the card is mounted", Toast.LENGTH_LONG);
        	//finish();
        }
    }
    
    private void getDir(String dirPath)
    {
    	myPath.setText("Location: " + dirPath);
    	
    	item = new ArrayList<String>();
    	path = new ArrayList<String>();
    	
    	File f = new File(dirPath);
    	File[] files = f.listFiles();
 

    	if(!dirPath.equals(root))
    	{

    		item.add("HOME");
    		path.add(root);
    		
    		item.add("");
    		path.add(f.getParent());
            
    	}
    	
    	for(int i=0; i < files.length; i++)
    	{
    			File file = files[i];
    			path.add(file.getPath());
    			if(file.isDirectory()){
    				item.add(file.getName() + "/");
    			}
    			else{
    				item.add(file.getName());
    			}
    	}

    	LibraryAdapter fileList = new LibraryAdapter(this,R.layout.row,item);
    	
    	setListAdapter(fileList);
    	
    }
    

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		File file = new File(path.get(position));
		
		if (file.isDirectory())
		{
			if(file.canRead())
				getDir(path.get(position));
			else
			{
				new AlertDialog.Builder(this)
				//.setIcon(R.drawable.icon)
				.setTitle("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK", 
						new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
							}
						}).show();
			}
		}
		else //Do something if not a directory
		{
			Intent intent = new Intent();
			intent.putExtra("id", buttonId);
			intent.putExtra("file",path.get(position));
			setResult(1,intent);
			finish();
		}
	}
	
	/*Customized view for the sd browser to efficiently change icons with respect to file types
	 * 
	 */
	private class LibraryAdapter extends ArrayAdapter<String>
	{
		private ArrayList<String> items;
		
		public LibraryAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
            super(context, textViewResourceId, items);
            this.items = items;
		}
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }
                String o = items.get(position);
                if (o != null) {
                        TextView text = (TextView) v.findViewById(R.id.rowtext);
                        ImageView image = (ImageView) v.findViewById(R.id.icon);
                        if (text != null) {
                              text.setText(o);                            
                              if(o.contains("/")){
                            	  image.setImageResource(R.drawable.folder);
                              }else if(o.contains(".mp3") || o.contains(".wav") || o.contains(".ogg")){
                            	  image.setImageResource(R.drawable.music);
                              }else if(o.equals("")){
                            	  image.setImageResource(R.drawable.up);  
                              }else if(o.equals("HOME")){
                            	  image.setImageResource(R.drawable.home);
                              }else{
                            	  image.setImageResource(R.drawable.document);
                              }
                        }
                        
                }
                return v;
        }
		
	}
}
