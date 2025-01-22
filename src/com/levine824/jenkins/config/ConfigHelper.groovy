package com.levine824.jenkins.config

import java.util.regex.Matcher
import java.util.regex.Pattern

class ConfigHelper {
    private ConfigLoader loader

    private Script step
    private String stepName

    //private String stageName
    //private Set parameterKeys

    private Map<String, Set> configKeysMap

    ConfigHelper(ConfigLoader loader, Script step) {
        this.loader = loader
        this.step = step
        this.stepName = step.getProperty('STEP_NAME')
        this.configKeysMap = step.getProperties()
                .findAll { it.key.toString().endsWith('_CONFIG_KEYS') }
                .collectEntries { key, value -> [(key): (Set) value] }
    }

    @Override
    Object getProperty(String property) {
        try {
            return super.getProperty(property)
        } catch (MissingPropertyException mpe) {
            return configKeysMap.get(property)
        }
    }

    @Override
    Object invokeMethod(String name, Object args) {
        try {
            return super.invokeMethod(name, args)
        } catch (MissingMethodException mme) {
            String regex = "get(.*)Config"
            Matcher matcher = Pattern.compile(regex).matcher(name)
            if (matcher.matches()) {
                return getConfig(matcher.group(1).toLowerCase())
            } else {
                throw mme
            }
        }
    }

    Map getAllConfig() {
        Map map = getStepConfig()
        configKeysMap.keySet().each { map.putAll(getConfig(it.replaceAll('_CONFIG_KEYS', "").toLowerCase())) }
        return map
    }

    Map getStepConfig() {
        return loader.getConfig('step', stepName) as Map
    }

    private Map getConfig(String type) {
        return loader.getConfig(type, (Set) getProperty(type.toUpperCase() + '_CONFIG_KEYS'))
    }

}