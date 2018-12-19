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

import java.util.Iterator;
import java.io.IOException;

/**
 * Wrapper for an Appendable which adds the ability for objects to append
 * themselves directly to the given Appendable instead of creating
 * intermediate CharSequence objects (Strings, etc).  This can make appends of
 * deep object hierarchies much more efficient.  An object can take advantage
 * of this facility by implementing the Appendee interface.
 * <p>
 * All methods will use the {@link Appendee#appendTo} method if passed an
 * Appendee.
 * <p>
 * Additionally, this wrapper adds two convenience methods:
 * <ul>
 * <li>{@link #append(Object)} - a method to append Objects by calling
 *     String.valueOf(obj) on the object and appending the result to the
 *     AppendableExt (unless the object is an Appendee or CharSequence)</li>
 * <li>{@link #append(Iterable,Object)} - a method to append an iteration
 *     (collection, etc) of objects separated by a delimiter</li>
 * </ul>
 * This class acts like the
 * {@link java.util.Formatter}/{@link java.util.Formattable} facility without
 * the extra overhead of parsing the format strings.  If complicated formatting
 * is needed for the generated strings, Formatter should be used instead.
 * <p>
 * Examples:
 * </p>
 * <pre>
 *
 * //
 * // *Without* using this interface.
 * //
 * public class Foo {
 *   private Bar b;
 *   public String toString() {
 *     return "Foo " + b;
 *   }
 * }
 * public class Bar {
 *   public String toString() {
 *     return "Bar";
 *   }
 * }
 *
 * Foo f = new Foo();
 * StringBuilder sb = new StringBuilder();
 *
 * // this will involve copying multiple strings before actual append!!!
 * sb.append(f);
 *
 *
 * //
 * // *With* using this interface.
 * //
 * public class Foo implements Appendee {
 *   private Bar b;
 *   public void appendTo(AppendableExt app) throws IOException {
 *     app.append("Foo").append(b);
 *   }
 * }
 * public class Bar implements Appendee {
 *   public void appendTo(AppendableExt app) throws IOException {
 *     app.append("Bar");
 *   }
 * }
 *
 * Foo f = new Foo();
 * AppendableExt ae = new AppendableExt(new StringBuilder());
 *
 * // this will involve no extra copies, both strings ("Foo", "Bar") will be
 * // written directly to the Appender
 * ae.append(f);
 *
 * </pre>
 *
 * @author James Ahlborn
 */
public class AppendableExt implements Appendable
{
  /**
   * The actual Appendable getting the chars
   */
  private final Appendable _baseApp;

  /**
   * Working appendable which may be changed during certain operations
   */
  private Appendable _app;

  /**
   * Custom context for the appendable
   */
  private Object _context;

  /**
   * Initialize a new AppendableExt based on the given Appendable.
   *
   * @param app initial underlying Appendable
   */
  public AppendableExt(Appendable app) {
    this(app, null);
  }

  /**
   * Initialize a new AppendableExt based on the given Appendable and
   * context.
   *
   * @param app initial underlying Appendable
   * @param context initial append context
   */
  public AppendableExt(Appendable app, Object context) {
    _baseApp = app;
    _app = app;
    _context = context;
  }

