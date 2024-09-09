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

    build job: "${params.jobName}", parameters: [
              string(name: "remoteHost", value: params.remoteHost),
              string(name: "imagenVersion", value: params.tag)
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
   def GIT_CREDENTIALS = 'git-token'
   def salida = ""
    withCredentials([string(credentialsId: GIT_CREDENTIALS, variable: 'GIT_TOKEN')]) {
                    
                    salida = "https://${GIT_TOKEN}@github.com/Gian1110/jenkins.git"
                    
                    sh """ 
                        git add -A
                        git commit -m "release/v${releaseVersion}" 
                        git push -f ${salida} main
                        git checkout -b release/v${releaseVersion}
                        git push ${salida}
                    """
                    }
    
}

def cleanImagenSsh(Map params) {
    def remoteH = dockerb.initial(params.remoteHost);
    
    // Obtener las imágenes en uso y unificarlas en una sola línea separada por saltos de línea      | sed 's/,$//'
    def usedImages = sshCommand remote: remoteH, command: """
    docker ps | grep ${params.imagen} | awk '{print \$2}'  | tr '\\n' ','
    """
    echo "${usedImages.trim().replaceAll(",", "\\n")}"
    // Eliminar las imágenes que no están en uso
    // sshCommand remote: remoteH, command: """
    // docker images --format '{{.Repository}}:{{.Tag}}' | grep 'node' | grep -v -F -f <(echo "${usedImages.trim().replaceAll(",", "\\n")}") | xargs -r docker rmi
    // """
}