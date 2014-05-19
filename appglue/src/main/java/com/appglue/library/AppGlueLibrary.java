package com.appglue.library;

public class AppGlueLibrary
{
	public static String createTableString(String tableName, String[][] cols)
	{
		StringBuilder createTable = new StringBuilder(String.format("CREATE TABLE %s (", tableName));
		
		int length = cols.length - 1;
		for(int i = 0; i < length; i++)
		{
//            if(cols[i].length > 2)
//                createTable.append(String.format("%s %s REFERENCES %s(%s) %s,  ", cols[i][0], cols[i][1], cols[i][2], cols[i][3], cols[i][4]));
//            else
			    createTable.append(String.format("%s %s,", cols[i][0], cols[i][1]));
		}
		createTable.append(String.format("%s %s)", cols[length][0], cols[length][1]));

		return createTable.toString();
	}
	
//	/**
//	 * Builds a string to get all of the given columns in the given table.
//	 *
//	 * @param table The table to get everything out of
//	 * @param columns The columns in said table
//	 * @return SQL
//	 */
//	public static String buildGetAllString(String table, String[] columns)
//	{
//		StringBuilder out = new StringBuilder();
//		for(int i = 0; i < columns.length; i++)
//		{
//			out.append(String.format("%s.%s AS %s_%s", table, columns[i], table, columns[i])).append(i < columns.length - 1 ? ", " : " ");
//		}
//
//		return out.toString();
//	}
	
	/**
	 * Builds a string to get all of the given columns in the given table.
	 *
	 * @param table The table to get everything out of
	 * @param columns The columns of said table
	 * @return A string of SQL
	 */
	public static String buildGetAllString(String table, String[][] columns)
	{
		StringBuilder out = new StringBuilder();
		for(int i = 0; i < columns.length; i++)
		{
			out.append(String.format("%s.%s AS %s_%s", table, columns[i][0], table, columns[i][0])).append(i < columns.length - 1 ? ", " : " ");
		}

		return out.toString();
	}
	
//	public static void dumpTable(String tableName, SQLiteOpenHelper dbHelper)
//	{
//		SQLiteDatabase db = dbHelper.getReadableDatabase();
//
//		String sql = String.format("SELECT * FROM %s", tableName);
//		Cursor c = db.rawQuery(sql, null);
//
//		if(c == null)
//			return;
//
//		if(c.getCount() == 0)
//			return;
//
//		String[] cols = c.getColumnNames();
//		String out = "";
//		int l = cols.length - 1;
//
//		for(int i = 0; i < l ; i++)
//			out += cols[i] + ",";
//
//		out += cols[l];
//		Log.d(TAG, out);
//
//		c.moveToFirst();
//
//		do
//		{
//			StringBuilder row = new StringBuilder();
//
//			for(String col : cols)
//			{
//				int index = c.getColumnIndex(col);
//				int type = c.getType(index);
//
//				switch(type)
//				{
//					case Cursor.FIELD_TYPE_FLOAT:
//						row.append(c.getFloat(index));
//						break;
//
//					case Cursor.FIELD_TYPE_INTEGER:
//						row.append(c.getInt(index));
//						break;
//
//					case Cursor.FIELD_TYPE_STRING:
//						row.append(c.getString(index));
//						break;
//				}
//
//				row.append(",  ");
//			}
//
//			Log.d(TAG, row.toString());
//		}
//		while(c.moveToNext());
//	}
}
