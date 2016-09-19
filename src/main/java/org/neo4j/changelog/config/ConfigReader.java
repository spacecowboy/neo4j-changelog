package org.neo4j.changelog.config;

import com.electronwill.toml.Toml;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Deal with config related things
 */
public class ConfigReader {

    public static ProjectConfig parseConfig(@Nonnull String[] args) throws IOException, ArgumentParserException {
        Namespace ns = parseArgs(args);
        Map<String, Object> toml = readConfig(ns.getString("config"));
        return ProjectConfig.from(toml);
    }

    @Nonnull
    private static Namespace parseArgs(String[] args) throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("neo4j-changelog")
                .defaultHelp(true)
                .description("Generate changelog for the given project.");

        parser.addArgument("-ght", "--githubtoken")
                .help("GitHub Token (not required but heavily recommended)")
                .setDefault("");
        parser.addArgument("-ghu", "--githubuser")
                .help("Used to build the uri: github.com/user/repo")
                .setDefault("neo4j");
        parser.addArgument("-ghr", "--githubrepo")
                .help("Used to build the uri: github.com/user/repo")
                .setDefault("neo4j");
        parser.addArgument("-o", "--output")
                .help("Path to output file")
                .setDefault("CHANGELOG.md");
        parser.addArgument("-d", "--directory")
                .help("Path to local checked out git repo")
                .setDefault("./");
        parser.addArgument("-f", "--from")
                .help("Gitref from which the changelog is generated. For any tags to be included in the log, this commit must be reachable from them. (default: earliest commit in the log)");
        parser.addArgument("-t", "--to")
                .help("Gitref up to which the changelog is generated. Any tags included in the log must be reachable from this commit.")
                .required(true);
        parser.addArgument("-v", "--version-prefix")
                .dest("version-prefix")
                .help("Only include tags which match the specified version prefix. Example, 3.1 will include all 3.1.x tags but not 3.0.x tags.")
                .required(true);
        parser.addArgument("-n", "--next-header")
                .dest("next-header")
                .help("Any changes occurring after the latest tag will be placed under this header at the top in the log.")
                .setDefault("Unreleased")
                .required(false);
        parser.addArgument("-rl", "--required-label")
                .dest("required-label")
                .help("Only include PRs with this label")
                .required(false)
                .setDefault("");
        parser.addArgument("-c", "--config")
                .help("Path to config file")
                .setDefault("changelog.toml");
        parser.addArgument("category")
                .nargs("*")
                .help("Categories to sort changes under. These should match (case-insensitively) the tags of the GitHub issues. Will always include the catch-all category 'Misc'");

        try {
            return parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw e;
        }
    }

    @Nonnull
    static Map<String, Object> readConfig(@Nullable String configPath) throws IOException {
        if (configPath == null || configPath.isEmpty()) {
            configPath = "changelog.toml";
        }
        File configFile = new File(configPath);
        if (!configFile.isFile()) {
            return Toml.read("", true);
        }
        return Toml.read(configFile);
    }
/*
    public String getDirectory() {
        return args.getString("directory");
    }

    public String getFrom() {
        return args.getString("from");
    }

    public String getTo() {
        return args.getString("to");
    }

    public String getVersionPrefix() {
        return args.getString("version-prefix");
    }

    public String getGithubUser() {
        return args.getString("githubuser");
    }

    public String getGithubRepo() {
        return args.getString("githubrepo");
    }

    public String getGithubToken() {
        return args.getString("githubtoken");
    }

    public String getOutputPath() {
        return args.getString("output");
    }

    public String getGithubRequiredLabel() {
        return args.getString("required-label");
    }

    public List<String> getCategories() {
        return args.getList("category");
    }

    public String getNextHeader() {
        return args.getString("next-header");
    }*/
}
