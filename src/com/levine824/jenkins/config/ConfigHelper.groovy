package com.levine824.jenkins.config

import java.util.regex.Matcher
import java.util.regex.Pattern

class ConfigHelper {
    private ConfigLoader loader

    private Script step
    private String stepName

    //private Set parameterKeys

    private Map<String, Set> configKeys

    ConfigHelper(ConfigLoader loader, Script step) {
        this.loader = loader
        this.step = step
        this.stepName = step.getProperty('STEP_NAME')
        this.configKeys = step.getProperties()
                .findAll { it.key.toString().endsWith('_CONFIG_KEYS') }
                .collectEntries { [it.key: (Set) it.value] }
    }

    @Override
    Object getProperty(String property) {
        try {
            return super.getProperty(property)
        } catch (MissingPropertyException mpe) {
            return configKeys.get(property)
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
                String type = matcher.group(1).toLowerCase()
                Map typeConfig = getConfig(config, type) as Map
                return getConfig(typeConfig, configKeys.get(type + '_CONFIG_KEYS'))
            } else {
                throw mme
            }
        }
    }


    Map getStepConfig() {
        return getConfig(config, 'step', stepName) as Map
    }

    Object getConfig(String... keys) {

    }
}