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

package com.healthmarketscience.sqlbuilder.dbspec;

import java.util.List;

/**
 * Maintains information about a database column for use with the sqlbuilder
 * utilities.
 *
 * @author James Ahlborn
 */
public interface Column {

  /** @return the parent database table of this column */
  public Table getTable();
  
  /** @return the simple name of this column */
  public String getColumnNameSQL();

  /** @return the type of this column */
  public String getTypeNameSQL();

  /** @return the length of the type of this column, may be
      <code>null</code> */
  public Integer getTypeLength();

  /** @return any constraints for this column */
  public List<? extends Constraint> getConstraints();

  /** @return the default value for this column, if any */
  public Object getDefaultValue();
}
