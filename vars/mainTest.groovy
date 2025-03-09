import com.levine824.jenkins.PipelineContext
import com.levine824.jenkins.config.ConfigLoader
import com.levine824.jenkins.utils.MapUtils
import groovy.transform.Field

@Field String STEP_NAME = 'mainTest'
@Field Set GENERAL_CONFIG_PATHS = [
        'projectName',
        'url'
]
@Field Set STAGE_CONFIG_PATHS = [
        'build.maven.command'
]

def main() {
    String config1 = '''
general:
  projectName: 'jenkins-library'
  url:
    - baidu
    - google
stage:
  build:
    maven:
      command: 'mvn clean package'
step:
  mainTest: 
    result: success
'''
    String config2 = '''
general:
  url:
    - github
'''
    // 测试代码
    def config = new ConfigLoader().load([config1, config2])
    def ctx = new PipelineContext(this, config as Map)
    println(ctx.getEnv(this))
    //println(MapUtils.getByPath(config,'step.mainTest[0][1]'))
}

main()