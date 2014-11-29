package us.findtable.princeton.restaurant;

import us.findtable.princeton.restaurant.R;

import android.preference.PreferenceScreen;
import android.view.View;

/** A helper class for SettingsFragment that sets the summary for the
 *  Restaurant name, address, and type on the main Settings page.
 * 
 * @author lilee
 *
 */
public class SettingsRestaurantInfo {
	
	/** Default values */
	private static final String RESTAURANT_NAME = SettingsFragment
			.context.getString(R.string.restaurant_name);
	private static final String RESTAURANT_ADDRESS = SettingsFragment
			.context.getString(R.string.restaurant_addr);

	
	public static void setSummary(PreferenceScreen name,
			PreferenceScreen address, PreferenceScreen type) {
		// Set summary for restaurant name, address, and type.
		String sName = MainActivity.pref.getString(SettingsFragment
				.KEY_RESTAURANT_NAME, RESTAURANT_NAME);
		String sAddr = MainActivity.pref.getString(SettingsFragment
				.KEY_RESTAURANT_ADDRESS, RESTAURANT_ADDRESS);
		String sType = MainActivity.pref.getString(SettingsFragment
				.KEY_RESTAURANT_TYPE, "Restaurant Type");
		
		// not selectable
		name.setSelectable(false);
		address.setSelectable(false);
		type.setSelectable(false);
		
		// summary
		name.setSummary(sName);
		address.setSummary(sAddr);
		type.setSummary(sType);
	}
	/** Set summary for restaurant name */
	public static void setSummaryName(PreferenceScreen name, View view) {
		String sName = MainActivity.pref.getString(
				SettingsFragment.KEY_RESTAURANT_NAME, "Restaurant Name");
		
		name.setSelectable(false);
		name.setSummary(sName);
		view.invalidate();
	}
	/** Set summary for restaurant address */
	public static void setSummaryAddress(PreferenceScreen address, View view) {
		String sAddr = MainActivity.pref.getString(
				SettingsFragment.KEY_RESTAURANT_ADDRESS, "Restaurant Address");

		address.setSelectable(false);
		address.setSummary(sAddr);
		view.invalidate();
	}
	
	public static void setSummaryType(PreferenceScreen type, View view) {
		String sType = MainActivity.pref.getString(
				SettingsFragment.KEY_RESTAURANT_TYPE, "Restaurant Type");

		type.setSelectable(false);
		type.setSummary(sType);
		view.invalidate();
	}
	
}
