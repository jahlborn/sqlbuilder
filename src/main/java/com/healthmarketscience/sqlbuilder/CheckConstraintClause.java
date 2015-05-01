/*
Copyright (c) 2015 James Ahlborn

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
import com.healthmarketscience.sqlbuilder.dbspec.CheckConstraint;

/**
 * Outputs a table or column CHECK constraint like:
 * {@code "[CONSTRAINT <name> ] CHECK (<condition>)"}
 * <p>
 * Note that if no conditions are added, then this object will not output
 * anything.
 * 
 * @author James Ahlborn
 */
public class CheckConstraintClause extends ConstraintClause 
{
  private ComboCondition _condition = ComboCondition.and();

  public CheckConstraintClause(CheckConstraint checkConstraint) 
  {
    this(checkConstraint, checkConstraint.getCondition());
  }

  protected CheckConstraintClause(Object name, Condition condition) {
    super(Type.CHECK, name, null);

    if(condition != null) {
      _condition.addCondition(condition);
    }
  }

  /**
   * Allows access to the AND ComboCondition of the condition clause to
   * facilitate common condition building code.
   * @return the AND ComboCondition of the CHECK constraint.
   */
  public ComboCondition getCondition() {
    return _condition;
  }
  
  /** 
   * Adds a condition to the condition clause for the CHECK constraint (AND'd
   * with any other conditions).  Note that the CHECK constraint will only be
   * generated if some conditions have been added.
   * <p>
   * For convenience purposes, the CheckConstraint generates it's own
   * ComboCondition allowing multiple conditions to be AND'd together.  To OR
   * conditions or perform other logic, the ComboCondition must be built and
   * added to the CheckConstraint.
   */
  public CheckConstraintClause addCondition(Condition newCondition) {
    _condition.addCondition(newCondition);
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    
    _condition.collectSchemaObjects(vContext);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    if(!_condition.isEmpty()) {
      super.appendTo(app);
    
      app.append(" ").append(_condition);
    }
  }
}
