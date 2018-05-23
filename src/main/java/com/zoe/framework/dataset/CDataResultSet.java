//	CDataResultSet.java
//	Casper Datasets (c) Framework 
//	

package com.zoe.framework.dataset;

//	Java imports 
import java.io.Serializable;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;


/**
 * 	This is a JDBC ResultSet-compliant implementation of the casper rowset.
 * 	The implementation of this wrapper will allow applications that rely upon JDBC resultsets
 * 	to plug into the casper datasets framework more or less seamlessly.  
 * 	<br/><br/>
 * 	Note: there are a number of methods that have been left as no-op or return "null" values. 
 * 	Please feel free to flesh out the implementation.
 *
 *  Requires Java 1.8 (or later).
 * 
 * 	@since v1.0
 * 	@author Jonathan Liang
 *  @version $Revision: 111 $ 
 */
public class CDataResultSet 
	implements ResultSet, Serializable 
{
	
	//	--- Static Variables ---
	
	/**	Required for serializable */
	private static final long serialVersionUID = 1L;

	
	//	--- Instance Variables ---
	
	/** The underlying CDataRowSet object. */
	private CDataRowSet rowset = null;
	
	
	//	--- Constructor(s) ---
	
	/**
	 *	The implementation must wrap a CDataRowSet,
	 *	Do not allow empty instantiation 
	 */
	private CDataResultSet() {
	}
	
	/**
	 *	Instantiates an instance of CDataResultSet by wrapping a CDataRowSet object.
	 * @param rowset
	 */
	public CDataResultSet(CDataRowSet rowset) {
		this.rowset = rowset;
	}
	
	
	//
	//	--- Interface Methods :: java.sql.ResultSet --- 
	//		Note: we only implement the raw-essentials here, to allow for pluggability
	//		into existing codebases that utilize the basics of java.sql.ResultSet
	//		Feel free to improve upon this implementation 
	//
	
	/**
	 * 	This is necessary because the column index numbers that are expected begin with 1..n, whereas
	 * 	the columns that is actually stored in this meta data implementation are stored within arrays,
	 * 	beginning at index 0.
	 * 
	 * 	@param column
	 * 	@return transformed column index 
	 */
	private int transformColumnToArrayIndex(int column) {
		return (column - 1);
	}
	
	
	public boolean next() throws SQLException {
		try { 
			return rowset.next(); 
		} 
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }
	}

	public void close() throws SQLException {
		return;
	}

	public boolean wasNull() throws SQLException {
		return false;
	}

	public String getString(int column) throws SQLException {
		try { return rowset.getString(transformColumnToArrayIndex(column)); } 
		catch (Exception ex){ throw new SQLException("Operation failed: " + ex.toString()); }
	}

	public boolean getBoolean(int column) throws SQLException {
		try {
			Boolean val = rowset.getBoolean(transformColumnToArrayIndex(column)); 
			return val.booleanValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }
	}

	public byte getByte(int column) throws SQLException {
		try {
			Byte val = rowset.getByte(transformColumnToArrayIndex(column));
			return val.byteValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); } 
	}

	public short getShort(int column) throws SQLException {
		try {
			Short val = rowset.getShort(transformColumnToArrayIndex(column));
			return val.shortValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }
	}

	public int getInt(int column) throws SQLException {
		try {
			Integer val = rowset.getInt(transformColumnToArrayIndex(column));
			return val.intValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }		
	}

	public long getLong(int column) throws SQLException {
		try {
			Long val = rowset.getLong(transformColumnToArrayIndex(column));
			return val.longValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }		
	}

	public float getFloat(int column) throws SQLException {
		try {
			Float val = rowset.getFloat(transformColumnToArrayIndex(column));
			return val.floatValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public double getDouble(int column) throws SQLException {
		try {
			Double val = rowset.getDouble(transformColumnToArrayIndex(column));
			return val.doubleValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public BigDecimal getBigDecimal(int column, int scale) throws SQLException {
		return null;
	}

	public byte[] getBytes(int column) throws SQLException {
		return null;
	}

	public Date getDate(int column) throws SQLException {
		try {
			java.util.Date val = rowset.getDate(transformColumnToArrayIndex(column));
			return new java.sql.Date(val.getTime());
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public Time getTime(int column) throws SQLException {
		try {
			java.util.Date val = rowset.getDate(transformColumnToArrayIndex(column));
			return new java.sql.Time(val.getTime());
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public Timestamp getTimestamp(int column) throws SQLException {
		try {
			java.util.Date val = rowset.getDate(transformColumnToArrayIndex(column));
			return new java.sql.Timestamp(val.getTime());
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public InputStream getAsciiStream(int column) throws SQLException {
		return null;
	}

	public InputStream getUnicodeStream(int column) throws SQLException {
		return null;
	}

	public InputStream getBinaryStream(int column) throws SQLException {
		return null;
	}

	public String getString(String columnName) throws SQLException {
		try {
			return rowset.getString(columnName);
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public boolean getBoolean(String columnName) throws SQLException {
		try {
			Boolean val = rowset.getBoolean(columnName);
			return val.booleanValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public byte getByte(String columnName) throws SQLException {
		try {
			Byte val = rowset.getByte(columnName);
			return val.byteValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public short getShort(String columnName) throws SQLException {
		try {
			Short val = rowset.getShort(columnName);
			return val.shortValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public int getInt(String columnName) throws SQLException {
		try {
			Integer val = rowset.getInt(columnName);
			return val.intValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public long getLong(String columnName) throws SQLException {
		try {
			Long val = rowset.getLong(columnName);
			return val.longValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public float getFloat(String columnName) throws SQLException {
		try {
			Float val = rowset.getFloat(columnName);
			return val.floatValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public double getDouble(String columnName) throws SQLException {
		try {
			Double val = rowset.getDouble(columnName);
			return val.doubleValue();
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public BigDecimal getBigDecimal(String columnName, int arg1) throws SQLException {
		return null;
	}

	public byte[] getBytes(String columnName) throws SQLException {
		return null;
	}

	public Date getDate(String columnName) throws SQLException {
		try {
			java.util.Date val = rowset.getDate(columnName);
			return new Date(val.getTime());
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public Time getTime(String columnName) throws SQLException {
		try {
			java.util.Date val = rowset.getDate(columnName);
			return new Time(val.getTime());
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public Timestamp getTimestamp(String columnName) throws SQLException {
		try {
			java.util.Date val = rowset.getDate(columnName);
			return new Timestamp(val.getTime());
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public InputStream getAsciiStream(String arg0) throws SQLException {
		return null;
	}

	public InputStream getUnicodeStream(String arg0) throws SQLException {
		return null;
	}

	public InputStream getBinaryStream(String arg0) throws SQLException {
		return null;
	}

	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	public void clearWarnings() throws SQLException {
	}

	public String getCursorName() throws SQLException {
		//	What the heck is this.
		return null;
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		//	The CRowMetaData implements ResultSetMetaData
		return rowset.getMetaDefinition();
	}

	public Object getObject(int column) throws SQLException {
		try { 
			return rowset.getObject(transformColumnToArrayIndex(column)); 
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }				
	}

	public Object getObject(String columnName) throws SQLException {
		try {
			return rowset.getObject(columnName);
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }						
	}

	public int findColumn(String columnName) throws SQLException {
		try {
			CRowMetaData meta = (CRowMetaData) getMetaData();
			return (meta.getColumnIndex(columnName) + 1);
		}
		catch (Exception ex) { throw new SQLException("Operation failed: " + ex.toString()); }						
	}
	
	public Reader getCharacterStream(int column) throws SQLException {
		return null;
	}

	public Reader getCharacterStream(String columnName) throws SQLException {
		return null;
	}

	public BigDecimal getBigDecimal(int column) throws SQLException {
		return null;
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return null;
	}

	public boolean isBeforeFirst() throws SQLException {
		return rowset.isBeforeFirst();
	}

	public boolean isAfterLast() throws SQLException {
		return rowset.isAfterLast();
	}

	public boolean isFirst() throws SQLException {
		return rowset.isFirst();
	}

	public boolean isLast() throws SQLException {
		return rowset.isLast();
	}

	public void beforeFirst() throws SQLException {
		rowset.beforeFirst();
	}

	public void afterLast() throws SQLException {
		rowset.afterLast();
	}

	public boolean first() throws SQLException {
		//issue #1 fix
		return rowset.first();
	}

	public boolean last() throws SQLException {
		//issue #1 fix
		return rowset.last();
	}

	public int getRow() throws SQLException {
		return rowset.getCursorPosition();
	}

	public boolean absolute(int row) throws SQLException {
		return rowset.absolute(row);
	}

	public boolean relative(int numRows) throws SQLException {
		return rowset.relative(numRows);
	}

	public boolean previous() throws SQLException {
		return rowset.previous();
	}

	public void setFetchDirection(int arg0) throws SQLException {
		//	Not implemented
		return;
	}

	public int getFetchDirection() throws SQLException {
		//	Not implemented
		return 0;
	}

	public void setFetchSize(int size) throws SQLException {
		//	Not implemented
		return;
	}

	public int getFetchSize() throws SQLException {
		//	Not implemented
		return 0;
	}

	public int getType() throws SQLException {
		return 0;
	}

	public int getConcurrency() throws SQLException {
		return 0;
	}

	public boolean rowUpdated() throws SQLException {
		return false;
	}

	public boolean rowInserted() throws SQLException {
		return false;
	}

	public boolean rowDeleted() throws SQLException {
		return false;
	}

	public void updateNull(int arg0) throws SQLException {
		return;
	}

	public void updateBoolean(int arg0, boolean arg1) throws SQLException {
		return;
	}

	public void updateByte(int arg0, byte arg1) throws SQLException {
		return;
	}

	public void updateShort(int arg0, short arg1) throws SQLException {
		return;
	}

	public void updateInt(int arg0, int arg1) throws SQLException {
		return;
	}

	public void updateLong(int arg0, long arg1) throws SQLException {
		return;
	}

	public void updateFloat(int arg0, float arg1) throws SQLException {
		return;
	}

	public void updateDouble(int arg0, double arg1) throws SQLException {
		return;
	}

	public void updateBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
		return;
	}

	public void updateString(int arg0, String arg1) throws SQLException {
		return;
	}

	public void updateBytes(int arg0, byte[] arg1) throws SQLException {
		return;
	}

	public void updateDate(int arg0, Date arg1) throws SQLException {
		return;
	}

	public void updateTime(int arg0, Time arg1) throws SQLException {
		return;
	}

	public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
		return;
	}

	public void updateAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		return;
	}

	public void updateBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
		return;
	}

	public void updateCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
		return;
	}

	public void updateObject(int arg0, Object arg1, int arg2) throws SQLException {
		return;
	}

	public void updateObject(int arg0, Object arg1) throws SQLException {
		return;
	}

	public void updateNull(String arg0) throws SQLException {
		return;
	}

	public void updateBoolean(String arg0, boolean arg1) throws SQLException {
		return;
	}

	public void updateByte(String arg0, byte arg1) throws SQLException {
		return;
	}

	public void updateShort(String arg0, short arg1) throws SQLException {
		return;
	}

	public void updateInt(String arg0, int arg1) throws SQLException {
		return;
	}

	public void updateLong(String arg0, long arg1) throws SQLException {
		return;
	}

	public void updateFloat(String arg0, float arg1) throws SQLException {
		return;
	}

	public void updateDouble(String arg0, double arg1) throws SQLException {
		return;
	}

	public void updateBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
		return;
	}

	public void updateString(String arg0, String arg1) throws SQLException {
		return;
	}

	public void updateBytes(String arg0, byte[] arg1) throws SQLException {
		return;
	}

	public void updateDate(String arg0, Date arg1) throws SQLException {
		return;
	}

	public void updateTime(String arg0, Time arg1) throws SQLException {
		return;
	}

	public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException {
		return;
	}

	public void updateAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException {
		return;
	}

	public void updateBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException {
		return;
	}

	public void updateCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException {
		return;
	}

	public void updateObject(String arg0, Object arg1, int arg2) throws SQLException {
		return;
	}

	public void updateObject(String arg0, Object arg1) throws SQLException {
		return;
	}

	public void insertRow() throws SQLException {
		return;
	}

	public void updateRow() throws SQLException {
		return;
	}

	public void deleteRow() throws SQLException {
		return;
	}

	public void refreshRow() throws SQLException {
		return;
	}

	public void cancelRowUpdates() throws SQLException {
		return;
	}

	public void moveToInsertRow() throws SQLException {
		return;
	}

	public void moveToCurrentRow() throws SQLException {
		return;
	}

	public Statement getStatement() throws SQLException {
		return null;
	}

	public Object getObject(int arg0, Map arg1) throws SQLException {
		return null;
	}

	public Ref getRef(int column) throws SQLException {
		return null;
	}

	public Blob getBlob(int column) throws SQLException {
		return null;
	}

	public Clob getClob(int column) throws SQLException {
		return null;
	}

	public Array getArray(int column) throws SQLException {
		return null;
	}

	public Object getObject(String arg0, Map arg1) throws SQLException {
		return null;
	}

	public Ref getRef(String columnName) throws SQLException {
		return null;
	}

	public Blob getBlob(String columnName) throws SQLException {
		return null;
	}

	public Clob getClob(String columnName) throws SQLException {
		return null;
	}

	public Array getArray(String columnName) throws SQLException {
		return null;
	}

	public Date getDate(int arg0, Calendar arg1) throws SQLException {
		return null;
	}

	public Date getDate(String arg0, Calendar arg1) throws SQLException {
		return null;
	}

	public Time getTime(int arg0, Calendar arg1) throws SQLException {
		return null;
	}

	public Time getTime(String arg0, Calendar arg1) throws SQLException {
		return null;
	}

	public Timestamp getTimestamp(int arg0, Calendar arg1) throws SQLException {
		return null;
	}

	public Timestamp getTimestamp(String arg0, Calendar arg1) throws SQLException {
		return null;
	}

	public URL getURL(int column) throws SQLException {
		return null;
	}

	public URL getURL(String arg0) throws SQLException {
		return null;
	}

	public void updateRef(int arg0, Ref arg1) throws SQLException {
		return;
	}

	public void updateRef(String arg0, Ref arg1) throws SQLException {
		return;
	}

	public void updateBlob(int arg0, Blob arg1) throws SQLException {
		return;
	}

	public void updateBlob(String arg0, Blob arg1) throws SQLException {
		return;
	}

	public void updateClob(int arg0, Clob arg1) throws SQLException {
		return;
	}

	public void updateClob(String arg0, Clob arg1) throws SQLException {
		return;
	}

	public void updateArray(int arg0, Array arg1) throws SQLException {
		return;
	}

	public void updateArray(String arg0, Array arg1) throws SQLException {
		return;
	}

	/**
	 * Retrieves the value of the designated column in the current row of this
	 * <code>ResultSet</code> object as a <code>java.sql.RowId</code> object in the Java
	 * programming language.
	 *
	 * @param columnIndex the first column is 1, the second 2, ...
	 * @return the column value; if the value is a SQL <code>NULL</code> the
	 * value returned is <code>null</code>
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row of this
	 * <code>ResultSet</code> object as a <code>java.sql.RowId</code> object in the Java
	 * programming language.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @return the column value ; if the value is a SQL <code>NULL</code> the
	 * value returned is <code>null</code>
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return null;
	}

	/**
	 * Updates the designated column with a <code>RowId</code> value. The updater
	 * methods are used to update column values in the current row or the insert
	 * row. The updater methods do not update the underlying database; instead
	 * the <code>updateRow</code> or <code>insertRow</code> methods are called
	 * to update the database.
	 *
	 * @param columnIndex the first column is 1, the second 2, ...
	 * @param x           the column value
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {

	}

	/**
	 * Updates the designated column with a <code>RowId</code> value. The updater
	 * methods are used to update column values in the current row or the insert
	 * row. The updater methods do not update the underlying database; instead
	 * the <code>updateRow</code> or <code>insertRow</code> methods are called
	 * to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param x           the column value
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {

	}

	/**
	 * Retrieves the holdability of this <code>ResultSet</code> object
	 *
	 * @return either <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
	 * @throws SQLException if a database access error occurs
	 *                      or this method is called on a closed result set
	 * @since 1.6
	 */
	@Override
	public int getHoldability() throws SQLException {
		return 0;
	}

	/**
	 * Retrieves whether this <code>ResultSet</code> object has been closed. A <code>ResultSet</code> is closed if the
	 * method close has been called on it, or if it is automatically closed.
	 *
	 * @return true if this <code>ResultSet</code> object is closed; false if it is still open
	 * @throws SQLException if a database access error occurs
	 * @since 1.6
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	/**
	 * Updates the designated column with a <code>String</code> value.
	 * It is intended for use when updating <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second 2, ...
	 * @param nString     the value for the column to be updated
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or if a database access error occurs
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {

	}

	/**
	 * Updates the designated column with a <code>String</code> value.
	 * It is intended for use when updating <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param nString     the value for the column to be updated
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set;
	 *                                         the result set concurrency is <CODE>CONCUR_READ_ONLY</code>
	 *                                         or if a database access error occurs
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {

	}

	/**
	 * Updates the designated column with a <code>java.sql.NClob</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second 2, ...
	 * @param nClob       the value for the column to be updated
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set;
	 *                                         if a database access error occurs or
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

	}

	/**
	 * Updates the designated column with a <code>java.sql.NClob</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param nClob       the value for the column to be updated
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set;
	 *                                         if a database access error occurs or
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>NClob</code> object
	 * in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return a <code>NClob</code> object representing the SQL
	 * <code>NCLOB</code> value in the specified column
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set
	 *                                         or if a database access error occurs
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>NClob</code> object
	 * in the Java programming language.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @return a <code>NClob</code> object representing the SQL <code>NCLOB</code>
	 * value in the specified column
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set
	 *                                         or if a database access error occurs
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in  the current row of
	 * this <code>ResultSet</code> as a
	 * <code>java.sql.SQLXML</code> object in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return a <code>SQLXML</code> object that maps an <code>SQL XML</code> value
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in  the current row of
	 * this <code>ResultSet</code> as a
	 * <code>java.sql.SQLXML</code> object in the Java programming language.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @return a <code>SQLXML</code> object that maps an <code>SQL XML</code> value
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return null;
	}

	/**
	 * Updates the designated column with a <code>java.sql.SQLXML</code> value.
	 * The updater
	 * methods are used to update column values in the current row or the insert
	 * row. The updater methods do not update the underlying database; instead
	 * the <code>updateRow</code> or <code>insertRow</code> methods are called
	 * to update the database.
	 * <p>
	 *
	 * @param columnIndex the first column is 1, the second 2, ...
	 * @param xmlObject   the value for the column to be updated
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs; this method
	 *                                         is called on a closed result set;
	 *                                         the <code>java.xml.transform.Result</code>,
	 *                                         <code>Writer</code> or <code>OutputStream</code> has not been closed
	 *                                         for the <code>SQLXML</code> object;
	 *                                         if there is an error processing the XML value or
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>.  The <code>getCause</code> method
	 *                                         of the exception may provide a more detailed exception, for example, if the
	 *                                         stream does not contain valid XML.
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

	}

	/**
	 * Updates the designated column with a <code>java.sql.SQLXML</code> value.
	 * The updater
	 * methods are used to update column values in the current row or the insert
	 * row. The updater methods do not update the underlying database; instead
	 * the <code>updateRow</code> or <code>insertRow</code> methods are called
	 * to update the database.
	 * <p>
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param xmlObject   the column value
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs; this method
	 *                                         is called on a closed result set;
	 *                                         the <code>java.xml.transform.Result</code>,
	 *                                         <code>Writer</code> or <code>OutputStream</code> has not been closed
	 *                                         for the <code>SQLXML</code> object;
	 *                                         if there is an error processing the XML value or
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>.  The <code>getCause</code> method
	 *                                         of the exception may provide a more detailed exception, for example, if the
	 *                                         stream does not contain valid XML.
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>String</code> in the Java programming language.
	 * It is intended for use when
	 * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public String getNString(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>String</code> in the Java programming language.
	 * It is intended for use when
	 * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public String getNString(String columnLabel) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a
	 * <code>java.io.Reader</code> object.
	 * It is intended for use when
	 * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return a <code>java.io.Reader</code> object that contains the column
	 * value; if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language.
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a
	 * <code>java.io.Reader</code> object.
	 * It is intended for use when
	 * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @return a <code>java.io.Reader</code> object that contains the column
	 * value; if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return null;
	}

	/**
	 * Updates the designated column with a character stream value, which will have
	 * the specified number of bytes.   The
	 * driver does the necessary conversion from Java character format to
	 * the national character set in the database.
	 * It is intended for use when
	 * updating  <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x           the new column value
	 * @param length      the length of the stream
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code> or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

	}

	/**
	 * Updates the designated column with a character stream value, which will have
	 * the specified number of bytes.  The
	 * driver does the necessary conversion from Java character format to
	 * the national character set in the database.
	 * It is intended for use when
	 * updating  <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param reader      the <code>java.io.Reader</code> object containing
	 *                    the new column value
	 * @param length      the length of the stream
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code> or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

	}

	/**
	 * Updates the designated column with an ascii stream value, which will have
	 * the specified number of bytes.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x           the new column value
	 * @param length      the length of the stream
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

	}

	/**
	 * Updates the designated column with a binary stream value, which will have
	 * the specified number of bytes.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x           the new column value
	 * @param length      the length of the stream
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

	}

	/**
	 * Updates the designated column with a character stream value, which will have
	 * the specified number of bytes.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x           the new column value
	 * @param length      the length of the stream
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

	}

	/**
	 * Updates the designated column with an ascii stream value, which will have
	 * the specified number of bytes.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param x           the new column value
	 * @param length      the length of the stream
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

	}

	/**
	 * Updates the designated column with a binary stream value, which will have
	 * the specified number of bytes.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param x           the new column value
	 * @param length      the length of the stream
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

	}

	/**
	 * Updates the designated column with a character stream value, which will have
	 * the specified number of bytes.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param reader      the <code>java.io.Reader</code> object containing
	 *                    the new column value
	 * @param length      the length of the stream
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

	}

	/**
	 * Updates the designated column using the given input stream, which
	 * will have the specified number of bytes.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param inputStream An object that contains the data to set the parameter
	 *                    value to.
	 * @param length      the number of bytes in the parameter data.
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

	}

	/**
	 * Updates the designated column using the given input stream, which
	 * will have the specified number of bytes.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param inputStream An object that contains the data to set the parameter
	 *                    value to.
	 * @param length      the number of bytes in the parameter data.
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

	}

	/**
	 * Updates the designated column using the given <code>Reader</code>
	 * object, which is the given number of characters long.
	 * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.Reader</code> object. The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param reader      An object that contains the data to set the parameter value to.
	 * @param length      the number of characters in the parameter data.
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

	}

	/**
	 * Updates the designated column using the given <code>Reader</code>
	 * object, which is the given number of characters long.
	 * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.Reader</code> object.  The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param reader      An object that contains the data to set the parameter value to.
	 * @param length      the number of characters in the parameter data.
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

	}

	/**
	 * Updates the designated column using the given <code>Reader</code>
	 * object, which is the given number of characters long.
	 * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.Reader</code> object. The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second 2, ...
	 * @param reader      An object that contains the data to set the parameter value to.
	 * @param length      the number of characters in the parameter data.
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set,
	 *                                         if a database access error occurs or
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

	}

	/**
	 * Updates the designated column using the given <code>Reader</code>
	 * object, which is the given number of characters long.
	 * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.Reader</code> object. The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param reader      An object that contains the data to set the parameter value to.
	 * @param length      the number of characters in the parameter data.
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set;
	 *                                         if a database access error occurs or
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

	}

	/**
	 * Updates the designated column with a character stream value.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.  The
	 * driver does the necessary conversion from Java character format to
	 * the national character set in the database.
	 * It is intended for use when
	 * updating  <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateNCharacterStream</code> which takes a length parameter.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x           the new column value
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code> or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

	}

	/**
	 * Updates the designated column with a character stream value.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.  The
	 * driver does the necessary conversion from Java character format to
	 * the national character set in the database.
	 * It is intended for use when
	 * updating  <code>NCHAR</code>,<code>NVARCHAR</code>
	 * and <code>LONGNVARCHAR</code> columns.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateNCharacterStream</code> which takes a length parameter.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param reader      the <code>java.io.Reader</code> object containing
	 *                    the new column value
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code> or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

	}

	/**
	 * Updates the designated column with an ascii stream value.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateAsciiStream</code> which takes a length parameter.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x           the new column value
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

	}

	/**
	 * Updates the designated column with a binary stream value.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateBinaryStream</code> which takes a length parameter.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x           the new column value
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

	}

	/**
	 * Updates the designated column with a character stream value.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateCharacterStream</code> which takes a length parameter.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x           the new column value
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

	}

	/**
	 * Updates the designated column with an ascii stream value.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateAsciiStream</code> which takes a length parameter.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param x           the new column value
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

	}

	/**
	 * Updates the designated column with a binary stream value.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateBinaryStream</code> which takes a length parameter.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param x           the new column value
	 * @throws SQLException                    if the columnLabel is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

	}

	/**
	 * Updates the designated column with a character stream value.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateCharacterStream</code> which takes a length parameter.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param reader      the <code>java.io.Reader</code> object containing
	 *                    the new column value
	 * @throws SQLException                    if the columnLabel is not valid; if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

	}

	/**
	 * Updates the designated column using the given input stream. The data will be read from the stream
	 * as needed until end-of-stream is reached.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateBlob</code> which takes a length parameter.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param inputStream An object that contains the data to set the parameter
	 *                    value to.
	 * @throws SQLException                    if the columnIndex is not valid; if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

	}

	/**
	 * Updates the designated column using the given input stream. The data will be read from the stream
	 * as needed until end-of-stream is reached.
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateBlob</code> which takes a length parameter.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param inputStream An object that contains the data to set the parameter
	 *                    value to.
	 * @throws SQLException                    if the columnLabel is not valid; if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

	}

	/**
	 * Updates the designated column using the given <code>Reader</code>
	 * object.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.  The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateClob</code> which takes a length parameter.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param reader      An object that contains the data to set the parameter value to.
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {

	}

	/**
	 * Updates the designated column using the given <code>Reader</code>
	 * object.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.  The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateClob</code> which takes a length parameter.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param reader      An object that contains the data to set the parameter value to.
	 * @throws SQLException                    if the columnLabel is not valid; if a database access error occurs;
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 *                                         or this method is called on a closed result set
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {

	}

	/**
	 * Updates the designated column using the given <code>Reader</code>
	 * <p>
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.  The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateNClob</code> which takes a length parameter.
	 *
	 * @param columnIndex the first column is 1, the second 2, ...
	 * @param reader      An object that contains the data to set the parameter value to.
	 * @throws SQLException                    if the columnIndex is not valid;
	 *                                         if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set,
	 *                                         if a database access error occurs or
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {

	}

	/**
	 * Updates the designated column using the given <code>Reader</code>
	 * object.
	 * The data will be read from the stream
	 * as needed until end-of-stream is reached.  The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 * <p>
	 * <p>
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 * <p>
	 * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
	 * it might be more efficient to use a version of
	 * <code>updateNClob</code> which takes a length parameter.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.  If the SQL AS clause was not specified, then the label is the name of the column
	 * @param reader      An object that contains the data to set the parameter value to.
	 * @throws SQLException                    if the columnLabel is not valid; if the driver does not support national
	 *                                         character sets;  if the driver can detect that a data conversion
	 *                                         error could occur; this method is called on a closed result set;
	 *                                         if a database access error occurs or
	 *                                         the result set concurrency is <code>CONCUR_READ_ONLY</code>
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.6
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {

	}

	/**
	 * <p>Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object and will convert from the
	 * SQL type of the column to the requested Java data type, if the
	 * conversion is supported. If the conversion is not
	 * supported  or null is specified for the type, a
	 * <code>SQLException</code> is thrown.
	 * <p>
	 * At a minimum, an implementation must support the conversions defined in
	 * Appendix B, Table B-3 and conversion of appropriate user defined SQL
	 * types to a Java type which implements {@code SQLData}, or {@code Struct}.
	 * Additional conversions may be supported and are vendor defined.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param type        Class representing the Java data type to convert the designated
	 *                    column to.
	 * @return an instance of {@code type} holding the column value
	 * @throws SQLException                    if conversion is not supported, type is null or
	 *                                         another error occurs. The getCause() method of the
	 *                                         exception may provide a more detailed exception, for example, if
	 *                                         a conversion error occurs
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.7
	 */
	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return null;
	}

	/**
	 * <p>Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object and will convert from the
	 * SQL type of the column to the requested Java data type, if the
	 * conversion is supported. If the conversion is not
	 * supported  or null is specified for the type, a
	 * <code>SQLException</code> is thrown.
	 * <p>
	 * At a minimum, an implementation must support the conversions defined in
	 * Appendix B, Table B-3 and conversion of appropriate user defined SQL
	 * types to a Java type which implements {@code SQLData}, or {@code Struct}.
	 * Additional conversions may be supported and are vendor defined.
	 *
	 * @param columnLabel the label for the column specified with the SQL AS clause.
	 *                    If the SQL AS clause was not specified, then the label is the name
	 *                    of the column
	 * @param type        Class representing the Java data type to convert the designated
	 *                    column to.
	 * @return an instance of {@code type} holding the column value
	 * @throws SQLException                    if conversion is not supported, type is null or
	 *                                         another error occurs. The getCause() method of the
	 *                                         exception may provide a more detailed exception, for example, if
	 *                                         a conversion error occurs
	 * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
	 *                                         this method
	 * @since 1.7
	 */
	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return null;
	}

	/**
	 * Returns an object that implements the given interface to allow access to
	 * non-standard methods, or standard methods not exposed by the proxy.
	 * <p>
	 * If the receiver implements the interface then the result is the receiver
	 * or a proxy for the receiver. If the receiver is a wrapper
	 * and the wrapped object implements the interface then the result is the
	 * wrapped object or a proxy for the wrapped object. Otherwise return the
	 * the result of calling <code>unwrap</code> recursively on the wrapped object
	 * or a proxy for that result. If the receiver is not a
	 * wrapper and does not implement the interface, then an <code>SQLException</code> is thrown.
	 *
	 * @param iface A Class defining an interface that the result must implement.
	 * @return an object that implements the interface. May be a proxy for the actual implementing object.
	 * @throws SQLException If no object found that implements the interface
	 * @since 1.6
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	/**
	 * Returns true if this either implements the interface argument or is directly or indirectly a wrapper
	 * for an object that does. Returns false otherwise. If this implements the interface then return true,
	 * else if this is a wrapper then return the result of recursively calling <code>isWrapperFor</code> on the wrapped
	 * object. If this does not implement the interface and is not a wrapper, return false.
	 * This method should be implemented as a low-cost operation compared to <code>unwrap</code> so that
	 * callers can use this method to avoid expensive <code>unwrap</code> calls that may fail. If this method
	 * returns true then calling <code>unwrap</code> with the same argument should succeed.
	 *
	 * @param iface a Class defining an interface.
	 * @return true if this implements the interface or directly or indirectly wraps an object that does.
	 * @throws SQLException if an error occurs while determining whether this is a wrapper
	 *                      for an object with the given interface.
	 * @since 1.6
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}
}
