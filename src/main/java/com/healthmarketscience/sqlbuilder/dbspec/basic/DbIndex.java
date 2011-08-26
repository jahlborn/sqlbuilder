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

package com.healthmarketscience.sqlbuilder.dbspec.basic;

import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.dbspec.Index;

/**
 * Representation of an index in a database schema.
 *
 * @author James Ahlborn
 */
public class DbIndex extends DbObject<DbSchema> implements Index {

  /** the table which is indexed */
  private final DbTable _table;
  /** the columns of the table which are indexed */
  private final List<DbColumn> _columns = new ArrayList<DbColumn>();

  public DbIndex(DbTable table, String name,
                 String... colNames) {
    this(table, name, table.findColumns(colNames));
  }
  
  public DbIndex(DbTable table, String name,
                 DbColumn... columns) {
    super(table.getParent(), name);
    _table = table;
    addObjects(_columns, _table, columns);
  }

  public DbSchema getSchema() {
    return getParent();
  }
    
  public String getIndexNameSQL() {
    return getAbsoluteName();
  }

  public DbTable getTable() {
    return _table;
  }

  public List<DbColumn> getColumns() {
    return _columns;
  }
    
 
}
