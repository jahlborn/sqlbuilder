/*
Copyright (c) 2008 Health Market Science, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.healthmarketscience.sqlbuilder;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
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
   * @return a new ListPlaceHolder tied to this QueryPreparer.  Its internal
   *         state is not valid until the query is converted to an actual
   *         string.  Also, it can only be used in <i>one</i> place in the
   *         query.
   */
  public ListPlaceHolder getNewListPlaceHolder() {
    return new ListPlaceHolder(this);
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
    return addStaticPlaceHolder(
        new ObjectStaticPlaceHolder<Object>(obj, this));
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
   * outputs the static place holders in order
   */
  @Override
  public String toString() {
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

    protected QueryPreparer getOuter() {
      return _outer;
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
     *         generated, -1 otherwise
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
    public void appendTo(AppendableExt app) throws IOException {
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
   * A SqlObject which outputs 0 or more '?' separated by commas, and records
   * the current indexes at the times the <code>appendTo</code> method is
   * called.  This enables the user to set parameters correctly in a
   * PreparedStatement where the position is not known at query creation time.
   *
   * Note: a ListPlaceHolder may not be used in more than one place in the
   * query unless all the underlying PlaceHolders are instances of {@link
   * MultiPlaceHolder}.
   */
  public static class ListPlaceHolder extends PlaceHolder
  {
    /** the delegate placeholders */
    private final SqlObjectList<PlaceHolder> _delegates = SqlObjectList.create();
    
    public ListPlaceHolder(QueryPreparer outer) {
      super(outer);
    }

    @Override
    public boolean isInQuery() {
      return (!_delegates.isEmpty() && _delegates.get(0).isInQuery());
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Note, this method should generally not be used for ListPlaceHolders as
     * they usually have more than one index, however if there is at most one
     * index, this method will behave like the parent class.
     * 
     * @throws UnsupportedOperationException if this method is called and
     *         there is more than one index
     */
    @Override
    public int getIndex() {
      if(_delegates.size() <= 1) {
        // support single-use-like behavior
        return((!_delegates.isEmpty()) ?
               _delegates.get(0).getIndex() :
               NO_INDEX);
      } 

      throw new UnsupportedOperationException(
          "This method may not be used for multi-value ListPlaceHolder");
    }

    @Override
    public List<Integer> getIndexes() {
      List<Integer> idxs = new ArrayList<Integer>(_delegates.size());
      for(PlaceHolder ph : _delegates) {
        idxs.addAll(ph.getIndexes());
      } 
      return idxs;
    }

    /**
     * Adds a new PlaceHolder to this list and returns it.
     */
    public PlaceHolder addNewPlaceHolder() {
      return addPlaceHolder(getOuter().getNewPlaceHolder());
    }

    /**
     * Adds a new MultiPlaceHolder to this list and returns it.
     */
    public MultiPlaceHolder addNewMultiPlaceHolder() {
      return addPlaceHolder(getOuter().getNewMultiPlaceHolder());
    }

    /**
     * Adds the given StaticPlaceHolder to this list and returns it.
     */
    public StaticPlaceHolder addStaticPlaceHolder(StaticPlaceHolder ph) {
      return addPlaceHolder(getOuter().addStaticPlaceHolder(ph));
    }

    /**
     * Adds the given number of PlaceHolders to this list.
     */
    public ListPlaceHolder addPlaceHolders(int size) {
      for(int i = 0; i < size; ++i) {
        addPlaceHolder(getOuter().getNewPlaceHolder());
      }
      return this;
    }

    private <P extends PlaceHolder> P addPlaceHolder(P ph) {
      _delegates.addObject(ph);
      return ph;
    }

    /**
     * Adds StaticPlaceHolders for the given String values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticStrings(String... values) {
      return addStaticStrings((values != null) ? Arrays.asList(values) : 
                              Collections.<String>emptyList());
    }

    /**
     * Adds StaticPlaceHolders for the given String values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticStrings(Iterable<? extends String> values) {
      if(values != null) {
        for(String value : values) {
          addPlaceHolder(getOuter().addStaticPlaceHolder(value));
        }
      }
      return this;
    }

    /**
     * Adds StaticPlaceHolders for the given long values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticLongs(long... values) {
      if(values != null) {
        for(long value : values) {
          addPlaceHolder(getOuter().addStaticPlaceHolder(value));
        }
      }
      return this;
    }

    /**
     * Adds StaticPlaceHolders for the given Long values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticLongs(Iterable<? extends Long> values) {
      if(values != null) {
        for(Long value : values) {
          addPlaceHolder(getOuter().addStaticPlaceHolder(value));
        }
      }
      return this;
    }

    /**
     * Adds StaticPlaceHolders for the given int values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticIntegers(int... values) {
      if(values != null) {
        for(int value : values) {
          addPlaceHolder(getOuter().addStaticPlaceHolder(value));
        }
      }
      return this;
    }

    /**
     * Adds StaticPlaceHolders for the given Integer values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticIntegers(Iterable<? extends Integer> values) {
      if(values != null) {
        for(Integer value : values) {
          addPlaceHolder(getOuter().addStaticPlaceHolder(value));
        }
      }
      return this;
    }

    /**
     * Adds StaticPlaceHolders for the given boolean values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticBooleans(boolean... values) {
      if(values != null) {
        for(boolean value : values) {
          addPlaceHolder(getOuter().addStaticPlaceHolder(value));
        }
      }
      return this;
    }

    /**
     * Adds StaticPlaceHolders for the given Boolean values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticBooleans(Iterable<? extends Boolean> values) {
      if(values != null) {
        for(Boolean value : values) {
          addPlaceHolder(getOuter().addStaticPlaceHolder(value));
        }
      }
      return this;
    }

    /**
     * Adds StaticPlaceHolders for the given Object values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticObjects(Object... values) {
      return addStaticObjects(
          (values != null) ? Arrays.asList(values) : Collections.emptyList());
    }

    /**
     * Adds StaticPlaceHolders for the given Object values.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticObjects(Iterable<?> values) {
      if(values != null) {
        for(Object value : values) {
          addPlaceHolder(getOuter().addStaticPlaceHolder(value));
        }
      }
      return this;
    }

    /**
     * Adds StaticPlaceHolders for the given Object values and given sql type.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticObjects(int sqlType, Object... values) {
      return addStaticObjects(sqlType, 
          (values != null) ? Arrays.asList(values) : Collections.emptyList());
    }

    /**
     * Adds StaticPlaceHolders for the given Object values and given sql type.
     * @see QueryPreparer#setStaticValues
     */
    public ListPlaceHolder addStaticObjects(int sqlType, Iterable<?> values) {
      if(values != null) {
        for(Object value : values) {
          addPlaceHolder(getOuter().addStaticPlaceHolder(value, sqlType));
        }
      }
      return this;
    }

    /**
     * Calls setNull on the given PreparedStatement with the given sql type
     * for the positions of this PlaceHolder.
     */
    public void setNulls(int sqlType, PreparedStatement ps)
      throws SQLException
    {
      if(isInQuery()) {
        for(PlaceHolder ph : _delegates) {
          ph.setNull(sqlType, ps);
        }
      }
    }
    
    /**
     * Calls setInt on the given PreparedStatement with the given values for
     * the positions of this PlaceHolder.
     */
    public void setInts(PreparedStatement ps, int... values)
      throws SQLException
    {
      if(isInQuery()) {
        int idx = 0;
        for(PlaceHolder ph : _delegates) {
          ph.setInt(values[idx++], ps);
        } 
      }
    }
    
    /**
     * Calls setInt on the given PreparedStatement with the given values for
     * the positions of this PlaceHolder.  If given value is
     * <code>null</code>, calls setNull with the sql type
     * <code>INTEGER</code>.
     */
    public void setInts(PreparedStatement ps, Iterable<? extends Integer> values)
      throws SQLException
    {
      if(isInQuery()) {
        Iterator<? extends Integer> iter = values.iterator();
        for(PlaceHolder ph : _delegates) {
          ph.setInt(iter.next(), ps);
        } 
      }
    }
    
    /**
     * Calls setLong on the given PreparedStatement with the given values
     * for the positions of this PlaceHolder.
     */
    public void setLongs(PreparedStatement ps, long... values)
      throws SQLException
    {
      if(isInQuery()) {
        int idx = 0;
        for(PlaceHolder ph : _delegates) {
          ph.setLong(values[idx++], ps);
        } 
      }
    }
    
    /**
     * Calls setLong on the given PreparedStatement with the given values for
     * the positions of this PlaceHolder.  If given value is
     * <code>null</code>, calls setNull with the sql type <code>BIGINT</code>.
     */
    public void setLongs(PreparedStatement ps, Iterable<? extends Long> values)
      throws SQLException
    {
      if(isInQuery()) {
        Iterator<? extends Long> iter = values.iterator();
        for(PlaceHolder ph : _delegates) {
          ph.setLong(iter.next(), ps);
        } 
      }
    }
    
    /**
     * Calls setBoolean on the given PreparedStatement with the given values
     * for the positions of this PlaceHolder.
     */
    public void setBooleans(PreparedStatement ps, boolean... values)
      throws SQLException
    {
      if(isInQuery()) {
        int idx = 0;
        for(PlaceHolder ph : _delegates) {
          ph.setBoolean(values[idx++], ps);
        } 
      }
    }
    
    /**
     * Calls setBoolean on the given PreparedStatement with the given values for
     * the positions of this PlaceHolder.  If given value is <code>null</code>,
     * calls setNull with the sql type <code>BOOLEAN</code>.
     */
    public void setBooleans(PreparedStatement ps, 
                            Iterable<? extends Boolean> values)
      throws SQLException
    {
      if(isInQuery()) {
        Iterator<? extends Boolean> iter = values.iterator();
        for(PlaceHolder ph : _delegates) {
          ph.setBoolean(iter.next(), ps);
        } 
      }
    }
    
    /**
     * Calls setString on the given PreparedStatement with the given values for
     * the positions of this PlaceHolder.  If given value is <code>null</code>,
     * calls setNull with the sql type <code>VARCHAR</code>.
     */
    public void setStrings(PreparedStatement ps, String... values)
      throws SQLException
    {
      if(isInQuery()) {
        setStringsImpl(ps, Arrays.asList(values));
      }
    }
    
    /**
     * Calls setString on the given PreparedStatement with the given values
     * for the positions of this PlaceHolder.  If given value is
     * <code>null</code>, calls setNull with the sql type
     * <code>VARCHAR</code>.
     */
    public void setStrings(PreparedStatement ps, 
                           Iterable<? extends String> values)
      throws SQLException
    {
      if(isInQuery()) {
        setStringsImpl(ps, values);
      }
    }

    private void setStringsImpl(PreparedStatement ps,
                                Iterable<? extends String> values)
      throws SQLException
    {
      Iterator<? extends String> iter = values.iterator();
      for(PlaceHolder ph : _delegates) {
        ph.setString(iter.next(), ps);
      } 
    }
    
    /**
     * Calls setObject on the given PreparedStatement with the given values
     * for the positions of this PlaceHolder.
     *
     * Note, calling this method with a <code>null</code> value may or may not
     * work, depending on the JDBC driver.  The only reliable (across all JDBC
     * drivers) way to set a <code>null</code> object is to call
     * {@link #setObjects(int,PreparedStatement,Object...)} with the correct SQL
     * type.
     */
    public void setObjects(PreparedStatement ps, Object... values)
      throws SQLException
    {
      if(isInQuery()) {
        setObjectsImpl(ps, Arrays.asList(values));
      }
    }

    /**
     * Calls setObject on the given PreparedStatement with the given values
     * for the positions of this PlaceHolder.
     *
     * Note, calling this method with a <code>null</code> value may or may not
     * work, depending on the JDBC driver.  The only reliable (across all JDBC
     * drivers) way to set a <code>null</code> object is to call
     * {@link #setObjects(int,PreparedStatement,Iterable)} with the correct SQL
     * type.
     */
    public void setObjects(PreparedStatement ps, Iterable<?> values)
      throws SQLException
    {
      if(isInQuery()) {
        setObjectsImpl(ps, values);
      }
    }
    
    private void setObjectsImpl(PreparedStatement ps, Iterable<?> values)
      throws SQLException
    {
      Iterator<?> iter = values.iterator();
      for(PlaceHolder ph : _delegates) {
        ph.setObject(iter.next(), ps);
      } 
    }
    
    /**
     * Calls setObject on the given PreparedStatement with the given values
     * and the given sql type for the positions of this PlaceHolder.
     */
    public void setObjects(int sqlType, PreparedStatement ps, Object... values)
      throws SQLException
    {
      if(isInQuery()) {
        setObjectsImpl(sqlType, ps, Arrays.asList(values));
      }
    }
        
    /**
     * Calls setObject on the given PreparedStatement with the given value and
     * the given sql type for the position of this PlaceHolder.  If given
     * value is <code>null</code>, calls setNull with the given sql type.
     */
    public void setObjects(int sqlType, PreparedStatement ps, Iterable<?> values)
      throws SQLException
    {
      if(isInQuery()) {
        setObjectsImpl(sqlType, ps, values);
      }
    }
        
    private void setObjectsImpl(int sqlType, PreparedStatement ps,
                                Iterable<?> values)
      throws SQLException
    {
      Iterator<?> iter = values.iterator();
      for(PlaceHolder ph : _delegates) {
        ph.setObject(iter.next(), sqlType, ps);
      } 
    }
        
    @Override
    public final void appendTo(AppendableExt app) throws IOException {  
      _delegates.appendTo(app);
    }
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
