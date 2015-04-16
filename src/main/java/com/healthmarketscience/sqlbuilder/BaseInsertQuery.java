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


/**
 * Base of a query which generates an INSERT statement.  Keeps track of the
 * table and column names.
 *
 * @author James Ahlborn
 */
abstract class BaseInsertQuery<ThisType extends BaseInsertQuery<ThisType>>
  extends Query<ThisType>
{
  private SqlObject _table;
  protected SqlObjectList<SqlObject> _columns = SqlObjectList.create();
    
  /** @param tableStr name of the table into which to insert the values. */
  public BaseInsertQuery(SqlObject tableStr) {
    _table = tableStr;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _table.collectSchemaObjects(vContext);
    _columns.collectSchemaObjects(vContext);
  }

  /**
   * Appends the prefix "INSERT INTO (&lt;columns&gt;)" to the given
   * AppendableExt.
   */
  protected void appendPrefixTo(AppendableExt app) throws IOException {
    app.append("INSERT INTO ").append(_table)
      .append(" (").append(_columns).append(") ");
  }
}
