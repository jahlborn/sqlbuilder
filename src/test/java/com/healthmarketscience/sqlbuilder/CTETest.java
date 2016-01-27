/*
Copyright (c) 2016 James Ahlborn

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

import com.healthmarketscience.sqlbuilder.dbspec.Column;

/**
 *
 * @author James Ahlborn
 */
public class CTETest extends BaseSqlTestCase
{
  public CTETest(String name) {
    super(name);
  }

  public void testBasicCTE() throws Exception
  {
    CommonTableExpression cte = new CommonTableExpression("cte_expr");
    Column cteCol = cte.addColumn("col1");
    cte.setQuery(new SelectQuery().addColumns(_defTable2_col_id));

    SelectQuery selectQuery1 = new SelectQuery()
      .addCommonTableExpression(cte)
      .addColumns(_table1_col1, _defTable1_col2, _defTable2_col5, cteCol);
    
    String selectStr1 = selectQuery1.validate().toString();
    assertEquals("WITH cte_expr (col1) AS (SELECT t2.col_id FROM Table2 t2) SELECT t0.col1,t1.col2,t2.col5,cte0.col1 FROM Schema1.Table1 t0,Table1 t1,Table2 t2,cte_expr cte0", selectStr1);

    cte = new CommonTableExpression("sales")
      .setQuery(new SelectQuery().addAliasedColumn(_defTable2_col_id, "id"));

    SelectQuery selectQuery2 = new SelectQuery()
      .addCommonTableExpression(cte)
      .addCustomColumns(new CustomSql("id"))
      .addCustomFromTable("sales");

    String selectStr2 = selectQuery2.validate().toString();
    assertEquals("WITH sales AS (SELECT t2.col_id AS id FROM Table2 t2) SELECT id FROM sales", selectStr2);

    CommonTableExpression cte1 = new CommonTableExpression("cte_expr");
    Column cteCol1 = cte1.addColumn("col1");
    cte1.setQuery(new SelectQuery().addColumns(_defTable2_col_id));

    CommonTableExpression cte2 = new CommonTableExpression("cte_expr2");
    cte2.addColumn("col2");
    cte2.setQuery(new SelectQuery().addColumns(_table1_col1)
                  .addCondition(BinaryCondition.equalTo(_table1_col2, cteCol1)));

    DeleteQuery deleteQuery1 = new DeleteQuery(cte2.getTable())
      .addCommonTableExpression(cte1)
      .addCommonTableExpression(cte2)
      .addCondition(BinaryCondition.greaterThan(cte2.findColumn("col2"), 10, false));

    String deleteStr1 = deleteQuery1.validate().toString();
    assertEquals("WITH cte_expr (col1) AS (SELECT t2.col_id FROM Table2 t2),cte_expr2 (col2) AS (SELECT t0.col1 FROM Schema1.Table1 t0,cte_expr cte0 WHERE (t0.col2 = cte0.col1)) DELETE FROM cte_expr2 WHERE (col2 > 10)", deleteStr1);
  }

  public void testRecursiveCTE() throws Exception
  {
    CommonTableExpression cte = new CommonTableExpression("temp");
    Column nCol = cte.addColumn("n");
    Column factCol = cte.addColumn("fact");

    SelectQuery selectQuery1 =
      new SelectQuery().addAllTableColumns(cte.getTable())
      .setRecursive(true)
      .addCommonTableExpression(
          cte.setQuery(
              SetOperationQuery.unionAll(
                  new SelectQuery().addCustomColumns(0, 1),
                  new SelectQuery().addCustomColumns(
                      ComboExpression.add(nCol, 1),
                      ComboExpression.multiply(
                          ComboExpression.add(nCol, 1),
                          factCol))
                  .addCondition(BinaryCondition.lessThan(nCol, 9, false)))));

    String selectStr1 = selectQuery1.validate().toString();
    assertEquals("WITH RECURSIVE temp (n,fact) AS (SELECT 0,1 UNION ALL SELECT (cte0.n + 1),((cte0.n + 1) * cte0.fact) FROM temp cte0 WHERE (cte0.n < 9)) SELECT cte0.* FROM temp cte0", selectStr1);
  }

  public void testValidation() throws Exception
  {
    try {
      new CommonTableExpression("test")
        .validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      CommonTableExpression cte = new CommonTableExpression("test")
        .setQuery(new SelectQuery().addCustomColumns(0, 1));
      cte.addColumn("col1");
      cte.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

    try {
      CommonTableExpression cte = new CommonTableExpression("test")
        .setQuery(new SelectQuery().addAllColumns());
      cte.addColumn("col1");
      cte.validate();
      fail("ValidationException should have been thrown");
    } catch(ValidationException e) {}

  }
}
