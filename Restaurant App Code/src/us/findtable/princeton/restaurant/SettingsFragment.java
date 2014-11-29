package us.findtable.princeton.restaurant;

import us.findtable.princeton.restaurant.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.BaseAdapter;

/** Displays and manages the list of Preference objects, and implements a
 *  preference change listener catcher.
 * 
 * @author lilee
 *
 */
public class SettingsFragment extends PreferenceFragment implements
OnSharedPreferenceChangeListener {
	/** SettingsFragment info */
	public static Context context;
	
	/** Keys */
	public static final String KEY_RESTAURANT_ADDRESS = MainActivity.context
			.getString(R.string.key_restaurant_address);
	public static final String KEY_RESTAURANT_NAME = MainActivity.context
			.getString(R.string.key_restaurant_name);
	public static final String KEY_RESTAURANT_TYPE = MainActivity.context
			.getString(R.string.key_restaurant_type);
	public static final String KEY_PREFERENCE_CATEGORY_TABLES = MainActivity
			.context.getString(R.string.key_preference_category_tables);
	public static final String KEY_ROOT_PREFERENCE_SCREEN = MainActivity
			.context.getString(R.string.key_root_preference_screen);
	public static final String KEY_TABLE = MainActivity.context
			.getString(R.string.key_table);
	public static final String KEY_TYPE = MainActivity.context
			.getString(R.string.key_type);
	public static final String KEY_SIZE = MainActivity.context.
			getString(R.string.key_size);
	public static final String KEY_TOT_CNT = MainActivity.context.
			getString(R.string.key_tot_cnt);
	public static final String KEY_BUTTON_UP = MainActivity.context.
			getString(R.string.key_button_up);
	public static final String KEY_BUTTON_DOWN = MainActivity.context.
			getString(R.string.key_button_down);
	public static final String KEY_DISPLAY = MainActivity.context.
			getString(R.string.key_display);
	public static final String KEY_COUNTER = MainActivity.context.
			getString(R.string.key_counter);
	public static final String KEY_NUM_TABLE_TYPE = MainActivity.
			context.getString(R.string.key_num_table_type);
	
	/** Constants */
	public static final int UNCHECKED_TABLE = MainActivity.res.
			getInteger(R.integer.unchecked_table);
	public static final int NONEXISTENT_TABLE = MainActivity.res.
			getInteger(R.integer.nonexistent_table);
	public static final int NUM_TABLE_TYPE_MIN = MainActivity.res.
			getInteger(R.integer.num_table_type_min);
	public static final int NUM_TABLE_TYPE_MAX = MainActivity.res.
			getInteger(R.integer.num_table_type_max);
	public static final int TABLE_SIZE_BAR = MainActivity.res.
			getInteger(R.integer.table_size_bar);
	public static final int TABLE_SIZE_MIN = MainActivity.res.
			getInteger(R.integer.table_size_min);
	public static final int TABLE_SIZE_MAX = MainActivity.res.
			getInteger(R.integer.table_size_max);
	public static final int TOT_CNT_MIN = MainActivity.res.
			getInteger(R.integer.tot_cnt_min);
	public static final int TOT_CNT_MAX = MainActivity.res.
			getInteger(R.integer.tot_cnt_max);
	
	/** Initially false when Settings is accessed;
	 *  reload Settings page if false */
	private boolean flag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		context = getActivity();
		flag = false;

		SharedPreferences.Editor ed = context.getSharedPreferences(
				"MyPreferences", Context.MODE_PRIVATE).edit();
		ed.putBoolean("HaveShownPrefs", true);
		ed.commit();

		// Set summary for restaurant name and address.
		PreferenceScreen name = (PreferenceScreen) findPreference(
				KEY_RESTAURANT_NAME);
		PreferenceScreen address = (PreferenceScreen) findPreference(
				KEY_RESTAURANT_ADDRESS);
		PreferenceScreen type = (PreferenceScreen) findPreference(
				KEY_RESTAURANT_TYPE);
		SettingsRestaurantInfo.setSummary(name, address, type);

		// Display the table types.
		displayTableTypes();
	}


	/** The preference change listener catcher. This is run when a preference
	 * is changed, and handles the change accordingly.
	 *  
	 *  @param  sharedPreferences
	 *  @param  key
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// Number of table types
		if (key.equals(KEY_NUM_TABLE_TYPE)) {
			
			// Check for empty input
			String s = MainActivity.pref.getString(key,
					Integer.toString(NUM_TABLE_TYPE_MIN));
			if (s.equals("")) {
				MainActivity.pref.edit().putString(key,
						Integer.toString(NUM_TABLE_TYPE_MIN)).commit();
			}

			// If the number is greater than the max, set it to max.
			if (TableTypeTable.getNumTableTypes() > NUM_TABLE_TYPE_MAX)
				MainActivity.pref.edit().putString(key,
						Integer.toString(NUM_TABLE_TYPE_MAX)).commit();

			// In case the user inputs a number that starts with 0, e.g., "04"
			EditTextPreference pref_num_table_type = (EditTextPreference)
					findPreference(KEY_NUM_TABLE_TYPE);
			try {
				pref_num_table_type.setText(
						Integer.toString(TableTypeTable.getNumTableTypes()));
				
			} catch(NullPointerException e) {}
			
			displayTableTypes();
			
			POST.setFlag();
		}

		// (Table) type
		if (key.contains(KEY_TYPE)) { 
			int tableId = Integer.parseInt(key.substring(KEY_TYPE.length()));

			// If table type is changed to Bar, then the table size should be 1.
			EditTextPreference size = (EditTextPreference)
					findPreference(KEY_SIZE + tableId);
			try {
				if (sharedPreferences.getString(key, "").equals(
						context.getString(R.string.table_type1))) {
					size.setText(Integer.toString(TABLE_SIZE_BAR));
					size.setEnabled(false);
				}
				else {
					size.setEnabled(true);
				}
			}
			catch (NullPointerException e) {}

			// If type is changed to Bar, rename title of tot_cnt
			EditTextPreference tot_cnt = (EditTextPreference)
					findPreference(KEY_TOT_CNT+tableId); 
			try {
				if (sharedPreferences.getString(key, "").equals(
						context.getString(R.string.table_type1))) {
					tot_cnt.setTitle(R.string.tot_num_bar_stools);
				}
				else {
					tot_cnt.setTitle(R.string.tot_cnt);
				}
			}
			catch (NullPointerException e) {}
			
			// TODO: MING-YEE
			MainActivity.pref.edit().putString(key,
					TableTypeTable.getTableType(tableId)).commit();
			
			ListPreference type = (ListPreference)
					findPreference(key);
			type.setValue(TableTypeTable.getTableType(tableId));
			
			displayTableType(tableId);
			
			POST.setFlag();
		}

		// (Table) size
		if (key.contains(KEY_SIZE)) {
			int tableId = Integer.parseInt(key.substring(KEY_SIZE.length()));
			
			// Check for empty input
			String s = MainActivity.pref.getString(key,
					Integer.toString(TABLE_SIZE_MIN));
			if (s.equals("")) {
				MainActivity.pref.edit().putString(key,
						Integer.toString(TABLE_SIZE_MIN)).commit();
			}
			
			EditTextPreference size = (EditTextPreference)
					findPreference(KEY_SIZE + tableId);
			try {
				size.setText(Integer.toString(
						TableTypeTable.getTableSize(tableId)));
			} catch (NullPointerException e) {}
			
			displayTableType(tableId);
			
			POST.setFlag();
		}

		// (Table) total count
		if (key.contains(KEY_TOT_CNT)) {
			int tableId = Integer.parseInt(key.substring(KEY_TOT_CNT.length()));
			
			// Check for empty input
			String s = MainActivity.pref.getString(key,
					Integer.toString(TOT_CNT_MIN));
			if (s.equals("")) {
				MainActivity.pref.edit().putString(key,
						Integer.toString(TOT_CNT_MIN)).commit();
			}

			EditTextPreference pref_tot_cnt = (EditTextPreference)
					findPreference(KEY_TOT_CNT+tableId);
			
			// Restrict the range of tot_cnt.
			int tot_cnt = TableTypeTable.getTotCnt(tableId);
			if (tot_cnt > TOT_CNT_MAX) {
				TableTypeTable.tableCountArray.setTotalCount(tableId,
						TOT_CNT_MAX);
			}
			
			try {
				pref_tot_cnt.setText(
						Integer.toString(TableTypeTable.getTotCnt(tableId)));
			} catch(NullPointerException e) {}
			
			displayTotCnt(Integer.parseInt(
					key.substring(KEY_TOT_CNT.length())));
			
			POST.setFlag();
		}
		
		// Display checkbox
		if (key.contains(KEY_DISPLAY)) {
			int tableId = Integer.parseInt(key.substring(KEY_DISPLAY.length()));
			
			PreferenceScreen prefScreen = (PreferenceScreen)
					findPreference(SettingsFragment.KEY_TABLE+tableId);
			try {
				if (TableTypeTable.isChecked(tableId)) {
					// if checked, set table count to full
					int tot_cnt = TableTypeTable.getTotCnt(tableId);
					TableTypeTable.tableCountArray.setCount(tableId, tot_cnt);
					
					prefScreen.setIcon(R.drawable.green_check);
				}
				else { // if unchecked, change table count to UNCHECKED_TABLE
					TableTypeTable.tableCountArray.setCount(tableId,
							UNCHECKED_TABLE);
					prefScreen.setIcon(R.drawable.red_x);
				}
			}
			catch (NullPointerException e) {}
			
			POST.setFlag();
		}
	}
	
	/** Update the main preferences screen to display the table's total
	 *  count. */
	private void displayTotCnt(int tableId) {
		// Set summary for total count.
		int tot = TableTypeTable.getTotCnt(tableId);
		String tot_cnt = Integer.toString(tot);
		PreferenceScreen prefScreen = (PreferenceScreen)
				findPreference(KEY_TABLE+tableId);
		prefScreen.setSummary(
				context.getString(R.string.tot_cnt) + ": " + tot_cnt);

		EditTextPreference total_count = (EditTextPreference)
				findPreference(KEY_TOT_CNT + tableId);
		if (total_count != null)
			total_count.setSummary(tot_cnt);

		// If total count is lower than current count,
		// then reset current count to the total count.
		int cur = TableTypeTable.tableCountArray.getCount(tableId);
		if (tot < cur) {
			
			SharedPreferences.Editor editor = MainActivity.pref.edit();
			editor.putInt(KEY_COUNTER+tableId, tot);
			editor.commit();

			// Make the POST request to update database
			POST.setFlag();
		}

		getView().invalidate();
	}

	/** If the table type/size already exists, disable the display checkbox. */
	public void disableCheckbox(int tableId) {
		// TODO: Make this code into a function
		boolean unique = true;
		CheckBoxPreference display = (CheckBoxPreference)
				findPreference(KEY_DISPLAY+tableId);
		PreferenceScreen prefScreen = (PreferenceScreen)
				findPreference(KEY_TABLE+tableId);
		String type = TableTypeTable.getTableType(tableId);
		int size = TableTypeTable.getTableSize(tableId);

		int numTableTypes = TableTypeTable.getNumTableTypes();
		for (int j = 0; j < numTableTypes; j++) {
			if (tableId == j)
				continue;
			if (size == TableTypeTable.getTableSize(j))
				if (type.equals(TableTypeTable.getTableType(j))) {
					CheckBoxPreference display1 = (CheckBoxPreference)
							findPreference(KEY_DISPLAY+j);
					if (display1 != null)
						if (display1.isChecked())
							unique = false;
				}
		}
		if (unique) {
			display.setEnabled(true);
			display.setSummary("");
		}
		else {
			display.setChecked(false);
			display.setEnabled(false);
			display.setSummary(context.getString(R.string.checkbox_msg));
			prefScreen.setIcon(R.drawable.red_x);
		}
	}

	public static void disableCheckbox(Preference preference, int tableId) {
		PreferenceManager pm = preference.getPreferenceManager();
		CheckBoxPreference display = (CheckBoxPreference)
				pm.findPreference(SettingsFragment.KEY_DISPLAY+tableId);
		PreferenceScreen prefScreen = (PreferenceScreen)
				pm.findPreference(SettingsFragment.KEY_TABLE+tableId);
		boolean unique = true;
		String type = MainActivity.pref.getString(
				SettingsFragment.KEY_TYPE+tableId, "");
		
		int size = Integer.parseInt(pm.getSharedPreferences().getString(
				SettingsFragment.KEY_SIZE+tableId,
				Integer.toString(SettingsFragment.TABLE_SIZE_MIN)));
		int numTableTypes = TableTypeTable.getNumTableTypes();
		for (int j = 0; j < numTableTypes; j++) {
			if (tableId == j)
				continue;
			if (size == TableTypeTable.getTableSize(j))
				if (type.equals(TableTypeTable.getTableType(j))) {
					CheckBoxPreference display1 = (CheckBoxPreference)
							pm.findPreference(SettingsFragment.KEY_DISPLAY+j);
					if (display1 != null)
						if (display1.isChecked())
							unique = false;
				}
		}
		if (unique) {
			display.setEnabled(true);
			display.setSummary("");
		}
		else {
			display.setChecked(false);
			display.setEnabled(false);
			display.setSummary(SettingsFragment.context.getString(
					R.string.checkbox_msg));
			prefScreen.setIcon(R.drawable.red_x);
		}
	}


	/** Update the main preferences screen to display the table's type and
	 *  size. */
	private void displayTableType(int tableId) {
		// Set summary for type and size.	
		EditTextPreference size = (EditTextPreference)
				findPreference(KEY_SIZE + tableId);
		ListPreference type = (ListPreference)
				findPreference(KEY_TYPE + tableId);

		String table_type = TableTypeTable.getTableType(tableId);
		int table_size = TableTypeTable.getTableSize(tableId);
		
		try {
			type.setSummary(table_type);
			size.setSummary(Integer.toString(table_size));
		}
		catch(NullPointerException e) {}

		// Restrict the table size range for tables of type not Bar.
		if (!table_type.equals(context.getString(R.string.table_type1))) {
			try {
					if (table_size < TABLE_SIZE_MIN) {
						table_size = TABLE_SIZE_MIN;
						size.setText(Integer.toString(table_size));
					}
					if (table_size > TABLE_SIZE_MAX) {
						table_size = TABLE_SIZE_MAX;
						size.setText(Integer.toString(table_size));
					}
			}
			catch(NumberFormatException e) {}
		}

		// If the table type/size already exists, disable the display checkbox.
		disableCheckbox(tableId);

		// Set title to match the table type and size.
		PreferenceScreen prefScreen = (PreferenceScreen)
				findPreference(KEY_TABLE + tableId);
		prefScreen.setTitle(TableTypeTable.getTableTypeSize_toString(tableId));

		((BaseAdapter) prefScreen.getRootAdapter()).notifyDataSetChanged();
		((BaseAdapter) ((PreferenceScreen)
				findPreference(KEY_ROOT_PREFERENCE_SCREEN)).getRootAdapter())
				.notifyDataSetChanged();
	}


	/** Update the main preferences screen to display the table types. */
	private void displayTableTypes() {
		String key = KEY_NUM_TABLE_TYPE;
		Preference tableTypeNumPref = findPreference(key);

		// Find new number of table types
		int newTableTypeNum = TableTypeTable.getNumTableTypes();

		if(!flag) {
			SettingsTable.setPrevTableTypeNum(0);
			flag = true;
		}
		int prevTableTypeNum = SettingsTable.getPrevTableTypeNum();

		if(newTableTypeNum <= NUM_TABLE_TYPE_MAX)
		{
			// Show the new number of table types
			tableTypeNumPref.setTitle(Integer.toString(
					TableTypeTable.getNumTableTypes()));

			// FIXME: Add defaults to size of -1. This is because the default
			//        does not get "set" yet since the settings page has not
			//        been visited.
			// Add table types if the number of table types has increased.
			PreferenceCategory tableCategory = (PreferenceCategory)
					findPreference(KEY_PREFERENCE_CATEGORY_TABLES);
			if (prevTableTypeNum < newTableTypeNum) {
				for (int tableId = prevTableTypeNum;
						tableId < newTableTypeNum; tableId++) {
					PreferenceScreen prefScreen = (PreferenceScreen)
							getPreferenceManager()
							.createPreferenceScreen(context);
					tableCategory.addPreference(prefScreen);
					SettingsTable.addTableSetting(prefScreen, tableId);

					// Hack fix to make the settings display properly
					tableCategory.removePreference(prefScreen);
					tableCategory.addPreference(prefScreen);

					displayTableType(tableId);
				}
			}

			// Else, delete table types.
			else if (prevTableTypeNum > newTableTypeNum) {
				for (int tableId = prevTableTypeNum - 1;
						tableId >= newTableTypeNum; tableId--) {

					// Remove preference screen from the main preference screen.
					tableCategory.removePreference(
							tableCategory.getPreference(tableId));
				}
			}
			SettingsTable.setPrevTableTypeNum(newTableTypeNum);
		}
		else {
			// Undo preference change
			SharedPreferences.Editor editor = MainActivity.pref.edit();
			editor.putString(key, ""+prevTableTypeNum);
			editor.commit();

			// Alert
			AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
			dlgAlert.setMessage(MainActivity.res.getString(
					R.string.dialog_num_table_type_max_message));
			dlgAlert.setTitle(MainActivity.res.getString(
					R.string.dialog_num_table_type_max_title));
			dlgAlert.setPositiveButton(
					MainActivity.res.getString(R.string.ok), null);
			dlgAlert.setCancelable(true);
			dlgAlert.create().show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		flag = true;
		getPreferenceManager().getSharedPreferences()
		.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences()
		.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
}