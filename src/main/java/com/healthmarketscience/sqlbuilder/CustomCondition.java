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
 * <code>"(&lt;customCond&gt;)"</code>.  Acts like {@link CustomSql} for
 * conditions.  If the given condition is <code>null</code>, nothing will be
 * output for the condition.
 *
 * @author James Ahlborn
 */
public class CustomCondition extends Condition
{
  private SqlObject _cond;

  public CustomCondition(Object condObj) {
    this((condObj != null) ?
         (new CustomSql(condObj)) :
         (SqlObject)null);
  }
  
  public CustomCondition(SqlObject condStr) {
    _cond = condStr;
  }

  @Override
  public boolean isEmpty() { return(_cond == null); }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    if(_cond != null) {
      _cond.collectSchemaObjects(vContext);
    }
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    appendCustomIfNotNull(app, _cond);
  }
  
}
