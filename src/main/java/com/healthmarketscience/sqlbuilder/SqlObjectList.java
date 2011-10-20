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

package com.healthmarketscience.sqlbuilder;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.healthmarketscience.common.util.AppendableExt;


/**
 * Maintains a list of SqlObjects.  Outputs each object separated by the
 * given delimiter (defaults to {@link #DEFAULT_DELIMITER}).
 * <p>
 * Note that this class is generally intended to be used internally by the
 * other SqlObjects.
 *
 * @author James Ahlborn
 */
public class SqlObjectList<ObjType extends SqlObject> extends SqlObject
  implements Iterable<ObjType>                                   
{
  /** the default delimiter used by a SqlObjectList */
  public static final String DEFAULT_DELIMITER = ",";

  private final String _delimiter;
  private final List<ObjType> _objects;
    
  public SqlObjectList() {
    this(DEFAULT_DELIMITER, new LinkedList<ObjType>());
  }

  public SqlObjectList(String delimiter) {
    this(delimiter, new LinkedList<ObjType>());
  }

  public SqlObjectList(String delimiter, List<ObjType> objects) {
    _delimiter = delimiter;
    _objects = objects;
  }

  /**
   * Constructs and returns a new SqlObjectList, conveniently allows
   * construction without respecifying generic param type.
   * @return a new SqlObjectList with the default delimiter
   */
  public static <ObjType extends SqlObject> SqlObjectList<ObjType> create() {
    return new SqlObjectList<ObjType>();
  }
  
  /**
   * Constructs and returns a new SqlObjectList, conveniently allows
   * construction without respecifying generic param type.
   * @param delimiter to use when appending the list
   * @return a new SqlObjectList with the given delimiter
   */
  public static <ObjType extends SqlObject> SqlObjectList<ObjType> create(
      String delimiter) {
    return new SqlObjectList<ObjType>(delimiter);
  }

  public String getDelimiter() {
    return _delimiter;
  }

  /**
   * @return the number of objects in the list
   */
  public int size() { return _objects.size(); }

  /**
   * @return {@code true} if there are no objects in the list, {@code false}
   *         otherwise.
   */
  public boolean isEmpty() { return _objects.isEmpty(); }

  /**
   * Removes all objects from the list.
   */
  public void clear() { _objects.clear(); }

  /**
   * Returns the object at the specified index.
   */
  public ObjType get(int index) { return _objects.get(index); }

  /**
   * @return a mutable Iterator over the objects in the list
   */
  public Iterator<ObjType> iterator() { return _objects.iterator(); }

  /**
   * @return a mutable ListIterator over the objects in the list
   */
  public ListIterator<ObjType> listIterator() {
    return _objects.listIterator();
  }
  
  /**
   * Adds the given object to the list
   * @param obj the object to be added
   */
  public SqlObjectList<ObjType> addObject(ObjType obj) {
    _objects.add(obj);
    return this;
  }

  /**
   * Adds the given objects to the list
   * @param objs the objects to be added, no-op if {@code null}
   */
  public SqlObjectList<ObjType> addObjects(ObjType... objs) {
    if(objs == null) {
      return this;
    }
    for(ObjType obj : objs) {
      _objects.add(obj);
    }
    return this;
  }

  /**
   * Adds the given objects to the list
   * @param objs the objects to be added, no-op if {@code null}
   */
  public SqlObjectList<ObjType> addObjects(Iterable<? extends ObjType> objs) {
    if(objs == null) {
      return this;
    }
    for(ObjType obj : objs) {
      _objects.add(obj);
    }
    return this;
  }

  /**
   * Adds the given objects to the list after converting each of them using
   * the given converter.
   * @param converter Converter which generates the actual objects to be added
   *                  from the given objects
   * @param objs the objects to be added, no-op if {@code null}
   */
  public <SrcType, DstType extends ObjType> SqlObjectList<ObjType> addObjects(
      Converter<SrcType, DstType> converter, SrcType... objs)
  {
    if(objs == null) {
      return this;
    }
    for(SrcType obj : objs) {
      _objects.add(converter.convert(obj));
    }
    return this;
  }

  /**
   * Adds the given objects to the list after converting each of them using
   * the given converter.
   * @param converter Converter which generates the actual objects to be added
   *                  from the given objects
   * @param objs the objects to be added, no-op if {@code null}
   */
  public <SrcType, DstType extends ObjType> SqlObjectList<ObjType> addObjects(
      Converter<SrcType, DstType> converter,
      Iterable<? extends SrcType> objs)
  {
    if(objs == null) {
      return this;
    }
    for(SrcType obj : objs) {
      _objects.add(converter.convert(obj));
    }
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    for(ObjType obj : _objects) {
      obj.collectSchemaObjects(vContext);
    }
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException
  {
    app.append(this, _delimiter);
  }
}
