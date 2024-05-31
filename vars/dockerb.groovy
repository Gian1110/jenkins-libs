def initial(String remoteHost){
    def sshId = ConfigJenkins.getSshCredentialId();
    echo "${sshId}"
    withCredentials([usernamePassword(credentialsId: sshId, usernameVariable: 'sshUser', passwordVariable: 'sshpass')]){
    
        remoteH = [
        name: "pipe",
        host: remoteHost,
        user: "${sshUser}",
        password: "${sshpass}",
        allowAnyHosts: true
        ]

    }
    echo "en la funcion ${remoteH}" 
    return remoteH;
}

def dockerpull(Map params){
    def remoteH = initial(params.remoteHost);
    sshCommand remote: remoteH, command: "docker pull ${params.nameImagen}"

}
//