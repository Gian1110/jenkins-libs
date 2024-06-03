def initial(String remoteHost){
    def sshId = ConfigJenkins.getSshCredentialId();
    withCredentials([usernamePassword(credentialsId: sshId, usernameVariable: 'sshUser', passwordVariable: 'sshpass')]){
    
        remoteH = [
        name: "pipe",
        host: remoteHost,
        user: "${sshUser}",
        password: "${sshpass}",
        allowAnyHosts: true
        ]

    } 
    return remoteH;
}


def dockerPull(Map params){
    String nameImage = ConfigJenkins.getImagenRegistry("iproyecto-qa","latest");
    def remoteH = initial(params.remoteHost);
    sshCommand remote: remoteH, command: "docker pull ${nameImage}"

}

def dockerRmRun(Map params) {
    def remoteH = initial(params.remoteHost);
    def DOCKER_EXIST = sshCommand remote: remoteH, command: "docker ps -a -q --filter name=${params.nameContainer}"

    if (DOCKER_EXIST != ''){
        sshCommand remote: remoteH, command: "docker rm -f ${params.nameContainer}"
    }               
    sshCommand remote: remoteH, command: "docker run -d -p ${params.puertoContainer}:${params.puertoContainer} --name ${params.nameContainer} ${params.nameImagen}"
}