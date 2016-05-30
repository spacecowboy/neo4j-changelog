package org.neo4j.changelog.github;

public class PullRequest {
    public int number;
    public String title;
    public String body;
    public String html_url;
    public String merged_at;
    public String merge_commit_sha;
    public Base base;

    public boolean isMerged() {
        return merged_at != null;
    }

    public static class Base {
        public String ref;
        public String sha;
    }
}
