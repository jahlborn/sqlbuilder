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

import java.util.Arrays;
import java.util.Collection;

/**
 * Base class for the simple implementations of the dbspec database objects.
 *
 * @author James Ahlborn
 */
public class DbObject<ParentType extends DbObject>
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
