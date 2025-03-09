package com.levine824.jenkins

class Validator {
    private Map rules = [:]

    Validator() {
        register('required') { field, value, _ ->
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("${field} is required")
            }
        }
    }

    Validator register(String name, Closure rule) {
        rules[name] = rule
        return this
    }

    Map validate(Map vars, Map validations) {
        Map errors = [:]
        validations.each { varName, expressions ->
            try {
                expressions.each { expr ->
                    String[] arr = expr?.toString()?.split(/:/, 2)
                    String name = arr[0]
                    String args = arr.size() > 1 ? arr[1] : null
                    (rules[name] as Closure)?.call(varName, vars[varName], args)
                }
            } catch (IllegalArgumentException e) {
                errors[varName] = e.message
            }
        }
        return errors
    }
}
