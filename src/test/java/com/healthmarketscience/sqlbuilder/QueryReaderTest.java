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
import java.sql.ResultSet;


/**
 * @author james
 */
public class QueryReaderTest extends BaseSqlTestCase {

  public QueryReaderTest(String name) {
    super(name);
  }

  public void testReader() throws Exception {
    doTestReader(1);
    doTestReader(13);
  }

  private void doTestReader(int startIndex) throws Exception
  {
    QueryReader prep = new QueryReader(startIndex);
    QueryReader.Column col1 = prep.getNewColumn();
    QueryReader.Column col2 = prep.getNewColumn();
    QueryReader.Column col3 = prep.getNewColumn();
    QueryReader.Column col4 = prep.getNewColumn();

    String selectStr = new SelectQuery()
      .addCustomColumns(
          col1.setColumnObject(_table1_col1),
          col4.setColumnObject(_table1_col3),
          col3.setCustomColumnObject(new CustomSql("foo")))
      .validate().toString();

    checkResult(selectStr,
                "SELECT t0.col1,t0.col3,foo FROM Schema1.Table1 t0");
    
    assertEquals((0 + startIndex), col1.getIndex());
    assertEquals((1 + startIndex), col4.getIndex());
    assertEquals(false, col2.isInQuery());
    assertEquals((2 + startIndex), col3.getIndex());
    
    MockResultSet mockRs = new MockResultSet();
    ResultSet rs = (ResultSet)
      Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                             new Class[]{ResultSet.class},
                             mockRs);

    col1.getString(rs);
    col3.getObject(rs);
    
  }

  
  private static class MockResultSet
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
