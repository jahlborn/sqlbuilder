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
