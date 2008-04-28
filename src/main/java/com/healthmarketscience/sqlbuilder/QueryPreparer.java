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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import com.healthmarketscience.common.util.AppendableExt;


/**
 * Helper class which keeps track of '?' positions in dynamically generated
 * prepared statements so that the query user can easily set the parameters
 * correctly, especially useful where the code which generates the prepared
 * query is separate from the code which uses the prepared query.
 *
 * A QueryPreparer may only be used for a single query generation.  Also, the
 * PlaceHolders will not have a valid position until the query is actually
 * converted to a string.  After that, the PlaceHolders will never change
 * their stored index(es), so reuse in a new query is impossible.  Likewise,
 * the state of the QueryPreparer is altered by the query generation, so, it
 * cannot be used in a new query either.  However, the QueryPreparer utility
 * is designed so that it is not modified after query generation, so it can
 * safely be used concurrently with the original query string as long as
 * desired (so it can safely be used in a static context along with the
 * associated query string).
 *
 * There are two main types of placeholders supported normal (dynamic) and
 * static.  Normal placeholders consist of two sub-types (the
 * {@link PlaceHolder} and {@link MultiPlaceHolder} classes) and should be
 * used for values which are going to change for each invocation of the
 * PreparedStatement.  A PlaceHolder may only be used in one place in a query,
 * while a MultiPlaceHolder can be re-used multiple times in the same query.
 * Static placeholders (subclasses of the
 * {@link StaticPlaceHolder} class) are a convenience placeholder which can be
 * used for values which will not change across invocations of a
 * PreparedStatement (and can also only be used in one place in a query, like
 * a PlaceHolder).
 *
 * Examples:
 * <pre>
 * 
 *   // example using placeholders where another class is generating
 *   // the actual query
 *   QueryPreparer preparer = new QueryPreparer();
 *   QueryPreparer.PlaceHolder param1PH = preparer.getNewPlaceHolder();
 *   QueryPreparer.PlaceHolder param2PH = preparer.getNewPlaceHolder();
 *   String prepQueryStr = otherObj.createQuery(param1PH, param2PH);
 *   PreparedStatement ps = conn.prepareStatement(prepQueryStr);
 *   for(Param param : paramList) {
 *     param1PH.setLong(param.getValue1(), ps);
 *     param2PH.setString(param.getValue2(), ps);
 *     ResultSet rs = ps.executeQuery();
 *     // ... parse results ...
 *   }
 *
 *   // example using static placeholders and regular placeholders where
 *   // this class is generating the query
 *   Long param1 = 13;
 *   String param2 = "foo";
 *   QueryPreparer preparer = new QueryPreparer();
 *   QueryPreparer.PlaceHolder param3PH = preparer.getNewPlaceHolder();
 *   String prepQueryStr = new SelectQuery()
 *     .addAllColumns()
 *     .addCustomTable(table)
 *     .setCondition(
 *       ComboCondition.and(
 *         BinaryCondition.eq(col1,
 *                            preparer.addStaticPlaceHolder(param1)),
 *         BinaryCondition.eq(col2,
 *                            preparer.addStaticPlaceHolder(param2)),
 *         BinaryCondition.eq(col3, param3PH)))
 *     .validate.toString();
 *   PreparedStatement ps = conn.prepareStatement(prepQueryStr);
 *   for(String param3 : param3List) {
 *     preparer.setStaticValues(ps);  // sets param1, param2
 *     param3PH.setString(param3, ps);
 *     ResultSet rs = ps.executeQuery();
 *     // ... parse results ...
 *   }
 *   
 * </pre>
 *
 * @author James Ahlborn
 */
public class QueryPreparer
{
  /** the default first index that will be assigned to a placeholder */
  public static final int DEFAULT_START_INDEX = 1;
  /** the value for a PlaceHolder's index when no index has been assigned yet.
      If the query has been generated, and this is the value for the
      PlaceHolder's index, then that PlaceHolder was not included in the
      query. */
  private static final int NO_INDEX = -1;

  /** the index that will be assigned to the next PlaceHolder written to the
      query */
  private int _curIndex;
  /** the list of any StaticPlaceHolders in this query, may be null */
  private List<StaticPlaceHolder> _staticPlaceHolders;
  
  public QueryPreparer() {
    this(DEFAULT_START_INDEX);
  }
  
