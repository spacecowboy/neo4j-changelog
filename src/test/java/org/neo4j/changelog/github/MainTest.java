package org.neo4j.changelog.github;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static junit.framework.TestCase.assertFalse;


public class MainTest {
    private Main main;
    private String user = "neo4j";
    private String repo = "neo4j";
    private Handler handler;

    @Before
    public void setUp() throws Exception {
        GitHubMockInterceptor mockedInterceptor = new GitHubMockInterceptor() {
            @Override
            protected String getBody(String url) throws Exception {
                File file = new File(getClass().getResource("github-pullrequests.json").toURI());
                return new String(Files.readAllBytes(file.toPath()));
            }
        };
        GitHubService service = GitHubService.GetService(GitHubService.API_URL, "", mockedInterceptor);
        handler = new Handler(service);

        main = new Main(handler);
    }

    @Test
    public void testGetChanges() throws IOException {
        // Issues and PRs should be merged
        List<Change> changes = main.getChanges(user, repo);

        assertFalse(changes.isEmpty());
    }
}
