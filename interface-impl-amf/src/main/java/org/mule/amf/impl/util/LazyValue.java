/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.util;


import java.util.function.Supplier;

/**
 * Provides a value which may be lazily computed.
 * <p>
 * The value is only computed on the first invokation of {@link #get()}. Subsequent calls to such method will always return the
 * same value.
 * <p>
 * This class is thread-safe. When invoking {@link #get()}, it is guaranteed that the value will be computed only once.
 *
 * @param <T> the generic type of the provided value
 * @since 1.0
 */
public class LazyValue<T> implements Supplier<T> {

  private volatile boolean initialized = false;
  private T value;
  private Supplier<T> valueSupplier;

  /**
   * Creates a new instance which lazily obtains its value from the given {@code supplier}. It is guaranteed that
   * {@link Supplier#get()} will only be invoked once. Because this class is thread-safe, the supplier is not required to be.
   *
   * @param supplier A {@link Supplier} through which the value is obtained
   */
  public LazyValue(Supplier<T> supplier) {
    // Args.notNull(supplier != null, "supplier cannot be null");
    valueSupplier = supplier;
  }


  /**
   * Returns the lazy value. If the value has not yet been computed, then it does so
   *
   * @return the lazy value
   */
  @Override
  public T get() {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          this.value = valueSupplier.get();
          this.valueSupplier = null;
          this.initialized = true;
        }
      }
    }

    return value;
  }
}
