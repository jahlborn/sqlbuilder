/*
Copyright (c) 2015 James Ahlborn

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

package com.healthmarketscience.sqlbuilder.custom;

import com.healthmarketscience.sqlbuilder.BaseSqlTestCase;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.CreateIndexQuery;
import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.ValidationException;
import com.healthmarketscience.sqlbuilder.custom.mysql.MysLimitClause;
import com.healthmarketscience.sqlbuilder.custom.mysql.MysObjects;
import com.healthmarketscience.sqlbuilder.custom.oracle.OraObjects;
import com.healthmarketscience.sqlbuilder.custom.oracle.OraTableSpaceClause;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgLimitClause;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgOffsetClause;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgObjects;
import com.healthmarketscience.sqlbuilder.custom.sqlserver.MssTopClause;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbIndex;

/**
 *
 * @author James Ahlborn
 */
public class CustomSyntaxTest extends BaseSqlTestCase
{

  public CustomSyntaxTest(String name) {
    super(name);
  }

  public void testMysqlLimitClause()
  {
    String selectQuery1 = new SelectQuery()
      .addColumns(_table1_col1)
      .addCustomization(new MysLimitClause(45))
      .validate().toString();
    checkResult(selectQuery1,
                "SELECT t0.col1 FROM Schema1.Table1 t0 LIMIT 45");
    
    String selectQuery2 = new SelectQuery()
      .addColumns(_table1_col1)
      .addOrderings(_table1_col1)
      .setForUpdate(true)
      .addCustomization(new MysLimitClause(5, 45))
      .validate().toString();
    checkResult(selectQuery2,
                "SELECT t0.col1 FROM Schema1.Table1 t0 ORDER BY t0.col1 LIMIT 5, 45 FOR UPDATE");

    try {
      new SelectQuery()
        .addColumns(_table1_col1)
        .addCustomization(new MysLimitClause(-1))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {
      // success
    }     

    try {
      new SelectQuery()
        .addColumns(_table1_col1)
        .addCustomization(new MysLimitClause(3.7))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {
      // success
    }     

    try {
      new SelectQuery()
        .addColumns(_table1_col1)
        .addCustomization(new MysLimitClause(0, -34))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {
      // success
    }     
  }

  public void testMysqlCreateTableClause()
  {
    String createTableStr = new CreateTableQuery(_table1, true)
      .addColumnConstraint(_table1_col3, MysObjects.AUTO_INCREMENT_COLUMN)
      .addCustomization(MysObjects.IF_NOT_EXISTS_TABLE)
      .validate().toString();
    checkResult(createTableStr,
                "CREATE TABLE IF NOT EXISTS Schema1.Table1 (col1 VARCHAR(213),col2 NUMBER(7),col3 DECIMAL(4,8) AUTO_INCREMENT,col4 VARCHAR(255))");
  }

  public void testPostgresqlLimitClause()
  {
    String selectQuery1 = new SelectQuery()
      .addColumns(_table1_col1)
      .addCustomization(new PgLimitClause(45))
      .validate().toString();
    checkResult(selectQuery1,
                "SELECT t0.col1 FROM Schema1.Table1 t0 LIMIT 45");
    
    String selectQuery2 = new SelectQuery()
      .addColumns(_table1_col1)
      .addOrderings(_table1_col1)
      .setForUpdate(true)
      .addCustomization(new PgLimitClause(45))
      .addCustomization(new PgOffsetClause(5))
      .validate().toString();
    checkResult(selectQuery2,
                "SELECT t0.col1 FROM Schema1.Table1 t0 ORDER BY t0.col1 LIMIT 45 OFFSET 5 FOR UPDATE");

    String selectQuery3 = new SelectQuery()
      .addColumns(_table1_col1)
      .addOrderings(_table1_col1)
      .addCustomization(new PgOffsetClause(5))
      .validate().toString();
    checkResult(selectQuery3,
                "SELECT t0.col1 FROM Schema1.Table1 t0 ORDER BY t0.col1 OFFSET 5");

    try {
      new SelectQuery()
        .addColumns(_table1_col1)
        .addCustomization(new PgLimitClause(-1))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {
      // success
    }     

    try {
      new SelectQuery()
        .addColumns(_table1_col1)
        .addCustomization(new PgLimitClause(4.2))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {
      // success
    }     

    try {
      new SelectQuery()
        .addColumns(_table1_col1)
        .addCustomization(new PgOffsetClause(-37))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {
      // success
    }     

    try {
      new SelectQuery()
        .addColumns(_table1_col1)
        .addCustomization(new PgOffsetClause(47.3))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {
      // success
    }     
  }

  public void testPostgresqlCreateTableClause()
  {
    String createTableStr = new CreateTableQuery(_table1, true)
      .addCustomization(PgObjects.IF_NOT_EXISTS_TABLE)
      .validate().toString();
    checkResult(createTableStr,
                "CREATE TABLE IF NOT EXISTS Schema1.Table1 (col1 VARCHAR(213),col2 NUMBER(7),col3 DECIMAL(4,8),col4 VARCHAR(255))");
  }

  public void testOracleTablespace()
  {
    String createTableStr = new CreateTableQuery(_table1, true)
      .addCustomization(new OraTableSpaceClause("test"))
      .validate().toString();
    checkResult(createTableStr,
                "CREATE TABLE Schema1.Table1 (col1 VARCHAR(213),col2 NUMBER(7),col3 DECIMAL(4,8),col4 VARCHAR(255)) TABLESPACE test");

    DbIndex index = _schema1.addIndex("Index2", "Table1", "col1");

    String createIndexStr = new CreateIndexQuery(index)
      .addCustomization(new OraTableSpaceClause("test"))
      .validate().toString();
    checkResult(createIndexStr,
                "CREATE INDEX Schema1.Index2 ON Schema1.Table1 (col1) TABLESPACE test");
  }

  public void testOracleLimitClause()
  {
    String selectQuery1 = new SelectQuery()
      .addColumns(_table1_col1)
      .addCondition(BinaryCondition.lessThan(OraObjects.ROWNUM, 100, false))
      .validate().toString();
    checkResult(selectQuery1,
                "SELECT t0.col1 FROM Schema1.Table1 t0 WHERE (ROWNUM < 100)");
  }

  public void testSQLServerTopClause()
  {
    String selectQuery1 = new SelectQuery()
      .addColumns(_table1_col1)
      .addCustomization(new MssTopClause(10))
      .validate().toString();
    checkResult(selectQuery1,
                "SELECT TOP 10 t0.col1 FROM Schema1.Table1 t0");

    String selectQuery2 = new SelectQuery()
      .addColumns(_table1_col1)
      .setIsDistinct(true)
      .addCustomization(new MssTopClause(30, true))
      .validate().toString();
    checkResult(selectQuery2,
                "SELECT DISTINCT TOP 30 PERCENT t0.col1 FROM Schema1.Table1 t0");
  }
}
