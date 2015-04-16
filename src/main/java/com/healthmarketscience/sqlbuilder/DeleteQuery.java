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
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Query which generates a DELETE statement.
 *  
 * @author James Ahlborn
 */
public class DeleteQuery extends Query<DeleteQuery>
{
  private SqlObject _table;
  private ComboCondition _condition =
    ComboCondition.and();

  public DeleteQuery(Table table) {
    this((Object)table);
  }

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public DeleteQuery(Object tableStr) {
    _table = Converter.toCustomTableSqlObject(tableStr);
  }

  /**
   * Allows access to the AND ComboCondition of the where clause to facilitate
   * common condition building code.
   * @return the AND ComboCondition of the WHERE clause for the delete query.
   */
  public ComboCondition getWhereClause() {
    return _condition;
  }
  
  /** 
   * Sets the WHERE clause for the delete query.  Note that the WHERE
   * clause will only be generated if conditions have been added
   * 
   * For convience purposes, the SelectQuery generates it's own ComboCondition
   * allowing multiple conditions to be AND'd together.  To OR conditions or 
   * perform other logic, the ComboCondition must be build and added to the 
   * selectQuery.
   */
  public DeleteQuery addCondition(Condition newCondition) {
    _condition.addCondition(newCondition);
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _table.collectSchemaObjects(vContext);
    _condition.collectSchemaObjects(vContext);
  }

  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    // append basic select
    app.append("DELETE FROM ").append(_table);

    if(!_condition.isEmpty()) {
      // append "where" condition(s)
      app.append(" WHERE ").append(_condition);
    }
  }

}
