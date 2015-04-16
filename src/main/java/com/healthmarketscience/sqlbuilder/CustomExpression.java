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
 * Outputs the given custom object surrounded by parentheses
 * <code>"(&lt;customExpr&gt;)"</code>.  Acts like {@link CustomSql} for
 * expressions.  If the given expression is <code>null</code>, nothing will be
 * output for the expression.
 *
 * @author James Ahlborn
 */
public class CustomExpression extends Expression
{
  private SqlObject _expr;

  public CustomExpression(Object exprObj) {
    this((exprObj != null) ?
         (new CustomSql(exprObj)) :
         (SqlObject)null);
  }
  
  public CustomExpression(SqlObject exprStr) {
    _expr = exprStr;
  }

  @Override
  public boolean isEmpty() { return(_expr == null); }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    if(_expr != null) {
      _expr.collectSchemaObjects(vContext);
    }
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    appendCustomIfNotNull(app, _expr);
  }
  
}
