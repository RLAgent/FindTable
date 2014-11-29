package us.findtable.princeton.restaurant;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import us.findtable.princeton.restaurant.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;

/** The POST request handler. All POST requests by the restaurant application
 *  are made through here, so that updates can be throttled (so as to not
 *  overload the server).
 * 
 * @author lilee
 *
 */
public class POST {
	/** Thread flag. */
	private static boolean threadFlag;
	private Timer timer;
	/** POST request period. */
	private final long DELAY;
	
	public POST() {
		threadFlag = false;
		DELAY = 1000;
		this.timer = new Timer();
		
		timer.schedule(new TimerTask() {
			public void run() {
				if (threadFlag) {
					unsetFlag();
					POSTrequest();
				}
			}
		}, 0, DELAY);
	}
	
	/** Map table type to its database key. */
	public static String mapTypeToKey(String type) {
		if (type.equals(MainActivity.context.getString(R.string.table_type0)))
			return "open_tables_Y";
		else if (type.equals(MainActivity.context.getString(R.string.table_type1)))
			return "open_tables_BAR";
		else if (type.equals(MainActivity.context.getString(R.string.table_type2)))
			return "open_tables_X";
		else if (type.equals(MainActivity.context.getString(R.string.table_type3)))
			return "open_tables_Z";
		else
			return "";
	}
	
	/** Makes a post request to the server. */
	private void POSTrequest() {
		// Client and POSTer
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(MainActivity.context.getString(R.string.post_url));
		
		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(63);
		
		String authenticationID = MainActivity.pref.getString(
				MainActivity.context.getString(R.string.key_restaurant_id), "");
		String authenticationToken = MainActivity.pref.getString(
				MainActivity.context.getString(R.string.key_authentication_token), "");
		params.add(new BasicNameValuePair("id", authenticationID));
		params.add(new BasicNameValuePair("token", authenticationToken));
		
		params.add(new BasicNameValuePair("waitlist_length", "0"));
		
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 2; i <= 20; i++) {
			map.put("open_tables_X"+i, ""+SettingsFragment.NONEXISTENT_TABLE);
			map.put("open_tables_Y"+i, ""+SettingsFragment.NONEXISTENT_TABLE);
			map.put("open_tables_Z"+i, ""+SettingsFragment.NONEXISTENT_TABLE);
		}
		map.put("open_tables_BAR", ""+SettingsFragment.NONEXISTENT_TABLE);
		
		for (int i = 0; i < TableTypeTable.tableCountArray.getNumTableTypes(); i++) {
			// Update map
			String type = mapTypeToKey(MainActivity.pref.getString(SettingsFragment.KEY_TYPE+i, ""));
			String size = MainActivity.pref.getString(SettingsFragment.KEY_SIZE+i, "0");
			if (type.equals("open_tables_BAR"))
				size = "";

			if (MainActivity.pref.getBoolean(SettingsFragment.KEY_DISPLAY+i, false)) {
				// Compress count and total in one int
				int data = TableTypeTable.tableCountArray.getCount(i)
						+ 256*Integer.parseInt(MainActivity.pref.getString(
								SettingsFragment.KEY_TOT_CNT+i, "0"));
				map.put(type+size, Integer.toString(data));
			}
			else {
				map.put(type+size, "-1");
			}
		}
		
		// Put map pairs into params
		for (int i = 2; i <= 20; i++)
			params.add(new BasicNameValuePair("open_tables_X"+i, map.get("open_tables_X"+i)));
		for (int i = 2; i <= 20; i++)
			params.add(new BasicNameValuePair("open_tables_Y"+i, map.get("open_tables_Y"+i)));
		for (int i = 2; i <= 20; i++)
			params.add(new BasicNameValuePair("open_tables_Z"+i, map.get("open_tables_Z"+i)));
		params.add(new BasicNameValuePair("open_tables_BAR", map.get("open_tables_BAR")));
		
		
		//System.err.println(map.toString());
		//System.err.println(params.toString());
		
		
		// Send request
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		try {
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			
			// Check for success
			//System.err.println(response.getStatusLine());
			String s = null;
			
			InputStream in = entity.getContent();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1000];
			int bytesRead;
			while((bytesRead = in.read(buf)) != -1)
				out.write(buf, 0, bytesRead);
			s = out.toString();
			if (s.equals("Logged out!"))
			{
				POST.unsetFlag();
				this.timer.cancel();
				Intent loginIntent = new Intent(MainActivity.ourClass, LoginActivity.class);
				MainActivity.ourClass.startActivity(loginIntent);
				System.err.println("We've been logged out!");
				
				// DO NOT execute this line until it's been logged back in!
				//new POST(mainActivityReference);
				
			}
			
		} catch (Exception e) {
			setFlag();
		}
		
	}
	
	/** Sets the flag for a POST request. */
	public static void setFlag(){
		threadFlag = true;
	}
	
	/** Unsets the flag for a POST request. */
	private static void unsetFlag(){
		threadFlag = false;
	}
}