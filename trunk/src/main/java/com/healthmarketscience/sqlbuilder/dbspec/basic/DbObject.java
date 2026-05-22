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

import java.util.Arrays;
import java.util.Collection;

/**
 * Base class for the simple implementations of the dbspec database objects.
 *
 * @author James Ahlborn
 */
public class DbObject<ParentType extends DbObject<?>>
{
  /** parent object for this db object */
  private final ParentType _parent;
  /** the simple name of this db object */
  private final String _name;

  protected DbObject(ParentType parent, String name) {
    _parent = parent;
    _name = name;
  }

  /**
   * Default implementation returns the parent's spec.
   * @return the db spec this db object is associated with
   */
  public DbSpec getSpec() {
    return getParent().getSpec();
  }

  protected ParentType getParent() {
    return _parent;
  }

  public String getName() {
    return _name;
  }

  /**
   * @return the name of this object qualified by any non-{@code null} names
   *         of parent objects, separated by '.' characters.
   */
  public String getAbsoluteName() {
    String absoluteName = getName();
    String prefix = ((getParent() != null) ?
                     getParent().getAbsoluteName() : null);
    if(absoluteName == null) {
      absoluteName = prefix;
    } else if(prefix != null) {
      absoluteName = prefix + "." + absoluteName;
    }
    return absoluteName;
  }

  /**
   * @throws IllegalArgumentException if the parent of the given object is not
   *         this object
   */
  protected <T extends DbObject<?>> T checkOwnership(T obj) {
    if(obj.getParent() != this) {
      throw new IllegalArgumentException(
          "Given " + obj.getClass().getSimpleName() + " is not owned by this " +
          getClass().getSimpleName());
    }
    return obj;
  }

  /**
   * @throws IllegalArgumentException if the parent of the given object is not
   *         this object
   */
  @SuppressWarnings("unchecked")
  protected <T extends DbObject<?>> T[] checkOwnership(T... objs) {
    for(DbObject<?> obj : objs) {
      checkOwnership(obj);
    }
    return objs;
  }

  /**
   * @param objects collection to search
   * @param name name of the object to find
   * @return the DbObject with the given name from the given collection, if
   *         any, {@code null} otherwise.
   */
  protected static <T extends DbObject<?>> T findObject(
      Collection<T> objects, String name) {
    for(T obj : objects) {
      if((name == obj.getName()) ||
         ((name != null) && name.equals(obj.getName()))) {
        return obj;
      }
    }
    return null;
  }

  /**
   * Adds the given objects to the given collection after verifying that they
   * are owned by the given parent.
   *
   * @param objs the collection to add the objects
   * @param parent the expected owner of the objects
   * @param objArr the objects to be added, may be {@code null}
   */
  @SuppressWarnings("unchecked")
  protected static <T extends DbObject<?>> void addObjects(
      Collection<T> objs, DbObject<?> parent, T... objArr)
  {
    if(objArr != null) {
      objs.addAll(Arrays.asList(parent.checkOwnership(objArr)));
    }
  }

  @Override
  public String toString() {
    return getAbsoluteName();
  }

}
