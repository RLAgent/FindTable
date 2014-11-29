package us.findtable.princeton.restaurant;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import us.findtable.princeton.restaurant.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;

public class MainActivity extends FragmentActivity {
	/** MainActivity info */
	public static Context context;
	public static SharedPreferences pref;
	public static Resources res;
    public static MainActivity ourClass;
    private static boolean firstFlag = true;
	
	/** Runs at application startup, initializing settings, and setting up the
	 *  restaurant information and the buttons for each table type.
	 *  
	 *  @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = getApplicationContext();
		res = getResources();
		ourClass = this;
		
		//getFragmentManager().beginTransaction().commit();
		// Load data from database
		
		MainActivity.pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		//SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		String authenticationID = pref.getString(
				context.getString(R.string.key_restaurant_id), "");
		String authenticationToken = pref.getString(
				context.getString(R.string.key_authentication_token), "");
		System.err.println(authenticationID+" "+authenticationToken+"<== MainActivity.onCreate()");
		
		boolean loggedIn = checkLogin(authenticationID, authenticationToken);
		System.err.println(loggedIn);
		
		// TODO: Set havePreferences to true, if successfully grabbed info from database
		
		// Login
		if (!loggedIn) { // not logged in
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivity(loginIntent);
		}
		else if (firstFlag) { // we have already shown the preferences activity before
			// Load information from server
			firstFlag = false;
			updateInfo(authenticationID, authenticationToken);
			System.err.println("Info updated.");
			// Set up buttons for each table type
			TableTypeTable.reset((TableLayout) findViewById(R.id.llayout));
			new POST();
		}
	}
	
	/* TODO: Move this and updateInfo() into a different class */
	private boolean checkLogin(String authID, String authToken) {
		if (authID.equals("")) return false;
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		// Client and POSTer
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://54.186.80.240/restaurant_authcheck.php");
		HttpResponse response = null;
		HttpEntity entity = null;
		List<NameValuePair> params = new ArrayList<NameValuePair>(1);

		/*String authenticationID = pref.getString(
				context.getString(R.string.key_restaurant_id), "");
		String authenticationToken = pref.getString(
				context.getString(R.string.key_authentication_token), "");*/
		params.add(new BasicNameValuePair("id", authID));
		params.add(new BasicNameValuePair("token", authToken));
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			System.err.println("Parameter binding failed."); 
			return false;
		}
		try {
			response = httpclient.execute(httppost);
			entity = response.getEntity();
		}
		catch (Exception e) {
			System.err.println("POST request failed."); 
			return false;
			// TODO: Exit with default values
		}
		
