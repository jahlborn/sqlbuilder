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

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbFunction;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbFunctionPackage;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbIndex;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;


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
                "CREATE TABLE Schema1.Table1 (col1 VARCHAR(213),col3 TIMESTAMP)");
    
    String createStr2 = new CreateTableQuery(_table1, true)
      .validate().toString();
    checkResult(createStr2,
                "CREATE TABLE Schema1.Table1 (col1 VARCHAR(213),col2 NUMBER(7),col3 TIMESTAMP)");

    String createStr3 = new CreateTableQuery(_defTable1, true)
      .validate().toString();
    checkResult(createStr3,
                "CREATE TABLE Table1 (col_id NUMBER,col2 VARCHAR(64),col3 DATE)");

    String createStr4 = new CreateTableQuery(_defTable1, true)
      .setColumnConstraint(_defTable1_col_id,
                           CreateTableQuery.ColumnConstraint.PRIMARY_KEY)
      .setColumnConstraint(_defTable1_col3,
                           CreateTableQuery.ColumnConstraint.NOT_NULL)
      .validate().toString();
    checkResult(createStr4,
                "CREATE TABLE Table1 (col_id NUMBER PRIMARY KEY,col2 VARCHAR(64),col3 DATE NOT NULL)");

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
    // add some joins to use in our selects
    DbJoin idJoin = _spec.addJoin(null, "Table1",
                                  null, "Table2",
                                  "col_id");

    _table1.addColumn("col4");
    _defTable1.addColumn("altCol4");
    
    DbJoin col4Join = _spec.addJoin("Schema1", "Table1",
                                    null, "Table1",
                                    new String[]{"col4"},
                                    new String[]{"altCol4"});
      
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
                                                col4Join)
        .addJoins(SelectQuery.JoinType.LEFT_OUTER, idJoin)
        .validate().toString();
      checkResult(selectStr3,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id)");

      String selectStr4 = selectQuery1.addOrderings(_defTable1_col2)
        .validate().toString();
      checkResult(selectStr4,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) ORDER BY t1.col2");

      String selectStr5 = selectQuery1.addCondition(
        BinaryCondition.greaterThan(_defTable2_col4, 42, true))
        .validate().toString();
      checkResult(selectStr5,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= 42) ORDER BY t1.col2");

      String selectStr6 = selectQuery1.addOrdering(_defTable2_col5,
                                                   OrderObject.Dir.DESCENDING)
        .validate().toString();
      checkResult(selectStr6,
                  "SELECT DISTINCT t0.col1,t1.col2,t2.col5 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col4 = t1.altCol4) LEFT OUTER JOIN Table2 t2 ON (t1.col_id = t2.col_id) WHERE (t2.col4 >= 42) ORDER BY t1.col2,t2.col5 DESC");

      String selectStr7 = selectQuery1.addHaving(BinaryCondition.greaterThan(_defTable1_col2, new NumberValueObject(1), false)).toString();
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
        .addCustomColumns(new CustomSql("col1"), new CustomSql("col2"))
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
      
  }

  public void testCondition()
  {
    String reallyComplicatedConditionStr = ComboCondition.and(
      BinaryCondition.lessThan(_table1_col1, "FOO", false),
      ComboCondition.or(),
      UnaryCondition.isNotNull(_defTable1_col_id),
      new ComboCondition(ComboCondition.Op.OR,
                         new CustomCondition("IM REALLY SNAZZY"),
                         new NotCondition(
                           BinaryCondition.like(_defTable2_col5,
                                                "BUZ%")),
                         new BinaryCondition(BinaryCondition.Op.EQUAL_TO,
                                             new CustomSql("YOU"),
                                             "ME")),
      ComboCondition.or(
        new UnaryCondition(UnaryCondition.Op.IS_NULL,
                           _table1_col2)),
      new InCondition(_defTable2_col4,
                      "this string",
                      new NumberValueObject(37))
      .addObject(new NumberValueObject(42)),
      BinaryCondition.notLike(_table1_col2, "\\_%").setLikeEscapeChar('\\'))
      .toString();
    checkResult(reallyComplicatedConditionStr,
                "((t0.col1 < 'FOO') AND (t1.col_id IS NOT NULL) AND ((IM REALLY SNAZZY) OR (NOT (t2.col5 LIKE 'BUZ%')) OR (YOU = 'ME')) AND (t0.col2 IS NULL) AND (t2.col4 IN ('this string',37,42) ) AND (t0.col2 NOT LIKE '\\_%' ESCAPE '\\'))");

    try {
      BinaryCondition.equalTo(_table1_col2, "\\37").setLikeEscapeChar('\\');
      fail("IllegalArgumentException should have been thrown");
    } catch(IllegalArgumentException e) {}
  }

  public void testExpression()
  {
    Expression expr = ComboExpression.add(
        37, _defTable2_col5,
        new NegateExpression(
            ComboExpression.multiply(_table1_col1, 4.7f)),
        ComboExpression.subtract(),
        new NegateExpression((Object)Expression.EMPTY),
        "PI", new CustomSql("8 - 3"));
    String reallyComplicatedExpression = expr.toString();
    checkResult(reallyComplicatedExpression,
                "(37 + t2.col5 + (- (t0.col1 * 4.7)) + 'PI' + (8 - 3))");

    String exprQuery = new SelectQuery()
      .addCustomColumns(expr, _table1_col2)
      .validate().toString();
    checkResult(exprQuery, "SELECT (37 + t2.col5 + (- (t0.col1 * 4.7)) + 'PI' + (8 - 3)),t0.col2 FROM Table2 t2,Schema1.Table1 t0");
    
    String concatExpression = ComboExpression.concatenate(
        "The answer is ", ComboExpression.add(40, 2), ".")
      .toString();
    checkResult(concatExpression,
                "('The answer is ' || (40 + 2) || '.')");
  }

  public void testFunction()
  {
    // add some functions to play with
    DbFunctionPackage funcPack1 = _schema1.addFunctionPackage("fpkg");
    DbFunction func1 = funcPack1.addFunction("func1");
    DbFunctionPackage funcPack2 = _schema1.addFunctionPackage(null);
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
      .addCustomFromTable(new CustomSql("otherTable"))
      .addCustomColumns(new CustomSql("fooCol"), new CustomSql("BazzCol"))
      .addCondition(ComboCondition.and(
                      new BinaryCondition(BinaryCondition.Op.LESS_THAN,
                                          new CustomSql("fooCol"),
                                          new ValueObject(37)),
                      new CustomCondition("bazzCol IS FUNKY")))
      .addCondition(new NotCondition((Object)Condition.EMPTY))
      .validate().toString();
    checkResult(customStr1,
                "SELECT t1.col_id,fooCol,BazzCol FROM Table1 t1, otherTable WHERE ((fooCol < '37') AND (bazzCol IS FUNKY))");
  }

  public void testCaseStatement() {
    String caseClause1 = new SimpleCaseStatement(_table1_col1)
      .addNumericWhen(1, "one")
      .addNumericWhen(2, "two")
      .addElse("three").validate().toString();

    checkResult(caseClause1,
                "(CASE t0.col1 WHEN 1 THEN 'one' WHEN 2 THEN 'two' ELSE 'three' END)");

    String caseClause2 = new CaseStatement()
      .addWhen(BinaryCondition.equalTo(_table1_col2, "13"), _table1_col3)
      .addWhen(BinaryCondition.equalTo(_table1_col2, "14"), "14")
      .addElseNull().validate().toString();
    
    checkResult(caseClause2,
                "(CASE WHEN (t0.col2 = '13') THEN t0.col3 WHEN (t0.col2 = '14') THEN '14' ELSE NULL END)");

    String caseClause3 = new SimpleCaseStatement(_table1_col2).validate()
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
      .addCondition(BinaryCondition.equalTo(_table1_col2, "13"))
      .validate().toString();

    checkResult(deleteQuery1,
                "DELETE FROM Schema1.Table1 WHERE (col2 = '13')");
    
  }

  public void testUpdate()
  {
    String updateQuery1 = new UpdateQuery(_table1)
      .addSetClause(_table1_col1, 47)
      .addSetClause(_table1_col3, "foo")
      .addCondition(BinaryCondition.equalTo(_table1_col2, "13"))
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
      .addCustomOrdering(_table1_col1, OrderObject.Dir.ASCENDING);
    
    String unionQuery3 = unionQuery.validate().toString();
    checkResult(unionQuery3,
                "SELECT t0.* FROM Schema1.Table1 t0 UNION SELECT * FROM Table2 t2 ORDER BY 1 DESC,col1 ASC");
  }

  public void testSqlContext()
  {
    SqlContext context = new SqlContext();
    Condition cond = ComboCondition.and(
        BinaryCondition.equalTo(_defTable1_col3, "foo"),
        BinaryCondition.lessThan(_table1_col1, 13, true));

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
    RejoinTable rejoinTable1 = new RejoinTable(_table1, "t5");
    assertSame(_table1, rejoinTable1.getOriginalTable());
    assertSame(_table1_col1,
               rejoinTable1.getColumns().get(0).getOriginalColumn());
    String rejoinQuery = (new SelectQuery())
      .addFromTable(_table1)
      .addColumns(_table1_col1, _table1_col2)
      .addFromTable(rejoinTable1)
      .addColumns(rejoinTable1.getColumns().get(0),
                  rejoinTable1.getColumns().get(1))
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
    checkResult(dateStr, "{d '2008-03-07'}");
    String timeStr = JdbcEscape.time(d).toString();
    checkResult(timeStr, "{t '12:05:00'}");
    String timestampStr = JdbcEscape.timestamp(d).toString();
    checkResult(timestampStr, "{ts '2008-03-07 12:05:00.692'}");
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
    checkResult(queryStr1, "SELECT t0.col1,t0.col2 FROM Schema1.Table1 t0 WHERE (t0.col1 IN ((SELECT t1.col3 FROM Table1 t1)) )");

    SelectQuery innerSelect = new SelectQuery()
      .addCustomColumns(_defTable2_col4)
      .addCondition(BinaryCondition.equalTo(_table1_col1, _defTable2_col_id));
    innerSelect.validate();
    String queryStr2 = innerSelect.toString();
    checkResult(queryStr2, "SELECT t2.col4 FROM Table2 t2,Schema1.Table1 t0 WHERE (t0.col1 = t2.col_id)");    
    SelectQuery outerSelect = new SelectQuery()
      .addCustomColumns(_table1_col1, _table1_col2)
      .addJoin(SelectQuery.JoinType.INNER, _table1, _defTable1,
               BinaryCondition.equalTo(_table1_col1, _defTable1_col_id))
      .addCondition(new InCondition(_table1_col1, new Subquery(innerSelect)));
    outerSelect.validate();
    String queryStr3 = outerSelect.toString();
    checkResult(queryStr3, "SELECT t0.col1,t0.col2 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col1 = t1.col_id) WHERE (t0.col1 IN ((SELECT t2.col4 FROM Table2 t2 WHERE (t0.col1 = t2.col_id))) )");

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
      .addCondition(BinaryCondition.equalTo(_table1_col1, _defTable1_col_id));
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
      .addCondition(new InCondition(_table1_col1, new Subquery(innerSelect)));
    outerSelect.validate();
    String queryStr5 = outerSelect.toString();
    checkResult(queryStr5, "SELECT t0.col1,t0.col2 FROM Schema1.Table1 t0 INNER JOIN Table1 t1 ON (t0.col1 = t1.col_id) WHERE (t0.col1 IN ((SELECT t2.col4 FROM Schema1.Table1 t0 INNER JOIN Table2 t2 ON (t0.col1 = t2.col_id) WHERE (t0.col1 = t1.col_id))) )");
  }

  public void testAlterTable()
  {
    String queryStr1 =
      new AlterTableQuery(_table1)
      .setAction(new AlterTableQuery.AddUniqueConstraintAction()
                 .addColumns(_table1_col2))
      .validate().toString();
    checkResult(queryStr1, "ALTER TABLE Schema1.Table1 ADD UNIQUE (col2)");

    String queryStr2 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddPrimaryConstraintAction()
                 .addColumns(_defTable1_col_id))
      .validate().toString();
    checkResult(queryStr2, "ALTER TABLE Table1 ADD PRIMARY KEY (col_id)");

    String queryStr3 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddForeignConstraintAction(_defTable2)
                 .addPrimaryKeyReference(_defTable1_col_id))
      .validate().toString();
    checkResult(queryStr3, 
                "ALTER TABLE Table1 ADD FOREIGN KEY (col_id) REFERENCES Table2");

    String queryStr4 =
      new AlterTableQuery(_defTable1)
      .setAction(new AlterTableQuery.AddForeignConstraintAction(_defTable2)
                 .addReference(_defTable1_col_id, _defTable2_col4)
                 .addReference(_defTable1_col2, _defTable2_col5))
      .validate().toString();
    checkResult(queryStr4, 
                "ALTER TABLE Table1 ADD FOREIGN KEY (col_id,col2) " +
                "REFERENCES Table2 (col4,col5)");

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
    assertSame(select, ve.getFailedVerifiable().get1());

    String msg = ve.getMessage();
    checkResult(msg, "Columns used for unreferenced tables [Failed clause:SELECT t1.col_id FROM Schema1.Table1 t0]");

    
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
    assertTrue(msg.matches("Columns used for unreferenced tables \\[Verifiable: com.healthmarketscience.sqlbuilder.SelectQuery@[0-9a-f]+\\]"));

  }

  public void testCreateView()
  {
    SelectQuery query1 = new SelectQuery()
      .addColumns(_defTable1_col_id, _defTable1_col2)
      .addCondition(UnaryCondition.isNotNull(_defTable1_col_id));
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
  
}
