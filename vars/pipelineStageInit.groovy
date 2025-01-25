import com.levine824.jenkins.config.ConfigHelper
import com.levine824.jenkins.config.ConfigLoader
import groovy.transform.Field

@Field String STEP_NAME = getClass().getName()

def call(Map args) {
    def defaultYaml = libraryResource 'com/levine824/jenkins/config/config.yml'

    def loader
    if (!args.configFile) {
        def yaml = readFile args.configFile
        loader = ConfigLoader.load(defaultYaml, yaml)
    } else {
        loader = ConfigLoader.load(defaultYaml)
    }
    def ctx = new ConfigHelper(loader, args.script as Script)
    env.CONTEXT = ctx
}
