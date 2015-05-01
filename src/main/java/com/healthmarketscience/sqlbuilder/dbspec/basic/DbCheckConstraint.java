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

package com.healthmarketscience.sqlbuilder.dbspec.basic;

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.CheckConstraint;

/**
 * Representation of a (table or column) check constraint in a database
 * schema.
 *
 * @author James Ahlborn
 */
public class DbCheckConstraint extends DbConstraint 
  implements CheckConstraint
{
  /** the condition for this constraint */
  private final Condition _condition;

  public DbCheckConstraint(DbColumn parent, String name, Condition condition) {
    super(parent, name, Type.CHECK);
    _condition = condition;
  }

  public DbCheckConstraint(DbTable parent, String name, Condition condition) {
    super(parent, name, Type.CHECK, (DbColumn[])null);
    _condition = condition;
  }

  public Condition getCondition() {
    return _condition;
  }
}
