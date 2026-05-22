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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
                             new Class<?>[]{ResultSet.class},
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

    col1.updateString("bar", rs);
    col2.updateObject("buzzard", rs);
    col3.updateObject(obj1, rs);
    col4.updateInt(52, rs);
    col5.updateLong(500L, rs);
    col6.updateBoolean(false, rs);

    assertEquals(Arrays.asList("bar", 52, obj1, 500L, false),
                 mockRs._updateResults);
  }


  private static class MockResultSet
    implements InvocationHandler
  {
    public int _startIndex;
    public List<Object> _results = new ArrayList<Object>();
    public List<Object> _updateResults = new ArrayList<Object>();

    public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable
    {
      if(method.getName().startsWith("get")) {
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

      if(method.getName().startsWith("update")) {
        if(args.length != 2) {
          throw new IllegalArgumentException("expecting 1 arg");
        }

        int colIdx = (Integer)args[0];
        int idx = colIdx - _startIndex;
        if(idx < 0) {
          throw new SQLException("invalid column index " + colIdx);
        }
        while(_updateResults.size() < (idx + 1)) {
          _updateResults.add(null);
        }
        return _updateResults.set(idx, args[1]);
      }

      throw new UnsupportedOperationException();
    }
  }

}
