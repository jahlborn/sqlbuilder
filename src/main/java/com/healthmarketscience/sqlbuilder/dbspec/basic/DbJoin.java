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

import com.healthmarketscience.sqlbuilder.dbspec.Join;

/**
 * Representation of a join between two database tables.
 *
 * @author James Ahlborn
 */
public class DbJoin extends DbObject<DbObject<?>> implements Join {
  
  /** the spec in which this schema exists */
  private final DbSpec _spec;
  /** left table of the join */
  private final DbTable _fromTable;
  /** right table of the join */
  private final DbTable _toTable;
  /** join columns from the left table */
  private final List<DbColumn> _fromColumns = new ArrayList<DbColumn>();
  /** join columns from the right table */
  private final List<DbColumn> _toColumns = new ArrayList<DbColumn>();

  public DbJoin(DbSpec spec, DbTable fromTable, DbTable toTable,
                String[] fromColNames, String[] toColNames) {
    this(spec, fromTable, toTable, fromTable.findColumns(fromColNames),
         toTable.findColumns(toColNames));
  }
  
  public DbJoin(DbSpec spec, DbTable fromTable, DbTable toTable,
                DbColumn[] fromColumns, DbColumn[] toColumns) {
    super(null, null);
    _spec = spec;
    _fromTable = fromTable;
    _toTable = toTable;
    addObjects(_fromColumns, _fromTable, fromColumns);
    addObjects(_toColumns, _toTable, toColumns);
  }

  @Override
  public DbSpec getSpec() {
    return _spec;
  }

  public DbTable getFromTable() {
    return _fromTable;
  }
    
  public DbTable getToTable() {
    return _toTable;
  }

  public List<DbColumn> getFromColumns() {
    return _fromColumns;
  }
    
  public List<DbColumn> getToColumns() {
    return _toColumns;
  }

}
