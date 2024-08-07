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
    def imageVersion = ""
    def imagenVersion = params.branchName.split("v")[1]
    def remoteH = initial(params.remoteHost)
    
    imageVersion = sshCommand remote: remoteH, command: "docker ps -a --format '{{.Image}}'| grep ${params.containerName} || true"
    if (imageVersion != '') {
        imageVersion = imageVersion.split(":")[2]
    }
    
      
    return  imageVersion == imagenVersion

}

def dockerBuildPush(Map params){
    def imagenVersion = params.branchName.split("v")[1]
    def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,imagenVersion);
    
    dir (params.path) {
        sh " docker build -t ${nameImage} ."
    }
    sh """
        docker push ${nameImage}
        docker rmi ${nameImage}
    """
}

def dockerPull(Map params){
    def imagenVersion = params.branchName.split("v")[1]
    def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,imagenVersion);

    def remoteH = initial(params.remoteHost);
    
    sshCommand remote: remoteH, command: "docker pull ${nameImage}"
}

def dockerRmRun(Map params) {
    try{
        def remoteH = initial(params.remoteHost);
        def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,params.imagenVersion);
        def DOCKER_EXIST = sshCommand remote: remoteH, command: "docker ps -a -q --filter name=${params.containerName}"

        if (DOCKER_EXIST != ''){
            sshCommand remote: remoteH, command: "docker rm -f ${params.containerName}"
        }               
        sshCommand remote: remoteH, command: "docker run -d -p ${params.containerPuert}:80 --name ${params.containerName} --hostname ${params.containerName} ${nameImage}"
    } catch(Exception e) {
        echo "${e}"
    }   
}

def dockerCompose(Map params) {
    try{
        def remoteH = initial(params.remoteHost);
        def DOCKER_EXIST = sshCommand remote: remoteH, command: "docker ps -a -q --filter name=${params.containerName}"
        
        if (DOCKER_EXIST != ''){
            sshCommand remote: remoteH, command: "docker compose -f ${params.pathYaml} down"
        }               
        sshCommand remote: remoteH, command: "docker compose -f ${params.pathYaml} up -d"
        
    } catch(Exception e) {
        echo "${e}"
    }   
}

def dockerEditYaml(Map params) {
    def imagenVersion = params.branchName.split("v")[1]
    def nameImage = ConfigJenkins.getImagenRegistry(params.containerName,imagenVersion);
    sshCommand remote: remoteH, command: "sed -i 's|image: .*|image: ${nameImage}|' ${params.pathYaml}"
}