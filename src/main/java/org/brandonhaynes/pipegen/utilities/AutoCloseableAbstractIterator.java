package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.AbstractIterator;

public abstract class AutoCloseableAbstractIterator<T> extends AbstractIterator<T> implements AutoCloseable {
}
