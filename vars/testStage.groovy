@Grab('org.codehaus.groovy:groovy-astbuilder:3.0.23')
import groovy.transform.Field

@Field String STEP_NAME = getClass().getName()
@Field String STAGE_NAME = getClass().getName()

def call(Map args = [:]) {
   env.testVar = 'successfully' 
}
