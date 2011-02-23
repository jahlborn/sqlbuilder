/*
Copyright (c) 2011 James Ahlborn

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
*/

package com.healthmarketscience.sqlbuilder.dbspec;

import java.util.List;

/**
 * Maintains information about a database (table or column) constraint for use
 * with the sqlbuilder utilities.
 *
 * @author James Ahlborn
 */
public interface Constraint 
{
  public enum Type {
    NOT_NULL,
    UNIQUE,
    PRIMARY_KEY,
    FOREIGN_KEY;
  }

  /** @return the type of this constraint */
  public Type getType();

  /** @return the name of this constraint, if any */
  public String getConstraintNameSQL();

  /** @return the constrained columns */
  public List<? extends Column> getColumns();
}
