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
    echo "llega ${imagenVersion}"
    imageVersion = sshCommand remote: remoteH, command: "docker ps -a --format '{{.Image}}'| grep ${params.containerName} || true"
    echo "${imageVersion}"
    if (imageVersion != '') {
        imageVersion = imageVersion.split(":")[2]
        echo "value ${imageVersion}"
        echo "value 2 ${imageVersion}"
        echo "value 3 ${params.imagenVersion}"
    }    
    return  imageVersion == params.imagenVersion

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

    def paramsexit = [:];
    paramsexit['remoteHost'] = params.remoteHost;
    paramsexit['path'] = params.pathYaml;
    
    if (!existFile(paramsexit)) {

        def paramscreate = [:];
        paramscreate['remoteHost'] = params.remoteHost
        paramscreate['pathYaml'] = params.pathYaml
        paramscreate['containerName'] = params.containerName
        paramscreate['containerPort'] = params.containerPort
        paramscreate['containerPortApp'] = params.containerPortApp
        paramscreate['pathLogHost'] = params.pathLogHost
        paramscreate['pathLogApp'] = params.pathLogApp
        paramscreate['pathAppsetting'] = params.pathAppsetting
        paramscreate['network'] = params.network
        createYaml(paramscreate)

    }

    def nameImage = "prueba:1"
    sshCommand remote: remoteH, command: "sed -i 's|image: .*|image: ${nameImage}|' ${params.pathYaml}"
}

def createYaml(Map params) {
    
    echo "no existe yaml, se crear"
    def fileYaml = libraryResource 'docker-compose.yaml';
    fileYaml = fileYaml.replace("#name#","${params.containerName}")
    fileYaml = fileYaml.replace("#portHost#","${params.containerPort}")
    fileYaml = fileYaml.replace("#portApp#","${params.containerPortApp}")
    fileYaml = fileYaml.replace("#pathLogHost#","${params.pathLogHost}")
    fileYaml = fileYaml.replace("#pathLogApp#","${params.pathLogApp}")
    fileYaml = fileYaml.replace("#pathAppsettingHost#","${params.pathAppsetting}")
    fileYaml = fileYaml.replace("#network#","${params.network}")

    def remoteH = initial(params.remoteHost);
    sshCommand remote: remoteH, command: """cat <<EOF > ${params.pathYaml} \n${fileYaml}\nEOF"""
    
}

def existFile(Map params){
    def path = params.path;
    def remoteH = initial(params.remoteHost);
    try {
        sshCommand remote: remoteH, command: "ls ${path}"
        return true 
    } catch (Exception e) {
        return false 
    }
    
}

def directoryTree(Map params){
    def paramsexit = [:];
    paramsexit['remoteHost'] = params.remoteHost;
    paramsexit['path'] = params.pathLogApp;
    
    if (!existFile(paramsexit)) {    
        def remoteH = initial(params.remoteHost);
        sshCommand remote: remoteH, command: "mkdir -p ${params.pathLogApp}"
    }
    
    paramsexit['path'] = "${params.pathAppsetting;}appsettings.json"
    if (existFile(paramsexit)) {    
        return true
    }
    def remoteH = initial(params.remoteHost);
    sshCommand remote: remoteH, command: "mkdir -p ${params.pathAppsetting}"
    echo "No existe un appsettings en ${params.pathAppsetting} para mapear"
    return false
}