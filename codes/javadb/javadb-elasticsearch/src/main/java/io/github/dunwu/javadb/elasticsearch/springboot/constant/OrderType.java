package io.github.dunwu.javadb.elasticsearch.springboot.constant;

import java.util.Locale;
import java.util.Optional;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2019-12-17
 */
public enum OrderType {

    ASC,
    DESC;

    /**
     * Returns the {@link OrderType} enum for the given {@link String} or null if it cannot be parsed into an enum
     * value.
     * @param value
     * @return
     */
    public static Optional<OrderType> fromOptionalString(String value) {

        try {
            return Optional.of(fromString(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the {@link OrderType} enum for the given {@link String} value.
     * @param value
     * @return
     * @throws IllegalArgumentException in case the given value cannot be parsed into an enum value.
     */
    public static OrderType fromString(String value) {

        try {
            return OrderType.valueOf(value.toUpperCase(Locale.US));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                "Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), e);
        }
    }

    /**
     * Returns whether the direction is ascending.
     * @return
     * @since 1.13
     */
    public boolean isAscending() {
        return this.equals(ASC);
    }

    /**
     * Returns whether the direction is descending.
     * @return
     * @since 1.13
     */
    public boolean isDescending() {
        return this.equals(DESC);
    }
}
