package com.levine824.jenkins

class Validator {
    private Map rules = [:]

    Validator() {
        registerDefaultRules()
    }

    Validator registerRule(String name, Closure c) {
        this.rules[name] = c
        return this
    }

    Map validate(Map variables, Map validations) {
        Map errors = [:]
        validations.each { varName, expr ->
            try {
                String[] rules = expr?.toString()?.split(/,/)
                rules.each { rule ->
                    String[] parts = rule?.split(/=/)
                    String ruleName = parts[0]
                    String args = parts.size() > 1 ? parts[1] : null
                    Closure closure = this.rules[ruleName] as Closure
                    closure.call(varName, variables[varName], args)
                }
            } catch (IllegalArgumentException e) {
                errors[varName] = e.message
            }
        }
        return errors
    }

    private registerDefaultRules() {
        registerRule('required') { name, value, _ ->
            if (value == null) {
                throw new IllegalArgumentException("${name} is required")
            }
        }
    }
}
