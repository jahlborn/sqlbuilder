/*
Copyright (c) 2026 James Ahlborn

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

package com.healthmarketscience.sqlbuilder.dbspec;

import java.util.Arrays;
import java.util.List;

/**
 * Simple Join implementation which can be used with RejoinTable.
 *
 * @author James Ahlborn
 */
public class SimpleJoin implements Join
{
  private final Table _fromTable;
  private final Table _toTable;
  private final List<Column> _fromColumns;
  private final List<Column> _toColumns;

  public SimpleJoin(Column fromColumn, Column toColumn)
  {
    this(fromColumn.getTable(), toColumn.getTable(),
         Arrays.asList(fromColumn), Arrays.asList(toColumn));
  }

  public SimpleJoin(Table fromTable, Table toTable,
                    List<Column> fromColumns, List<Column> toColumns)
  {
    _fromTable = fromTable;
    _toTable = toTable;
    _fromColumns = fromColumns;
    _toColumns = toColumns;
  }

  @Override
  public Table getFromTable()
  {
    return _fromTable;
  }

  @Override
  public Table getToTable()
  {
    return _toTable;
  }

  @Override
  public List<Column> getFromColumns()
  {
    return _fromColumns;
  }

  @Override
  public List<Column> getToColumns()
  {
    return _toColumns;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() +
      " from " + _fromColumns + " to " + _toColumns;
  }
}
