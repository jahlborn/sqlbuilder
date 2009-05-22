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
