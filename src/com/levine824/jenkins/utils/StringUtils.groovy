package com.levine824.jenkins.utils

import java.util.regex.Pattern

class StringUtils {

    static String toEnvKey(String s, String separator = '_') {
        if (s == null || s.isEmpty()) return s
        if (separator == null || separator.isEmpty()) {
            throw new IllegalArgumentException('Separator cannot be null or empty')
        }
        StringBuilder sb = new StringBuilder()
        // Track if the last character added was a separator
        boolean lastCharWasSep = false
        for (int i = 0; i < s.length(); i++) {
            char currentChar = s.charAt(i)
            if (Character.isUpperCase(currentChar)) {
                if (i > 0 && !Character.isUpperCase(s.charAt(i - 1))) {
                    sb.append(separator)
                    lastCharWasSep = true
                }
                sb.append(Character.toLowerCase(currentChar))
            } else if (Character.isLetterOrDigit(currentChar)) {
                sb.append(currentChar)
                lastCharWasSep = false
            } else if (!lastCharWasSep) {
                sb.append(separator)
                lastCharWasSep = true
            }
        }
        String escapeSep = Pattern.quote(separator)
        String envVar = sb.toString().toUpperCase()
        // Remove leading or trailing separators
        envVar = envVar.replaceAll(/^${escapeSep}+|${escapeSep}+$/, '')
        // Remove consecutive separators
        envVar.replaceAll(/${escapeSep}+/, separator)
    }
}