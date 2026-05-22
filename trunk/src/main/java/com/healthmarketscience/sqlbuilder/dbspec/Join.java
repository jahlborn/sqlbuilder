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
 * Maintains information about a database join for use with the sqlbuilder
 * utilities.
 *
 * @author James Ahlborn
 */
public interface Join {

  /** @return the table which is the "left side" of this join */
  public Table getFromTable();
  
  /** @return the table which is the "right side" of this join */
  public Table getToTable();

  /** @return the columns in the "left side" table which are related to the
      columns in the "right side" table.  must be the same length as
      the list returned from <code>getToColumns</code> */
  public List<? extends Column> getFromColumns();
  
  /** @return the columns in the "right side" table which are related to the
      columns in the "to side" table.  must be the same length as
      the list returned from <code>getFromColumns</code> */
  public List<? extends Column> getToColumns();
  
}
