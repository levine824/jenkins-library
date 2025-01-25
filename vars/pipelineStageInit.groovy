import com.levine824.jenkins.config.ConfigHelper
import com.levine824.jenkins.config.ConfigLoader
import groovy.transform.Field

@Field String STEP_NAME = getClass().getName()

def call(Script script, String configFile = '') {
    def defaultYaml = libraryResource 'com/levine824/jenkins/config/config.yml'

    def loader
    if (!configFile) {
        def yaml = readFile configFile
        loader = ConfigLoader.load(defaultYaml, yaml)
    } else {
        loader = ConfigLoader.load(defaultYaml)
    }
    def ctx = new ConfigHelper(loader, script)
    env.CONTEXT = ctx
}
