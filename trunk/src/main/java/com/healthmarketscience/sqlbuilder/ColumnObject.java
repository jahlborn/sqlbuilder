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
 * Outputs the name of the column optionally qualified by its table's alias
 * if the current SqlContext has table aliases enabled
 * <code>"[&lt;tableAlias&gt;.]&lt;column&gt;"</code>.
 *
 * @author James Ahlborn
 */
class ColumnObject extends Expression
{
  protected Column _column;
  
  protected ColumnObject(Column column) {
    _column = column;
  }

  @Override
  public boolean hasParens() { return false; }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    vContext.addColumn(_column);
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    appendTableAliasPrefix(app, _column.getTable());
    app.append(_column.getColumnNameSQL());
  }

  /**
   * Outputs the table alias prefix <code>"[&lt;tableAlias&gt;.]"</code> for a
   * column reference if the current SqlContext specifies table aliases should
   * be used (and the table has an alias), otherwise does nothing.
   */
  static void appendTableAliasPrefix(AppendableExt app, Table table)
    throws IOException
  {
    if(SqlContext.getContext(app).getUseTableAliases()) {
      String alias = table.getAlias();
      if(TableDefObject.hasAlias(alias)) {
        app.append(alias).append(".");
      }
    }
  }  
}
