/*
Copyright (c) 2015 James Ahlborn

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

package com.healthmarketscience.sqlbuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.custom.HookType;
import com.healthmarketscience.sqlbuilder.custom.HookAnchor;

/**
 * Base class for all SqlObjects which support SQL syntax customizations.
 *
 * See {@link com.healthmarketscience.sqlbuilder.custom} for more details on custom SQL syntax.
 *
 * @author James Ahlborn
 */
public abstract class CustomizableSqlObject extends SqlObject
{
  private List<Customization> _customizations;

  protected CustomizableSqlObject() {
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    if(_customizations != null) {
      for(Customization cust : _customizations) {
        cust.getObject().collectSchemaObjects(vContext);
      }
    }
  }

  /**
   * Appends the appropriate customizations for the given anchor (and possibly
   * the anchor clause itself) based on whether or not the clause should be
   * included.
   */
  protected void maybeAppendTo(AppendableExt app, HookAnchor anchor, 
                               String clauseText, SqlObject clauseContent, 
                               boolean includeClause)
    throws IOException
  {
    if(includeClause) {
      customAppendTo(app, anchor, clauseText).append(clauseContent);
    } else {
      customAppendTo(app, anchor);
    }
  }

  /**
   * Appends the appropriate customizations for the given anchor (and possibly
   * the anchor clause itself) based on whether or not the clause should be
   * included.
   */
  protected void maybeAppendTo(AppendableExt app, HookAnchor anchor, 
                               String clauseText, boolean includeClause)
    throws IOException
  {
    if(includeClause) {
      customAppendTo(app, anchor, clauseText);
    } else {
      customAppendTo(app, anchor);
    }
  }

  /**
   * Appends any {@link HookType#BEFORE} or {@link HookType#AFTER}
   * customizations for the given anchor.
   */
  protected AppendableExt customAppendTo(AppendableExt app, HookAnchor anchor)
    throws IOException
  {
    List<Customization> custs = null;
    if((_customizations != null) &&
       ((custs = findCustomizations(anchor)) != null)) {
      appendCustomizations(app, custs, HookType.BEFORE);
      appendCustomizations(app, custs, HookType.AFTER);
    }
    return app;
  }

  /**
   * Appends all customizations for the given anchor.
   */
  protected AppendableExt customAppendTo(AppendableExt app, HookAnchor anchor,
                                         String str)
    throws IOException
  {
    List<Customization> custs = null;
    if((_customizations == null) || 
       ((custs = findCustomizations(anchor)) == null)) {
      return app.append(str);
    }

    appendCustomizations(app, custs, HookType.BEFORE);
    appendCustomizations(app, custs, HookType.PREFIX);
    if(!appendCustomizations(app, custs, HookType.REPLACEMENT)) {
      app.append(str);
    }
    appendCustomizations(app, custs, HookType.SUFFIX);
    appendCustomizations(app, custs, HookType.AFTER);
    return app;
  }

  /**
   * Returns any customizations for the given anchor.
   */
  private List<Customization> findCustomizations(HookAnchor anchor) {
    List<Customization> custs = null;
    for(Customization cust : _customizations) {
      if(cust.forAnchor(anchor)) {
        if(custs == null) {
          custs = new ArrayList<Customization>(2);
        }
        custs.add(cust);
      }
    } 
    return custs;
  }

  /**
   * Appends any of the given customizations of the given type.
   */
  private static boolean appendCustomizations(
      AppendableExt app, List<Customization> custs, HookType type)
    throws IOException
  {
    boolean found = false;
    for(Customization cust : custs) {
      if(cust.forType(type)) {
        app.append(cust.getObject());
        found |= true;
      }
    } 
    return found;
  }

  /**
   * Adds a new customization to this object.
   */
  protected void addCustomization(HookAnchor anchor, HookType type, Object obj) {
    if(obj == null) {
      return;
    }

    if(_customizations == null) {
      _customizations = new ArrayList<Customization>(2);
    }

    _customizations.add(new Customization(anchor, type, 
                                          Converter.toCustomSqlObject(obj)));
  }

  /**
   * Utility class for a SQL customization.
   */
  private static final class Customization
  {
    private final HookAnchor _anchor;
    private final HookType _type;
    private final SqlObject _obj;

    private Customization(HookAnchor anchor, HookType type, SqlObject obj)
    {
      _anchor = anchor;
      _type = type;
      _obj = obj;
    }

    public SqlObject getObject() {
      return _obj;
    }

    public boolean forAnchor(HookAnchor anchor) {
      return (_anchor == anchor);
    }

    public boolean forType(HookType type) {
      return (_type == type);
    }
  }
}
