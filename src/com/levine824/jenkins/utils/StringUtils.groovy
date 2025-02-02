package com.levine824.jenkins.utils

import java.util.regex.Pattern

/**
 * Provides utility methods for common string manipulations,
 * including regex escaping and environment variable key formatting.
 * <p>
 * This class contains static utility methods and should not be instantiated.
 *
 * @since 1.0
 */
class StringUtils {
    /**
     * Escapes a string for literal use in regular expressions
     * <p>
     * Uses {@link java.util.regex.Pattern#quote(String)} to ensure the string is treated as a literal,
     * preventing special characters from being interpreted as regex metacharacters.
     *
     * @param str The input string to escape (nullable or empty)
     * @return The regex-escaped literal string, or null if input is null/empty
     * @see java.util.regex.Pattern#quote(String)
     */
    static String escapeRegex(String str) {
        if (!str) return null
        return Pattern.quote(str)
    }

    /**
     * Converts a string to environment variable key format (uppercase with separator)
     * <p>
     * Conversion rules:
     * <ul>
     *   <li>Insert separator before camel-case uppercase letters (e.g., "myVar" → "MY_VAR")</li>
     *   <li>Replace non-alphanumeric characters with separator</li>
     *   <li>Collapse consecutive non-alphanumerics to single separator</li>
     *   <li>Remove leading/trailing separators</li>
     *   <li>Convert result to uppercase</li>
     * </ul>
     * Example: "my.config.key" → "MY_CONFIG_KEY" (with default "_" separator)
     *
     * @param str The input string (nullable or empty returns original value)
     * @param separator The separator character (non-null/non-empty), typically "_"
     * @return Formatted environment variable key string, or original value if input is null/empty
     * @throws IllegalArgumentException if separator is null or empty
     */
    static String toEnvKey(String str, String separator = "_") {
        if (str == null || str.isEmpty()) {
            return str
        }
        if (separator == null || separator.isEmpty()) {
            throw new IllegalArgumentException("Separator cannot be null or empty")
        }
        StringBuilder builder = new StringBuilder()
        // Track if the last character added was a separator
        boolean lastCharWasSep = false
        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i)
            if (Character.isUpperCase(currentChar)) { // Handle uppercase letters (camel case)
                if (i > 0 && !Character.isUpperCase(str.charAt(i - 1))) {
                    builder.append(separator)
                    lastCharWasSep = true
                }
                builder.append(Character.toLowerCase(currentChar))
            } else if (Character.isLetterOrDigit(currentChar)) { // Handle lowercase letters or digits
                builder.append(currentChar)
                lastCharWasSep = false
            } else if (!lastCharWasSep) { // Handle other characters (e.g., dots, spaces)
                builder.append(separator)
                lastCharWasSep = true
            }
        }
        String escapedSep = escapeRegex(separator)
        String envVar = builder.toString().toUpperCase()
        // Remove leading or trailing separators
        envVar = envVar.replaceAll(/^${escapedSep}+|${escapedSep}+$/, '')
        // Remove consecutive separators
        envVar = envVar.replaceAll(/${escapedSep}+/, separator)
        return envVar
    }
}
