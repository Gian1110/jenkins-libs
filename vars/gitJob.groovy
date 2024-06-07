def checkoutBranch(Map params) {
    def releaseBranch = "release/v"+params.imagenVersion
    
    checkout([$class: 'GitSCM', 
                branches: [[name: releaseBranch]], 
                doGenerateSubmoduleConfigurations: false, 
                extensions: [], 
                userRemoteConfigs: [[
                    url: scm.getUserRemoteConfigs()[0].getUrl()
                ]]
    ])
}

def callJob(Map params) {
    def jsonData = readJSON file: params.pathJson
    def imageVersion = jsonData[params.jobName] 

    build job: "${params.jobName}", parameters: [
              string(name: "remoteHost", value: params.remoteHost),
              string(name: "imagenVersion", value: imageVersion)
            ]
}