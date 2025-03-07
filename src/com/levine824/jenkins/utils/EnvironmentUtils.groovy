package com.levine824.jenkins.utils

import java.util.regex.Pattern

class EnvironmentUtils {
    private static final Pattern CAMEL_CASE_PATTERN = ~/(?<=[a-z0-9])(?=[A-Z])/
    private static final Pattern NON_ALPHANUMERIC_PATTERN = ~/[^a-zA-Z0-9]+/

    static List toEnvVars(Map map) {
        map?.collect { k, v ->
            "${toEnvKey(k.toString())}=${toEnvValue(v)}".toString()
        } ?: []
    }

    static String toEnvKey(String str, String separator = '_') {
        if (!str) return str
        if (!separator) {
            throw new IllegalArgumentException('Separator cannot be null or empty')
        }
        String escapedSep = Pattern.quote(separator)
        str.replaceAll(CAMEL_CASE_PATTERN, separator)
                .replaceAll(NON_ALPHANUMERIC_PATTERN, separator)
                .replaceAll(/^${escapedSep}+|${escapedSep}+$/, '')
                .toUpperCase()
    }

    static String toEnvValue(Object obj, String separator = ',') {
        if (obj == null) return ''
        switch (obj) {
            case List:
                return obj.join(separator)
            case Map:
                return obj.collect { k, v -> "${k}:${v}" }.join(separator)
            default:
                return obj.toString()
        }
    }
}
