package com.levine824.jenkins.utils

class StringUtils {

    /**
     * Converts the specified string to the environment variable case.
     *
     * @param str the string to be converted
     * @param regex the regular expression which is replaced with underline
     * @return the converted {@code String}
     */
    static String toEnvCase(String str, String regex = "/\\.|-|_/") {
        return str.replaceAll(regex, '_')
                .replaceAll("/([a-z])([A-Z])/", "\$1_\$2")
                .replaceAll("/([a-z\\d])([A-Z])/", "\$1_\$2")
                .toUpperCase()
    }

}
