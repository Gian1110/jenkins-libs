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

def editPush(Map params) {
    def releaseVersion = params.releaseVersion
    def pathJson = params.pathJson
    //edit
    sh """
        sed -i 's#"picking":"[0-9.]*"#"picking":"${params.pickingVersion}"#' ${pathJson}
        sed -i 's#"cac":"[0-9.]*"#"cac":"${params.cacVersion}"#' ${pathJson}
        sed -i 's#"checkout":"[0-9.]*"#"checkout":"${params.checkoutVersion}"#' ${pathJson}
        sed -i 's#"hub":"[0-9.]*"#"hub":"${params.hubVersion}"#' ${pathJson}
        sed -i 's#"login":"[0-9.]*"#"login":"${params.loginVersion}"#' ${pathJson}
        sed -i 's#"tools":"[0-9.]*"#"tools":"${params.toolsVersion}"#' ${pathJson}
        sed -i 's#"facturacion":"[0-9.]*"#"facturacion":"${params.facturacionVersion}"#' ${pathJson}
        sed -i 's#"printer":"[0-9.]*"#"printer":"${params.printerVersion}"#' ${pathJson}
        sed -i 's#"front":"[0-9.]*"#"front":"${params.frontVersion}"#' ${pathJson}
    """
    //git push
    sh """
        git checkout main
        git add -A
        git commit -m "release/v${releaseVersion}"
        git push
        git checkout -b release/v${releaseVersion}
    """
}
