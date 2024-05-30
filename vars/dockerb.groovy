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
                
        env.REMOTE_HOST_JSON = groovy.json.JsonOutput.toJson(remoteHost);
    }  
}