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
                        
            remote["name" : "pipe",
            "host":"${params.remoteHost}",
            "user" : "${sshUser}",
            "password" : "${sshpass}",
            "allowAnyHosts" :  true] 
            sshCommand remote: "${remote}", command: "docker ps"
    }
    
     
    return params;
}
//docker pull ${params.nameImagen}