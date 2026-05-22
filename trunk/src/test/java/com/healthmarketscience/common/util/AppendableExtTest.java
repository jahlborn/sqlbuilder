/*
Copyright (c) 2007 Health Market Science, Inc.

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

package com.healthmarketscience.common.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author James Ahlborn
 */
public class AppendableExtTest extends TestCase {

  public AppendableExtTest(String name) {
    super(name);
  }

  protected AppendableExt createAppendableExt() {
    return createAppendableExt(new StringBuilder());
  }

  protected AppendableExt createAppendableExt(Appendable app) {
    AppendableExt appExt = new AppendableExt(app);
    assertSame(app, appExt.getAppendable());
    return appExt;
  }

  protected IOException getFailure(AppendableExt app, IOException caught) {
    return caught;
  }
  
  public void testSimple() throws Exception {

    String foo = "foo";
    Integer val = 13;
    StringBuilder bar = new StringBuilder("bar");

    assertEquals("foo 13 bar",
                 createAppendableExt()
                 .append(foo).append(" ").append(val).append(' ').append(bar)
                 .toString());
  }

  public void testCollection() throws Exception {

    assertEquals("",
                 createAppendableExt()
                 .append(Collections.<String>emptyList(), ", ")
                 .toString());

    assertEquals("foo|bar|baz",
                 createAppendableExt()
                 .append(Arrays.asList("foo","bar","baz"), "|")
                 .toString());
  }

  public void testAppendee() throws Exception {
    AppObj obj1 = new AppObj(Arrays.asList("foo", " ", 13, ' ',
                                           new StringBuilder("bar")));
    Object obj2 = new AppObj(Arrays.asList("buzz: ", obj1, "; bazz"));

    assertEquals("foo 13 bar",
                 createAppendableExt()
                 .append((Appendee)obj1)
                 .toString());
    assertEquals("foo 13 bar",
                 createAppendableExt()
                 .append((CharSequence)obj1)
                 .toString());
    assertEquals("buzz: foo 13 bar; bazz",
                 createAppendableExt()
                 .append(obj2)
                 .toString());
  }

  public void testRange() throws Exception {

    StringBuilder foo = new StringBuilder("fuzz 13 buzz yo");
    AppObj obj1 = new AppObj(Arrays.asList("foo", " ", 13, ' ',
                                           new StringBuilder("bar")));
    CharSequence obj2 = new AppObj(Arrays.asList("buzz: ", obj1, "; bazz"));

    assertEquals("z 13 buzz ",
                 createAppendableExt()
                 .append(foo, 3, 13)
                 .toString());
    assertEquals("foo 13 bar",
                 createAppendableExt()
                 .append(obj1, 0, 10)
                 .toString());
    assertEquals("zz:",
                 createAppendableExt()
                 .append(obj2, 2, 5)
                 .toString());

    CharSequence obj3 = new AppObj(Arrays.asList("some ", "other ", "stuff "),
                                   obj2);

    assertEquals("some other stuff z: foo 13 ",
                 createAppendableExt()
                 .append(obj3)
                 .toString());
    
    assertEquals("other stuff z: f",
                 createAppendableExt()
                 .append(obj3, 5, 21)
                 .toString());
    
  }

  public void testException() throws Exception {

    AppendableExt app = null;
    IOException caught = null;
    try {
      app = createAppendableExt(new FailingAppendable());
      app.append('b').toString();
    } catch(IOException e) {
      caught = e;
    }
    assertNotNull(getFailure(app, caught));

    app = null;
    caught = null;
    try {
      app = createAppendableExt(new FailingAppendable());
      app.append("foo").toString();
    } catch(IOException e) {
      caught = e;
    }
    assertNotNull(getFailure(app, caught));
    
    app = null;
    caught = null;
    try {
      app = createAppendableExt(new FailingAppendable());
      app.append("foo", 2, 3).toString();
    } catch(IOException e) {
      caught = e;
    }
    assertNotNull(getFailure(app, caught));    

    app = null;
    caught = null;
    try {
      app = createAppendableExt(new FailingAppendable());
      app.append((Appendee)new AppObj(Arrays.asList("foo"))).toString();
    } catch(IOException e) {
      caught = e;
    }
    assertNotNull(getFailure(app, caught));    

    app = null;
    caught = null;
    try {
      app = createAppendableExt(new FailingAppendable());
      app.append(13).toString();
    } catch(IOException e) {
      caught = e;
    }
    assertNotNull(getFailure(app, caught));    

    app = null;
    caught = null;
    try {
      app = createAppendableExt(new FailingAppendable());
      app.append(Arrays.asList("foo", "bar"), ", ").toString();
    } catch(IOException e) {
      caught = e;
    }
    assertNotNull(getFailure(app, caught));    
    
  }

  public void testContext() throws Exception {
    AppendableExt app = createAppendableExt();
    assertNull(app.getContext());
    String ctx = "foo";
    app.setContext(ctx);
    assertSame(ctx, app.getContext());
  }
  
  private static class AppObj extends AppendeeObject
    implements CharSequence
  {

    private final List<?> _data;
    private final CharSequence _rangeObj;

    private AppObj(List<?> data) {
      this(data, null);
    }

    private AppObj(List<?> data, CharSequence rangeObj) {
      _data = data;
      _rangeObj = rangeObj;
    }    
    
    @Override
    public void appendTo(AppendableExt a) throws IOException {
      for(Object obj : _data) {
        if(obj instanceof Character) {
          a.append((char)((Character)obj));
        } else {
          a.append(obj);
        }
      }
      if(_rangeObj != null) {
        a.append(_rangeObj, 3, 13);
      }
      a.append("", 0, 0);
    }

    public char charAt(int index) {
      throw new UnsupportedOperationException();
    }

    public int length() {
      throw new UnsupportedOperationException();
    }

    public CharSequence subSequence(int start, int end) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      throw new UnsupportedOperationException();
    }
  }

  public static class FailingAppendable implements Appendable
  {
    public Appendable append(char c)
      throws IOException
    {
      throw new IOException();
    }

    public Appendable append(CharSequence csq)
      throws IOException
    {
      throw new IOException();
    }
    
    public Appendable append(CharSequence csq, int start, int end)
      throws IOException
    {
      throw new IOException();
    }    
  }
  
}
