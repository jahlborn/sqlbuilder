// Copyright (c) 2011 James Ahlborn

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
}
