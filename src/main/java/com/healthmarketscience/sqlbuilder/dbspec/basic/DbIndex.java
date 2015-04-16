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
