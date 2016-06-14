package org.neo4j.changelog.github;


import java.util.List;

public class Issue {
    public int number;
    public String title;
    public String body;
    public String html_url;
    public List<Label> labels;

    public static class Label {
        public String name;
    }
}
