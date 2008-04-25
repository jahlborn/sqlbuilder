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
import java.util.Collection;
import java.util.HashSet;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;



/**
 * Base class for all query statments which adds a validation facility.
 *
 * The query classes are designed for "builder" type use, so all return
 * values are the query object itself.
 *
 * @author James Ahlborn
 */
public abstract class Query extends SqlObject
{
  protected Query() {}

  /**
   * Verifies that any columns referenced in the query have their respective
   * tables also referenced in the query.
   */
  public Query validate()
    throws ValidationException
  {
    validate(true, new ValidationContext());
    return this;
  }
    
  /**
   * Collects the schemaobjects for this query and optionally verifies that
   * any columns referenced in the query have their respective tables also
   * referenced in the query.
   *
   * @param checkTables iff <code>true</code>, check tables against
   *                    referenced columns, otherwise, don't
   * @param vContext handle to the current validation context
   */
  protected void validate(boolean checkTables, ValidationContext vContext)
    throws ValidationException
  {      
    // collect the tables and columns
    collectSchemaObjects(vContext);

    // make sure all column tables are referenced by a table (if desired)
    if(checkTables &&
       !vContext.getTables().containsAll(
           vContext.getColumnTables())) {
      throw new ValidationException("Columns used for unreferenced tables");
    }
  }

  @Override
  public final void appendTo(AppendableExt app) throws IOException {
    SqlContext newContext = SqlContext.pushContext(app);
    newContext.setQuery(this);
    appendTo(app, newContext);
    // note, this is not within a finally block because any exceptions from
    // appendTo are expected to be unrecoverable, and we don't want to muddy
    // the water with possible exceptions from popContext
    SqlContext.popContext(app, newContext);
  }

  /**
   * Appends the sql query to the given AppendableExt within the given,
   * modifiable SqlContext.  This method is invoked by the
   * {@link #appendTo(AppendableExt)} method within the context of calls to
   * {@link SqlContext#pushContext} and {@link SqlContext#popContext}, so
   * the implementation is free to modify the given SqlContext.
   * @param app the target for the sql query generation
   * @param newContext modifiable SqlContext for nested Appendees
   */
  protected abstract void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException;

  
}
