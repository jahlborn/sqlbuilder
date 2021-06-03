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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.custom.CustomSyntax;
import com.healthmarketscience.sqlbuilder.custom.HookAnchor;
import com.healthmarketscience.sqlbuilder.custom.HookType;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Join;
import com.healthmarketscience.sqlbuilder.dbspec.Table;



/**
 * Query which generates a SELECT statement.  Supports arbitrary columns
 * (including "DISTINCT" modifier), "FOR UPDATE" clause, all join types,
 * "WHERE" clause, "GROUP BY" clause, "ORDER BY" clause, and "HAVING" clause.
 * <p>
 * Note that the "OFFSET" and "FETCH NEXT" clauses are supported from "SQL
 * 2008".
 * <p>
 * If Columns are used for any referenced columns, and no complicated joins
 * are required, the table list may be left empty and it will be
 * auto-generated in the append call.  Note, that this is not the most
 * efficient method (as this list will not be cached for the future due to
 * mutability constraints on <code>appendTo</code>).
 * <p>
 * Note that this query supports custom SQL syntax, see {@link Hook} for more
 * details.
 *
 * @author James Ahlborn
 */
public class SelectQuery extends BaseCTEQuery<SelectQuery>
{
  /**
   * Enum which defines the join types supported in a FROM clause.
   */
  public enum JoinType
  {
    INNER(" INNER JOIN "),
    LEFT_OUTER(" LEFT OUTER JOIN "),
    RIGHT_OUTER(" RIGHT OUTER JOIN "),
    FULL_OUTER(" FULL OUTER JOIN ");

    private final String _joinClause;

    private JoinType(String joinClause) {
      _joinClause = joinClause;
    }

    @Override
    public String toString() { return _joinClause; }
  }

  /**
   * The HookAnchors supported for SELECT queries.  See {@link com.healthmarketscience.sqlbuilder.custom} for
   * more details on custom SQL syntax.
   */
  public enum Hook implements HookAnchor
  {
    /** Anchor for the beginning of the query, only supports {@link
        HookType#BEFORE} */
    HEADER,
    /** Anchor for the "SELECT " clause */
      SELECT,
    /** Anchor for the "DISTINCT " clause */
      DISTINCT,
    /** Anchor for the " FROM " clause */
      FROM,
    /** Anchor for the " WHERE " clause */
      WHERE,
    /** Anchor for the " GROUP BY " clause */
      GROUP_BY,
    /** Anchor for the " HAVING " sub-clause (only possible if there is a
        GROUP BY clause) */
      HAVING,
    /** Anchor for the " WINDOW " clause */
      WINDOW,
    /** Anchor for the " ORDER BY " clause */
      ORDER_BY,
    /** Anchor for the " FOR UPDATE " clause */
      FOR_UPDATE,
    /** Anchor for the end of the query, only supports {@link
        HookType#BEFORE} */
      TRAILER;
  }

  private boolean _isDistinct;
  private boolean _forUpdate;
  private SqlObjectList<SqlObject> _columns = SqlObjectList.create();
  private SqlObjectList<SqlObject> _joins = SqlObjectList.create("");
  private List<SqlObject> _joinFromTables = new ArrayList<SqlObject>(4);
  private ComboCondition _condition = ComboCondition.and();
  private SqlObjectList<SqlObject> _grouping = SqlObjectList.create();
  private SqlObjectList<SqlObject> _ordering = SqlObjectList.create();
  private ComboCondition _having = ComboCondition.and();
  private SqlObjectList<SqlObject> _windows = SqlObjectList.create();
  private SqlObject _offset;
  private SqlObject _fetchCount;

  public SelectQuery() {
    this(false);
  }

  public SelectQuery(boolean isDistinct) {
    _isDistinct = isDistinct;
  }

  /** Returns the columns in this select query. */
  SqlObjectList<SqlObject> getColumns() { return _columns; }

  /** Returns the ordering in this select query. */
  SqlObjectList<SqlObject> getOrdering() { return _ordering; }

  /**
   * Returns <code>true</code> iff this select query is using some sort of
   * "*" syntax as a column placeholder.
   * <p>
   * Note, this method is package scoped because it should not be used
   * externally, just by some related query classes for internal validation.
   */
  boolean hasAllColumns()
  {
    return hasAllColumns(_columns);
  }

