def call(){
    //def branchEnv = "${BRANCH_NAME.split("/")[1]}";
    //def envbranch = ConfigJenkins.getBranchEnvRunning(branchEnv);
               
    def yamlContent = libraryResource 'jenkins-agent-pod.yaml'
    return yamlContent;
}