  /**
   * outputs the static place holders in order
   */
  @Override
  public String toString(){
    Formatter fmt = new Formatter();

    if(_staticPlaceHolders != null) {
      
      for(StaticPlaceHolder placeHolder : _staticPlaceHolders){
        if(placeHolder != null) {
          fmt.format("[%s] %s%n",
                     placeHolder.getClass().getSimpleName(),
                     placeHolder.displayToString());
        } else {
          // this is generally not a good thing, but don't fail here
          fmt.format("<null>???");
        }
      }
      
    } else {
      
      fmt.format("<No static place holders>%n");
    }

    return fmt.toString();
  }

  /**
   * Creates a QueryPreparer with a different startIndex from the default.
   * This may be useful if there are other parameters in the PreparedStatement
   * which are not owned by this QueryPreparer.
   */
  public QueryPreparer(int startIndex) {
    if(startIndex < DEFAULT_START_INDEX) {
      throw new IllegalArgumentException("invalid start index");
    }
    _curIndex = startIndex;
  }

  /**
   * @return a new PlaceHolder tied to this QueryPreparer.  Its internal state
   *         is not valid until the query is converted to an actual string.
   *         Also, it can only be used in <i>one</i> place in the query.
   */
  public PlaceHolder getNewPlaceHolder() {
    return new PlaceHolder(this);
  }

  /**
   * @return a new MultiPlaceHolder tied to this QueryPreparer.  Its internal
   *         state is not valid until the query is converted to an actual
   *         string.  It can be used in multiple places in the same query.
   */
  public MultiPlaceHolder getNewMultiPlaceHolder() {
    return new MultiPlaceHolder(this);
  }

  /**
   * @return a new StaticPlaceHolder which will always insert the given String
   *         value
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(String val) {
		return addStaticPlaceHolder(new StringStaticPlaceHolder(val, this));
  }

  /**
   * @return a new StaticPlaceHolder which will always insert the given Long
   *         value
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(Long val) {
    if(val != null) {
      return addStaticPlaceHolder(new LongStaticPlaceHolder(val, this));
    }
    return addStaticPlaceHolder(new NullStaticPlaceHolder(Types.BIGINT, this));
  }
  
  /**
   * @return a new StaticPlaceHolder which will always insert the given
   *         Integer value
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(Integer val) {
    if(val != null) {
      return addStaticPlaceHolder(new IntegerStaticPlaceHolder(val, this));
    }
    return addStaticPlaceHolder(new NullStaticPlaceHolder(Types.INTEGER,
                                                          this));
  }

  /**
   * @return a new StaticPlaceHolder which will always insert the given
   *         Boolean value
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(Boolean val) {
    if(val != null) {
      return addStaticPlaceHolder(new BooleanStaticPlaceHolder(val, this));
    }
    return addStaticPlaceHolder(new NullStaticPlaceHolder(Types.BOOLEAN,
                                                          this));
  }

  /**
   * @return a new StaticPlaceHolder which will always insert the given long
   *         value
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(long val) {
    return addStaticPlaceHolder(new LongStaticPlaceHolder(val, this));
  }
  
  /**
   * @return a new StaticPlaceHolder which will always insert the given int
   *         value
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(int val) {
    return addStaticPlaceHolder(new IntegerStaticPlaceHolder(val, this));
  }

  /**
   * @return a new StaticPlaceHolder which will always insert the given
   *         boolean value
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(boolean val) {
    return addStaticPlaceHolder(new BooleanStaticPlaceHolder(val, this));
  }

  /**
   * @return a new StaticPlaceHolder which will always insert the given Object
   *         value
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(Object obj) {
    return addStaticPlaceHolder(new ObjectStaticPlaceHolder<Object>(obj,
                                                                    this));
  }

  /**
   * @return a new StaticPlaceHolder which will always insert the given Object
   *         value as the given sql type
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(Object obj, int sqlType) {
    return addStaticPlaceHolder(new TypedStaticPlaceHolder(obj, sqlType,
                                                           this));
  }
  
  /**
   * Adds a new StaticPlaceHolder to the list maintained by this class
   * @return the given StaticPlaceHolder
   * @see #setStaticValues
   */
  public StaticPlaceHolder addStaticPlaceHolder(StaticPlaceHolder ph) {
    if(_staticPlaceHolders == null) {
      _staticPlaceHolders =  new ArrayList<StaticPlaceHolder>();
    }
    _staticPlaceHolders.add(ph);
    return ph;
  }

