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
 * Outputs the table definition <code>"&lt;table&gt; [&lt;alias&gt;]"</code>
 * (used for FROM clauses).
 *
 * @author James Ahlborn
 */
class TableDefObject extends TableObject
{
  TableDefObject(Table table) {
    super(table);
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_table.getTableNameSQL());
    String alias = _table.getAlias();
    if(hasAlias(alias)) {
      app.append(" ").append(alias);
    }
  }

  /**
   * Returns {@code true} if the given alias is a non-empty string, {@code
   * false} otherwise.
   */
  public static boolean hasAlias(String alias) {
    return ((alias != null) && (alias.length() > 0));
  }
}