  /**
   * Handles updating the internal collections with the "from" table in a
   * join.
   */
  private void addJoinFromTable(SqlObject fromTable)
  {
    if(_joins.isEmpty()) {
      // add first from table
      _joins.addObject(fromTable);
    }
    // track all join from tables in case the user does validation
    _joinFromTables.add(fromTable);
  }

  /** Iff isDistinct is <code>true</code>, adds the DISTINCT keyword to the
      SELECT clause so that only unique rows are returned */
  public SelectQuery setIsDistinct(boolean isDistinct) {
    _isDistinct = isDistinct;
    return this;
  }

  /** Iff forUpdate is <code>true</code>, adds the FOR UPDATE clause to the
      end of the SELECT clause */
  public SelectQuery setForUpdate(boolean forUpdate) {
    _forUpdate = forUpdate;
    return this;
  }

  /**
   * Adds the given columns to the SELECT column list.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public SelectQuery addCustomColumns(Object... columnStrs) {
    _columns.addObjects(Converter.COLUMN_VALUE_TO_OBJ, columnStrs);
    return this;
  }

  /** Adds the ALL_SYMBOL to the select column list. */
  public SelectQuery addAllColumns() {
    _columns.addObject(ALL_SYMBOL);
    return this;
  }

  /** Adds a <code>"&lt;alias&gt;.*"</code> column to the select column
      list. */
  public SelectQuery addAllTableColumns(Table table) {
    _columns.addObject(new AllTableColumns(table));
    return this;
  }

  /** Adds the given columns to the SELECT column list. */
  public SelectQuery addColumns(Column... columns) {
    return addCustomColumns((Object[])columns);
  }

  /**
   * Adds the given column with the given alias to the SELECT column list.
   * This is equivalent to
   * {@code addCustomColumns(Converter.toColumnSqlObject(column, alias))}.
   */
  public SelectQuery addAliasedColumn(Object column, String alias) {
    return addCustomColumns(Converter.toColumnSqlObject(column, alias));
  }

  /**
   * Adds a table to the FROM clause, should not be used with any
   * <code>add*Join</code> methods
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableDefSqlObject(Object)}.
   */
  public SelectQuery addCustomFromTable(Object tableStr)
  {
    SqlObject tableObj = Converter.toCustomTableDefSqlObject(tableStr);
    if(_joins.isEmpty()) {
      _joins.addObject(tableObj);
    } else {
      _joins.addObject(new JoinTo(tableObj));
    }
    return this;
  }

  /** Adds a table to the FROM clause, should not be used with any
      <code>add*Join</code> methods */
  public SelectQuery addFromTable(Table table)
  {
    return addCustomFromTable(table);
  }

  /**
   * Adds a custom join string.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableDefSqlObject(Object)}.
   */
  public SelectQuery addCustomJoin(Object joinStr)
  {
    SqlObject joinObj = Converter.toCustomTableDefSqlObject(joinStr);
    _joins.addObject(joinObj);
    return this;
  }

  /**
   * Adds a join of the given type from fromTableStr to toTableStr on
   * joinCond of the given join type.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableDefSqlObject(Object)}.
   */
  public SelectQuery addCustomJoin(JoinType joinType, Object fromTableStr,
                                   Object toTableStr, Condition joinCond)
  {
    addJoinFromTable(Converter.toCustomTableDefSqlObject(fromTableStr));

    // add to table
    _joins.addObject(
        new JoinTo(joinType,
                   Converter.toCustomTableDefSqlObject(toTableStr),
                   joinCond));
    return this;
  }

  /** Adds a join of the given type from fromTable to toTable on joinCond of
      the given join type. */
  public SelectQuery addJoin(JoinType joinType, Table fromTable,
                             Table toTable, Condition joinCond)
  {
    return addCustomJoin(joinType, Converter.toTableDefSqlObject(fromTable),
                         Converter.toTableDefSqlObject(toTable), joinCond);
  }

  /**
   * Adds a join of the given type from fromTable to toTable with a join
   * condition requiring each column in fromColumns to equal the corresponding
   * column in toColumns.
   */
  public SelectQuery addJoin(JoinType joinType,
                             Table fromTable,
                             Table toTable,
                             List<? extends Column> fromColumns,
                             List<? extends Column> toColumns) {
    addJoinFromTable(Converter.toTableDefSqlObject(fromTable));

    // add to table
    _joins.addObject(new JoinTo(joinType,
                                Converter.toTableDefSqlObject(toTable),
                                fromColumns, toColumns));
    return this;
  }