  /**
   * Calls {@link StaticPlaceHolder#setValue} on all the StaticPlaceHolders
   * held by this class with the given PreparedStatement.
   */
  public void setStaticValues(PreparedStatement ps)
    throws SQLException
  {
    if(_staticPlaceHolders != null) {
      for(StaticPlaceHolder ph : _staticPlaceHolders) {
        ph.setValue(ps);
      }
    }
  }

  /**
   * A SqlObject which outputs a '?', and records the current index at the
   * time the <code>appendTo</code> method is called.  This enables the user
   * to set parameters correctly in a PreparedStatement where the position is
   * not known at query creation time.
   *
   * Note: a PlaceHolder may not be used in more than one place in
   * the query.  For this functionality, use {@link MultiPlaceHolder}.
   */
  public static class PlaceHolder extends SqlObject
  {
    /** handle to the owning QueryPreparer */
    private QueryPreparer _outer;
    /** the current index of this PlaceHolder in the generated query.  Not
        valid until after the query has been converted to a string.  If the
        value is NO_INDEX at that time, this PlaceHolder was not used in the
        query. */
    private int _index = NO_INDEX;

    public PlaceHolder(QueryPreparer outer) {
      _outer = outer;
    }

    /**
     * Returns {@code true} if this PlaceHolder was used in the query,
     * {@code false} otherwise.
     */
    public boolean isInQuery() {
      return(getIndex() != NO_INDEX);
    }
    
    private void setIndex(int index) {
      _index = index;
    }

    /**
     * Returns the 1-based index of this PlaceHolder in the query.
     * @return 1-based index of this PlaceHolder if the query has been
     *         generated, {@value QueryPreparer#NO_INDEX} otherwise
     */
    public int getIndex() {
      return _index;
    }

    public List<Integer> getIndexes() {
      return Collections.singletonList(_index);
    }
    
    protected void addIndex(int index) {
      if(getIndex() == NO_INDEX) {
        setIndex(index);
      } else {
        throw new IllegalStateException("attempt to reset value of PlaceHolder, cannot reuse or use multiple times in the same query");
      }
    }
    
    /**
     * Calls setNull on the given PreparedStatement with the given sql type
     * for the position of this PlaceHolder.
     */
    public void setNull(int sqlType, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        ps.setNull(getIndex(), sqlType);
      }
    }
    
