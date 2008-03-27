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
 * Representation of a database schema, for aggregating tables, indexes, and
 * function packages.
 *
 * @author James Ahlborn
 */
public class DbSchema extends DbObject<DbObject<?>> {

  /** the spec in which this schema exists */
  private final DbSpec _spec;
  /** tables currently created for this db spec */
  private final List<DbTable> _tables = new ArrayList<DbTable>();
  /** indexes currently created for this db spec */
  private final List<DbIndex> _indexes = new ArrayList<DbIndex>();
  /** function packages currently created for this db spec */
  private final List<DbFunctionPackage> _functionPackages =
    new ArrayList<DbFunctionPackage>();

  public DbSchema(DbSpec spec, String name) {
    super(null, name);
    _spec = spec;
  }

  @Override
  public DbSpec getSpec() {
    return _spec;
  }

  /**
   * @param name name of the table to find
   * @return the table previously added to this schema with the given name, or
   *         {@code null} if none.
   */
  public DbTable findTable(String name) {
    return findObject(_tables, name);
  }

  /**
   * Creates and adds a table with the given name to this schema.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new table
   * @return the freshly created table
   */
  public DbTable addTable(String name) {
    DbTable table = new DbTable(this, name);
    _tables.add(table);
    return table;
  }

  /**
   * Creates and adds a index with the given parameters to this schema.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new index
   * @param tableName name of the table indexed
   * @param colNames names of the columns indexed in the given table
   * @return the freshly created index
   */
  public DbIndex addIndex(String name, String tableName,
                          String... colNames) {
    DbIndex index = new DbIndex(findTable(tableName), name, colNames);
    _indexes.add(index);
    return index;
  }

  /**
   * @return the default package previously added to this spec, or
   *         {@code null} if none.
   */
  public DbFunctionPackage getDefaultFunctionPackage() {
    return findFunctionPackage(null);
  }

  /**
   * @param name name of the package to find
   * @return the package previously added to this spec with the given name, or
   *         {@code null} if none.
   */
  public DbFunctionPackage findFunctionPackage(String name) {
    return DbObject.findObject(_functionPackages, name);
  }

  /**
   * Creates and adds a package with no name to this spec (often referred to
   * as the default package).
   * <p>
   * Note, no effort is made to make sure the package is unique.
   * @return the freshly created default package
   */
  public DbFunctionPackage addDefaultFunctionPackage() {
    return addFunctionPackage(null);
  }
  
  /**
   * Creates and adds a package with the given name to this spec.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new package
   * @return the freshly created package
   */
  public DbFunctionPackage addFunctionPackage(String name) {
    DbFunctionPackage functionPackage = new DbFunctionPackage(this, name);
    _functionPackages.add(functionPackage);
    return functionPackage;
  }    
  
}
