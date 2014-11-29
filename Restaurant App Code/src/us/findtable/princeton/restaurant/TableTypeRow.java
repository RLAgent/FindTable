package us.findtable.princeton.restaurant;

import us.findtable.princeton.restaurant.R;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;

/** An extension of the TableRow object, it initializes a "row" for a
 *  specified table, and contains its current count, type/size, and buttons
 *  in the Main screen.
 * 
 * @author lilee
 *
 */
@SuppressLint("ViewConstructor") public class TableTypeRow extends TableRow {
	
	private static final int INIT_INTERVAL = MainActivity
			.res.getInteger(R.integer.init_interval);
	private static final int NORMAL_INTERVAL = MainActivity
			.res.getInteger(R.integer.normal_interval);
	
	/** Initializes a TableTypeRow object for the specified table.
	 *  A TableTypeRow object contains, in order:
	 *    (1) the table type
	 *    (2) the table size
	 *    (3) Up button (to increment count)
	 *    (4) the table count
	 *    (5) Down button (to decrement count).
	 *    
	 * @param tableId
	 *        the id of the specified table
	 */
	public TableTypeRow(int tableId) {
		super(MainActivity.context);
		
		this.setGravity(Gravity.CENTER_VERTICAL);
		
		// Table type and size
		TextView tableType = new TextView(MainActivity.context);
		tableType.setId(getId(tableId, SettingsFragment.KEY_TYPE));
		tableType.setText(TableTypeTable.getTableType_toString(tableId));
		tableType.setTextSize(getResources().getDimension(
				R.dimen.text_type_size));
		tableType.setTextColor(MainActivity.res.getColor(
				R.color.table_type_color));
		
		TextView tableSize = new TextView(MainActivity.context);
		tableSize.setId(getId(tableId, SettingsFragment.KEY_TYPE));
		tableSize.setText(TableTypeTable.getTableSize_toString(tableId));
		tableSize.setTextSize(getResources().getDimension(
				R.dimen.text_size_size));
		tableSize.setTextColor(MainActivity.res.getColor(
				R.color.table_size_color));
		
		// Up button
		ImageButton upButton = new ImageButton(MainActivity.context);
		upButton.setId(getId(tableId, SettingsFragment.KEY_BUTTON_UP));
		upButton.setImageResource(R.drawable.button_up);
		upButton.setBackgroundColor(Color.TRANSPARENT);
		upButton.setOnTouchListener(new RepeatListener(
				INIT_INTERVAL, NORMAL_INTERVAL, new OnClickListener() {
			  @Override
			  public void onClick(View view) {
				int tableId = view.getId();
				// Increment count.
				TableTypeTable.tableCountArray.increment(tableId);

				// Update displayed count
				TextView txtValue = (TextView) findViewById(getId(tableId,
						SettingsFragment.KEY_COUNTER));
				txtValue.setText(Integer.toString(
						TableTypeTable.tableCountArray.getCount(tableId)));

				// Make the POST request to update database
				POST.setFlag();

			  }
		}));
		
		// Count
		TextView cnt = new TextView(MainActivity.context);
		cnt.setTextColor(MainActivity.res.getColor(R.color.table_count_color));
		cnt.setId(getId(tableId, SettingsFragment.KEY_COUNTER));
		cnt.setText(Integer.toString(TableTypeTable.tableCountArray
				.getCount(tableId)));
		cnt.setTextSize(getResources().getDimension(R.dimen.text_cnt_size));
		cnt.setGravity(Gravity.CENTER);
		cnt.setMinWidth(100);
		
		// Down button
		ImageButton downButton = new ImageButton(MainActivity.context);
		downButton.setId(getId(tableId, SettingsFragment.KEY_BUTTON_DOWN));
		downButton.setImageResource(R.drawable.button_down);
		downButton.setBackgroundColor(Color.TRANSPARENT);
		downButton.setOnTouchListener(new RepeatListener(INIT_INTERVAL,
				NORMAL_INTERVAL, new OnClickListener() {
			  @Override
			  public void onClick(View view) {
				int tableId = view.getId();

				// Decrement count.
				TableTypeTable.tableCountArray.decrement(tableId);

				// Update displayed count
				TextView txtValue = (TextView) findViewById(getId(tableId,
						SettingsFragment.KEY_COUNTER));
				try {
					txtValue.setText(Integer.toString(TableTypeTable
							.tableCountArray.getCount(tableId)));
				}
				catch (NullPointerException e) {
					System.err.println("In TableTypeRow: txtValue is null"); }
				
				// Make the POST request to update database
				POST.setFlag();
			  }
			}));
		
		// Add views to this TableTypeRow object.
		this.addView(tableType);
		this.addView(tableSize);
		this.addView(downButton);
		this.addView(cnt);
		this.addView(upButton);
	}
	
	/**
	 * Returns the id of the up button, down button, counter, or table type of
	 * a specified table.
	 * 
	 * @param tableId
	 *            the id of the specified table
	 * @param query
	 *            the table parameter or button id query
	 * @return the id of the button or counter of the specified table
	 */
	public int getId(int tableId, String query) {
		if (query == SettingsFragment.KEY_BUTTON_UP)
			return tableId;
		else if (query == SettingsFragment.KEY_BUTTON_DOWN)
			return tableId;
		else if (query == SettingsFragment.KEY_COUNTER)
			return tableId * SettingsFragment.NUM_TABLE_TYPE_MAX + 2;
		else if (query == SettingsFragment.KEY_TYPE)
			return tableId * SettingsFragment.NUM_TABLE_TYPE_MAX + 3;
		else
			return -1;
	}
}
