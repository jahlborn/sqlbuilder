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
 * Outputs the negation of the given condition "(NOT &lt;condition&gt;)"
 *
 * @author James Ahlborn
 */
public class NotCondition extends Condition
{
  private Condition _condition;

  /**
   * {@code Object} -&gt; {@code Condition} conversions handled by
   * {@link Converter#toConditionObject(Object)}.
   */
  public NotCondition(Object condition) {
    this(Converter.toConditionObject(condition));
  }
  
  public NotCondition(Condition condition) {
    _condition = condition;
  }

  @Override
  public boolean isEmpty() {
    return _condition.isEmpty();
  }
    
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _condition.collectSchemaObjects(vContext);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException
  {
    if(!_condition.isEmpty()) {
      openParen(app);
      app.append("NOT ").append(_condition);
      closeParen(app);
    }
  }

}
