package com.knightcode.appliedstoragesorter.application.result;

import java.util.List;
import java.util.Objects;

import net.minecraft.network.chat.Component;

public record SorterFeedbackResult(boolean success, List<Component> lines) {
    public SorterFeedbackResult {
        lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines must not be empty");
        }
    }

    public static SorterFeedbackResult failure(Component line) {
        return new SorterFeedbackResult(false, List.of(line));
    }

    public static SorterFeedbackResult failure(String message) {
        return new SorterFeedbackResult(false, List.of(Component.literal(message)));
    }

    public static SorterFeedbackResult success(List<Component> lines) {
        return new SorterFeedbackResult(true, lines);
    }

    public static SorterFeedbackResult success(Component line) {
        return new SorterFeedbackResult(true, List.of(line));
    }
}
