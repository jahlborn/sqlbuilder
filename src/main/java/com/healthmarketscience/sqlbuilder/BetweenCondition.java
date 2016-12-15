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

import java.io.IOException;

import com.healthmarketscience.common.util.AppendableExt;


/**
 * Outputs a "BETWEEN" condition
 * <code>"(&lt;column&gt; [NOT] BETWEEN (&lt;rightObj1&gt;, &lt;rightObj2&gt;, ...) )"</code>
 * 
 * @author James Ahlborn
 */
public class BetweenCondition extends Condition {

  private boolean _negate;
  private SqlObject _value;
  private SqlObject _minValue;
  private SqlObject _maxValue;

  /**
   * Column {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject}.
   */
  public BetweenCondition(Object obj, Object minObj, Object maxObj) {
    _value = Converter.toColumnSqlObject(obj);
    _minValue = Converter.toColumnSqlObject(minObj);
    _maxValue = Converter.toColumnSqlObject(maxObj);
  }

  /** Sets whether or not the between condition should be negated or not */
  public BetweenCondition setNegate(boolean negate) {
    _negate = negate;
    return this;
  }
  
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _value.collectSchemaObjects(vContext);
    _minValue.collectSchemaObjects(vContext);
    _maxValue.collectSchemaObjects(vContext);
  }
  
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    // (x between min and max )
    openParen(app);
    app.append(_value)
      .append(_negate ? " NOT BETWEEN " : " BETWEEN ")
      .append(_minValue).append(" AND ").append(_maxValue);
    closeParen(app);
  }
}
