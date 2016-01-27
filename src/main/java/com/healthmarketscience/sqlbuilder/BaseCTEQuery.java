/*
Copyright (c) 2016 James Ahlborn

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
 * Base class for all queries which support common table expressions (the
 * "WITH " clause).  Note that CTEs are defined in "SQL 99".
 *
 * @author James Ahlborn
 */
public abstract class BaseCTEQuery<ThisType extends BaseCTEQuery<ThisType>> 
  extends Query<ThisType>
{
  private boolean _recursive;
  private SqlObjectList<SqlObject> _ctes = SqlObjectList.create();
  /** unique id for the next cte alias for this query */
  private int _nextCteAliasNum;

  protected BaseCTEQuery() 
  {
  }

  public ThisType setRecursive(boolean recursive) {
    _recursive = recursive;
    return getThisType();
  }

  /**
   * Adds the given common table expression to this query.
   * 
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public ThisType addCommonTableExpression(Object cteObj) {
    // if cte doesn't current have alias, create one now
    if(cteObj instanceof CommonTableExpression) {
      CommonTableExpression cte = (CommonTableExpression)cteObj;
      if(!cte.hasTableAlias()) {
        String alias = "cte" + _nextCteAliasNum++;
        cte.setTableAlias(alias);
      }
    }
    _ctes.addObject(Converter.toCustomSqlObject(cteObj));
    return getThisType();
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _ctes.collectSchemaObjects(vContext);
  }

  @Override
  protected void prependTo(AppendableExt app) throws IOException {
    // we prepend the CTE outside the context of this query because the CTE is
    // really more like a wrapping query than part of this query
    if(!_ctes.isEmpty()) {
      app.append("WITH ");
      if(_recursive) {
        app.append("RECURSIVE ");
      }
      app.append(_ctes).append(" ");
    }
  }
}
