package us.findtable.princeton.restaurant;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/** Extends the PreferenceActivity class and uses an XML file to create
 *  preference settings.
 * 
 * @author lilee
 *
 */
public class SettingsActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(
        		android.R.id.content, new SettingsFragment()).commit();

    }
}

