package com.levine824.jenkins.utils

class StringUtils {

    /**
     * Converts the specified string to the environment variable.
     *
     * @param str the string to be converted
     * @return the {@code String}, converted to the environment variable
     */
    static String toEnv(String str) {
        return str.replaceAll(/\.|-|_/, '_')
                .replaceAll(/([a-z])([A-Z])/, '$1_$2')
                .replaceAll(/([a-z\d])([A-Z])/, '$1_$2')
                .toUpperCase()
    }

    /**
     * Converts all of the characters in this String to lower case and
     * splits this string around matches of the given regular expression.
     *
     * @param str a {@code String}
     * @param regex the delimiting regular expression
     * @return the array of strings computed by splitting this string
     */
    static String[] toStringArray(String str, String regex) {
        return str.toLowerCase().split(regex)
    }

}
