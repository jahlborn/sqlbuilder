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
