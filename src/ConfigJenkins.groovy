public class ConfigJenkins {
    private static String registryURL = "192.168.100.173:5000";
    private static String sshCredentialId = "ssh-id";

    public static String getRegistryURL(){
        return registryURL;
    }
    
    public static String getSshCredentialId(){
        return sshCredentialId;
    }

    public static String getImagenRegistry(String nameContainer,String versionImagen){
        return registryURL+"/"+nameContainer+":"+versionImagen;
    }

     public static String getImagenRegistrywithout(String nameContainer){
        return registryURL+"/"+nameContainer;
    }
}