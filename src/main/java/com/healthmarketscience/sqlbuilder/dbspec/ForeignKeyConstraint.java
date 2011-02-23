// Copyright (c) 2011 James Ahlborn

package com.healthmarketscience.sqlbuilder.dbspec;

import java.util.List;


/**
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
