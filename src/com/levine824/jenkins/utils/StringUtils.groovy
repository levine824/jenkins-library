package com.levine824.jenkins.utils

import java.util.regex.Pattern

/**
 * A utility class providing static methods for common string manipulations,
 * particularly focused on escaping regular expressions and converting strings
 * into environment variable key formats.
 */
class StringUtils {

    /**
     * Escapes a regular expression string to be treated as a literal pattern.
     * This ensures special characters in the regex are interpreted literally.
     *
     * @param str The input regular expression to escape. If null or empty, returns null.
     * @return The escaped regex as a literal pattern, or null if input is null/empty.
     * @see java.util.regex.Pattern#quote(String)
     */
    static String escape(String str) {
        if (!str) return null
        return Pattern.quote(str)
    }

    /**
     * Converts a string to a standardized environment variable key format:
     * - Converts camelCase to UPPER_CASE_WITH_SEPARATOR
     * - Replaces non-alphanumeric characters with the specified separator
     * - Removes leading/trailing separators and collapses consecutive separators
     * - Output is always uppercase
     *
     * @param str The input string to convert. Returns null if input is null.
     * @param separator The separator used to replace special characters and camelCase.
     *                  Defaults to "_". Cannot be null/empty.
     * @return The formatted environment variable key, or original value for null/empty input.
     * @throws IllegalArgumentException if separator is null or empty.
     *
     * @example
     * toEnvKey("myApp.config") → "MYAPP_CONFIG"
     * toEnvKey("myApp.config", "-") → "MYAPP-CONFIG"
     * toEnvKey("MyHTTPResponse", "_") → "MY_HTTP_RESPONSE"
     */
    static String toEnvKey(String str, String separator = "_") {
        if (str == null || str.isEmpty()) {
            return str
        }
        if (separator == null || separator.isEmpty()) {
            throw new IllegalArgumentException("Separator cannot be null or empty")
        }
        StringBuilder sb = new StringBuilder()
        // Track if the last character added was a separator
        boolean lastCharWasSep = false
        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i)
            if (Character.isUpperCase(currentChar)) { // Handle uppercase letters (camel case)
                if (i > 0 && !Character.isUpperCase(str.charAt(i - 1))) {
                    sb.append(separator)
                    lastCharWasSep = true
                }
                sb.append(Character.toLowerCase(currentChar))
            } else if (Character.isLetterOrDigit(currentChar)) { // Handle lowercase letters or digits
                sb.append(currentChar)
                lastCharWasSep = false
            } else if (!lastCharWasSep) { // Handle other characters (e.g., dots, spaces)
                sb.append(separator)
                lastCharWasSep = true
            }
        }
        String escapedSep = escape(separator)
        String envVar = sb.toString().toUpperCase()
        // Remove leading or trailing separators
        envVar = envVar.replaceAll(/^${escapedSep}+|${escapedSep}+$/, '')
        // Remove consecutive separators
        envVar = envVar.replaceAll(/${escapedSep}+/, separator)
        return envVar
    }
}