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
    try{
        def remoteH = initial(params.remoteHost)
        def imageVersion = sshCommand remote: remoteH, command: "docker ps -a --format '{{.Image}}'| grep ${params.containerName}"
        imageVersion = imageVersion.split(":")[2]
    } catch(Exception e) {
        echo "${e}"
    }    
    return  imageVersion == params.imagenVersion

}

def dockerBuildPush(Map params){
    try{
        def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,params.imagenVersion);
        dir (params.path) {
            sh " docker build -t ${nameImage} ."
        }
        sh """
            docker push ${nameImage}
            docker rmi ${nameImage}
        """
     } catch(Exception e) {
        echo "${e}"
    }  
}

def dockerPull(Map params){
    try{
        def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,params.imagenVersion);
        def remoteH = initial(params.remoteHost);
        sshCommand remote: remoteH, command: "docker pull ${nameImage}"
    } catch(Exception e) {
        echo "${e}"
    }
}

def dockerRmRun(Map params) {
    try{
        def remoteH = initial(params.remoteHost);
        def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,params.imagenVersion);
        def DOCKER_EXIST = sshCommand remote: remoteH, command: "docker ps -a -q --filter name=${params.containerName}"

        if (DOCKER_EXIST != ''){
            sshCommand remote: remoteH, command: "docker rm -f ${params.containerName}"
        }               
        sshCommand remote: remoteH, command: "docker run -d -p ${params.containerPuert}:80 --name ${params.containerName} ${nameImage}"
    } catch(Exception e) {
        echo "${e}"
    }   
}