// Copyright (c) 2011 James Ahlborn

package com.healthmarketscience.sqlbuilder.dbspec;

import java.util.List;

/**
 *
 * @author James Ahlborn
 */
public interface Constraint {

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
