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
 * Outputs the negation of the given expression "(- &lt;expression&gt;)"
 *
 * @author James Ahlborn
 */
public class NegateExpression extends Expression
{
  private Expression _expression;

  /**
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#toExpressionObject(Object)}.
   */
  public NegateExpression(Object obj) {
    this(Converter.toExpressionObject(obj));
  }
    
  public NegateExpression(Expression expr) {
    _expression = expr;
  }
    
  @Override
  public boolean isEmpty() {
    return _expression.isEmpty();
  }
    
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _expression.collectSchemaObjects(vContext);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    if(!_expression.isEmpty()) {
      openParen(app);
      app.append("- ").append(_expression);
      closeParen(app);
    }
  }

}
