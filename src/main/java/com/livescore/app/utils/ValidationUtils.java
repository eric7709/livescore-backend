package com.livescore.app.utils;

import com.livescore.app.exceptions.BadRequestException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ValidationUtils {
    // ------------------ STRING VALIDATIONS ------------------
    
    /** Throws if the string is null or blank */
    public static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
    }

    /** Throws if the string is null or empty */
    public static void requireNonEmpty(String value, String message) {
        if (value == null || value.isEmpty()) {
            throw new BadRequestException(message);
        }
    }

    /** Returns true if string is null, empty, or blank */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Returns true if string is not null, not empty, and not blank */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /** Throws if the string length is less than min or greater than max */
    public static void requireLength(String value, int min, int max, String message) {
        if (value == null || value.length() < min || value.length() > max) {
            throw new BadRequestException(message);
        }
    }

    /** Throws if string does not match regex */
    public static void requirePattern(String value, String regex, String message) {
        if (value == null || !value.matches(regex)) {
            throw new BadRequestException(message);
        }
    }

    // ------------------ NUMBER VALIDATIONS ------------------

    /** Throws if number is null */
    public static void requireNonNull(Number value, String message) {
        if (value == null) {
            throw new BadRequestException(message);
        }
    }

    /** Throws if number is negative */
    public static void requireNonNegative(Number value, String message) {
        requireNonNull(value, message);
        if (value.doubleValue() < 0) {
            throw new BadRequestException(message);
        }
    }

    /** Throws if number is less than min or greater than max */
    public static void requireInRange(Number value, double min, double max, String message) {
        requireNonNull(value, message);
        double val = value.doubleValue();
        if (val < min || val > max) {
            throw new BadRequestException(message);
        }
    }

    // ------------------ OBJECT VALIDATIONS ------------------

    /** Throws if object is null */
    public static void requireNonNullObject(Object obj, String message) {
        if (obj == null) {
            throw new BadRequestException(message);
        }
    }
    public static boolean isNonNullObject(Object obj) {
        return obj != null;
    }
    public static boolean isNullObject(Object obj) {
        return obj == null;
    }

    /** Throws if collection is null or empty */
    public static void requireNonEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BadRequestException(message);
        }
    }
    public static void requireNonEmpty(List<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BadRequestException(message);
        }
    }

    public static boolean isListEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /** Throws if map is null or empty */
    public static void requireNonEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new BadRequestException(message);
        }
    }
}