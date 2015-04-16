/*
Copyright (c) 2011 James Ahlborn

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
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;

/**
 * Outputs the beginning of a constraint clause, either
 * <code>CONSTRAINT &lt;name&gt; </code> or an empty string if the constraint
 * is unnamed.
 *
 * @author James Ahlborn
 */
class ConstraintObject extends SqlObject
{
  protected Constraint _constraint;

  protected ConstraintObject(Constraint constraint) {
    _constraint = constraint;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    String name = _constraint.getConstraintNameSQL();
    if(name != null) {
      app.append("CONSTRAINT ").append(name).append(" ");
    }
  }
}
