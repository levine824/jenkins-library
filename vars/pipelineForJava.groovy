def call(Map args) {
    pipeline {
        commonPipeline {
            stages {
                stage('Init') {
                    steps {
                        pipelineStageInit script: args.script, configFile: args.configFile
                    }
                }


            }
        }

    }
}
