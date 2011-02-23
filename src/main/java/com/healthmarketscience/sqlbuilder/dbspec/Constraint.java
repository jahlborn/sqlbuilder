// Copyright (c) 2011 James Ahlborn

package com.healthmarketscience.sqlbuilder.dbspec;

import java.util.List;

/**
 * Maintains information about a database (table or column) constraint for use
 * with the sqlbuilder utilities.
 *
 * @author James Ahlborn
 */
public interface Constraint 
{
  public enum Type {
    NOT_NULL,
    UNIQUE,
    PRIMARY_KEY,
    FOREIGN_KEY;
  }

  /** @return the type of this constraint */
  public Type getType();

  /** @return the name of this constraint, if any */
  public String getConstraintNameSQL();

  /** @return the constrained columns */
  public List<? extends Column> getColumns();
}
