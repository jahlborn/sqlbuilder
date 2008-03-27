/*
Copyright (c) 2008 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.sqlbuilder.dbspec.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * Root object for a collection of db objects all residing in the same
 * logical database.
 * <p>
 * Note, you generally do not want to mix objects from different specs because
 * they may have conflicting aliases.
 *
 * @author James Ahlborn
 */
public class DbSpec {

  /** schemas currently created for this db spec */
  private final List<DbSchema> _schemas = new ArrayList<DbSchema>();
  /** joins currently created for this db spec */
  private final List<DbJoin> _joins = new ArrayList<DbJoin>();
  /** unique id for the next alias for this db spec */
  private int _nextAliasNum;

  public DbSpec() {
  }

  /**
   * @return the next unused alias for this group of db objects
   */
  public String getNextAlias() {
    return "t" + _nextAliasNum++;
  }

  /**
   * @return the default schema previously added to this spec, or {@code null}
   *         if none.
   */
  public DbSchema getDefaultSchema() {
    return findSchema(null);
  }

  /**
   * @param name name of the schema to find
   * @return the schema previously added to this spec with the given name, or
   *         {@code null} if none.
   */
  public DbSchema findSchema(String name) {
    return DbObject.findObject(_schemas, name);
  }

  /**
   * Creates and adds a schema with no name to this spec (often referred to as
   * the default schema).
   * <p>
   * Note, no effort is made to make sure the schema is unique.
   * @return the freshly created default schema
   */
  public DbSchema addDefaultSchema() {
    return addSchema(null);
  }
  
  /**
   * Creates and adds a schema with the given name to this spec.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new schema
   * @return the freshly created schema
   */
  public DbSchema addSchema(String name) {
    DbSchema schema = new DbSchema(this, name);
    _schemas.add(schema);
    return schema;
  }

  /**
   * Creates and adds a join with the given parameters to this spec.
   * <p>
   * Note, no effort is made to make sure the given join is unique.
   * @param schemaFrom schema for the left side of the join
   * @param tableFrom table for the left side of the join
   * @param schemaTo schema for the right side of the join
   * @param tableTo table for the right side of the join
   * @param colNames the column names for the join (same for both tables)
   * @return the freshly created schema
   */
  public DbJoin addJoin(String schemaFrom, String tableFrom,
                        String schemaTo, String tableTo,
                        String... colNames)
  {
    return addJoin(schemaFrom, tableFrom, schemaTo, tableTo,
                   colNames, colNames);
  }
  
  /**
   * Creates and adds a join with the given parameters to this spec.
   * <p>
   * Note, no effort is made to make sure the given join is unique.
   * @param schemaFrom schema for the left side of the join
   * @param tableFrom table for the left side of the join
   * @param schemaTo schema for the right side of the join
   * @param tableTo table for the right side of the join
   * @param fromColNames the column names for the left side of the join
   * @param toColNames the column names for the right side of the join
   * @return the freshly created schema
   */
  public DbJoin addJoin(String schemaFrom, String tableFrom,
                        String schemaTo, String tableTo,
                        String[] fromColNames, String[] toColNames)
  {
    DbJoin join = new DbJoin(this,
                             findSchema(schemaFrom).findTable(tableFrom),
                             findSchema(schemaTo).findTable(tableTo),
                             fromColNames, toColNames);
    _joins.add(join);
    return join;
  }

}