  /**
   * Adds a join of the given type from fromTable to toTable with a join
   * condition requiring fromColumn to equal toColumn.
   */
  public SelectQuery addJoin(JoinType joinType,
                             Table fromTable, Table toTable,
                             Column fromColumn,
                             Column toColumn)
  {
    return addJoin(joinType, fromTable, toTable,
                   Collections.singletonList(fromColumn), Collections.singletonList(toColumn));
  }

  /** Adds all of the joins of the given join type where each join is from
      join.getFromTable() to join.getToTable() with a join condition
      requiring each column in join.getFromColumns() to equal the
      corresponding column in join.getToColumns(). */
  public SelectQuery addJoins(JoinType joinType, Join... joins) {
    if(joins != null) {
      for(Join join : joins) {
        addJoin(joinType, join.getFromTable(), join.getToTable(),
                join.getFromColumns(), join.getToColumns());
      }
    }
    return this;
  }

  /**
   * Adds the given column with the given direction to the "ORDER BY"
   * clause
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomColumnSqlObject(Object)}.
   */
  public SelectQuery addCustomOrdering(Object columnStr,
                                       OrderObject.Dir dir) {
    return addCustomOrderings(
        new OrderObject(dir, Converter.toCustomColumnSqlObject(columnStr)));
  }

  /**
   * Adds the given columns to the "ORDER BY" clause
   * <p>
   * Note, NULL ordering can be specified by passing an appropriately
   * configured instance of {@link OrderObject} to this method.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  public SelectQuery addCustomOrderings(Object... columnStrs) {
    _ordering.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }

  /** Adds the given column with the given direction to the "ORDER BY"
      clause */
  public SelectQuery addOrdering(Column column, OrderObject.Dir dir) {
    return addCustomOrdering(column, dir);
  }

  /** Adds the given columns to the "ORDER BY" clause */
  public SelectQuery addOrderings(Column... columns) {
    return addCustomOrderings((Object[])columns);
  }

  /** Adds the given column index with the given direction to the "ORDER BY"
      clause */
  public SelectQuery addIndexedOrdering(Integer columnIdx,
                                        OrderObject.Dir dir) {
    return addCustomOrdering(columnIdx, dir);
  }

  /** Adds the given column index to the "ORDER BY" clause */
  public SelectQuery addIndexedOrderings(Integer... columnIdxs) {
    return addCustomOrderings((Object[])columnIdxs);
  }

  /**
   * Adds the given columns to the "GROUP BY" clause
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  public SelectQuery addCustomGroupings(Object... columnStrs) {
    _grouping.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }

  /** Adds the given columns to the "GROUP BY" clause */
  public SelectQuery addGroupings(Column... columns) {
    return addCustomGroupings((Object[])columns);
  }

  /**
   * Allows access to the AND ComboCondition of the where clause to facilitate
   * common condition building code.
   * @return the AND ComboCondition of the WHERE clause for the select query.
   */
  public ComboCondition getWhereClause() {
    return _condition;
  }

  /**
   * Adds a condition to the WHERE clause for the select query (AND'd with any
   * other WHERE conditions).  Note that the WHERE clause will only be
   * generated if some conditions have been added.
   * <p>
   * For convenience purposes, the SelectQuery generates it's own
   * ComboCondition allowing multiple conditions to be AND'd together.  To OR
   * conditions or perform other logic, the ComboCondition must be built and
   * added to the SelectQuery.
   */
  public SelectQuery addCondition(Condition newCondition) {
    _condition.addCondition(newCondition);
    return this;
  }

  /**
   * Allows access to the AND ComboCondition of the having clause to
   * facilitate common condition building code.
   * @return the AND ComboCondition of the HAVING clause for the select query.
   */
  public ComboCondition getHavingClause() {
    return _having;
  }

  /**
   * Adds a condition to the HAVING clause for the select query (AND'd with
   * any other HAVING conditions).  Note that the HAVING clause will only be
   * generated if some conditions have been added.
   * <p>
   * For convenience purposes, the SelectQuery generates it's own
   * ComboCondition allowing multiple HAVING conditions to be AND'd together.
   * To OR conditions or perform other logic, the ComboCondition must be built
   * and added to the SelectQuery.
   */
  public SelectQuery addHaving(Condition newCondition) {
    _having.addCondition(newCondition);
    return this;
  }

