package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.exc.StreamConstraintsException;
/**
 * The constraints for streaming reads.
 */
public class StreamReadConstraints {

    private static final int DEFAULT_MAX_NUM_LEN = 1000;
    public static final int DEFAULT_MAX_DEPTH = 1000;

    private final int _maxNumLen;
    private final int _maxNestingDepth;

    public static final StreamReadConstraints UNLIMITED = new StreamReadConstraints(Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static final class Builder {
        private int _maxNumLen = StreamReadConstraints.DEFAULT_MAX_NUM_LEN;
        private int _maxNestingDepth = StreamReadConstraints.DEFAULT_MAX_DEPTH;

        /**
         * Sets the maximum number length (in chars or bytes, depending on input context).
         * The default is 1000 (since Jackson 2.14).
         * @param maxNumLen the maximum number length (in chars or bytes, depending on input context)
         * @return this builder
         */
        public StreamReadConstraints.Builder withMaxNumberLength(int maxNumLen) {
            _maxNumLen = maxNumLen;
            return this;
        }

        public StreamReadConstraints.Builder withMaxNestingDepth(int maxNestingDepth) {
            if (maxNestingDepth < 0) {
                throw new IllegalArgumentException("Cannot set maxNestingDepth to a negative value");
            }
            _maxNestingDepth = maxNestingDepth;
            return this;
        }

        public Builder maxNestingDepth(final int maxNestingDepth) {
            if (maxNestingDepth < 0) {
                throw new IllegalArgumentException("Cannot set maxNestingDepth to a negative value");
            }
            _maxNestingDepth = maxNestingDepth;
            return this;
        }

        public StreamReadConstraints build() {
            return new StreamReadConstraints(_maxNumLen, _maxNestingDepth);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private StreamReadConstraints(int maxNumLen, int maxNestingDepth) {
        this._maxNestingDepth = maxNestingDepth;
        _maxNumLen = maxNumLen;
    }

    public int getMaxNestingDepth() {
        return _maxNestingDepth;
    }

    public int getMaxNumberLength() {
        return _maxNumLen;
    }

    /**
     * Convenience method that can be used to verify that the
     * nesting depth does not exceed the maximum specified by this
     * constraints object: if it does, a
     * {@link StreamConstraintsException}
     * is thrown.
     *
     * @param depth count of unclosed objects and arrays
     *
     * @throws StreamConstraintsException If depth exceeds maximum
     */
    public void validateNestingDepth(int depth) throws StreamConstraintsException
    {
        if (depth > _maxNestingDepth) {
            throw new StreamConstraintsException(String.format("Depth (%d) exceeds the maximum allowed nesting depth (%d)",
                    depth, _maxNestingDepth));
        }
    }

    /**
     * Convenience method that can be used to verify that an integer
     * number length does not exceed the maximum specified by this
     * constraints object.
     *
     * @param length the integer number length
     * @throws StreamConstraintsException if length exceeds maximum
     */
    public void validateIntegerLength(int length) throws StreamConstraintsException
    {
        if (length > _maxNumLen) {
            throw new StreamConstraintsException(
                String.format("Number value length (%d) exceeds the maximum allowed (%d)",
                    length, _maxNumLen));
        }
    }

    /**
     * Convenience method that can be used to verify that a floating-point
     * number length does not exceed the maximum specified by this
     * constraints object.
     *
     * @param length the floating-point number length
     * @throws StreamConstraintsException if length exceeds maximum
     */
    public void validateFPLength(int length) throws StreamConstraintsException
    {
        if (length > _maxNumLen) {
            throw new StreamConstraintsException(
                String.format("Number value length (%d) exceeds the maximum allowed (%d)",
                    length, _maxNumLen));
        }
    }
}
