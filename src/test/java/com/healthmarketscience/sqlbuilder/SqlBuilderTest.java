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

import java.io.IOException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbFunction;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbFunctionPackage;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbIndex;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import static com.healthmarketscience.sqlbuilder.Conditions.*;
import static com.healthmarketscience.sqlbuilder.Expressions.*;

/**
 * @author James Ahlborn
 */
public class SqlBuilderTest extends BaseSqlTestCase
{

  public SqlBuilderTest(String name) {
    super(name);
  }

  public void testCreateTable()
  {
    String createStr1 = new CreateTableQuery(_table1)
      .addColumns(_table1_col1, _table1_col3).validate().toString();
    checkResult(createStr1,
                "CREATE TABLE Schema1.Table1 (col1 VARCHAR(213),col3 DECIMAL(4,8))");

    String createStr2 = new CreateTableQuery(_table1, true)
      .validate().toString();
    checkResult(createStr2,
                "CREATE TABLE Schema1.Table1 (col1 VARCHAR(213),col2 NUMBER(7),col3 DECIMAL(4,8),col4 VARCHAR(255))");

    String createStr3 = new CreateTableQuery(_defTable1, true)
      .validate().toString();
    checkResult(createStr3,
                "CREATE TABLE Table1 (col_id NUMBER,col2 VARCHAR(64) DEFAULT 'blah',col3 DATE,altCol4 VARCHAR(255))");

    @SuppressWarnings("deprecation")
    String createStr4 = new CreateTableQuery(_defTable1, false)
      .addColumns(_defTable1_col_id, _defTable1_col2)
      .setColumnConstraint(_defTable1_col_id,
                           CreateTableQuery.ColumnConstraint.PRIMARY_KEY)
      .addColumn(_defTable1_col3, CreateTableQuery.ColumnConstraint.NOT_NULL)
      .addColumnConstraint(_defTable1_col3,
                           ConstraintClause.foreignKey("col3_fk", _table1)
                           .addRefColumns(_table1_col3))
      .addCustomColumn("col4 NUMBER", CreateTableQuery.ColumnConstraint.NOT_NULL)
      .addColumnConstraint(_table1_col1, CreateTableQuery.ColumnConstraint.UNIQUE)
      .validate().toString();
    checkResult(createStr4,
                "CREATE TABLE Table1 (col_id NUMBER PRIMARY KEY,col2 VARCHAR(64) DEFAULT 'blah',col3 DATE NOT NULL CONSTRAINT col3_fk REFERENCES Schema1.Table1 (col3),col4 NUMBER NOT NULL)");

    String createStr5 = new CreateTableQuery(_defTable1, true)
      .addColumnConstraint(_defTable1_col_id,
                           ConstraintClause.notNull())
      .addColumnConstraint(_defTable1_col_id,
                           ConstraintClause.primaryKey("id_pk"))
      .setColumnTypeName(_defTable1_col_id, "BIGINT")
      .addColumnConstraint(_defTable1_col_id, ConstraintClause.checkCondition(
                               greaterThan(_defTable1_col_id, 10, false)))
      .addColumnConstraint(_defTable1_col3,
                           ConstraintClause.notNull())
      .setColumnDefaultValue(_defTable1_col3, new CustomSql("CURRENT_DATE"))
      .addCustomConstraints(ConstraintClause.unique()
                            .addColumns(_defTable1_col2, _defTable1_col3))
      .validate().toString();
    checkResult(createStr5,
                "CREATE TABLE Table1 (col_id BIGINT NOT NULL CONSTRAINT id_pk PRIMARY KEY CHECK (col_id > 10),col2 VARCHAR(64) DEFAULT 'blah',col3 DATE DEFAULT CURRENT_DATE NOT NULL,altCol4 VARCHAR(255),UNIQUE (col2,col3))");

    String createStr6 = new CreateTableQuery(_defTable2, true)
      .setTableType(CreateTableQuery.TableType.TEMPORARY)
      .validate().toString();
    checkResult(createStr6,
                "CREATE TEMPORARY TABLE Table2 (col_id NUMBER NOT NULL CONSTRAINT col_id_pk PRIMARY KEY,col4 VARCHAR(64),col5 DATE,CONSTRAINT t2_fk FOREIGN KEY (col4,col5) REFERENCES Table1 (col2,col3),CONSTRAINT neq_cond CHECK (col4 <> col5))");

    try {
      new CreateTableQuery(_table1).validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
  }

  public void testIndex()
  {
    DbIndex index = _schema1.addIndex("Index1", "Table1",
                                      "col1", "col2");
    CreateIndexQuery query = new CreateIndexQuery(index);
    String createStr1 = query.validate().toString();
    checkResult(createStr1,
                "CREATE INDEX Schema1.Index1 ON Schema1.Table1 (col1,col2)");


    String createStr2 = query.setIndexType(CreateIndexQuery.IndexType.UNIQUE)
      .validate().toString();
    checkResult(createStr2,
                "CREATE UNIQUE INDEX Schema1.Index1 ON Schema1.Table1 (col1,col2)");

    String dropStr1 = query.getDropQuery().validate().toString();
    checkResult(dropStr1,
                "DROP INDEX Schema1.Index1");
  }

  public void testDropTable()
  {
    String dropStr1 = DropQuery.dropTable(_table1).validate().toString();
    checkResult(dropStr1, "DROP TABLE Schema1.Table1");

    String dropStr2 = DropQuery.dropTable(_defTable1)
      .setBehavior(DropQuery.Behavior.CASCADE).validate().toString();
    checkResult(dropStr2, "DROP TABLE Table1 CASCADE");

    String dropStr3 = new CreateTableQuery(_table1)
      .addColumns(_table1_col1, _table1_col3)
      .getDropQuery().validate().toString();
    checkResult(dropStr3, "DROP TABLE Schema1.Table1");

    String dropStr4 = new CreateTableQuery(_defTable1, true)
      .getDropQuery().validate().toString();
    checkResult(dropStr4, "DROP TABLE Table1");
  }

  public void testInsert()
  {
    String insertStr1 = new InsertQuery(_table1)
      .addColumns(new DbColumn[]{_table1_col1, _table1_col3, _table1_col2},
                  new Object[]{13, "feed me seymor", true})
      .validate().toString();
    checkResult(insertStr1,
                "INSERT INTO Schema1.Table1 (col1,col3,col2) VALUES (13,'feed me seymor',1)");

    String insertStr2 = new InsertQuery(_table1)
      .addColumns(new DbColumn[]{_table1_col1},
                  new Object[]{"13"})
      .addPreparedColumns(_table1_col2, _table1_col3)
      .validate().toString();
    checkResult(insertStr2,
                "INSERT INTO Schema1.Table1 (col1,col2,col3) VALUES ('13',?,?)");

    String insertStr3 = new InsertQuery(_defTable1)
      .addColumns(new DbColumn[]{_defTable1_col_id},
                  new Object[]{13})
      .addPreparedColumns(_defTable1_col2, _defTable1_col3)
      .validate().toString();
    checkResult(insertStr3,
                "INSERT INTO Table1 (col_id,col2,col3) VALUES (13,?,?)");

    try {
      new InsertQuery(_table1)
        .addColumns(new DbColumn[]{_table1_col1, _table1_col3},
                    new Object[]{13})
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
  }

  public void testInsertSelect()
  {
    SelectQuery selectQuery = new SelectQuery()
      .addColumns(_table1_col1, _table1_col2, _table1_col3).validate();

    String insertStr1 = new InsertSelectQuery(_defTable1)
      .addColumns(_defTable1_col_id, _defTable1_col2,
                  _defTable1_col3)
      .setSelectQuery(selectQuery)
      .validate().toString();
    checkResult(insertStr1,
                "INSERT INTO Table1 (col_id,col2,col3) SELECT t0.col1,t0.col2,t0.col3 FROM Schema1.Table1 t0");

    try {
      new InsertSelectQuery(_defTable1)
        .addColumns(_defTable1_col_id, _defTable1_col2,
                    _defTable1_col3)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new InsertSelectQuery(_defTable1)
        .addColumns(_defTable1_col_id, _defTable1_col2)
        .setSelectQuery(selectQuery)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
  }

  public void testSelect()
  {
    {
      SelectQuery selectQuery1 = new SelectQuery()
        .addColumns(_table1_col1, _defTable1_col2, _defTable2_col5);

      String selectStr1 = selectQuery1.validate().toString();
      checkResult(selectStr1,
                "SELECT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0,Table1 t1,Table2 t2");

      String selectStr2 = selectQuery1.setIsDistinct(true)
        .validate().toString();
      checkResult(selectStr2,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0,Table1 t1,Table2 t2");

      String selectStr3 = selectQuery1.addJoins(SelectQuery.JoinType.INNER,
                                                _col4Join)
        .addJoins(SelectQuery.JoinType.LEFT_OUTER, _idJoin)
        .validate().toString();
      checkResult(selectStr3,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id)");

      String selectStr4 = selectQuery1.addOrderings(_defTable1_col2)
        .validate().toString();
      checkResult(selectStr4,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) ORDER BY t1.col2");

      String selectStr5 = selectQuery1.addCondition(
        greaterThan(_defTable2_col4, 42, true))
        .validate().toString();
      checkResult(selectStr5,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= 42) ORDER BY t1.col2");

      String selectStr6 = selectQuery1.addOrdering(_defTable2_col5,
                                                   OrderObject.Dir.DESCENDING)
        .validate().toString();
      checkResult(selectStr6,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= 42) ORDER BY t1.col2,t2.col5 DESC");

      String selectStr7 = selectQuery1.addHaving(greaterThan(_defTable1_col2, new NumberValueObject(1), false)).toString();
      checkResult(selectStr7,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= 42) ORDER BY t1.col2,t2.col5 DESC");

      String selectStr8 = selectQuery1.addGroupings(_defTable1_col2,
                                                    _defTable2_col5)
        .validate().toString();
      checkResult(selectStr8,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= 42) GROUP BY t1.col2,t2.col5 HAVING (t1.col2 > 1) ORDER BY t1.col2,t2.col5 DESC");
    }

    String selectStr6 = new SelectQuery()
      .addAllTableColumns(_table1)
      .validate().toString();
    checkResult(selectStr6,
                "SELECT t0.* FROM Schema1.Table1 t0");

    String selectStr7 = new SelectQuery()
      .addAllColumns()
      .addFromTable(_defTable1)
      .addFromTable(_defTable2)
      .validate().toString();
    checkResult(selectStr7,
                "SELECT * FROM Table1 t1, Table2 t2");

    String selectStr8 = new SelectQuery()
      .setForUpdate(true)
      .addAllColumns()
      .addFromTable(_defTable1)
      .addFromTable(_defTable2)
      .validate().toString();
    checkResult(selectStr8,
                "SELECT * FROM Table1 t1, Table2 t2 FOR UPDATE");

    String selectStr9 = new SelectQuery()
      .addColumns(_table1_col1, _defTable1_col2)
      .addCustomColumns(Converter.toColumnSqlObject(
                            _defTable2_col5, "MyCol"))
      .addAliasedColumn(_defTable2_col4, "SomeCol")
      .validate().toString();
    checkResult(selectStr9,
                "SELECT t0.col1,t1.col2,t2.col5 AS MyCol,t2.col4 AS SomeCol FROM Schema1.Table1 t0,Table1 t1,Table2 t2");

    String selectStr10 = new SelectQuery()
      .addColumns(_table1_col1, _defTable1_col2)
      .addCustomJoin(
          SqlObjectList.create(" CROSS JOIN ")
          .addObjects(Converter.CUSTOM_TABLE_DEF_TO_OBJ, _table1, _defTable1))
      .validate().toString();
    checkResult(selectStr10,
                "SELECT t0.col1,t1.col2 FROM Schema1.Table1 t0 CROSS JOIN Table1 t1");

    String selectStr11 = new SelectQuery()
      .addAllTableColumns(_table1)
      .setOffset(0)
      .validate().toString();
    checkResult(selectStr11,
                "SELECT t0.* FROM Schema1.Table1 t0 OFFSET 0 ROWS");

    String selectStr12 = new SelectQuery()
      .addAllTableColumns(_table1)
      .addOrdering(_table1_col1, OrderObject.Dir.DESCENDING)
      .setOffset(0)
      .setFetchNext(25)
      .validate().toString();
    checkResult(selectStr12,
                "SELECT t0.* FROM Schema1.Table1 t0 ORDER BY t0.col1 DESC OFFSET 0 ROWS FETCH NEXT 25 ROWS ONLY");

    try {
      new SelectQuery()
        .addColumns(_table1_col1, _defTable1_col2, _defTable2_col5)
        .addFromTable(_defTable1).validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new SelectQuery()
        .addColumns(_defTable1_col2)
        .addFromTable(_defTable1)
        .addOrderings(_table1_col1)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new SelectQuery()
        .addColumns(_defTable1_col2)
        .addFromTable(_defTable1)
        .addIndexedOrderings(2)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new SelectQuery()
        .addColumns(_defTable1_col_id, _defTable1_col2)
        .addFromTable(_defTable1)
        .addCustomOrderings(1.5d)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      DbTable table3 = _schema1.addTable("Table3");

      new SelectQuery()
        .addColumns(_table1_col1)
        .addJoin(SelectQuery.JoinType.INNER, _table1, _defTable1,
                 Arrays.asList(_table1_col1),
                 Arrays.asList(_defTable1_col_id))
        .addJoin(SelectQuery.JoinType.INNER, table3, _defTable2,
                 Arrays.asList(_defTable1_col_id),
                 Arrays.asList(_defTable2_col_id))
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new SelectQuery()
        .addAllTableColumns(_table1)
        .setOffset(-1)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

  }

  public void testSelectNoAlias()
  {
    RejoinTable noAliasTable = new RejoinTable(_table1, null);
    Column col1 = noAliasTable.findColumnByName("col1");

    SelectQuery selectQuery1 = new SelectQuery()
      .addColumns(col1, _defTable1_col2, _defTable2_col5);

    String selectStr1 = selectQuery1.addJoin(
        SelectQuery.JoinType.INNER,
        noAliasTable, _defTable1,
        col1, _defTable1_col2)
        .addJoins(SelectQuery.JoinType.LEFT_OUTER, _idJoin)
        .validate().toString();
      checkResult(selectStr1,
                  "SELECT col1,t1.col2,t2.col5 FROM Schema1.Table1 INNER JOIN Table1 t1 ON (col1 = t1.col2) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id)");

      String selectStr2 = selectQuery1
        .addCondition(greaterThan(_defTable2_col4, 42, true))
        .addCondition(equalTo(col1, "foo"))
        .validate().toString();
      checkResult(selectStr2,
                  "SELECT col1,t1.col2,t2.col5 FROM Schema1.Table1 INNER JOIN Table1 t1 ON (col1 = t1.col2) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE ((t2.col4 >= 42) AND (col1 = 'foo'))");
  }

  public void testCondition()
  {
    SelectQuery sq = new SelectQuery().addColumns(_table1_col1);

    String reallyComplicatedConditionStr = and(
      lessThan(_table1_col1, "FOO", false),
      or(),
      isNotNull(_defTable1_col_id),
      new ComboCondition(ComboCondition.Op.OR,
                         new CustomCondition("IM REALLY SNAZZY"),
                         new NotCondition(
                           like(_defTable2_col5,
                                                "BUZ%")),
                         new BinaryCondition(BinaryCondition.Op.EQUAL_TO,
                                             customSql("YOU"),
                                             "ME")),
      or(
        new UnaryCondition(UnaryCondition.Op.IS_NULL,
                           _table1_col2)),
      new InCondition(_defTable2_col4,
                      "this string",
                      new NumberValueObject(37))
      .addObject(new NumberValueObject(42)),
      notLike(_table1_col2, "\\_%").setLikeEscapeChar('\\'),
      exists(sq))
      .toString();
    checkResult(reallyComplicatedConditionStr,
                "((t0.col1 < 'FOO') AND (t1.col_id IS NOT NULL) AND ((IM REALLY SNAZZY) OR (NOT (t2.col5 LIKE 'BUZ%')) OR (YOU = 'ME')) AND (t0.col2 IS NULL) AND (t2.col4 IN ('this string',37,42) ) AND (t0.col2 NOT LIKE '\\_%' ESCAPE '\\') AND (EXISTS (SELECT t0.col1 FROM Schema1.Table1 t0)))");

    checkResult(new InCondition(_defTable2_col4,
                                new NumberValueObject(37)).toString(),
                "(t2.col4 IN (37) )");

    try {
      equalTo(_table1_col2, "\\37").setLikeEscapeChar('\\');
      fail("IllegalArgumentException should have been thrown");
    } catch(IllegalArgumentException e) {}
  }

  public void testConditionAlterParens()
  {
    SelectQuery sq = new SelectQuery().addColumns(_table1_col1);

    String reallyComplicatedConditionStr = and(
      lessThan(_table1_col1, "FOO", false),
      or(),
      isNotNull(_defTable1_col_id),
      new ComboCondition(ComboCondition.Op.OR,
                         new CustomCondition("IM REALLY SNAZZY")
                           .setDisableParens(true),
                         new NotCondition(
                           like(_defTable2_col5,
                                                "BUZ%")),
                         new BinaryCondition(BinaryCondition.Op.EQUAL_TO,
                                             customSql("YOU"),
                                             "ME"))
        .setDisableParens(true),
      or(
        new UnaryCondition(UnaryCondition.Op.IS_NULL,
                           _table1_col2)),
      new InCondition(_defTable2_col4,
                      "this string",
                      new NumberValueObject(37))
      .addObject(new NumberValueObject(42))
      .setDisableParens(true),
      notLike(_table1_col2, "\\_%").setLikeEscapeChar('\\')
        .setDisableParens(true),
      exists(sq))
      .toString();
    checkResult(reallyComplicatedConditionStr,
                "((t0.col1 < 'FOO') AND (t1.col_id IS NOT NULL) AND IM REALLY SNAZZY OR (NOT (t2.col5 LIKE 'BUZ%')) OR (YOU = 'ME') AND (t0.col2 IS NULL) AND t2.col4 IN ('this string',37,42) AND t0.col2 NOT LIKE '\\_%' ESCAPE '\\' AND (EXISTS (SELECT t0.col1 FROM Schema1.Table1 t0)))");
  }

  public void testExpression()
  {
    Expression expr = add(
        37, _defTable2_col5,
        negate(multiply(_table1_col1, 4.7f)),
        subtract(),
        new NegateExpression((Object)Expression.EMPTY),
        "PI", customSql("8 - 3"));
    String reallyComplicatedExpression = expr.toString();
    checkResult(reallyComplicatedExpression,
                "(37 + t2.col5 + (- (t0.col1 * 4.7)) + 'PI' + (8 - 3))");

    String exprQuery = new SelectQuery()
      .addCustomColumns(expr, _table1_col2)
      .validate().toString();
    checkResult(exprQuery, "SELECT (37 + t2.col5 + (- (t0.col1 * 4.7)) + 'PI' + (8 - 3)),t0.col2 FROM Table2 t2,Schema1.Table1 t0");

    String concatExpression = concatenate(
        "The answer is ", add(40, 2), ".")
      .toString();
    checkResult(concatExpression,
                "('The answer is ' || (40 + 2) || '.')");
  }

  public void testFunction()
  {
    // add some functions to play with
    DbFunctionPackage funcPack1 = _schema1.addFunctionPackage("fpkg");
    DbFunction func1 = funcPack1.addFunction("func1");
    DbFunctionPackage funcPack2 = _schema1.addFunctionPackage((String)null);
    DbFunction func2 = funcPack2.addFunction("Func2");
    DbFunctionPackage funcPack3 = _defSchema.addDefaultFunctionPackage();
    DbFunction func3 = funcPack3.addFunction("func3");

    String funcStr1 = new FunctionCall(func1).toString();
    checkResult(funcStr1, "Schema1.fpkg.func1()");

    String funcStr2 = new FunctionCall(func2)
      .addColumnParams(_table1_col1)
      .toString();
    checkResult(funcStr2, "Schema1.Func2(t0.col1)");

    String funcStr3 = new FunctionCall(func2)
      .setIsDistinct(true)
      .addColumnParams(_table1_col1)
      .toString();
    checkResult(funcStr3, "Schema1.Func2(DISTINCT t0.col1)");

    String funcStr4 = new FunctionCall(func3)
      .addColumnParams(_table1_col1)
      .addCustomParams("42")
      .toString();
    checkResult(funcStr4, "func3(t0.col1,'42')");

    String funcStr5 = new FunctionCall(func3)
      .addCustomParams(new String("HAPPY"), _table1_col1)
      .toString();
    checkResult(funcStr5, "func3('HAPPY',t0.col1)");

    String funcStr6 = FunctionCall.sum()
      .addColumnParams(_table1_col3)
      .toString();
    checkResult(funcStr6, "SUM(t0.col3)");

    String funcStr7 = FunctionCall.countAll()
      .toString();
    checkResult(funcStr7, "COUNT(*)");

    String funcStr8 = new FunctionCall(func3)
      .addColumnParams(_table1_col1)
      .addNumericValueParam(42)
      .toString();
    checkResult(funcStr8, "func3(t0.col1,42)");

  }

  public void testCustom()
  {
    String customStr1 = new SelectQuery()
      .addColumns(_defTable1_col_id)
      .addFromTable(_defTable1)
      .addCustomFromTable(customSql("otherTable"))
      .addCustomColumns(customSql("fooCol"), customSql("BazzCol"))
      .addCondition(and(
                      lessThan(customSql("fooCol"),
                                          new ValueObject(37)),
                      new CustomCondition("bazzCol IS FUNKY")))
      .addCondition(new NotCondition(emptyCond()))
      .validate().toString();
    checkResult(customStr1,
                "SELECT t1.col_id,fooCol,BazzCol FROM Table1 t1, otherTable WHERE ((fooCol < '37') AND (bazzCol IS FUNKY))");
  }

  public void testCaseStatement() {
    String caseClause1 = caseStmt(_table1_col1)
      .addNumericWhen(1, "one")
      .addNumericWhen(2, "two")
      .addElse("three").validate().toString();

    checkResult(caseClause1,
                "(CASE t0.col1 WHEN 1 THEN 'one' WHEN 2 THEN 'two' ELSE 'three' END)");

    String caseClause2 = caseStmt()
      .addWhen(equalTo(_table1_col2, "13"), _table1_col3)
      .addWhen(equalTo(_table1_col2, "14"), "14")
      .addElseNull().validate().toString();

    checkResult(caseClause2,
                "(CASE WHEN (t0.col2 = '13') THEN t0.col3 WHEN (t0.col2 = '14') THEN '14' ELSE NULL END)");

    String caseClause3 = caseStmt(_table1_col2).validate()
      .toString();
    checkResult(caseClause3, "");

    try {
      new SimpleCaseStatement(_table1_col1)
        .addNumericWhen(1, "one")
        .addNumericWhen(2, "two")
        .addElse("three")
        .addElseNull().validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}


    SimpleCaseStatement invalidCase = new SimpleCaseStatement(_table1_col1)
      .addNumericWhen(1, "one")
      .addElse("three")
      .addNumericWhen(2, "two");

    try {
      invalidCase.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new SelectQuery()
        .addCustomColumns(invalidCase)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

  }

  public void testDelete()
  {
    String deleteQuery1 = new DeleteQuery(_table1)
      .addCondition(equalTo(_table1_col2, "13"))
      .validate().toString();

    checkResult(deleteQuery1,
                "DELETE FROM Schema1.Table1 WHERE (col2 = '13')");

  }

  public void testUpdate()
  {
    String updateQuery1 = new UpdateQuery(_table1)
      .addSetClause(_table1_col1, 47)
      .addSetClause(_table1_col3, "foo")
      .addCondition(equalTo(_table1_col2, "13"))
      .validate().toString();

    checkResult(updateQuery1,
                "UPDATE Schema1.Table1 SET col1 = 47,col3 = 'foo' WHERE (col2 = '13')");

  }

  public void testUnion()
  {
    SelectQuery q1 = new SelectQuery()
      .addColumns(_table1_col1, _table1_col2, _table1_col3);
    SelectQuery q2 = new SelectQuery()
      .addColumns(_defTable2_col_id, _defTable2_col4, _defTable2_col5);

    UnionQuery unionQuery = UnionQuery.unionAll(q1, q2);

    String unionQuery1 = unionQuery.validate().toString();
    checkResult(unionQuery1,
                "SELECT t0.col1,t0.col2,t0.col3 FROM Schema1.Table1 t0 UNION ALL SELECT t2.col_id,t2.col4,t2.col5 FROM Table2 t2");

    q1.addColumns(_defTable1_col3);
    try {
      unionQuery.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    q1 = new SelectQuery()
      .addColumns(_table1_col1, _table1_col2, _table1_col3);
    q2 = new SelectQuery()
      .addColumns(_defTable2_col_id, _defTable2_col4, _defTable2_col5);

    unionQuery = UnionQuery.unionAll(q1, q2)
      .addIndexedOrderings(1)
      .addOrderings(_table1_col1);

    String unionQuery2 = unionQuery.validate().toString();
    checkResult(unionQuery2,
                "SELECT t0.col1,t0.col2,t0.col3 FROM Schema1.Table1 t0 UNION ALL SELECT t2.col_id,t2.col4,t2.col5 FROM Table2 t2 ORDER BY 1,col1");

    q1.addOrderings(_table1_col2);
    try {
      unionQuery.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    q1 = new SelectQuery()
      .addAllTableColumns(_table1);
    q2 = new SelectQuery()
      .addFromTable(_defTable2)
      .addAllColumns();

    unionQuery = UnionQuery.union(q1, q2)
      .addIndexedOrdering(1, OrderObject.Dir.DESCENDING)
      .addCustomOrderings(
          new OrderObject(OrderObject.Dir.ASCENDING, _table1_col1)
          .setNullOrder(OrderObject.NullOrder.FIRST));

    String unionQuery3 = unionQuery.validate().toString();
    checkResult(unionQuery3,
                "SELECT t0.* FROM Schema1.Table1 t0 UNION SELECT * FROM Table2 t2 ORDER BY 1 DESC,col1 ASC NULLS FIRST");
  }

  public void testSetOperationQueries()
  {
    SelectQuery q1 = new SelectQuery()
      .addAllTableColumns(_table1);
    SelectQuery q2 = new SelectQuery()
      .addFromTable(_defTable2)
      .addAllColumns();
    SelectQuery q3 = new SelectQuery()
      .addFromTable(_defTable1)
      .addAllColumns();

    SetOperationQuery<?> setOpQuery = SetOperationQuery.except(q1, q2)
      .addQueries(SetOperationQuery.Type.INTERSECT_ALL, q3);

    String setOpQueryStr = setOpQuery.validate().toString();
    checkResult(setOpQueryStr,
                "SELECT t0.* FROM Schema1.Table1 t0 EXCEPT SELECT * FROM Table2 t2 INTERSECT ALL SELECT * FROM Table1 t1");

  }

  public void testSqlContext()
  {
    SqlContext context = new SqlContext();
    Condition cond = and(
        equalTo(_defTable1_col3, "foo"),
        lessThan(_table1_col1, 13, true));

    String condStr1 = cond.toString(32, context);
    checkResult(condStr1,
                "((t1.col3 = 'foo') AND (t0.col1 <= 13))");

    context.setUseTableAliases(false);
    String condStr2 = cond.toString(32, context);
    checkResult(condStr2,
                "((col3 = 'foo') AND (col1 <= 13))");

  }

  public void testRejoinTable()
  {
    RejoinTable rejoinTable1 = _table1.rejoin("t5");
    assertSame(_table1, rejoinTable1.getOriginalTable());
    assertSame(_table1_col1,
               rejoinTable1.getColumns().get(0).getOriginalColumn());
    assertSame(_table1.getConstraints(), rejoinTable1.getConstraints());
    assertNull(rejoinTable1.findColumnByName("bogus"));

    RejoinTable.RejoinColumn rejoinCol2 = rejoinTable1.findColumnByName("col2");
    assertSame(_table1_col2, rejoinCol2.getOriginalColumn());
    assertSame(rejoinCol2, rejoinTable1.findColumn(_table1_col2));
    assertSame(_table1_col2.getTypeNameSQL(), rejoinCol2.getTypeNameSQL());
    assertSame(_table1_col2.getTypeLength(), rejoinCol2.getTypeLength());
    assertSame(_table1_col2.getConstraints(), rejoinCol2.getConstraints());

    String rejoinQuery = (new SelectQuery())
      .addFromTable(_table1)
      .addColumns(_table1_col1, _table1_col2)
      .addFromTable(rejoinTable1)
      .addColumns(rejoinTable1.getColumns().get(0),
                  rejoinTable1.findColumn(_table1_col2))
      .validate().toString();

    checkResult(rejoinQuery,
                "SELECT t0.col1,t0.col2,t5.col1,t5.col2 FROM Schema1.Table1 t0, Schema1.Table1 t5");
  }

  public void testJdbcEscape()
  {
    String escapeStr1 = new InsertQuery(_table1)
      .addColumns(new DbColumn[]{_table1_col1, _table1_col3},
                  new Object[]{13, JdbcScalarFunction.NOW})
      .validate().toString();
    checkResult(escapeStr1,
                "INSERT INTO Schema1.Table1 (col1,col3) VALUES (13,{fn NOW()})");

    Date d = new Date(1204909500692L);
    String dateStr = JdbcEscape.date(d).toString();
    String date = new SimpleDateFormat("yyyy-MM-dd").format(d);
    checkResult(dateStr, "{d '"+date+"'}");
    String timeStr = JdbcEscape.time(d).toString();
    String time = new SimpleDateFormat("HH:mm:ss").format(d);
    checkResult(timeStr, "{t '"+time+"'}");
    String timestampStr = JdbcEscape.timestamp(d).toString();
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(d);
    checkResult(timestampStr, "{ts '"+timestamp+"'}");
  }

  public void testGrantRevoke()
  {
    String grantStr1 = new GrantQuery()
      .setTarget(GrantQuery.targetTable(_table1))
      .addPrivileges(GrantQuery.privilegeInsert(_table1_col1),
                     GrantQuery.privilegeUsage())
      .addGrantees("bob", "Mark")
      .validate().toString();
    checkResult(grantStr1, "GRANT INSERT(col1),USAGE ON TABLE Schema1.Table1 TO bob,Mark");

    String revokeStr1 = new RevokeQuery()
      .setTarget(GrantQuery.targetTable(_table1))
      .addPrivileges(GrantQuery.privilegeInsert(_table1_col1),
                     GrantQuery.privilegeUsage())
      .addCustomGrantees(RevokeQuery.PUBLIC_GRANTEE)
      .validate().toString();
    checkResult(revokeStr1, "REVOKE INSERT(col1),USAGE ON TABLE Schema1.Table1 FROM PUBLIC");

    try {
      new GrantQuery()
        .setTarget(GrantQuery.targetTable(_table1))
        .addPrivileges(GrantQuery.privilegeInsert(_defTable1_col3))
        .addGrantees("bob")
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}
  }

  public void testSubquery()
  {
    String queryStr1 =
      new SelectQuery()
      .addColumns(_table1_col1, _table1_col2)
      .addCondition(new InCondition(
                        _table1_col1, new Subquery(
                            new SelectQuery()
                            .addColumns(_defTable1_col3)
                            .validate())))
      .validate().toString();
    checkResult(queryStr1, "SELECT t0.col1,t0.col2 FROM Schema1.Table1 t0 WHERE (t0.col1 IN (SELECT t1.col3 FROM Table1 t1) )");

    SelectQuery innerSelect = new SelectQuery()
      .addCustomColumns(_defTable2_col4)
      .addCondition(equalTo(_table1_col1, _defTable2_col_id));
    innerSelect.validate();
    String queryStr2 = innerSelect.toString();
    checkResult(queryStr2, "SELECT t2.col4 FROM Table2 t2,Schema1.Table1 t0 WHERE (t0.col1 = t2.col_id)");
    SelectQuery outerSelect = new SelectQuery()
      .addCustomColumns(_table1_col1, _table1_col2)
      .addJoin(SelectQuery.JoinType.INNER, _table1, _defTable1,
               equalTo(_table1_col1, _defTable1_col_id))
      .addCondition(new InCondition(_table1_col1, new Subquery(innerSelect)));
    outerSelect.validate();
    String queryStr3 = outerSelect.toString();
    checkResult(queryStr3, "SELECT t0.col1,t0.col2 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col1 = t1.col_id) WHERE (t0.col1 IN (SELECT t2.col4 FROM Table2 t2 WHERE (t0.col1 = t2.col_id)) )");

    innerSelect.addCustomColumns()
      .addJoin(SelectQuery.JoinType.INNER, _table1, _defTable1, _table1_col1,
               _defTable1_col_id);
    try {
      innerSelect.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      outerSelect.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    innerSelect = new SelectQuery()
      .addCustomColumns(_defTable2_col4)
      .addJoin(SelectQuery.JoinType.INNER, _table1, _defTable2, _table1_col1,
               _defTable2_col_id)
      .addCondition(equalTo(_table1_col1, _defTable1_col_id));
    String queryStr4 = innerSelect.toString();
    checkResult(queryStr4, "SELECT t2.col4 FROM Schema1.Table1 t0 INNER JOIN Table2 t2 ON (t0.col1 = t2.col_id) WHERE (t0.col1 = t1.col_id)");

    try {
      innerSelect.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    outerSelect = new SelectQuery()
      .addCustomColumns(_table1_col1, _table1_col2)
      .addJoin(SelectQuery.JoinType.INNER, _table1, _defTable1, _table1_col1,
               _defTable1_col_id)
      .addCondition(new InCondition(_table1_col1,
                                    subquery(innerSelect)));
    outerSelect.validate();
    String queryStr5 = outerSelect.toString();
    checkResult(queryStr5, "SELECT t0.col1,t0.col2 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col1 = t1.col_id) WHERE (t0.col1 IN (SELECT t2.col4 FROM Schema1.Table1 t0 INNER JOIN Table2 t2 ON (t0.col1 = t2.col_id) WHERE (t0.col1 = t1.col_id)) )");
  }

  public void testAlterTable()
  {
    @SuppressWarnings("deprecation")
    String queryStr1 =
      new AlterTableQuery(_table1)
      .setAction(new AlterTableQuery.AddUniqueConstraintAction()
                 .addColumns(_table1_col2))
      .validate().toString();
    checkResult(queryStr1, "ALTER TABLE Schema1.Table1 ADD UNIQUE (col2)");

    @SuppressWarnings("deprecation")
    String queryStr2 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddPrimaryConstraintAction()
                 .addColumns(_defTable1_col_id))
      .validate().toString();
    checkResult(queryStr2, "ALTER TABLE Table1 ADD PRIMARY KEY (col_id)");

    @SuppressWarnings("deprecation")
    String queryStr3 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddForeignConstraintAction(_defTable2)
                 .addPrimaryKeyReference(_defTable1_col_id))
      .validate().toString();
    checkResult(queryStr3,
                "ALTER TABLE Table1 ADD FOREIGN KEY (col_id) REFERENCES Table2");

    @SuppressWarnings("deprecation")
    String queryStr4 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddForeignConstraintAction(_defTable2)
                 .addReference(_defTable1_col_id, _defTable2_col4)
                 .addReference(_defTable1_col2, _defTable2_col5))
      .validate().toString();
    checkResult(queryStr4,
                "ALTER TABLE Table1 ADD FOREIGN KEY (col_id,col2) " +
                "REFERENCES Table2 (col4,col5)");

    String queryStr5 =
      new AlterTableQuery(_defTable2)
      .setAddConstraint(_defTable2.getConstraints().get(0))
      .validate().toString();
    checkResult(queryStr5,
                "ALTER TABLE Table2 ADD CONSTRAINT t2_fk FOREIGN KEY (col4,col5) REFERENCES Table1 (col2,col3)");

    DbColumn toAdd = _defTable1.addColumn("col5", Types.VARCHAR, 255);
    toAdd.notNull();
    String queryStr6 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddColumnAction(toAdd)
                 .addConstraint(new ConstraintClause(ConstraintClause.Type.UNIQUE, null)))
      .validate().toString();
    checkResult(queryStr6,
                "ALTER TABLE Table1 ADD col5 VARCHAR(255) NOT NULL UNIQUE");

    toAdd.setDefaultValue("someValue");
    String queryStr7 =
      new AlterTableQuery(_defTable1)
      .setAddColumn(toAdd)
      .validate().toString();
    checkResult(queryStr7,
                "ALTER TABLE Table1 ADD col5 VARCHAR(255) DEFAULT 'someValue' NOT NULL");

    String queryStr8 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddColumnAction(toAdd)
                 .setTypeName("NVARCHAR"))
      .validate().toString();
    checkResult(queryStr8,
                "ALTER TABLE Table1 ADD col5 NVARCHAR(255) DEFAULT 'someValue' NOT NULL");

  }

  public void testComment()
  {
    String queryStr1 = new SelectQuery()
      .addCustomColumns(_table1_col1, _table1_col2, new Comment("foo bar"))
                        .validate().toString();
    checkResult(queryStr1, "SELECT t0.col1,t0.col2, -- foo bar\n FROM Schema1.Table1 t0");

    String queryStr2 = new SelectQuery()
      .addCustomColumns(_table1_col1, _table1_col2)
                        .validate().toString() +
      new Comment("My coolest query ever");
    checkResult(queryStr2, "SELECT t0.col1,t0.col2 FROM Schema1.Table1 t0 -- My coolest query ever\n");

  }

  public void testEscapeLiteral()
  {
    String orig = "/this%is_a ' literal/pattern";

    assertEquals("//this/%is/_a ' literal//pattern",
                 BinaryCondition.escapeLikeLiteral(orig, '/'));
    assertEquals("/this\\%is\\_a ' literal/pattern",
                 BinaryCondition.escapeLikeLiteral(orig, '\\'));
  }

  public void testValidationException() {
    SelectQuery select = new SelectQuery()
      .addCustomColumns(_defTable1_col_id)
      .addFromTable(_table1);

    ValidationException ve = null;
    try {
      select.validate();
    } catch(ValidationException tmp) {
      ve = tmp;
    }

    assertNotNull(ve);
    assertNotNull(ve.getFailedVerifiable());
    assertSame(select, ve.getFailedVerifiable().getValue());

    String msg = ve.getMessage();
    checkResult(msg, "Columns used for unreferenced tables [Table1(t1)] [Failed clause: SELECT t1.col_id FROM Schema1.Table1 t0]");


    select.addCustomColumns(new SqlObject() {
        @Override
        public void appendTo(AppendableExt app) throws IOException {
          throw new NullPointerException("BOO");
        }
        @Override
        protected void collectSchemaObjects(ValidationContext vContext) {}
      });
    try {
      select.validate();
    } catch(ValidationException tmp) {
      ve = tmp;
    }

    assertNotNull(ve);

    msg = ve.getMessage();
    assertTrue(msg.matches("Columns used for unreferenced tables \\[Table1\\(t1\\)\\] \\[Verifiable: com.healthmarketscience.sqlbuilder.SelectQuery@[0-9a-f]+\\]"));

  }

  public void testCreateView()
  {
    SelectQuery query1 = new SelectQuery()
      .addColumns(_defTable1_col_id, _defTable1_col2)
      .addCondition(isNotNull(_defTable1_col_id));
    SelectQuery query2 = new SelectQuery().addAllTableColumns(_defTable2);

    String createStr1 = new CreateViewQuery(_table1)
      .setSelectQuery(query1)
      .validate().toString();
    checkResult(createStr1,
                "CREATE VIEW Schema1.Table1 AS SELECT t1.col_id,t1.col2 FROM Table1 t1 WHERE (t1.col_id IS NOT NULL)");


    String createStr2 = new CreateViewQuery(_table1)
      .addColumns(_table1_col1, _table1_col3)
      .setSelectQuery(query1)
      .setWithCheckOption(true)
      .validate().toString();
    checkResult(createStr2,
                "CREATE VIEW Schema1.Table1 (col1,col3) AS SELECT t1.col_id,t1.col2 FROM Table1 t1 WHERE (t1.col_id IS NOT NULL) WITH CHECK OPTION");

    String createStr3 = new CreateViewQuery(_table1)
      .addCustomColumns(_table1_col1, _table1_col3)
      .setSelectQuery(query2)
                    .validate().toString();
    checkResult(createStr3,
                "CREATE VIEW Schema1.Table1 (col1,col3) AS SELECT t2.* FROM Table2 t2");

    CreateViewQuery viewQuery = new CreateViewQuery(_table1)
      .setSelectQuery(query2);

    String createStr4 = viewQuery.validate().toString();
    checkResult(createStr4,
                "CREATE VIEW Schema1.Table1 AS SELECT t2.* FROM Table2 t2");

    String dropStr1 = viewQuery.getDropQuery().validate().toString();
    checkResult(dropStr1,
                "DROP VIEW Schema1.Table1");

    try {
      new CreateViewQuery(_table1).validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new CreateViewQuery(_table1)
        .addColumns(_table1_col1)
        .setSelectQuery(query1)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      new CreateViewQuery(_table1)
        .addCustomColumns(SqlObject.ALL_SYMBOL)
        .setSelectQuery(query2)
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

  }

  public void testCrossSchemaFK() throws Exception
  {
    String createStr = new CreateTableQuery(_defTable3, true)
      .validate().toString();

    checkResult(createStr, "CREATE TABLE DefTable3 (col_id NUMBER CONSTRAINT t2_id_fk REFERENCES Schema1.Table1 (col2))");
  }

  public void testCustomPrefix() throws Exception
  {
    DbTable table = new DbSpec("pre_").addDefaultSchema().addTable("NewTable");
    table.addColumn("col1");
    table.addColumn("col2");

    String sqlStr = new SelectQuery().addAllTableColumns(table)
      .validate().toString();
    checkResult(sqlStr, "SELECT pre_0.* FROM NewTable pre_0");
  }

  public void testBetweenCondition()
  {
    String conditionStr = and(
      lessThan(_table1_col1, "FOO", false),
      or(),
      between(_defTable2_col4,
              "this string",
              new NumberValueObject(37)))
      .toString();
    checkResult(conditionStr,
                "((t0.col1 < 'FOO') AND (t2.col4 BETWEEN 'this string' AND 37))");
  }

  public void testExtractExpression()
  {
    String exprStr = equalTo(
        "2016",
        new ExtractExpression(ExtractExpression.DatePart.YEAR, "2016-01-01"))
      .toString();
    checkResult(exprStr, "('2016' = EXTRACT(YEAR FROM '2016-01-01'))");
  }

  public void testWindowFunctions()
  {
    String exprStr = FunctionCall.avg()
      .setWindow(new WindowDefinitionClause())
      .toString();

    checkResult(exprStr, "AVG() OVER ()");

    exprStr = FunctionCall.avg()
      .setWindow(new WindowDefinitionClause()
                 .addPartitionColumns(_defTable1_col_id))
      .toString();

    checkResult(exprStr, "AVG() OVER (PARTITION BY t1.col_id)");

    exprStr = FunctionCall.avg()
      .setWindow(new WindowDefinitionClause()
                 .addPartitionColumns(_defTable1_col_id)
                 .addOrderings(_defTable1_col2))
      .toString();

    checkResult(exprStr, "AVG() OVER (PARTITION BY t1.col_id ORDER BY t1.col2)");

    exprStr = FunctionCall.avg()
      .setWindow(new WindowDefinitionClause()
                 .addPartitionColumns(_defTable1_col_id)
                 .addOrderings(_defTable1_col2)
                 .setFrame(
                     WindowDefinitionClause.FrameUnits.ROWS,
                     WindowDefinitionClause.FrameBound.CURRENT_ROW))
      .toString();

    checkResult(exprStr, "AVG() OVER (PARTITION BY t1.col_id ORDER BY t1.col2 ROWS CURRENT ROW)");


    exprStr = FunctionCall.avg()
      .setWindow(new WindowDefinitionClause()
                 .addPartitionColumns(_defTable1_col_id)
                 .addOrderings(_defTable1_col2)
                 .setFrameBetween(
                     WindowDefinitionClause.FrameUnits.ROWS,
                     WindowDefinitionClause.FrameBound.UNBOUNDED_PRECEDING,
                     WindowDefinitionClause.FrameBound.boundedFollowing(5)))
      .toString();

    checkResult(exprStr, "AVG() OVER (PARTITION BY t1.col_id ORDER BY t1.col2 ROWS BETWEEN UNBOUNDED PRECEDING AND 5 FOLLOWING)");

    String queryStr = new SelectQuery()
      .addColumns(_table1_col1, _table1_col2)
      .addAliasedColumn(FunctionCall.avg().setWindowByName("w"), "average")
      .addWindowDefinition(
          "w", new WindowDefinitionClause()
          .addPartitionColumns(_defTable1_col_id)
          .addOrderings(_defTable1_col2)
          .setFrameBetween(
              WindowDefinitionClause.FrameUnits.ROWS,
              WindowDefinitionClause.FrameBound.UNBOUNDED_PRECEDING,
              WindowDefinitionClause.FrameBound.boundedFollowing(5),
              WindowDefinitionClause.FrameExclusion.CURRENT_ROW))
      .validate()
      .toString();

    checkResult(queryStr, "SELECT t0.col1,t0.col2,AVG() OVER w AS average FROM Schema1.Table1 t0,Table1 t1 WINDOW w AS (PARTITION BY t1.col_id ORDER BY t1.col2 ROWS BETWEEN UNBOUNDED PRECEDING AND 5 FOLLOWING EXCLUDE CURRENT ROW)");
  }
}
