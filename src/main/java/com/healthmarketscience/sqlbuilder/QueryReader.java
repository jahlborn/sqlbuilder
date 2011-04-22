/*
Copyright (c) 2008 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.sqlbuilder;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.healthmarketscience.common.util.AppendableExt;


/**
 * Helper class which keeps track of the column positions in dynamically
 * generated select statements so that the query user can easily get the
 * results correctly, especially useful where the code which generates the
 * query is separate from the code which uses the query.
 *
 * A QueryReader may only be used for a single query generation.  Also, the
 * Columns will not have a valid position until the query is actually
 * converted to a string.  After that, the Columns will never change their
 * stored index(es), so reuse in a new query is impossible.  Likewise, the
 * state of the QueryReader is altered by the query generation, so, it cannot
 * be used in a new query either.  However, the QueryReader utility
 * is designed so that it is not modified after query generation, so it can
 * safely be used concurrently with the original query string as long as
 * desired (so it can safely be used in a static context along with the
 * associated query string).
 *
 * Examples:
 * <pre>
 * 
 *   // example where another class is generating the actual query
 *   QueryReader reader = new QueryReader();
 *   QueryReader.Column col1 = reader.getNewColumn();
 *   QueryReader.Column col2 = reader.getNewColumn();
 *   String queryStr = otherObj.createQuery(col1, col2);
 *   Statement stmt = conn.createStatement();
 *   ResultSet rs = stmt.executeQuery(queryStr);
 *   while(rs.hasNext()) {
 *     String col1Str = col1.getString(rs);
 *     int col2Int = col2.getInt(rs);
 *     // ... handle results ...
 *   }
 *
 *  // example query generation
 *  QueryReader.Column rCol1;
 *  QueryReader.Column rCol2;
 *  Column col1, col2, idCol;
 *
 *  String queryStr = new SelectQuery()
 *    .addCustomColumns(rCol1.setColumnObject(col1),
 *                      rCol2.setColumnObject(col2))
 *    .setCondition(
 *      UnaryCondition.isNotNull(idCol)).validate().toString();
 *   
 * </pre>
 *
 * @author James Ahlborn
 */
public class QueryReader {

  /** the default first index that will be assigned to a column */
  public static final int DEFAULT_START_INDEX = 1;
  /** the value for a Column's index when no index has been assigned yet.
      If the query has been generated, and this is the value for the
      Column's index, then that Column was not included in the
      query. */
  private static final int NO_INDEX = -1;

  /** the index that will be assigned to the next Column written to the
      query */
  private int _curIndex;

  public QueryReader() {
    this(DEFAULT_START_INDEX);
  }

  /**
   * Creates a QueryReader with a different startIndex from the default.
   * This may be useful if there are other columns in the ResultSet
   * which are not owned by this QueryReader.
   */
  public QueryReader(int startIndex) {
    if(startIndex < DEFAULT_START_INDEX) {
      throw new IllegalArgumentException("invalid start index");
    }
    _curIndex = startIndex;
  }

  /**
   * @return a new Column tied to this QueryReader.  Its internal state
   *         is not valid until the query is converted to an actual string.
   *         Also, it can only be used in <i>one</i> place in the query.
   */
  public Column getNewColumn() {
    return new Column(this);
  }
  
  /**
   * A SqlObject which outputs the passed in SqlObject, and records the
   * current index at the time the <code>appendTo</code> method is called.
   * This enables the user to get parameters correctly from a ResultSet
   * where the position is not known at query creation time.
   *
   * Note: a Column may not be used in more than one place in the query.
   */
  public static class Column extends SqlObject
  {
    /** handle to the owning QueryReader */
    private QueryReader _outer;
    /** the current index of this Column in the generated query.  Not
        valid until after the query has been converted to a string.  If the
        value is NO_INDEX at that time, this Column was not used in the
        query. */
    private int _index = NO_INDEX;
    /** the actual column sql string */
    private SqlObject _columnObj;
    
    public Column(QueryReader outer) {
      _outer = outer;
    }

    /**
     * Sets the actual sql to be generated by this column.
     * @return a handle to this object so that it can be used "builder" style.
     */
    public Column setColumnObject(com.healthmarketscience.sqlbuilder.dbspec.Column columnObj) {
      return setCustomColumnObject(columnObj);
    }

    /**
     * Sets the actual sql to be generated by this column.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#toColumnSqlObject(Object)}.
     * 
     * @return a handle to this object so that it can be used "builder" style.
     */
    public Column setCustomColumnObject(Object columnObj) {
      _columnObj = Converter.toColumnSqlObject(columnObj);
      return this;
    }

    /**
     * @return a handle to the actual sql object which will generate the sql
     *         for this column.
     */
    public SqlObject getCustomColumnObject() {
      return _columnObj;
    }
    
    public boolean isInQuery() {
      return(getIndex() != NO_INDEX);
    }
    
    private void setIndex(int index) {
      _index = index;
    }

    public int getIndex() { return _index; }

    protected void addIndex(int index) {
      if(getIndex() == NO_INDEX) {
        setIndex(index);
      } else {
        throw new IllegalStateException("attempt to reset value of Column, cannot reuse or use multiple times in the same query");
      }
    }
    
