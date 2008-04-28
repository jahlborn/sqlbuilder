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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


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
    QueryReader.Column col5 = prep.getNewColumn();
    QueryReader.Column col6 = prep.getNewColumn();

    SelectQuery query = new SelectQuery()
      .addCustomColumns(
          col1.setColumnObject(_table1_col1),
          col4.setColumnObject(_table1_col3),
          col3.setCustomColumnObject(new CustomSql("foo")),
          col5.setCustomColumnObject(new CustomSql("bar")),
          col6.setCustomColumnObject(_table1_col2))
      .validate();
    String selectStr = query.toString();
    checkResult(selectStr,
                "SELECT t0.col1,t0.col3,foo,bar,t0.col2 FROM Schema1.Table1 t0");
    
    assertEquals((0 + startIndex), col1.getIndex());
    assertEquals((1 + startIndex), col4.getIndex());
    assertEquals(false, col2.isInQuery());
    assertEquals((2 + startIndex), col3.getIndex());
    
    MockResultSet mockRs = new MockResultSet();
    ResultSet rs = (ResultSet)
      Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                             new Class[]{ResultSet.class},
                             mockRs);

    Object obj1 = new Object();
    
    mockRs._startIndex = startIndex;
    mockRs._results.add("foo");
    mockRs._results.add(42);
    mockRs._results.add(obj1);
    mockRs._results.add(13L);
    mockRs._results.add(true);
    
    assertEquals("foo", col1.getString(rs));
    assertNull(col2.getObject(rs));
    assertEquals(obj1, col3.getObject(rs));
    assertEquals(42, col4.getInt(rs));
    assertEquals(13L, col5.getLong(rs));
    assertEquals(true, col6.getBoolean(rs));

    try {
      col2.getInt(rs);
      fail("SQLException should have been thrown");
    } catch(SQLException e) {}      
    
    try {
      query.toString();
      fail("IllegalStateException should have been thrown");
    } catch(IllegalStateException e) {}
    
  }

  
  private static class MockResultSet
    implements InvocationHandler
  {
    private int _startIndex;
    private List<Object> _results = new ArrayList<Object>();
    
    public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable
    {
      if(args.length != 1) {
        throw new IllegalArgumentException("expecting 1 arg");
      }

      int colIdx = (Integer)args[0];
      int idx = colIdx - _startIndex;
      if((idx < 0) || (idx >= _results.size())) {
        throw new SQLException("invalid column index " + colIdx);
      }
      return _results.get(idx);
    }
  }
  
}
