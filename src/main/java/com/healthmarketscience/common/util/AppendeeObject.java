/*
Copyright (c) 2007 Health Market Science, Inc.

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

package com.healthmarketscience.common.util;

import java.io.IOException;

/**
 * Simple subclass for objects wishing to use the AppendableExt/Appendee
 * interface in new class hierarchies.  The {@link #toString()} method is
 * overridden to call the {@link #appendTo} method, so a subclass need only
 * provide a new implementation for the {@code appendTo()} method.  The
 * default {@code appendTo()} implementation appends the result of calling
 * {@link Object#toString}.  Also adds a {@link #toString(int)} method for
 * specifying the initial size of the underlying StringBuilder.
 *
 * @author James Ahlborn
 */
public class AppendeeObject
  implements Appendee
{
  public AppendeeObject() {}

  public void appendTo(AppendableExt a) throws IOException {
    // append Object.toString() to the AppendableExt
    a.append(super.toString());
  }

  public String toString(int size) {
    return(new StringAppendableExt(size)).append(this).toString();
  }
  
  @Override
  public String toString() {
    return(new StringAppendableExt()).append(this).toString();
  }
  
}