    /**
     * Calls getInt on the given ResultSet for the position of this Column.
     * <p>
     * Since there is no possible {@code null} value return for this method if
     * this Column was not in the query, the caller is expected to already
     * know if this is a valid call (possibly via {@link #isInQuery}).
     */
    public int getInt(ResultSet rs)
      throws SQLException
    {
      return rs.getInt(getIndex());
    }
    
    /**
     * Calls getLong on the given ResultSet for the position of this Column.
     * <p>
     * Since there is no possible {@code null} value return for this method if
     * this Column was not in the query, the caller is expected to already
     * know if this is a valid call (possibly via {@link #isInQuery}).
     */
    public long getLong(ResultSet rs)
      throws SQLException
    {
      return rs.getLong(getIndex());
    }
    
    /**
     * Calls getBoolean on the given ResultSet for the position of this
     * Column.
     * <p>
     * Since there is no possible {@code null} value return for this method if
     * this Column was not in the query, the caller is expected to already
     * know if this is a valid call (possibly via {@link #isInQuery}).
     */
    public boolean getBoolean(ResultSet rs)
      throws SQLException
    {
      return rs.getBoolean(getIndex());
    }
    
    /**
     * Calls getString on the given ResultSet for the position of this Column.
     * <p>
     * Returns {@code null} if this Column was not in the query.
     */
    public String getString(ResultSet rs)
      throws SQLException
    {
      if(isInQuery()) {
        return rs.getString(getIndex());
      }
      return null;
    }
    
    /**
     * Calls getObject on the given ResultSet for the position of this Column.
     * <p>
     * Returns {@code null} if this Column was not in the query.
     */
    public Object getObject(ResultSet rs)
      throws SQLException
    {
      if(isInQuery()) {
        return rs.getObject(getIndex());
      }
      return null;
    }

    /**
     * Calls updateNull on the given ResultSet with the given sql type
     * for the position of this PlaceHolder.
     */
    public void updateNull(ResultSet rs)
      throws SQLException
    {
      if(isInQuery()) {
        rs.updateNull(getIndex());
      }
    }
    
    /**
     * Calls updateInt on the given ResultSet with the given value
     * for the position of this PlaceHolder.
     */
    public void updateInt(int value, ResultSet rs)
      throws SQLException
    {
      if(isInQuery()) {
        rs.updateInt(getIndex(), value);
      }
    }
    
    /**
     * Calls updateInt on the given ResultSet with the given value for
     * the position of this PlaceHolder.  If given value is <code>null</code>,
     * calls updateNull with the sql type <code>INTEGER</code>.
     */
    public void updateInt(Integer value, ResultSet rs)
      throws SQLException
    {
      if(value != null) {
        updateInt((int)value, rs);
      } else {
        updateNull(rs);
      }
    }
    
    /**
     * Calls updateLong on the given ResultSet with the given value
     * for the position of this PlaceHolder.
     */
    public void updateLong(long value, ResultSet rs)
      throws SQLException
    {
      if(isInQuery()) {
        rs.updateLong(getIndex(), value);
      }
    }
    
    /**
     * Calls updateLong on the given ResultSet with the given value for
     * the position of this PlaceHolder.  If given value is <code>null</code>,
     * calls updateNull with the sql type <code>BIGINT</code>.
     */
    public void updateLong(Long value, ResultSet rs)
      throws SQLException
    {
      if(value != null) {
        updateLong((long)value, rs);
      } else {
        updateNull(rs);
      }
    }
    
    /**
     * Calls updateBoolean on the given ResultSet with the given value
     * for the position of this PlaceHolder.
     */
    public void updateBoolean(boolean value, ResultSet rs)
      throws SQLException
    {
      if(isInQuery()) {
        rs.updateBoolean(getIndex(), value);
      }
    }
    
    /**
     * Calls updateBoolean on the given ResultSet with the given value for
     * the position of this PlaceHolder.  If given value is <code>null</code>,
     * calls updateNull with the sql type <code>BOOLEAN</code>.
     */
    public void updateBoolean(Boolean value, ResultSet rs)
      throws SQLException
    {
      if(value != null) {
        updateBoolean((boolean)value, rs);
      } else {
        updateNull(rs);
      }
    }

    /**
     * Calls updateString on the given ResultSet with the given value
     * for the position of this PlaceHolder.
     */
    public void updateString(String value, ResultSet rs)
      throws SQLException
    {
      if(value != null) {
        if(isInQuery()) {
          rs.updateString(getIndex(), value);
        }
      } else {
        updateNull(rs);
      }
    }
    
    /**
     * Calls updateObject on the given ResultSet with the given value for the
     * position of this PlaceHolder.
     */
    public void updateObject(Object value, ResultSet rs)
      throws SQLException
    {
      if(value != null) {
        if(isInQuery()) {
          rs.updateObject(getIndex(), value);
        }
      } else {
        updateNull(rs);
      }
    }
        
    @Override
    protected void collectSchemaObjects(ValidationContext vContext)
    {
      _columnObj.collectSchemaObjects(vContext);
    }

    @Override
    public final void appendTo(AppendableExt app) throws IOException {
      addIndex(_outer._curIndex++);
      app.append(_columnObj);
    }
    
  }

}
