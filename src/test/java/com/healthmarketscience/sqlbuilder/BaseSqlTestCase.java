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
