package BlameInspector.VCS;


import java.util.HashMap;

public abstract class VersionControlService {

    protected HashMap<String, String> filesInRepo;
    protected String repositoryURL;

    public abstract String getBlamedUserCommit(String fileName, int lineNumber) throws Exception;
    public abstract String getBlamedUserEmail(String fileName, int lineNumer) throws Exception;

    public abstract String getRepositoryOwner();

    public boolean containsFile(final String fileName){
        return filesInRepo.containsKey(fileName);
    }
}
