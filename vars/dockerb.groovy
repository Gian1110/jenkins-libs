def initial(String remoteHost){
    def sshId = ConfigJenkins.getSshCredentialId();
    withCredentials([usernamePassword(credentialsId: sshId, usernameVariable: 'sshUser', passwordVariable: 'sshpass')]){
                        
        remoteHost = [
            name: "pipe",
            host: remoteHost,
            user: "${sshUser}",
            password: "${sshpass}",
            allowAnyHosts: true,
        ]
                
        return remoteHost;
    }  
}

def dockerpull(Maps params){
    def remoteHost = initial(params.remoteHost);
    sshCommand remote: remoteHost, command: "docker pull ${params.nameImagen}";
}
