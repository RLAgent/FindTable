package com.example.find_a_tablerestaurant;

import us.findtable.princeton.restaurant.R;
import android.content.SharedPreferences;
import android.widget.TableLayout;
import android.widget.TextView;

/** A table with rows of type TableTypeRow. */
public class TableTypeTable {
	
	/** Table count information */
	public static TableCountArray tableCountArray;
	
	/** Default display values */
	public static final String RESTAURANT_NAME = MainActivity
			.context.getString(R.string.restaurant_name);
	public static final String RESTAURANT_ADDRESS = MainActivity
			.context.getString(R.string.restaurant_addr);
	
	/** Reset TableLayout by re-adding TableTypeRow objects (of each table type)
	 *  to parentTL.
	 *  
	 *  @param  parentTL
	 *          the TableLayout object 
	 */
	public static int getNumTableTypes() {
		String s = MainActivity.pref.getString(
				SettingsFragment.KEY_NUM_TABLE_TYPE,
				Integer.toString(SettingsFragment.NUM_TABLE_TYPE_MIN));
		
		// Check for empty input
		if (s.equals("")) {
			MainActivity.pref.edit().putString(
					SettingsFragment.KEY_NUM_TABLE_TYPE,
					Integer.toString(SettingsFragment.NUM_TABLE_TYPE_MIN))
					.commit();
			return SettingsFragment.NUM_TABLE_TYPE_MIN;
		}
		POST.setFlag();
		
		return Integer.parseInt(s);
	}
	
	/** Return the total count of the specified table.
	 * 
	 * @param  tableId
	 *         the id of the specified table
	 */
	public static int getTotCnt(int tableId) {
		String s = MainActivity.pref.getString(
				SettingsFragment.KEY_TOT_CNT+ tableId,
				Integer.toString(SettingsFragment.TOT_CNT_MIN));
		if (s.equals("")) {
			MainActivity.pref.edit().putString(
					SettingsFragment.KEY_TOT_CNT+ tableId,
					Integer.toString(SettingsFragment.TOT_CNT_MIN)).commit();
			return SettingsFragment.TOT_CNT_MIN;
		}
		POST.setFlag();
		
		return Integer.parseInt(s);
	}
	
	/** Return the count of the specified table.
	 * 
	 * @param  tableId
	 *         the id of the specified table
	 */
	public static int getCnt(int tableId, int defaultVal) {
		return MainActivity.pref.getInt(SettingsFragment.KEY_COUNTER
				+ tableId, defaultVal);
	}
	/** Return whether the display checkbox is checked or not.
	 * 
	 * @param tableId
	 *        the id of the specified table
	 * @return true if the display checkbox is checked
	 */
	public static boolean isChecked(int tableId) {
		return MainActivity.pref.getBoolean(
				SettingsFragment.KEY_DISPLAY+tableId, false);
	}
	
	/** Returns the restaurant name.
	 * 
	 * @return the restaurant name
	 */
	public static String getRestaurantName() {
		return MainActivity.pref.getString(
				SettingsFragment.KEY_RESTAURANT_NAME, RESTAURANT_NAME);
	}
	
	/** Returns the restaurant address.
	 * 
	 * @return the restaurant address
	 */
	public static String getRestaurantAddr() {
		return MainActivity.pref.getString(
				SettingsFragment.KEY_RESTAURANT_ADDRESS, RESTAURANT_ADDRESS);
	}
	
	/** Returns the type of the specified table.
	 * 
	 * @param tableId
	 *        the id of the specified table
	 * @return the type of the specified table
	 */
	public static String getTableType(int tableId) {
		return MainActivity.pref.getString(
				SettingsFragment.KEY_TYPE + tableId,
				MainActivity.context.getString(R.string.table_type0));
	}
	
	/** Returns the size of the specified table.
	 * 
	 * @param tableId
	 *        the id of the specified table
	 * @return the size of the specified table
	 */
	public static int getTableSize(int tableId) {
		String s = MainActivity.pref.getString(
				SettingsFragment.KEY_SIZE + tableId,
				Integer.toString(SettingsFragment.TABLE_SIZE_MIN));
		if (s.equals("")) {
			MainActivity.pref.edit().putString(
					SettingsFragment.KEY_SIZE + tableId,
					Integer.toString(SettingsFragment.TABLE_SIZE_MIN)).commit();
			return SettingsFragment.TABLE_SIZE_MIN;
		}
		return Integer.parseInt(s);
	}
	
	/** Resets parentTL: loads counts into tableCountArray and creates buttons
	 * for each table type.
	 * 
	 * @param parentTL
	 *        the parent TableLayout where the buttons are added
	 */
	public static void reset(TableLayout parentTL) {
		
		// Load table count into tableCountArray.
		int numTableTypes = getNumTableTypes();
		int[] counter = new int[numTableTypes];
		int[] totCnt = new int[numTableTypes];
		for (int tableId = 0; tableId < numTableTypes; tableId++) {
			totCnt[tableId] = getTotCnt(tableId);
			counter[tableId] = getCnt(tableId, totCnt[tableId]);
		}
		tableCountArray = new TableCountArray(numTableTypes, counter, totCnt);
		
		// Create buttons for each table type
		for (int tableId = 0; tableId < numTableTypes; tableId++) {
			if (isChecked(tableId))
				parentTL.addView(new TableTypeRow(tableId));
		}
	}
	
	/** Save table info.
	 */
	public static void save() {
		SharedPreferences.Editor editor = MainActivity.pref.edit();
		for (int tableId = 0; tableId < tableCountArray.getNumTableTypes();
				tableId++) {
			editor.putBoolean(SettingsFragment.KEY_DISPLAY+tableId,
					isChecked(tableId));
			editor.putString(SettingsFragment.KEY_TOT_CNT+tableId,
					Integer.toString(getTotCnt(tableId)));
			editor.putString(SettingsFragment.KEY_SIZE+tableId,
					Integer.toString(getTableSize(tableId)));
			editor.putInt(
					SettingsFragment.KEY_COUNTER + tableId,
					tableCountArray.getCount(tableId));
			editor.putString(SettingsFragment.KEY_TYPE+tableId,
					getTableType(tableId));
		}
		editor.putString(SettingsFragment.KEY_NUM_TABLE_TYPE, Integer.toString(getNumTableTypes()));
		editor.commit();

		POST.setFlag();
	}
	
	/** Display restaurant name and address above the table. */
	public static void displayRestaurantInfo(TextView name, TextView address) {
		// Set up restaurant info
		String sName = getRestaurantName();
		String sAddress = getRestaurantAddr();
		
		name.setText(sName);
		address.setText(sAddress);
		
	}
	
	/** Returns the type and size of the specified table, in String form.
	 * 
	 *  @param  tableId
	 *          the id of the specified table
	 *  @return the table type and size of the specified table, in String form
	 */
	public static String getTableTypeSize_toString(int tableId) {
		return getTableType(tableId) + " (Size: " + getTableSize(tableId) + ")";
	}
	
	/** Returns the type of the specified table, in String form.
	 * 
	 * @param tableId
	 *        the id of the specified table
	 * @return the table type of the specified table, in String form
	 */
	public static String getTableType_toString(int tableId) {
		return getTableType(tableId);
	}
	
	/** Returns the size of the specified table, in String form.
	 * 
	 * @param tableId
	 *        the id of the specified table
	 * @return the size of the specified table, in String form
	 */
	public static String getTableSize_toString(int tableId) {
		return "  (Size: "+getTableSize(tableId)+") ";
	}

}
