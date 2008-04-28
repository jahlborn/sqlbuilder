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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;


/**
 * @author james
 */
public class QueryPreparerTest extends BaseSqlTestCase {

  public QueryPreparerTest(String name) {
    super(name);
  }

  public void testPreparer() throws Exception
  {
    doTestPreparer(1);
    doTestPreparer(13);
  }

  private void doTestPreparer(int startIndex) throws Exception
  {
    QueryPreparer prep = new QueryPreparer(startIndex);
    QueryPreparer.PlaceHolder ph1 = prep.getNewPlaceHolder();
    QueryPreparer.PlaceHolder ph2 = prep.getNewPlaceHolder();
    QueryPreparer.PlaceHolder ph3 = prep.getNewPlaceHolder();
    QueryPreparer.PlaceHolder sph1 = prep.addStaticPlaceHolder(42);
    QueryPreparer.PlaceHolder sph2 = prep.addStaticPlaceHolder("place");
    QueryPreparer.PlaceHolder sph3 = prep.addStaticPlaceHolder(13L);
    QueryPreparer.PlaceHolder sph4 = prep.addStaticPlaceHolder(true);
    QueryPreparer.PlaceHolder sph5 = prep.addStaticPlaceHolder((Long)null);
    QueryPreparer.PlaceHolder sph6 = prep.addStaticPlaceHolder(new Object());
    QueryPreparer.PlaceHolder sph7 = prep.addStaticPlaceHolder(
        new Object(), Types.VARCHAR);
    QueryPreparer.MultiPlaceHolder mph1 = prep.getNewMultiPlaceHolder();

    String queryStr = new SelectQuery()
      .addCustomColumns(sph2, sph3, sph4, sph5, sph6, sph7)
      .addCondition(
          ComboCondition.and(
              BinaryCondition.lessThan(_table1_col1, ph1, false),
              BinaryCondition.lessThan(_table1_col2, mph1, true),
              UnaryCondition.isNotNull(_defTable1_col_id),
              new ComboCondition(ComboCondition.Op.OR,
                                 new CustomCondition("IM REALLY SNAZZY"),
                                 new NotCondition(
                                     BinaryCondition.like(_defTable2_col5,
                                                          ph2)),
                                 new BinaryCondition(BinaryCondition.Op.EQUAL_TO,
                                                     new CustomSql("YOU"),
                                                     sph1)),
              ComboCondition.or(
                  new UnaryCondition(UnaryCondition.Op.IS_NULL,
                                     _table1_col2),
                  BinaryCondition.notEqualTo(mph1, mph1)))).toString();
    checkResult(queryStr,
                "SELECT ?,?,?,?,?,? FROM Schema1.Table1 t0,Table1 t1,Table2 t2 WHERE ((t0.col1 < ?) AND (t0.col2 <= ?) AND (t1.col_id IS NOT NULL) AND ((IM REALLY SNAZZY) OR (NOT (t2.col5 LIKE ?)) OR (YOU = ?)) AND ((t0.col2 IS NULL) OR (? <> ?)))");

    assertEquals((0 + startIndex), sph2.getIndex());
    assertEquals((1 + startIndex), sph3.getIndex());
    assertEquals((2 + startIndex), sph4.getIndex());
    assertEquals((3 + startIndex), sph5.getIndex());
    assertEquals((4 + startIndex), sph6.getIndex());
    assertEquals((5 + startIndex), sph7.getIndex());
    assertEquals((6 + startIndex), ph1.getIndex());
    assertEquals((8 + startIndex), ph2.getIndex());
    assertEquals(false, ph3.isInQuery());
    assertEquals((9 + startIndex), sph1.getIndex());
    assertEquals(3, mph1.getIndexes().size());
    assertEquals(Arrays.asList((7 + startIndex), (10 + startIndex),
                               (11 + startIndex)),
                 mph1.getIndexes());

    MockPreparedStatement mockStmt = new MockPreparedStatement();
    PreparedStatement stmt = (PreparedStatement)
      Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                             new Class[]{PreparedStatement.class},
                             mockStmt);

    prep.setStaticValues(stmt);
    ph1.setNull(Types.BLOB, stmt);
    ph2.setObject(new Object(), stmt);
    ph3.setObject(new Date(), Types.DATE, stmt);
    mph1.setString("foo", stmt);
  }

  
  private static class MockPreparedStatement
    implements InvocationHandler
  {
    public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable
    {
      // FIXME, write verification code
      return null;
    }
  }

}
