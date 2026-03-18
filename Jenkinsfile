node{
    stage('Load Jenkinsfile'){
        checkout([ $class: 'GitSCM', branches: [[name: "CONDO-243"]], doGenerateSubmoduleConfigurations: false,
                   extensions: [[ $class: 'RelativeTargetDirectory', relativeTargetDir: 'Jenkinsfile']],
                   submoduleCfg: [],
                   userRemoteConfigs: [[ credentialsId: 'github-api-token',
                                         url: 'https://github.com/farhanfaiz-wl/concerto-devops.git']]
                 ])
        jenkinsfile = load 'Jenkinsfile/jenkins/kube-gradle-deployment.pipeline'
    }
}
