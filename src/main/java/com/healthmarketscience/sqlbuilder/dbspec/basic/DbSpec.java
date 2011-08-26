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
 * <p>
 * The DbSpec also acts as the "factory" for creating other DbObject instances
 * (via the various {@code create*()} methods).  All the other DbObject types
 * delegate object creation to the referenced DbSpec.  Thus, custom model
 * classes can easily be plugged in by creating a subclass of this class which
 * overrides the relevant creation methods.
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

  public List<DbSchema> getSchemas() {
    return _schemas;
  }
  
  public List<DbJoin> getJoins() {
    return _joins;
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
    return addSchema((String)null);
  }
  
  /**
   * Creates and adds a schema with the given name to this spec.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new schema
   * @return the freshly created schema
   */
  public DbSchema addSchema(String name) {
    DbSchema schema = createSchema(name);
    return addSchema(schema);
  }

  /**
   * Adds the given schema to this schema.
   * <p>
   * Note, no effort is made to make sure the given schema is unique.
   * @param schema the schema to be added
   * @return the given schema
   */
  public <T extends DbSchema> T addSchema(T schema) {
    _schemas.add(checkOwnership(schema));
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
    DbJoin join = createJoin(findSchema(schemaFrom).findTable(tableFrom),
                             findSchema(schemaTo).findTable(tableTo),
                             fromColNames, toColNames);
    return addJoin(join);
  }

  /**
   * Adds the given join to this schema.
   * <p>
   * Note, no effort is made to make sure the given join is unique.
   * @param join the join to be added
   * @return the given join
   */
  public <T extends DbJoin> T addJoin(T join) {
    _joins.add(checkOwnership(join));
    return join;
  }
  
  /**
   * Creates and returns a new {@link DbSchema} with the given parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbSchema createSchema(String name)
  {
    return new DbSchema(this, name);
  }
  
  /**
   * Creates and returns a new {@link DbTable} with the given parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbTable createTable(DbSchema parent, String name)
  {
    return new DbTable(parent, name);
  }
  
  /**
   * Creates and returns a new {@link DbColumn} with the given parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbColumn createColumn(DbTable parent, String name,
                               String typeName, Integer typeLength)
  {
    return new DbColumn(parent, name, typeName, typeLength);
  }
  
  /**
   * Creates and returns a new {@link DbJoin} with the given parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbJoin createJoin(DbTable fromTable, DbTable toTable,
                           String[] fromColNames, String[] toColNames)
  {
    return new DbJoin(this, fromTable, toTable, fromColNames, toColNames);
  }

  /**
   * Creates and returns a new {@link DbIndex} with the given parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbIndex createIndex(DbTable table, String name, String... colNames)
  {
    return new DbIndex(table, name, colNames);
  }
  
  /**
   * Creates and returns a new {@link DbFunctionPackage} with the given
   * parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbFunctionPackage createFunctionPackage(DbSchema parent, String name)
  {
    return new DbFunctionPackage(parent, name);
  }

  /**
   * Creates and returns a new {@link DbFunction} with the given parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbFunction createFunction(DbFunctionPackage parent, String name)
  {
    return new DbFunction(parent, name);
  }
  
  /**
   * Creates and returns a new column {@link DbConstraint} with the given
   * parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbConstraint createColumnConstraint(
      DbColumn parent, String name,
      com.healthmarketscience.sqlbuilder.dbspec.Constraint.Type type)
  {
    return new DbConstraint(parent, name, type);
  }

  /**
   * Creates and returns a new table {@link DbConstraint} with the given
   * parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbConstraint createTableConstraint(
      DbTable parent, String name,
      com.healthmarketscience.sqlbuilder.dbspec.Constraint.Type type, 
      String... colNames)
  {
    return new DbConstraint(parent, name, type, colNames);
  }
  
  /**
   * Creates and returns a new column {@link DbForeignKeyConstraint} with the
   * given parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbForeignKeyConstraint createColumnForeignKeyConstraint(
      DbColumn parent, String name, DbTable referencedTable, String refColName)
  {
    return new DbForeignKeyConstraint(parent, name, referencedTable,
                                      refColName);
  }

  /**
   * Creates and returns a new table {@link DbForeignKeyConstraint} with the
   * given parameters.
   * <p>
   * This method can be overriden to utilize custom model subclasses.
   */
  public DbForeignKeyConstraint createTableForeignKeyConstraint(
      DbTable parent, String name, DbTable referencedTable,
      String[] colNames, String[] refColNames)
  {
    return new DbForeignKeyConstraint(parent, name, referencedTable,
                                      colNames, refColNames);
  }
  
  /**
   * @throws IllegalArgumentException if the parent of the given object is not
   *         this object
   */
  protected <T extends DbObject<?>> T checkOwnership(T obj) {
    if(obj.getSpec() != this) {
      throw new IllegalArgumentException(
          "Given " + obj.getClass().getSimpleName() + " is not owned by this " +
          getClass().getSimpleName());
    }
    return obj;
  }
  
}
