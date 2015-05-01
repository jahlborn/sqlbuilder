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

package com.healthmarketscience.sqlbuilder;

import java.sql.Types;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import junit.framework.TestCase;

/**
 * @author james
 */
public abstract class BaseSqlTestCase extends TestCase
{
  protected DbSpec _spec;
  protected DbSchema _schema1;
  protected DbSchema _defSchema;
  protected DbTable _table1;
  protected DbColumn _table1_col1;
  protected DbColumn _table1_col2;
  protected DbColumn _table1_col3;
  protected DbTable _defTable1;
  protected DbColumn _defTable1_col_id;
  protected DbColumn _defTable1_col2;
  protected DbColumn _defTable1_col3;
  protected DbTable _defTable2;
  protected DbColumn _defTable2_col_id;
  protected DbColumn _defTable2_col4;
  protected DbColumn _defTable2_col5;

  protected DbJoin _idJoin;    
  protected DbJoin _col4Join;
  
  protected BaseSqlTestCase(String name) {
    super(name);
  }
  
  @Override
  protected void setUp() throws Exception
  {
    _spec = new DbSpec();
    _schema1 = _spec.addSchema("Schema1");
    _defSchema = _spec.addDefaultSchema();
    _table1 = _schema1.addTable("Table1");
    _table1_col1 = _table1.addColumn("col1", "VARCHAR", 213);
    _table1_col2 = _table1.addColumn("col2", "NUMBER", 7);
    _table1_col3 = _table1.addColumn("col3", Types.DECIMAL, 4, 8);
    _defTable1 = _defSchema.addTable("Table1");
    _defTable1_col_id = _defTable1.addColumn("col_id", "NUMBER", null);
    _defTable1_col2 = _defTable1.addColumn("col2", Types.VARCHAR, 64).setDefaultValue("blah");
    _defTable1_col3 = _defTable1.addColumn("col3", Types.DATE, null, null);
    _defTable2 = _defSchema.addTable("Table2");
    _defTable2_col_id = _defTable2.addColumn("col_id", "NUMBER", null);
    _defTable2_col_id.notNull();
    _defTable2_col_id.primaryKey("col_id_pk");
    _defTable2_col4 = _defTable2.addColumn("col4", "VARCHAR", 64);
    _defTable2_col5 = _defTable2.addColumn("col5", "DATE", null, null);
    _defTable2.foreignKey("t2_fk", new String[]{"col4","col5"},
                          null, "Table1", new String[]{"col2", "col3"});
    _defTable2.checkCondition("neq_cond", BinaryCondition.notEqualTo(
                                  _defTable2_col4, _defTable2_col5));

    _idJoin = _spec.addJoin(null, "Table1",
                            null, "Table2",
                            "col_id");

    _table1.addColumn("col4", Types.VARCHAR, 255);
    _defTable1.addColumn("altCol4", Types.VARCHAR, 255);
    
    _col4Join = _spec.addJoin("Schema1", "Table1",
                              null, "Table1",
                              new String[]{"col4"},
                              new String[]{"altCol4"});
  }

  protected static void checkResult(String result, String expected)
  {
    assertEquals(expected, result);
  }
  
}
