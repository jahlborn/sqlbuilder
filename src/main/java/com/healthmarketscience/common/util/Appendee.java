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
 * Should be implemented by an object which can append itself to an
 * AppendableExt object, where the work of generating a result for
 * {@code toString()} would itself involve the appending of multiple
 * CharSequences (Strings, etc).
 * <p>
 * In general, it is expected that the data appended to the AppendableExt an
 * {@link #appendTo} invocation be the same as the result of a call to
 * {@link Object#toString}.  If this is not the case, it should be clearly
 * documented in the class.
 * 
 * @author James Ahlborn
 */
public interface Appendee {

  /**
   * Appends this object to the given AppendableExt.  Called by an
   * AppendableExt when a request is made to append an instance of Appendee.
   *
   * @param a the AppendableExt to which this class should append itself
   * @throws IOException if the append fails
   */
  public void appendTo(AppendableExt a) throws IOException;
  
}
