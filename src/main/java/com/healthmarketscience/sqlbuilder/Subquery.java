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
