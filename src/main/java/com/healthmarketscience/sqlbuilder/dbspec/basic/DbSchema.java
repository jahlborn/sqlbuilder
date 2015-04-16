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

  public List<DbTable> getTables() {
    return _tables;
  }
  
  public List<DbIndex> getIndexs() {
    return _indexes;
  }

  public List<DbFunctionPackage> getFunctionPackages() {
    return _functionPackages;
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
    DbTable table = getSpec().createTable(this, name);
    return addTable(table);
  }

  /**
   * Adds the given table to this schema.
   * <p>
   * Note, no effort is made to make sure the given table is unique.
   * @param table the table to be added
   * @return the given table
   */
  public <T extends DbTable> T addTable(T table) {
    _tables.add(checkOwnership(table));
    return table;
  }

  /**
   * @param name name of the index to find
   * @return the index previously added to this schema with the given name, or
   *         {@code null} if none.
   */
  public DbIndex findIndex(String name) {
    return findObject(_indexes, name);
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
    DbIndex index = getSpec().createIndex(findTable(tableName), name, colNames);
    return addIndex(index);
  }

  /**
   * Adds the given index to this schema.
   * <p>
   * Note, no effort is made to make sure the given index is unique.
   * @param index the index to be added
   * @return the given index
   */
  public <T extends DbIndex> T addIndex(T index) {
    _indexes.add(checkOwnership(index));
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
    return addFunctionPackage((String)null);
  }
  
  /**
   * Creates and adds a package with the given name to this spec.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new package
   * @return the freshly created package
   */
  public DbFunctionPackage addFunctionPackage(String name) {
    DbFunctionPackage functionPackage =
      getSpec().createFunctionPackage(this, name);
    return addFunctionPackage(functionPackage);
  }    

  /**
   * Adds the given functionPackage to this schema.
   * <p>
   * Note, no effort is made to make sure the given functionPackage is unique.
   * @param functionPackage the functionPackage to be added
   * @return the given functionPackage
   */
  public <T extends DbFunctionPackage> T addFunctionPackage(T functionPackage) {
    _functionPackages.add(checkOwnership(functionPackage));
    return functionPackage;
  }

  
}
