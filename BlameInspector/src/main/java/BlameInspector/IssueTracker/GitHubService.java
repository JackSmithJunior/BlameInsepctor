package BlameInspector.IssueTracker;

import BlameInspector.VCS.VersionControlService;
import BlameInspector.VCS.VersionControlServiceException;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;


public class GitHubService extends IssueTrackerService {

    private Issue issue;
    private IssueService issueService;
    private Repository repository;
    private CommitService commitService;

    private static final String GITHUB_SEARCH_EMAIL_URL = "https://api.github.com/search/users?q={0}+in:email";



    public GitHubService(final String userName,
                         final String password,
                         final String repositoryOwner,
                         final String repositoryName) throws IOException {
        super(userName, password, repositoryOwner, repositoryName);
        ISSUE_URL = "https://github.com/{0}/{1}/issues/";
        ASSIGNEE_URL = "https://github.com/{0}";
        this.ISSUE_URL = MessageFormat.format(ISSUE_URL, repositoryOwner, repositoryName);
        GitHubClient client = new GitHubClient();
        client.setCredentials(userName, password);
        this.issueService = new IssueService(client);
        RepositoryService repositoryService = new RepositoryService(client);
        repository = repositoryService.getRepository(repositoryOwner, repositoryName);
        commitService = new CommitService(client);
        this.numberOfTickets = repositoryService.getRepository(repositoryOwner, repositoryName).getOpenIssues();
    }


    @Override
    public String getIssueBody(final int issueNumber) throws IOException {
        issue = issueService.getIssue(repositoryOwner, repositoryName, issueNumber);
        return issue.getBody();
    }

    @Override
    public void setIssueAssignee(final String blameLogin) throws IOException, JSONException {
        User blamedUser = new User();
        issue.setAssignee(blamedUser.setLogin(blameLogin));
        issueService.editIssue(repositoryOwner, repositoryName, issue);
    }


    public String getUserLogin(final VersionControlService vcs, final String file, final int number) throws IOException,
            JSONException,
            VersionControlServiceException,
            IssueTrackerException {
        String commitId;
        try {
            commitId = vcs.getBlamedUserCommit(file, number);
        } catch (Exception e) {
            throw new VersionControlServiceException(e);
        }
        try {
            String login = commitService.getCommit(repository, commitId).getAuthor().getLogin();
            return login;
        } catch (IOException | NullPointerException e) {
            throw new IssueTrackerException(e, "Can not get data for commit!");
        }
    }

    private String getUserByEmail(final String blamedUserEmail) throws IOException, JSONException {
        String email = blamedUserEmail.split("@")[0];
        String url = MessageFormat.format(GITHUB_SEARCH_EMAIL_URL, blamedUserEmail);

        String result = getRequest(url, null);

        JSONObject searchResult = new JSONObject(result.replaceAll(", ", ", \\"));
        JSONArray items = searchResult.getJSONArray("items");

        return items.getJSONObject(0).getString("login");
    }

    @Override
    public void refresh() {
        issue = null;
    }
}
