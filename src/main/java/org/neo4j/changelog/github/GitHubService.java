package org.neo4j.changelog.github;

//import retrofit2.Call;
//import retrofit2.http.GET;
//import retrofit2.http.Query;

public interface GitHubService {
    @GET("users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);
}