  /**
   * Adds a named window definition to the select query's WINDOW definitions
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject}.
   * @see "SQL 2003"
   */
  public SelectQuery addWindowDefinition(String name, Object window) {
    _windows.addObject(new NamedWindowDefinition(
                           name, Converter.toCustomSqlObject(window)));
    return this;
  }

  /**
   * Sets the value for the "OFFSET" clause.  Note that this clause is defined
   * in "SQL 2008".
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toValueSqlObject}.
   * @see "SQL 2008"
   */
  public SelectQuery setOffset(Object offset) {
    _offset = Converter.toValueSqlObject(offset);
    return this;
  }

  /**
   * Sets the value for the "FETCH NEXT" clause.  Note that this clause is
   * defined in "SQL 2008".
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toValueSqlObject}.
   * @see "SQL 2008"
   */
  public SelectQuery setFetchNext(Object fetchCount) {
    _fetchCount = Converter.toValueSqlObject(fetchCount);
    return this;
  }

  /**
   * Adds custom SQL to this query.  See {@link com.healthmarketscience.sqlbuilder.custom} for more details on
   * custom SQL syntax.
   * @param hook the part of the query being customized
   * @param type the type of customization
   * @param obj the custom sql.  The {@code Object} -&gt; {@code SqlObject}
   *            conversions handled by {@link Converter#toCustomSqlObject}.
   */
  public SelectQuery addCustomization(Hook hook, HookType type, Object obj) {
    super.addCustomization(hook, type, obj);
    return this;
  }

  /**
   * Adds custom SQL to this query.  See {@link com.healthmarketscience.sqlbuilder.custom} for more details on
   * custom SQL syntax.
   * @param obj the custom sql syntax on which the
   *            {@link CustomSyntax#apply(SelectQuery)} method will be
   *            invoked (may be {@code null}).
   */
  public SelectQuery addCustomization(CustomSyntax obj) {
    if(obj != null) {
      obj.apply(this);
    }
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _joins.collectSchemaObjects(vContext);
    _columns.collectSchemaObjects(vContext);
    _condition.collectSchemaObjects(vContext);
    _grouping.collectSchemaObjects(vContext);
    _ordering.collectSchemaObjects(vContext);
    _having.collectSchemaObjects(vContext);
    _windows.collectSchemaObjects(vContext);
    if(_offset != null) {
      _offset.collectSchemaObjects(vContext);
    }
    if(_fetchCount != null) {
      _fetchCount.collectSchemaObjects(vContext);
    }
  }

  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    // if we have joins, check the tables, otherwise, the join tables will
    // be auto generated during output (so don't bother checking them)
    boolean checkTables = !(_joins.isEmpty());

    if(checkTables) {
      // run default table validation
      validateTables(vContext);
    }

    // note, if _joinFromTables is empty, then all the referenced tables are in
    // the _joins collection (using add*FromTable() methods), and no
    // extended validation needs to be done
    if(checkTables && !_joinFromTables.isEmpty()) {

      // verify that all the "from" tables not added to the _joins list
      // actually show up where they should.
      //
      // Given (join F0 to T0), (join F1 to T1), (join F2 to T2), ... :
      // Each table F<N> must show up among (F<0> U F<0..N-1> U T<0..N-1>)
      //
      // _joins  = F0, T0, T1, T2 ...
      // _joinFromTables = F0, F1, F2 ...
      //
      Set<Table> joinTables = new HashSet<Table>();
      Set<Table> fromTable = new HashSet<Table>();
      Set<Column> joinColumns = new HashSet<Column>();
      Iterator<SqlObject> fromIter = _joinFromTables.iterator();
      Iterator<SqlObject> toIter = _joins.iterator();

      // the first toIter table is actually F0 (see comment above)
      toIter.next().collectSchemaObjects(
          new ValidationContext(fromTable, joinColumns));

      while(fromIter.hasNext() && toIter.hasNext()) {

        // add the previous from table to the common from/to tables collection
        joinTables.addAll(fromTable);

        // grab the next from table
        fromTable.clear();
        fromIter.next().collectSchemaObjects(
            new ValidationContext(fromTable, joinColumns));

        // verify that it exists among the previous from/to tables
        if(!joinTables.containsAll(fromTable)) {
          throw new ValidationException(
              "Table " + fromTable +
              " used in join is not given among the previous tables: " +
              joinTables);
        }

        // grab the next to table
        toIter.next().collectSchemaObjects(
            new ValidationContext(joinTables, joinColumns));
      }
      if(fromIter.hasNext() || toIter.hasNext()) {
        // mismatched tables?
        String type = "from";
        Collection<SqlObject> extras = null;
        if(fromIter.hasNext()) {
          extras = getAll(fromIter);
        } else {
          type = "to";
          extras = getAll(toIter);
        }
        throw new ValidationException("Mismatched tables in joins, found extra " + type +
                                      " tables " + extras);
      }

    }

