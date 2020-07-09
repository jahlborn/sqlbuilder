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

package com.healthmarketscience.sqlbuilder.dbspec;

import java.util.List;


/**
 * Maintains information about a database (table or column) foreign key
 * constraint for use with the sqlbuilder utilities.
 *
 * @author James Ahlborn
 */
public interface ForeignKeyConstraint extends Constraint 
{
  /** @return the table which is referenced by this constraint */
  public Table getReferencedTable();

  /** @return the columns in the referenced table, if not the primary key */
  public List<? extends Column> getReferencedColumns();

  @Override
  public default Type getType() {
    return Type.FOREIGN_KEY;
  }
}
