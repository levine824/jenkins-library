package com.levine824.jenkins.config


import java.util.regex.Matcher
import java.util.regex.Pattern

class ConfigHelper {
    private static final String STEP_PROPERTY_SUFFIX = "_CONFIG_KEYS"

    private ConfigLoader loader

    private Script step
    private String stepName

    private Map<String, Set> properties

    ConfigHelper(ConfigLoader loader, Script step) {
        this.loader = loader
        this.step = step
        this.stepName = step.getProperty('STEP_NAME')
        this.properties = step.getProperties()
                .findAll { it.key.toString().endsWith(STEP_PROPERTY_SUFFIX) }
                .collectEntries { key, value -> [(key): (Set) value] }
    }

    @Override
    Object getProperty(String property) {
        try {
            return super.getProperty(property)
        } catch (MissingPropertyException mpe) {
            return properties.get(property)
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

    Map getStepConfig() {
        Map map = loader.get(ConfigType.STEP, stepName) as Map
        properties.keySet().each { str ->
            map.putAll(getConfig(str.replaceAll(STEP_PROPERTY_SUFFIX, "").toLowerCase()))
        }
        return map
    }

    private Map getConfig(String type) {
        return loader.get(type, (Set) getProperty(type.toUpperCase() + STEP_PROPERTY_SUFFIX))
    }

}