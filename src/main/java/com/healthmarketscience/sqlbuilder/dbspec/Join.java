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
 * Maintains information about a database join for use with the sqlbuilder
 * utilities.
 *
 * @author James Ahlborn
 */
public interface Join {

  /** @return the table which is the "left side" of this join */
  public Table getFromTable();
  
  /** @return the table which is the "right side" of this join */
  public Table getToTable();

  /** @return the columns in the "left side" table which are related to the
      columns in the "right side" table.  must be the same length as
      the list returned from <code>getToColumns</code> */
  public List<? extends Column> getFromColumns();
  
  /** @return the columns in the "right side" table which are related to the
      columns in the "to side" table.  must be the same length as
      the list returned from <code>getFromColumns</code> */
  public List<? extends Column> getToColumns();
  
}
