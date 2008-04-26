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
