package org.neo4j.changelog.github;

import org.junit.Before;
import org.junit.Test;
import retrofit2.Response;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;


public class HandlerTest {


    private GitHubService service;
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
        service = GitHubService.GetService(GitHubService.API_URL, "", mockedInterceptor);
        handler = new Handler(service);
    }

    @Test
    public void getPullRequests() throws Exception {
        Response<List<PullRequest>> response = handler.getPullRequests("neo4j", "neo4j", 1);

        assertTrue(response.isSuccessful());

        List<PullRequest> pullRequests = response.body();

        assertFalse(pullRequests.isEmpty());
    }
}
