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
 * Outputs the given object with a column alias
 * <code>"&lt;obj&gt; AS &lt;alias&gt;"</code>.
 *
 * @author James Ahlborn
 */
public class AliasedObject extends SqlObject
{
  private SqlObject _obj;
  private String _alias;

  public AliasedObject(SqlObject obj, String alias) {
    _obj = obj;
    _alias = alias;
  }

  /**
   * Optionally adds an alias to a SqlObject.
   * @return the given SqlObject wrapped by an AliasedObject if the given
   *         alias is non-<code>null</code>, otherwise, the given SqlObject.
   */
  public static SqlObject toAliasedObject(SqlObject obj, String alias) {
    if(alias != null) {
      obj = new AliasedObject(obj, alias);
    }
    return obj;
  }
  
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _obj.collectSchemaObjects(vContext);
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_obj).append(" AS ").append(_alias);
  }

}