		// Parse
		String s = null;
		try {
			InputStream in = entity.getContent();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1000];
			int bytesRead;
			while((bytesRead = in.read(buf)) != -1)
				out.write(buf, 0, bytesRead);
			s = out.toString();
		}
		catch(Exception e) {
			System.err.println("Parsing failed.");
			return false;
		}
		
		if (s.equals("Success!"))
			return true;
		else
			return false;
		
	}
	
	/** Requests information from server on load. */
	private void updateInfo(String authID, String authToken) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		// Client and POSTer
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://54.186.80.240/restaurant_info.php");
		HttpResponse response = null;
		HttpEntity entity = null;
		
		// Request parameters and other properties
		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		
		params.add(new BasicNameValuePair("id", authID));
		params.add(new BasicNameValuePair("token", authToken));
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) { System.err.println("Parameter binding failed."); }
		
		try {
			response = httpclient.execute(httppost);
			entity = response.getEntity();
		}
		catch (Exception e) {
			System.err.println("POST request failed."); 
			// TODO: Exit with default values.
		}
		
		// Read in
		String s = null;
		try {
			InputStream in = entity.getContent();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1000];
			int bytesRead;
			while((bytesRead = in.read(buf)) != -1)
				out.write(buf, 0, bytesRead);
			s = out.toString();
		}
		catch(Exception e) { System.err.println("Parsing failed."); }
		
		if (s != null) {
			// Parse
			boolean backslashFlag = false, parseErrorFlag = false;
			int prev = 0;
			String[] serverParams = new String[63];
			int paramIndex = 0;
			for (int i = 0; i < s.length(); i++) {
				if (paramIndex == 63) {
					parseErrorFlag = true;
					break;
				}
				if (s.charAt(i) == '\\') {
					backslashFlag = !backslashFlag;
					continue;
				}
				if (s.charAt(i) != '/' && backslashFlag) 
					backslashFlag = !backslashFlag;
				if (s.charAt(i) == '/' && backslashFlag) {
					serverParams[paramIndex++] = s.substring(prev, i-1);
					prev = i+1;
				}
			}
			serverParams[paramIndex++] =  s.substring(prev, s.length());
			parseErrorFlag |= paramIndex < 62;
			
			System.err.println(java.util.Arrays.toString(serverParams));
			
			// Write settings
			if (!parseErrorFlag) {
				SharedPreferences.Editor editor = pref.edit();
				
				editor.putString(MainActivity.context.getString(R.string.key_restaurant_name), serverParams[0]);
				editor.putString(MainActivity.context.getString(R.string.key_restaurant_type), serverParams[1]);
				editor.putString(MainActivity.context.getString(R.string.key_restaurant_address), serverParams[2]);
				
				// TODO: add line for reservation link [3]
				// TODO: add line for waitlist [4]
				HashMap<String, String> map = new HashMap<String, String>();
				for (int i = 2; i <= 20; i++) {
					map.put("open_tables_X"+i, serverParams[ 3+i]); //  5--23
					map.put("open_tables_Y"+i, serverParams[22+i]); // 24--42
					map.put("open_tables_Z"+i, serverParams[41+i]); // 43--61
				}
				map.put("open_tables_BAR", serverParams[62]);
				
				for (int i = 0; i < SettingsFragment.NUM_TABLE_TYPE_MAX; i++)
					try {
						editor.remove(SettingsFragment.KEY_DISPLAY+i);
						editor.remove(SettingsFragment.KEY_TOT_CNT+i);
						editor.remove(SettingsFragment.KEY_SIZE+i);
						editor.remove(SettingsFragment.KEY_COUNTER+i);
						editor.remove(SettingsFragment.KEY_TYPE+i);
					}
					catch(Exception e) {
						break;
					}
				int tableId = 0;
				for (int i = 2; i <= 20; i++)
					if (Integer.parseInt(map.get("open_tables_X"+i)) != SettingsFragment.NONEXISTENT_TABLE) {
						editor.putBoolean(SettingsFragment.KEY_DISPLAY+tableId,
								Integer.parseInt(map.get("open_tables_X"+i)) != SettingsFragment.UNCHECKED_TABLE);
						editor.putString(SettingsFragment.KEY_TOT_CNT+tableId,
								""+Integer.parseInt(map.get("open_tables_X"+i))/256);
						editor.putString(SettingsFragment.KEY_SIZE+tableId,
								""+i);
						editor.putInt(SettingsFragment.KEY_COUNTER+tableId,
								Integer.parseInt(map.get("open_tables_X"+i))%256);
						editor.putString(SettingsFragment.KEY_TYPE+tableId++,
								MainActivity.context.getString(R.string.table_type2));
					}
				for (int i = 2; i <= 20; i++)
					if (Integer.parseInt(map.get("open_tables_Y"+i)) != SettingsFragment.NONEXISTENT_TABLE) {
						editor.putBoolean(SettingsFragment.KEY_DISPLAY+tableId,
								Integer.parseInt(map.get("open_tables_Y"+i)) != SettingsFragment.UNCHECKED_TABLE);
						editor.putString(SettingsFragment.KEY_TOT_CNT+tableId,
								""+Integer.parseInt(map.get("open_tables_Y"+i))/256);
						editor.putString(SettingsFragment.KEY_SIZE+tableId,
								""+i);
						editor.putInt(SettingsFragment.KEY_COUNTER+tableId,
								Integer.parseInt(map.get("open_tables_Y"+i))%256);
						editor.putString(SettingsFragment.KEY_TYPE+tableId++,
								MainActivity.context.getString(R.string.table_type0));
					}
				for (int i = 2; i <= 20; i++)
					if (Integer.parseInt(map.get("open_tables_Z"+i)) != SettingsFragment.NONEXISTENT_TABLE) {
						editor.putBoolean(SettingsFragment.KEY_DISPLAY+tableId,
								Integer.parseInt(map.get("open_tables_Z"+i)) != SettingsFragment.UNCHECKED_TABLE);
						editor.putString(SettingsFragment.KEY_TOT_CNT+tableId,
								""+Integer.parseInt(map.get("open_tables_Z"+i))/256);
						editor.putString(SettingsFragment.KEY_SIZE+tableId,
								""+i);
						editor.putInt(SettingsFragment.KEY_COUNTER+tableId,
								Integer.parseInt(map.get("open_tables_Z"+i))%256);
						editor.putString(SettingsFragment.KEY_TYPE+tableId++,
								MainActivity.context.getString(R.string.table_type3));
					}
				if (Integer.parseInt(map.get("open_tables_BAR")) != SettingsFragment.NONEXISTENT_TABLE) {
					editor.putBoolean(SettingsFragment.KEY_DISPLAY+tableId,
							Integer.parseInt(map.get("open_tables_BAR")) != SettingsFragment.UNCHECKED_TABLE);
					editor.putString(SettingsFragment.KEY_TOT_CNT+tableId,
							""+Integer.parseInt(map.get("open_tables_BAR"))/256);
					editor.putString(SettingsFragment.KEY_SIZE+tableId,
							"1");
					editor.putInt(SettingsFragment.KEY_COUNTER+tableId,
							Integer.parseInt(map.get("open_tables_BAR"))%256);
					editor.putString(SettingsFragment.KEY_TYPE+tableId++,
							MainActivity.context.getString(R.string.table_type1));
				}
				editor.putString(SettingsFragment.KEY_NUM_TABLE_TYPE,
						Integer.toString(tableId));
				
				editor.commit();
			}
			else {
				System.err.println("Parsing error.");
			}
		}
	}
	
	/** Inflates the menu, adding items to the action bar, if it is present.
	 *  
	 *  @param  menu
	 *          the menu to inflate
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/** Handles action bar item clicks: The action bar will automatically handle
	 *  clicks on the Home/Up button, so long as parent activity is specified (in
	 *  AndroidManifest.xml).
	 * 
	 *  @param  item
	 *          the clicked item
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/** Runs when the application resumes, setting up the restaurant info and the buttons for each table type. */
	@Override
	public void onResume() {
		super.onResume();
		LinearLayout parentll = (LinearLayout) findViewById(R.id.llayout);
		parentll.removeAllViews();
		
		// Set up buttons for each table type
		TableTypeTable.reset((TableLayout) findViewById(R.id.llayout));
	}
	
	/** Runs before the application stops, saving the table counts. */
	@Override
	public void onStop() {
		super.onPause();
		
		// Save table counts
		TableTypeTable.save();
	}
}
