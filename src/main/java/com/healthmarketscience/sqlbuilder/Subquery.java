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
 * Outputs the given query surrounded by parentheses
 * <code>"(&lt;query&gt;)"</code>, useful for embedding one query within
 * another.
 *
 * @author James Ahlborn
 */
public class Subquery extends Expression
{

  protected SqlObject _query;

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public Subquery(Object query) {
    _query = ((query != null) ? Converter.toCustomSqlObject(query) :
              null);
  }

  @Override
  public boolean isEmpty() {
    return _query == null;
  }
  
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    // we do not collect into the subquery if this a "local only" collection
    if((_query != null) && (!vContext.isLocalOnly())) {
      // subqueries need a nested validation context because their schema
      // objects *do not* affect the outer query, but the outer query's
      // schema objects *do* affect their query
      _query.collectSchemaObjects(new ValidationContext(vContext));
    }
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    appendCustomIfNotNull(app, _query);
  }

}
