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
import com.healthmarketscience.common.util.AppendableExt;


/**
 * Outputs the given object along with an order specification
 * <code>&lt;obj&gt; &lt;dir&gt;</code>
 *
 * @author James Ahlborn
 */
public class OrderObject extends SqlObject
{

  /** Enumeration representing the direction of an ordering clause */
  public enum Dir {
    ASCENDING(" ASC"),
    DESCENDING(" DESC");

    private final String _dirStr;

    private Dir(String dirStr) {
      _dirStr = dirStr;
    }

    @Override
    public String toString() { return _dirStr; }
  }

  /**
   * Enumeration representing the order of NULL 
   * @see "SQL 2003" 
   */
  public enum NullOrder {
    FIRST(" FIRST"),
    LAST(" LAST");

    private final String _dirStr;

    private NullOrder(String dirStr) {
      _dirStr = dirStr;
    }

    @Override
    public String toString() { return _dirStr; }
  }

  

  private final Dir _dir;
  private final SqlObject _obj;
  private NullOrder _nullOrder;

  public OrderObject(Dir dir, Object obj) {
    this(dir, Converter.toCustomColumnSqlObject(obj));
  }
  
  public OrderObject(Dir dir, SqlObject obj) {
    _dir = dir;
    _obj = obj;
  }

  /**
   * Sets the null order for this order specification.
   */
  public OrderObject setNullOrder(NullOrder nullOrder) {
    _nullOrder = nullOrder;
    return this;
  }

  /** @return the object held by this OrderObject */
  SqlObject getObject() {
    return _obj;
  }
  
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _obj.collectSchemaObjects(vContext);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_obj).append(_dir);
    if(_nullOrder != null) {
      app.append(" NULLS").append(_nullOrder);
    }
  }
  
}
