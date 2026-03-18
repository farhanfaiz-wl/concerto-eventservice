node{
    stage('Load Jenkinsfile'){
        checkout([ $class: 'GitSCM', branches: [[name: "develop"]], doGenerateSubmoduleConfigurations: false,
                   extensions: [[ $class: 'RelativeTargetDirectory', relativeTargetDir: 'Jenkinsfile']],
                   submoduleCfg: [],
                   userRemoteConfigs: [[ credentialsId: 'github-api-token',
                                         url: 'https://github.com/wellnessliving-digital/concerto-devops.git']]
                 ])
        jenkinsfile = load 'Jenkinsfile/jenkins/kube-gradle-deployment.pipeline'
    }
}
