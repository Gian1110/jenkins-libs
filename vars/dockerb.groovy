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

def dockerVersionContainer(Map params) {
    def remoteH = initial(params.remoteHost);
    def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,params.imagenVersion);
    def imageVersion = sshCommand remote: remoteH, command: "docker ps -a --format '{{.Image}}'| grep ${params.containerName}"
    imageVersion = imageVersion.split(":")[1]
    return  imageVersion == params.imageVersion;
}

def dockerBuildPush(Map params){
    def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,params.imagenVersion);
    dir (params.path) {
        sh " docker build -t ${nameImage} ."
    }
    sh """
        docker push ${nameImage}
        docker rmi ${nameImage}
      """                     
}

def dockerPull(Map params){
    def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,params.imagenVersion);
    def remoteH = initial(params.remoteHost);
    sshCommand remote: remoteH, command: "docker pull ${nameImage}"

}

def dockerRmRun(Map params) {
    def remoteH = initial(params.remoteHost);
    def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,params.imagenVersion);
    def DOCKER_EXIST = sshCommand remote: remoteH, command: "docker ps -a -q --filter name=${params.containerName}"

    if (DOCKER_EXIST != ''){
        sshCommand remote: remoteH, command: "docker rm -f ${params.containerName}"
    }               
    sshCommand remote: remoteH, command: "docker run -d -p ${params.containerPuert}:80 --name ${params.containerName} ${nameImage}"
}