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

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;

import com.healthmarketscience.sqlbuilder.dbspec.Table;


/**
 * Query which generates an UPDATE statement.
 *
 * @author James Ahlborn
 */
public class UpdateQuery extends Query<UpdateQuery>
{
  private SqlObject _table;
  private SqlObjectList<SetClauseObject> _sets = SqlObjectList.create();
  private ComboCondition _condition = ComboCondition.and();
  

  public UpdateQuery(Table table) {
    this((Object)table);
  }

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public UpdateQuery(Object tableStr) {
    _table = Converter.toCustomTableSqlObject(tableStr);
  }

  /** Adds the given column and new value to the SET clause list. */
  public UpdateQuery addSetClause(Column column, Object value)
  {
    return addCustomSetClause(column, value);
  }
  
  /**
   * Adds the given column and new value to the SET clause list.
   *
   * @param column {@code Object} -&gt; {@code SqlObject} conversions handled
   *               by {@link Converter#toCustomColumnSqlObject(Object)}.
   * @param value {@code Object} -&gt; {@code SqlObject} conversions handled
   *              by {@link Converter#toColumnSqlObject(Object)}.
   */
  public UpdateQuery addCustomSetClause(Object column, Object value)
  {
    _sets.addObject(new SetClauseObject(
                        Converter.toCustomColumnSqlObject(column),
                        Converter.toColumnSqlObject(value)));
    return this;
  }

  /**
   * Allows access to the AND ComboCondition of the where clause to facilitate
   * common condition building code.
   * @return the AND ComboCondition of the WHERE clause for the update query.
   */
  public ComboCondition getWhereClause() {
    return _condition;
  }
  
  /** 
   * Sets the WHERE clause for the update query.  Note that the WHERE
   * clause will only be generated if conditions have been added
   * 
   * For convience purposes, the UpdateQuery generates it's own ComboCondition
   * allowing multiple conditions to be AND'd together.  To OR conditions or 
   * perform other logic, the ComboCondition must be built and added to the 
   * UpdateQuery.
   */
  public UpdateQuery addCondition(Condition newCondition) {
    _condition.addCondition(newCondition);
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _table.collectSchemaObjects(vContext);
    _sets.collectSchemaObjects(vContext);
    _condition.collectSchemaObjects(vContext);
  }

  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    // append basic select
    app.append("UPDATE ").append(_table).append(" SET ").append(_sets);

    if(!_condition.isEmpty()) {
      // append "where" condition(s)
      app.append(" WHERE ").append(_condition);
    }
  }  

  /**
   * Utility class which outputs a set clause for the UPDATE query e.g.:
   * <col> = <value>
   */
  private static class SetClauseObject extends SqlObject
  {
    private SqlObject _column;
    private SqlObject _value;

    private SetClauseObject(SqlObject column, SqlObject value) {
      _column = column;
      _value = value;
    }
    
    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
      _column.collectSchemaObjects(vContext);
      _value.collectSchemaObjects(vContext);
    }
    
    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(_column).append(" = ").append(_value);
    }
  }
  
}
