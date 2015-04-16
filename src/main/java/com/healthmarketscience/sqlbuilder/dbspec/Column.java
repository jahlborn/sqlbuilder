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

package com.healthmarketscience.sqlbuilder.dbspec;

import java.util.List;

/**
 * Maintains information about a database column for use with the sqlbuilder
 * utilities.
 *
 * @author James Ahlborn
 */
public interface Column {

  /** @return the parent database table of this column */
  public Table getTable();
  
  /** @return the simple name of this column */
  public String getColumnNameSQL();

  /** @return the type of this column */
  public String getTypeNameSQL();

  /**
   * @return the length of the type of this column, may be <code>null</code>
   * @deprecated use {@link #getTypeQualifiers} instead
   */
   @Deprecated
  public Integer getTypeLength();

  /**
   * @return the various type qualifiers for this column (e.g. length or
   *         scale/precision) in declaration order.  May be {@code null} or
   *         empty if none.
   */
  public List<?> getTypeQualifiers();

  /** @return any constraints for this column */
  public List<? extends Constraint> getConstraints();

  /** @return the default value for this column, if any */
  public Object getDefaultValue();
}
