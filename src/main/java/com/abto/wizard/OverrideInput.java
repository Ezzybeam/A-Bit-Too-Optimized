package com.abto.wizard;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Parses user-typed hardware override fields. Blank input means "no override"
 * (empty result), not an error. Invalid or non-positive input is also treated as
 * empty so a typo never corrupts the config. Pure logic, unit tested.
 */
public final class OverrideInput {

    private OverrideInput() {
    }

    public static OptionalLong parsePositiveLong(String raw) {
        if (raw == null) {
            return OptionalLong.empty();
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return OptionalLong.empty();
        }
        try {
            long value = Long.parseLong(s);
            return value > 0 ? OptionalLong.of(value) : OptionalLong.empty();
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    public static OptionalInt parsePositiveInt(String raw) {
        if (raw == null) {
            return OptionalInt.empty();
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return OptionalInt.empty();
        }
        try {
            int value = Integer.parseInt(s);
            return value > 0 ? OptionalInt.of(value) : OptionalInt.empty();
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public static Optional<String> cleanGpuName(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String s = raw.trim();
        return s.isEmpty() ? Optional.empty() : Optional.of(s);
    }
}