  public AppendableExt append(char c)
    throws IOException
  {
    _app.append(c);
    return this;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note that if the given CharSequence is also an Appendee, it will be
   * handled as an Appendee instead.
   */
  public AppendableExt append(CharSequence s)
    throws IOException
  {
    if(s instanceof Appendee) {
      return this.append((Appendee)s);
    }

    _app.append(s);
    return this;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note that if the given CharSequence is also an Appendee, it will be
   * handled as an Appendee instead (but the range will still be respected).
   */
  public AppendableExt append(CharSequence s, int start, int end)
    throws IOException
  {
    if(start == end) {
      // empty range, don't bother doing any work
      return this;
    }

    if(s instanceof Appendee) {
      // we want to allow the appendee to append directly, but we need to
      // restrict the range of output.  so, we will put a SubRangeAppendable
      // into place for this call which will filter out the unneeded
      // characters while still allowing for direct appending
      Appendable oldApp = _app;
      _app = new SubRangeAppendable(oldApp, start, end);
      try {
        return this.append((Appendee)s);
      } finally {
        _app = oldApp;
      }
    }

    _app.append(s, start, end);
    return this;
  }

  /**
   * Will call the appendTo() method on the given object.
   *
   * @param a object to append to this AppendableExt
   * @return this AppendableExt object
   * @throws IOException if the append fails
   */
  public AppendableExt append(Appendee a)
    throws IOException
  {
    a.appendTo(this);
    return this;
  }

  /**
   * Will call append(String.valueOf(o)) with the given object (unless the
   * object is an Appendee or CharSequence in which case it will be passed to
   * the appropriate method).
   *
   * @param o object to append to this AppendableExt
   * @return this AppendableExt object
   * @throws IOException if the append fails
   */
  public AppendableExt append(Object o)
    throws IOException
  {
    if(o instanceof Appendee) {
      return this.append((Appendee)o);
    }
    if(o instanceof CharSequence) {
      return this.append((CharSequence)o);
    }

    _app.append(String.valueOf(o));
    return this;
  }

  /**
   * Will iterate the given Iterable and append each object separated by the
   * given delimiter.
   *
   * @param iable an Iterable object
   * @param delimiter delimiter to append between each object in the iable
   * @return this AppendableExt object
   * @throws IOException if the append fails
   */
  public AppendableExt append(Iterable<?> iable, Object delimiter)
    throws IOException
  {
    for(Iterator<?> iter = iable.iterator(); iter.hasNext(); ) {
      append(iter.next());
      if(iter.hasNext()) {
        append(delimiter);
      }
    }
    return this;
  }

  /**
   * Get the underlying Appendable.
   *
   * @return the underlying Appendable
   */
  public Appendable getAppendable() { return _baseApp; }

  /**
   * Get the result of calling toString() on the underlying Appendable.
   *
   * @return the result of calling toString() on the underlying Appendable.
   */
  @Override
  public String toString() {
    return _baseApp.toString();
  }

  /**
   * @return the current "context" as set through {@link #setContext}, if any.
   */
  public Object getContext() {
    return _context;
  }

  /**
   * Sets the "context" that will be returned from subsequent
   * {@link #getContext} calls.  Useful for Appendee implementations that
   * want to change the behavior of nested, context-aware Appendee objects.
   *
   * @param newContext the new context for any subsequent append calls
   */
  public void setContext(Object newContext) {
    _context = newContext;
  }

  /**
   * Appendable which delegates actual appending to another appendable, but
   * restricts the passed through characters based on a given range.
   */
  private static class SubRangeAppendable implements Appendable
  {
    private final Appendable _delegate;
    private final int _start;
    private final int _end;
    private int _pos;

    private SubRangeAppendable(Appendable delegate,
                               int start, int end) {
      _delegate = delegate;
      _start = start;
      _end = end;
    }

    public Appendable append(char c)
      throws IOException
    {
      if((_pos >= _start) && (_pos < _end)) {
        _delegate.append(c);
      }
      ++_pos;
      return this;
    }

    public Appendable append(CharSequence csq)
      throws IOException
    {
      int len = csq.length();
      if((_pos >= _start) && ((_end - _pos) >= len)) {
        // special case entire incoming range being valid
        _delegate.append(csq);
        _pos += len;
      } else {
        // handle range manipulation in common method
        append(csq, 0, len);
      }
      return this;
    }

    public Appendable append(CharSequence csq, int start, int end)
      throws IOException
    {
      if((start < 0) || (end < 0) || (start > end) || (end > csq.length())) {
        throw new IndexOutOfBoundsException("invalid start " + start +
                                            " or end " + end + ", length " +
                                            csq.length());
      }

      if(_pos >= _end) {
        // already past our target range, skip everything else
        return this;
      }

      // skip any leading chars in the incoming range that are not within our
      // target range
      if(_pos < _start) {
        int skip = Math.min((end - start), (_start - _pos));
        start += skip;
        _pos += skip;
      }

      if(start >= end) {
        // no chars left in incoming range
        return this;
      }

      // shrink incoming range if fewer chars are needed in target range
      int len = Math.min((end - start), (_end - _pos));
      end = start + len;

      // finally pass the adjusted range to the delegate
      _delegate.append(csq, start, end);
      _pos += len;

      return this;
    }

  }

}