    validateOrdering(_columns.size(), _ordering, hasAllColumns());

    validateValue(_offset, "Offset", 0);
    validateValue(_fetchCount, "Fetch", 1);
  }

  /**
   * Checks any indexed ordering values for validity using a variety of
   * criteria.
   * @param numColumns number of column objects in the query
   * @param ordering the ordering objects for the query
   * @param ignoreColumnCount whether the given numColumns is meaningful
   *                          (i.e. does not include a '*' character)
   */
  protected static void validateOrdering(int numColumns,
                                         SqlObjectList<SqlObject> ordering,
                                         boolean ignoreColumnCount)
    throws ValidationException
  {
    // if we should ignore the column count, just set it to max integer (we
    // can still check other things)
    if(ignoreColumnCount) {
      numColumns = Integer.MAX_VALUE;
    }

    // check that any ordering indexes are valid
    for(SqlObject orderObj : ordering) {
      if(orderObj instanceof OrderObject) {
        orderObj = ((OrderObject)orderObj).getObject();
      }
      // note that index is 1 based
      if((orderObj instanceof NumberValueObject) &&
         !((NumberValueObject)orderObj).isIntegralInRange(1, numColumns)) {
        throw new ValidationException(
            "Ordering index '" + orderObj + "' must be integer in range: 1 to "
            + numColumns);
      }
    }
  }

  private static void validateValue(SqlObject valueObj, String type, int minVal) {
    if(!(valueObj instanceof NumberValueObject)) {
      // nothing we can do, custom value
      return;
    }
    if(!((NumberValueObject)valueObj).isIntegralInRange(minVal, Long.MAX_VALUE)) {
      throw new ValidationException(
          type + " value must be an integer >= " + minVal + ", given: " +
          valueObj);
    }
  }

  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(true);

    customAppendTo(app, Hook.HEADER);

    // append basic select
    customAppendTo(app, Hook.SELECT, "SELECT ");

    maybeAppendTo(app, Hook.DISTINCT, "DISTINCT ", _isDistinct);

    app.append(_columns);

    SqlObjectList<SqlObject> joins = _joins;
    if(joins.isEmpty()) {
      // auto generate the join tables from all the referenced columns
      joins = buildJoins(newContext);
    }

    // append the joins
    maybeAppendTo(app, Hook.FROM, " FROM ", joins, !joins.isEmpty());

    // append "where" condition(s)
    maybeAppendTo(app, Hook.WHERE, " WHERE ", _condition, !_condition.isEmpty());

    // append grouping clause
    boolean hasGroupings = !_grouping.isEmpty();
    maybeAppendTo(app, Hook.GROUP_BY, " GROUP BY ", _grouping, hasGroupings);
    if(hasGroupings) {
      // append having clause (which is considered a sub-clause of the GROUP
      // BY clause)
      maybeAppendTo(app, Hook.HAVING, " HAVING ", _having, !_having.isEmpty());
    }

    // append window definition clauses
    maybeAppendTo(app, Hook.WINDOW, " WINDOW ", _windows, !_windows.isEmpty());

    // append ordering clause
    maybeAppendTo(app, Hook.ORDER_BY, " ORDER BY ", _ordering,
                  !_ordering.isEmpty());

    if(_offset != null) {
      app.append(" OFFSET ").append(_offset).append(" ROWS");
    }
    if(_fetchCount != null) {
      app.append(" FETCH NEXT ").append(_fetchCount).append(" ROWS ONLY");
    }

    maybeAppendTo(app, Hook.FOR_UPDATE, " FOR UPDATE", _forUpdate);

    customAppendTo(app, Hook.TRAILER);
  }

  private SqlObjectList<SqlObject> buildJoins(SqlContext newContext) {

    // auto generate the join tables from all the referenced columns
    SqlObjectList<SqlObject> joins = SqlObjectList.create();

    // note, we don't cache this collection because we don't want the
    // appendTo() method to mutate object state.
    // note, we use LinkedHashSet to preserve the order that the tables were
    // referenced (for lack of a better choice of ordering)
    ValidationContext tmpVContext = new ValidationContext(
        null, new LinkedHashSet<Column>());
    collectSchemaObjects(tmpVContext);

    if(tmpVContext.getColumns().isEmpty()) {
      // this is some sort of "constant" select, no columns/tables
      return joins;
    }

    Collection<Table> columnTables = tmpVContext.getColumnTables(
        new LinkedHashSet<Table>());

    if(newContext.getParent() != null) {

      // this query is nested.  some of the column refs may be from tables
      // in the outer queries.  note, we do "local only" collection as we
      // are going up the nesting chain and do not need to descend past the
      // relevant local context
      ValidationContext outerVContext = new ValidationContext(true);
      SqlContext tmpContext = newContext;
      while((tmpContext = tmpContext.getParent()) != null) {
        Query<?> parentQuery = tmpContext.getQuery();
        if(parentQuery != null) {
          parentQuery.collectSchemaObjects(outerVContext);
        }
      }

      // remove any outer tables from the columnTables collection
      columnTables.removeAll(outerVContext.getColumnTables());
    }

    for(Table table : columnTables) {
      joins.addObject(Converter.toTableDefSqlObject(table));
    }

    return joins;
  }

  /**
   * Returns <code>true</code> iff the given column list contains some sort of
   * "*" syntax as a column placeholder.
   * <p>
   * Note, this method is package scoped because it should not be used
   * externally, just by some related query classes for internal validation.
   */
  static boolean hasAllColumns(SqlObjectList<? extends SqlObject> columns) {
    for(SqlObject sqlObj : columns) {
      if((sqlObj instanceof AllTableColumns) || (sqlObj == ALL_SYMBOL)) {
        return true;
      }
    }
    return false;
  }

  private static final <T> Collection<T> getAll(Iterator<T> iter) {
    Collection<T> list = new ArrayList<>();
    while(iter.hasNext()) {
      list.add(iter.next());
    }
    return list;
  }

  /**
   * Outputs the right side of a join clause
   * <code>"&lt;joinType&gt; &lt;toTable&gt; ON &lt;joinCondition&gt;"</code>.
   */
  private static final class JoinTo extends SqlObject
  {
    private SqlObject _toTable;
    private JoinType _joinType;
    private Condition _onCondition;

    private JoinTo(SqlObject toTable) {
      this(null, toTable, null);
    }

    private JoinTo(JoinType joinType,
                   SqlObject toTable,
                   List<? extends Column> fromColumns,
                   List<? extends Column> toColumns)
    {
      this(joinType, toTable, ComboCondition.and());

      // create join condition
      ComboCondition onCondition = (ComboCondition)_onCondition;
      for(int i = 0; i < fromColumns.size(); ++i) {
        onCondition.addCondition(
            BinaryCondition.equalTo(fromColumns.get(i), toColumns.get(i)));
      }
    }

    private JoinTo(JoinType joinType, SqlObject toTable, Condition onCondition)
    {
      _toTable = toTable;
      _joinType = joinType;
      _onCondition = onCondition;
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
      _toTable.collectSchemaObjects(vContext);
      if(_onCondition != null) {
        _onCondition.collectSchemaObjects(vContext);
      }
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      if(_joinType != null) {
        // this is a "complicated" join
        app.append(_joinType).append(_toTable)
          .append(" ON ").append(_onCondition);
      } else {
        // this is a "simple" join
        app.append(", ").append(_toTable);
      }
    }
  }

  /**
   * Outputs a named window definition clause like
   * <code>"&lt;name&gt; AS &lt;windowDefinition&gt;"</code>.
   */
  private static final class NamedWindowDefinition extends SqlObject
  {
    private final String _name;
    private final SqlObject _definition;

    private NamedWindowDefinition(String name, SqlObject definition) {
      _name = name;
      _definition = definition;
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
      _definition.collectSchemaObjects(vContext);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(_name).append(" AS ").append(_definition);
    }
  }
}
