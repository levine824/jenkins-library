def call(Map args) {
    pipeline {
        commonPipeline {
            stages {
                stage('Init') {
                    steps {
                        pipelineStageInit script: args.script, configFile: args.configFile
                    }
                }

                stage('Checkout') {
                    steps {
                        pipelineStageCheckout
                    }
                }

                stage('UnitTest'){

                }

                stage('CodeScan'){

                }

                stage('Compile'){

                }

                stage('Package'){

                }

                stage('Deploy'){

                }

                stage('Auto_test'){

                }

                stage('Final'){

                }
            }
        }

    }
}
