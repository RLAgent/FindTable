package us.findtable.princeton.restaurant;

import us.findtable.princeton.restaurant.R;

import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;


/** A helper class for SettingsFragment that dynamically adds table settings
 *  for a specified table to the Settings screen.
 * 
 * @author lilee
 *
 */
public class SettingsTable {

	/** Constants */
	private final static int TABLE_COUNT_DEFAULT = MainActivity.res
			.getInteger(R.integer.table_count_default);
	private final static int INT_DEFAULT_TABLE_SIZE = MainActivity.res
			.getInteger(R.integer.table_size_default);
	private static int prevTableTypeNum = 0;

	/** Dynamically adds table settings for the specified table to prefScreen.
	 * 
	 *  @param  tableId
	 *          the id of the specified table
	 */
	public static void addTableSetting(PreferenceScreen prefScreen,
			int tableId) {
		
		prefScreen.setKey(SettingsFragment.KEY_TABLE + tableId);
		
		// Display checkbox
		CheckBoxPreference display = new CheckBoxPreference(
				SettingsFragment.context);
        display.setKey(SettingsFragment.KEY_DISPLAY + tableId);
        display.setDefaultValue(false);
        display.setTitle(SettingsFragment.context.getString(
        		R.string.display_on_main_screen));
		
		// Total table count
		EditTextPreference prefTotCnt = new EditTextPreference(
				SettingsFragment.context);
		prefTotCnt.setKey(SettingsFragment.KEY_TOT_CNT + tableId);
		prefTotCnt.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		prefTotCnt.setDefaultValue(Integer.toString(TABLE_COUNT_DEFAULT));
		prefTotCnt.setDialogMessage(R.string.query_tot_cnt);
		prefTotCnt.getEditText().setSelectAllOnFocus(true); // TODO: CHANGED
		
		// Table size
		EditTextPreference tableSize = new EditTextPreference(
				SettingsFragment.context);
		tableSize.setKey(SettingsFragment.KEY_SIZE + tableId);
		tableSize.setTitle(R.string.table_size);
		tableSize.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		tableSize.setDefaultValue(Integer.toString(INT_DEFAULT_TABLE_SIZE));
		tableSize.setDialogMessage(R.string.query_table_size);
		tableSize.getEditText().setSelectAllOnFocus(true); // TODO: CHANGED

		// Update array
		int cnt;
		int tot_cnt = TableTypeTable.getTotCnt(tableId);
		
		if (TableTypeTable.tableCountArray.getNumTableTypes() > tableId)
			cnt = TableTypeTable.tableCountArray.getCount(tableId);
		else
			cnt = TABLE_COUNT_DEFAULT;
		
		TableTypeTable.tableCountArray.setCount(tableId, cnt);
		TableTypeTable.tableCountArray.setTotalCount(tableId, tot_cnt);
		
		// Table type
		ListPreference tableType = new ListPreference(SettingsFragment.context);
		tableType.setKey(SettingsFragment.KEY_TYPE + tableId);
		tableType.setEntries(R.array.tabletypes);
		tableType.setEntryValues(R.array.tabletypes);
		tableType.setDefaultValue(SettingsFragment.context.getString(
				R.string.table_type0));
		tableType.setTitle(R.string.table_type);
		
		// Add to preference screen
		prefScreen.addPreference(display);
		prefScreen.addPreference(prefTotCnt);

		PreferenceCategory cat3 = new PreferenceCategory(
				SettingsFragment.context);
		cat3.setTitle(SettingsFragment.context.getString(
				R.string.subscreen_category3));
		
		prefScreen.addPreference(cat3);
		cat3.addPreference(tableType);
		cat3.addPreference(tableSize);
		
		// Set title of prefTotCnt depending on the table type.
		String table_type = tableType.getValue();
		if (!table_type.equals(MainActivity.context.getString(
				R.string.table_type1)))
			prefTotCnt.setTitle(R.string.tot_cnt);
		else
			prefTotCnt.setTitle(R.string.tot_num_bar_stools);

		// Icon (displayed or not)
		if (TableTypeTable.isChecked(tableId)) {
			prefScreen.setIcon(R.drawable.green_check);
		}
		else { // if unchecked, set table count to 0
			TableTypeTable.tableCountArray.setCount(tableId,
					SettingsFragment.UNCHECKED_TABLE);
			prefScreen.setIcon(R.drawable.red_x);
		}
		
		prefScreen.setSummary(SettingsFragment.context.getString(
				R.string.tot_cnt) + ": " + Integer.toString(tot_cnt));
		
		// Set up to disable setting size if table type is bar
		final int j = tableId;
		prefScreen.setOnPreferenceClickListener(new
				Preference.OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
		    	PreferenceManager pm = preference.getPreferenceManager();
				if (pm.getSharedPreferences().getString(
						SettingsFragment.KEY_TYPE+j, "").equals(
								SettingsFragment.context.getString(
										R.string.table_type1)))
					((EditTextPreference) pm.findPreference(
							SettingsFragment.KEY_SIZE+j)).setEnabled(false);
				
				((EditTextPreference) pm.findPreference(
						SettingsFragment.KEY_TOT_CNT+j)).setSummary(
								Integer.toString(TableTypeTable.getTotCnt(j)));
				
				SettingsFragment.disableCheckbox(preference, j);
		    	return true;
		    }
		});
		

		POST.setFlag();
	}
	
	/** Returns the previous number of table types.
	 * 
	 * @return the previous number of table types
	 */
	public static int getPrevTableTypeNum() {
		return prevTableTypeNum;
	}

	/** Sets the previous number of table types to num. 
	 * 
	 * @param num
	 *        the new previous number of table types
	 */
	public static void setPrevTableTypeNum(int num) {
		prevTableTypeNum = num;
	}
}
