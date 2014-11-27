package com.example.find_a_tablerestaurant;

import java.util.ArrayList;


public class TableCountArray {
	
	/** The number of open tables of each type. */
	private ArrayList<Integer> counter;
	
	/** The total number of open tables of each type. */ 
	private ArrayList<Integer> totalCount;
	
	/** The number of table types */
	private static int numTableTypes;
	
	/** Initializes a TableCountArray object.
	 *  @param  num
	 *          the number of table types
	 *  @param  cnt
	 *          array of counts of each table type
	 *  @param  totCount
	 *          array of total counts of each table type
	 */
	public TableCountArray(int num, int[] cnt, int[] totCount) {
		
		if ((cnt.length != num) || (totCount.length != num)) {
			System.err.println("In TableCountArray: array(s) of wrong length.");
			return;
		}
		
		numTableTypes = num;
		counter = new ArrayList<Integer>(numTableTypes);
		totalCount = new ArrayList<Integer>(numTableTypes);
		
		for (int tableId = 0; tableId < numTableTypes; tableId++) {
			counter.add(tableId, cnt[tableId]);
			totalCount.add(tableId, totCount[tableId]);
		}
	}
	
	/** Sets the total count of the specified table to total.
	 * 
	 *  @param  tableId
	 *          the id of the specified table
	 *  @param  total
	 *          the new total count (of tables)
	 */
	public void setTotalCount(int tableId, int total) {
		if (tableId >= numTableTypes) {
			for (int i = numTableTypes; i <= tableId; i++) {
				totalCount.add(SettingsFragment.TOT_CNT_MIN);
				counter.add(SettingsFragment.TOT_CNT_MIN);
			}
			numTableTypes = counter.size();
		}
		totalCount.set(tableId, total);
		
		// Save
		MainActivity.pref.edit().putString(SettingsFragment.KEY_TOT_CNT+tableId,
				Integer.toString(total)).commit();
		POST.setFlag();
	}
	
	/** Sets the number of tables of the specified table to count.
	 * 
	 *  @param  tableId
	 *          the id of the specified table
	 *  @param  count
	 *          the new count (of tables)
	 */
	public void setCount(int tableId, int count) {
		if (tableId >= numTableTypes) {
			System.err.printf("numTableTypes = %d,  tableId = %d\n",
					numTableTypes, tableId);
			
			for (int i = numTableTypes; i <= tableId; i++) {
				totalCount.add(SettingsFragment.TOT_CNT_MIN);
				counter.add(SettingsFragment.TOT_CNT_MIN);
			}
			numTableTypes = counter.size();
		}
		counter.set(tableId, count);
		
		// Save
		MainActivity.pref.edit().putInt(SettingsFragment.KEY_COUNTER+tableId,
				count).commit();
		POST.setFlag();
	}
	
	/** Returns the number of tables of the specified table.
	 * 
	 *  @param  tableId
	 *          the id of the specified table
	 *  @return the count of the specified table
	 */
	public int getCount(int tableId) {
		if (tableId >= counter.size()) {
			System.err.println("TableCountArray.getCount: Out of bounds");
			return -1;
		}
		return counter.get(tableId);
	}
	
	/** Returns the total number of tables of the specified table.
	 * 
	 * @param  tableId
	 *         the id of the specified table
	 * @return the total number of tables of the specified table
	 */
	public int getTotalCount(int tableId) {
		return totalCount.get(tableId);
	}
	
	/** Returns the number of table types.
	 * 
	 *  @return the number of table types
	 */
	public int getNumTableTypes() {
		return numTableTypes;
	}
	
	/** Decrements the count of the specified table.
	 * 
	 * @param  tableId
	 *         the id of the specified table
	 */
	public void decrement(int tableId) {
		// Error check
		if (tableId > counter.size()) {
			System.err.println("TableCountArray.decrement: Out of bounds");
			return;
		}
		
		if (counter.get(tableId) > 0)
			counter.set(tableId, counter.get(tableId)-1);
	}
	
	/** Increments the count of the specified table.
	 * 
	 * @param  tableId
	 *         the id of the specified table
	 */
	public void increment(int tableId) {
		// Error check
		if (tableId > counter.size()) {
			System.err.println("TableCountArray.increment: Out of bounds");
			return;
		}
		if (counter.get(tableId) < totalCount.get(tableId))
			counter.set(tableId, counter.get(tableId)+1);
	}	
}
