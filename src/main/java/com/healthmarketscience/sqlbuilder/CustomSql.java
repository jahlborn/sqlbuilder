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
 * Outputs the given object as is (can be used to insert any custom SQL into a
 * statement).
 * <p>
 * Example:
 * <pre>
 * String selectQuery =
 *   (new SelectQuery())
 *   .addCustomColumns(new CustomSql("foo"),
 *                     new CustomSql("baz"),
 *                     new CustomSql("buzz"))
 *   .addCustomJoin(SelectQuery.JoinType.INNER_JOIN,
 *                  new CustomSql("table1"), new CustomSql("table2"),
 *                  BinaryCondition.equalTo(
 *                    new CustomSql("table1.id"), new CustomSql("table2.id")))
 *   .addCustomOrderings(new CustomSql("foo"))
 *   .validate().toString();
 *
 * // Output:
 * SELECT foo,baz,buzz FROM table1 INNER JOIN table2 ON (table1.id = table2.id) ORDER BY foo
 * </pre>
 *
 * @see com.healthmarketscience.sqlbuilder.dbspec dbspec database model
 * 
 * @author James Ahlborn
 */
public class CustomSql extends SqlObject
{
  private Object _obj;

  public CustomSql(Object obj) {
    _obj = obj;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
  }
  
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_obj);
  }
}