    /**
     * Calls setInt on the given PreparedStatement with the given value
     * for the position of this PlaceHolder.
     */
    public void setInt(int value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        ps.setInt(getIndex(), value);
      }
    }
    
    /**
     * Calls setInt on the given PreparedStatement with the given value for
     * the position of this PlaceHolder.  If given value is <code>null</code>,
     * calls setNull with the sql type <code>INTEGER</code>.
     */
    public void setInt(Integer value, PreparedStatement ps)
      throws SQLException
    {
      if(value != null) {
        setInt((int)value, ps);
      } else {
        setNull(Types.INTEGER, ps);
      }
    }
    
    /**
     * Calls setLong on the given PreparedStatement with the given value
     * for the position of this PlaceHolder.
     */
    public void setLong(long value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        ps.setLong(getIndex(), value);
      }
    }
    
    /**
     * Calls setLong on the given PreparedStatement with the given value for
     * the position of this PlaceHolder.  If given value is <code>null</code>,
     * calls setNull with the sql type <code>BIGINT</code>.
     */
    public void setLong(Long value, PreparedStatement ps)
      throws SQLException
    {
      if(value != null) {
        setLong((long)value, ps);
      } else {
        setNull(Types.BIGINT, ps);
      }
    }
    
    /**
     * Calls setBoolean on the given PreparedStatement with the given value
     * for the position of this PlaceHolder.
     */
    public void setBoolean(boolean value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        ps.setBoolean(getIndex(), value);
      }
    }
    
    /**
     * Calls setBoolean on the given PreparedStatement with the given value for
     * the position of this PlaceHolder.  If given value is <code>null</code>,
     * calls setNull with the sql type <code>BOOLEAN</code>.
     */
    public void setBoolean(Boolean value, PreparedStatement ps)
      throws SQLException
    {
      if(value != null) {
        setBoolean((boolean)value, ps);
      } else {
        setNull(Types.BOOLEAN, ps);
      }
    }

    /**
     * Calls setString on the given PreparedStatement with the given value
     * for the position of this PlaceHolder.
     */
    protected void setNonNullString(String value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        ps.setString(getIndex(), value);
      }
    }
    
    /**
     * Calls setString on the given PreparedStatement with the given value
     * for the position of this PlaceHolder.
     */
    public void setString(String value, PreparedStatement ps)
      throws SQLException
    {
      if(value != null) {
        setNonNullString(value, ps);
      } else {
        setNull(Types.VARCHAR, ps);
      }
    }
    
    /**
     * Calls setObject on the given PreparedStatement with the given value
     * for the position of this PlaceHolder.
     *
     * Note, calling this method with a <code>null</code> value may or may not
     * work, depending on the JDBC driver.  The only reliable (across all JDBC
     * drivers) way to set a <code>null</code> object is to call
     * {@link #setObject(Object,int,PreparedStatement)} with the correct SQL
     * type.
     */
    public void setObject(Object value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        ps.setObject(getIndex(), value);
      }
    }

    /**
     * Calls setObject on the given PreparedStatement with the given value
     * and the given sql type for the position of this PlaceHolder.
     */
    protected void setNonNullObject(Object value, int sqlType,
                                    PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        ps.setObject(getIndex(), value, sqlType);
      }
    }
    
    /**
     * Calls setObject on the given PreparedStatement with the given value
     * and the given sql type for the position of this PlaceHolder.
     */
    public void setObject(Object value, int sqlType, PreparedStatement ps)
      throws SQLException
    {
      if(value != null) {
        setNonNullObject(value, sqlType, ps);
      } else {
        setNull(sqlType, ps);
      }
    }
    
    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
    }
    
    @Override
    public final void appendTo(AppendableExt app) throws IOException {
      addIndex(_outer._curIndex++);
      SqlObject.QUESTION_MARK.appendTo(app);
    }
  }

  
  /**
   * A SqlObject which outputs a '?', and records the current index at the
   * time(s) the <code>appendTo</code> method is called.  This enables the
   * user to set parameters correctly in a PreparedStatement where the
   * position is not known at query creation time.
   *
   * Note: a MultiPlaceHolder may be used in more than one place in the query.
   */
  public static class MultiPlaceHolder extends PlaceHolder
  {
    /** the current indexes of this PlaceHolder in the generated query.  Not
        valid until after the query has been converted to a string.  If the
        List is empty at that time, this PlaceHolder was not used in the
        query. */
    private List<Integer> _indexes = new LinkedList<Integer>();
    
    public MultiPlaceHolder(QueryPreparer outer) {
      super(outer);
    }

    @Override
    public List<Integer> getIndexes() { return _indexes; }

    @Override
    public boolean isInQuery() {
      return !getIndexes().isEmpty();
    }

    @Override
    protected void addIndex(int index) {
      getIndexes().add(index);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note, this method should generally not be used for MultiPlaceHolders as
     * they usually have more than one index, however if there is at most one
     * index, this method will behave like the parent class.
     * 
     * @throws UnsupportedOperationException if this method is called and
     *         there is more than one index
     */
    @Override
    public int getIndex() {
      if(getIndexes().size() <= 1) {
        // support single-use-like behavior
        return((!getIndexes().isEmpty()) ?
               (int)getIndexes().get(0) :
               NO_INDEX);
      } 

      throw new UnsupportedOperationException(
          "This method may not be used for multi-use MultiPlaceHolder");
    }

    @Override
    public void setNull(int sqlType, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        for(Integer index : getIndexes()) {
          ps.setNull(index, sqlType);
        }
      }
    }
    
    @Override
    public void setInt(int value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        for(Integer index : getIndexes()) {
          ps.setInt(index, value);
        }
      }
    }
    
    @Override
    public void setLong(long value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        for(Integer index : getIndexes()) {
          ps.setLong(index, value);
        }
      }
    }
    
    @Override
    public void setBoolean(boolean value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        for(Integer index : getIndexes()) {
          ps.setBoolean(index, value);
        }
      }
    }
    
    @Override
    protected void setNonNullString(String value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        for(Integer index : getIndexes()) {
          ps.setString(index, value);
        }
      }
    }
    
    @Override
    public void setObject(Object value, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        for(Integer index : getIndexes()) {
          ps.setObject(index, value);
        }
      }
    }

    @Override
    protected void setNonNullObject(Object value, int sqlType,
                                    PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        for(Integer index : getIndexes()) {
          ps.setObject(index, value, sqlType);
        }
      }
    }
    
  }

  /**
   * Convenience PlaceHolder which also maintains a value which will always be
   * inserted into the PreparedStatement when <code>setValue</code> is called.
   */
  public static abstract class StaticPlaceHolder extends PlaceHolder
  {
    public StaticPlaceHolder(QueryPreparer outer)
    {
      super(outer);
    }

    /**
     * Calls the appropriate set method on the given PreparedStatement with
     * the saved static value for the position of this PlaceHolder.
     */
    public abstract void setValue(PreparedStatement ps)
      throws SQLException;
    
    /**
     * Displays the value of this place holder as a String
     */
    public abstract String displayToString();
  }

  /**
   * StaticPlaceHolder which always calls setNull on the PreparedStatement
   * with the saved sql type.
   */
  public static class NullStaticPlaceHolder extends StaticPlaceHolder
  {
    private int _sqlType;
    
    public NullStaticPlaceHolder(int sqlType, QueryPreparer outer)
    {
      super(outer);
      _sqlType = sqlType;
    }

    @Override
    public void setValue(PreparedStatement ps)
      throws SQLException
    {
      setNull(_sqlType, ps);
    }

    @Override
    public String displayToString() {
      return "NullSqlType=" + _sqlType;
    }
  }

  /**
   * StaticPlaceHolder which calls setObject on the PreparedStatement
   * with the saved value.
   */
  public static class ObjectStaticPlaceHolder<ObjType>
    extends StaticPlaceHolder
  {
    protected ObjType _val;
    
    public ObjectStaticPlaceHolder(ObjType val, QueryPreparer outer)
    {
      super(outer);
      _val = val;
    }

    @Override
    public void setValue(PreparedStatement ps)
      throws SQLException
    {
      setObject(_val, ps);
    }

    @Override
    public String displayToString() {
      return "'" + _val + "'";
    }
  }
    
  /**
   * StaticPlaceHolder which calls setObject on the PreparedStatement
   * with the saved value and the saved sql type.
   */
  public static class TypedStaticPlaceHolder
    extends ObjectStaticPlaceHolder<Object>
  {
    private int _sqlType;
    
    public TypedStaticPlaceHolder(Object val, int sqlType, QueryPreparer outer)
    {
      super(val, outer);
      _sqlType = sqlType;
    }

    @Override
    public void setValue(PreparedStatement ps)
      throws SQLException
    {
      setObject(_val, _sqlType, ps);
    }
  }
  
  /**
   * StaticPlaceHolder which calls setString on the PreparedStatement
   * with the saved value.
   */
  public static class StringStaticPlaceHolder
    extends ObjectStaticPlaceHolder<String>
  {
    public StringStaticPlaceHolder(String val, QueryPreparer outer)
    {
      super(val, outer);
    }

    @Override
    public void setValue(PreparedStatement ps)
      throws SQLException
    {
      setString(_val, ps);
    }
  }

  /**
   * StaticPlaceHolder which calls setLong on the PreparedStatement
   * with the saved value.
   */
  public static class LongStaticPlaceHolder extends StaticPlaceHolder
  {
    private long _val;
    
    public LongStaticPlaceHolder(long val, QueryPreparer outer)
    {
      super(outer);
      _val = val;
    }

    @Override
    public void setValue(PreparedStatement ps)
      throws SQLException
    {
      setLong(_val, ps);
    }

    @Override
    public String displayToString() {
      return "'" + _val + "'";
    }
  }

  /**
   * StaticPlaceHolder which calls setInt on the PreparedStatement
   * with the saved value.
   */
  public static class IntegerStaticPlaceHolder extends StaticPlaceHolder
  {
    private int _val;
    
    public IntegerStaticPlaceHolder(int val, QueryPreparer outer)
    {
      super(outer);
      _val = val;
    }

    @Override
    public void setValue(PreparedStatement ps)
      throws SQLException
    {
      setInt(_val, ps);
    }

    @Override
    public String displayToString() {
      return "'" + _val + "'";
    }
  }

  /**
   * StaticPlaceHolder which calls setInt on the PreparedStatement
   * with the saved value.
   */
  public static class BooleanStaticPlaceHolder extends StaticPlaceHolder
  {
    private boolean _val;
    
    public BooleanStaticPlaceHolder(boolean val, QueryPreparer outer)
    {
      super(outer);
      _val = val;
    }

    @Override
    public void setValue(PreparedStatement ps)
      throws SQLException
    {
      setBoolean(_val, ps);
    }

    @Override
    public String displayToString() {
      return "'" + _val + "'";
    }
  }

  
}
