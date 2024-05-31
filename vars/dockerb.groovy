def initial(String remoteHost){
    def sshId = ConfigJenkins.getSshCredentialId();
    def remote = [:]

    withCredentials([usernamePassword(credentialsId: sshId, usernameVariable: 'sshUser', passwordVariable: 'sshpass')]){
                        
            remote["name"] = "pipe"
            remote["host"] = remoteHost
            remote["user"] = "${sshUser}"
            remote["password"] = "${sshpass}"
            remote["allowAnyHosts"] =  true

    }  
    return remote;
}

def dockerpull(Map params){
    //def remoteHost = initial(params.remoteHost);
    //echo "${remoteHost}"
    def sshId = ConfigJenkins.getSshCredentialId();
    def remote = [:]

    withCredentials([usernamePassword(credentialsId: sshId, usernameVariable: 'sshUser', passwordVariable: 'sshpass')]){
                        
            remote["name"] = "pipe"
            remote["host"] = params.remoteHost
            remote["user"] = "${sshUser}"
            remote["password"] = "${sshpass}"
            remote["allowAnyHosts"] =  true

    }
    echo "${remote}"  
    sshCommand remote: "${remote}", command: "docker pull ${params.nameImagen}"
    return params;
}
