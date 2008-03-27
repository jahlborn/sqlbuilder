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
import java.util.ArrayList;


/**
 * An object representing a clause which generally expects to be
 * combined/nested with other clauses.  Implementations are expected to manage
 * to delimit themselves so that they do not interfere with other clauses at a
 * peer level.  Also, implementations which may contain a variable number of
 * expressions should override the {@link #isEmpty} appropriately.
 *
 * @author James Ahlborn
 */
abstract class NestableClause extends SqlObject
{  
  protected NestableClause() {}

  /**
   * Returns <code>true</code> iff the output of this instance would be an
   * empty expression, <code>false</code> otherwise.
   * <p>
   * Default implementation returns {@code false}.
   */
  public boolean isEmpty() { return false; }


  /**
   * Determines if any of the given clauses are non-empty.
   * @return {@code false} if at least one clause is non-empty, {@code true}
   *         otherwise
   */
  protected static boolean areEmpty(
      SqlObjectList<? extends NestableClause> nestedClauses)
  {
    for(NestableClause nc : nestedClauses) {
      if(!nc.isEmpty()) {
        // we contain a non-empty clause, therefore we are not empty
        return false;
      }
    }
      
    // we're empty!
    return true;
  }

  /**
   * Appends the given custom clause to the given AppendableExt, handling
   * {@code null} and enclosing parens.
   */
  protected static void appendCustomIfNotNull(AppendableExt app,
                                              SqlObject obj)
    throws IOException
  {
    if(obj != null) {
      app.append("(").append(obj).append(")");
    }
  }
  
  /**
   * Appends the given nested clauses to the given AppendableExt, handling
   * empty nested clauses and enclosing parens.
   */
  protected static void appendNestedClauses(
      AppendableExt app,
      SqlObjectList<? extends NestableClause> nestedClauses)
    throws IOException
  {
    // optimize for the expected case of no empty sub-conditions (otherwise,
    // we would copy/filter the list every time instead of testing first, then
    // filtering)
    boolean hasEmptyNestedClause = false;
    for(NestableClause nestedClause : nestedClauses) {
      if(nestedClause.isEmpty()) {
        // doh!  need to filter
        hasEmptyNestedClause = true;
        break;
      }
    }

    SqlObjectList<? extends NestableClause> tmpNestedClauses;
    if(!hasEmptyNestedClause) {
      
      // cool, use existing list
      tmpNestedClauses = nestedClauses;
      
    } else {
      
      // create a filtered list of non-empty sub-nestedClauses
      SqlObjectList<NestableClause> nonEmptyNestableClauses =
        new SqlObjectList<NestableClause>(
          nestedClauses.getDelimiter(),
          new ArrayList<NestableClause>(nestedClauses.size()));
      for(NestableClause nestedClause : nestedClauses) {
        if(!nestedClause.isEmpty()) {
          nonEmptyNestableClauses.addObject(nestedClause);
        }
      }
      
      // use this list instead
      tmpNestedClauses = nonEmptyNestableClauses;
    }

    // append the non-empty nestedClauses
    if(tmpNestedClauses.size() > 1) {
      app.append("(").append(tmpNestedClauses).append(")");
    } else {
      app.append(tmpNestedClauses);
    }
  }

}
