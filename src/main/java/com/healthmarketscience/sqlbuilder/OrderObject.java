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
import com.healthmarketscience.common.util.AppendableExt;


/**
 * Outputs the given object along with an order specification
 * <code>&lt;obj&gt; &lt;dir&gt;</code>
 *
 * @author James Ahlborn
 */
public class OrderObject extends SqlObject
{

  /** Enumeration representing the direction of an ordering clause */
  public enum Dir {
    ASCENDING(" ASC"),
    DESCENDING(" DESC");

    private final String _dirStr;

    private Dir(String dirStr) {
      _dirStr = dirStr;
    }

    @Override
    public String toString() { return _dirStr; }
  }

  private Dir _dir;
  private SqlObject _obj;
  
  public OrderObject(Dir dir,
                     SqlObject obj)
  {
    _dir = dir;
    _obj = obj;
  }

  /** @return the object held by this OrderObject */
  SqlObject getObject() {
    return _obj;
  }
  
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _obj.collectSchemaObjects(vContext);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_obj).append(_dir);
  }
  
}
