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
