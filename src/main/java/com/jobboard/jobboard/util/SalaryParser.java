package com.jobboard.jobboard.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalaryParser {
    private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)(?:k|K)?\\s*-\\s*(\\d+)(?:k|K)?");
    private static final Pattern SINGLE_VALUE_PATTERN = Pattern.compile("(\\d+)(?:k|K)?");
    private static final Pattern MIN_PATTERN = Pattern.compile("from\\s+(\\d+)(?:k|K)?");
    private static final Pattern MAX_PATTERN = Pattern.compile("up\\s+to\\s+(\\d+)(?:k|K)?");

    public static class SalaryRange {
        private final Double min;
        private final Double max;

        public SalaryRange(Double min, Double max) {
            this.min = min;
            this.max = max;
        }

        public Double getMin() {
            return min;
        }

        public Double getMax() {
            return max;
        }
    }

    public static SalaryRange parseSalary(String salaryStr) {
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            return new SalaryRange(null, null);
        }

        String normalized = salaryStr.toLowerCase().trim();

        // Try to match different salary patterns
        Matcher rangeMatcher = RANGE_PATTERN.matcher(normalized);
        if (rangeMatcher.find()) {
            double min = parseNumber(rangeMatcher.group(1));
            double max = parseNumber(rangeMatcher.group(2));
            return new SalaryRange(min, max);
        }

        Matcher minMatcher = MIN_PATTERN.matcher(normalized);
        if (minMatcher.find()) {
            double min = parseNumber(minMatcher.group(1));
            return new SalaryRange(min, null);
        }

        Matcher maxMatcher = MAX_PATTERN.matcher(normalized);
        if (maxMatcher.find()) {
            double max = parseNumber(maxMatcher.group(1));
            return new SalaryRange(null, max);
        }

        Matcher singleMatcher = SINGLE_VALUE_PATTERN.matcher(normalized);
        if (singleMatcher.find()) {
            double value = parseNumber(singleMatcher.group(1));
            return new SalaryRange(value, value);
        }

        return new SalaryRange(null, null);
    }

    private static double parseNumber(String numberStr) {
        try {
            double value = Double.parseDouble(numberStr);
            // If the number is less than 100, assume it's in thousands
            if (value < 100) {
                value *= 1000;
            }
            return value;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
} 