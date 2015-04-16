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
