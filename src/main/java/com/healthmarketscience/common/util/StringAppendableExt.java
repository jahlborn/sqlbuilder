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
 * Simple extension of AppendableExt that swallows IOExceptions under the
 * assumption that it will generally be used with Appendables which do not
 * actually throw any IOExceptions (i.e. StringBuilder).  Default constructors
 * will use a StringBuilder for the underlying Appendable.
 * <p>
 * In the event that an underlying Appendable is used which does actually
 * thrown IOExceptions, they will be trapped, and at any time the last
 * IOException can be obtained using {@link #getIOException}.
 * </p>
 * <p>
 * This class is not thread-safe.
 * </p>
 */
public class StringAppendableExt extends AppendableExt
{

  /** 
   * The last IOException thrown by an append call
   */
  private IOException _ioException;
    
  /** 
   * Initialize a new StringAppendableExt based on a StringBuilder.
   */
  public StringAppendableExt() {
    this(new StringBuilder(), null);
  }

  /** 
   * Initialize a new StringAppendableExt based on a StringBuilder, with a
   * specified initial capacity.
   * 
   * @param capacity the initial capacity for the StringBuilder.
   */
  public StringAppendableExt(int capacity) {
    this(capacity, null);
  }

  /** 
   * Initialize a new StringAppendableExt based on a StringBuilder, with a
   * specified initial capacity and given context.
   * 
   * @param size the initial capacity for the StringBuilder.
   * @param context initial append context
   */
  public StringAppendableExt(int size, Object context) {
    this(new StringBuilder(size), context);
  }

  /** 
   * Initialize a new StringAppendableExt based on the given Appendable.
   * 
   * @param app initial underlying Appendable
   */
  public StringAppendableExt(Appendable app) {
    this(app, null);
  }

  /** 
   * Initialize a new StringAppendableExt based on the given Appendable and
   * context.
   * 
   * @param app initial underlying Appendable
   * @param context initial append context
   */
  public StringAppendableExt(Appendable app, Object context) {
    super(app, context);
  }

  @Override
  public StringAppendableExt append(char c)
  {
    try {
      super.append(c);
    } catch(IOException e) {
      _ioException = e;
    }
    return this;
  }
  
  @Override
  public StringAppendableExt append(CharSequence s)
  {
    try {
      super.append(s);
    } catch(IOException e) {
      _ioException = e;
    }
    return this;
  }
  
  @Override
  public StringAppendableExt append(CharSequence s, int start, int end)
  {
    try {
      super.append(s, start, end);
    } catch(IOException e) {
      _ioException = e;
    }
    return this;
  }

  @Override
  public StringAppendableExt append(Appendee a)
  {
    try {
      super.append(a);
    } catch(IOException e) {
      _ioException = e;
    }
    return this;
  }

  @Override
  public StringAppendableExt append(Object o)
  {
    try {
      super.append(o);
    } catch(IOException e) {
      _ioException = e;
    }
    return this;
  }

  @Override
  public StringAppendableExt append(Iterable<?> iable, Object delimiter)
  {
    try {
      super.append(iable, delimiter);
    } catch(IOException e) {
      _ioException = e;
    }
    return this;
  }

  /**
   * Get the last IOException thrown by an append call, or {@code null} if 
   * no IOException was caught.
   *
   * @return the last IOException thrown by an append call, or {@code null}
   *         if no IOException was caught.
   */
  public IOException getIOException() {
    return _ioException;
  }
  
}
