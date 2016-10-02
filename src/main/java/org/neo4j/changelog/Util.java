package org.neo4j.changelog;

import org.eclipse.jgit.lib.Ref;
import org.neo4j.changelog.git.GitHelper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Some utilities
 */
public class Util {

@Nonnull
    public static String formatChangeText(@Nonnull String msg, @Nonnull List<String> additions) {
        return formatChangeText(msg, additions.toArray(new String[additions.size()]));
    }

    @Nonnull
    public static String formatChangeText(@Nonnull String msg, @Nonnull String... firstLineAdditions) {
        // In case of multiple lines
        String[] lines = msg.trim().split("\n");

        String firstLine = lines[0];
        // Indent rest of the lines
        String rest = Arrays.stream(lines).skip(1).reduce("", (r, l) -> {
            if (r.isEmpty()) {
                return "    " + l;
            }
            return String.join("\n    ", r, l);
        });

        String changeText = firstLine;

        for (String addition: firstLineAdditions) {
            changeText = String.join(" ", changeText.trim(), addition);
        }

        // Join first line and rest with paragraph space
        return String.join("\n\n", changeText, rest).trim();
    }

    @Nonnull
    public static Comparator<Ref> getGitRefSorter(GitHelper gitHelper) {
        return (ref1, ref2) -> {
            try {
                if (gitHelper.getCommitFromString(ref1.getName()).equals(gitHelper.getCommitFromString(ref2.getName()))) {
                    return 0;
                } else if (gitHelper.isAncestorOf(ref1.getName(), ref2.getName())) {
                    return -1;
                } else {
                    return 1;
                }
            } catch (IOException | NullPointerException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Nonnull
    private static String[] splitSemanticVersion(@Nonnull String semanticVersion) {
        int metaDataStart = !semanticVersion.contains("+") ? semanticVersion.length() : semanticVersion.indexOf("+");
        int start = 0;
        if (semanticVersion.toLowerCase().startsWith("v")) {
            start = 1;
        }
        return semanticVersion.substring(start, metaDataStart).split("[.|-]");
    }

    @Nonnull
    public static String getTagName(@Nonnull Ref ref) {
        int i = ref.getName().lastIndexOf("/");
        return ref.getName().substring(i + 1);
    }

    @Nonnull
    static SemanticVersion asSemanticVersion(@Nonnull String version) throws IllegalArgumentException {
        String[] parts = splitSemanticVersion(version);

        try {
            SemanticVersion sv = new SemanticVersion();

            sv.major = Integer.parseInt(parts[0]);

            if (parts.length > 1) {
                sv.minor = Integer.parseInt(parts[1]);
            }

            if (parts.length > 2) {
                sv.patch = Integer.parseInt(parts[2]);
            }

            if (parts.length > 3) {
                sv.label = parts[3];
            }

            if (parts.length > 4) {
                sv.labelNumber = Integer.parseInt(parts[4]);
            }

            if (parts.length > 5) {
                throw new IllegalArgumentException("Not a semantic version");
            }

            return sv;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a semantic version");
        }
    }

    public static boolean isSemanticVersion(@Nonnull String version) {
        try {
            asSemanticVersion(version);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class SemanticVersion implements Comparable<SemanticVersion> {
        int major = -1;
        int minor = -1;
        int patch = -1;
        String label = "";
        int labelNumber = -1;

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public int getPatch() {
            return patch;
        }

        public String getLabel() {
            return label;
        }

        public int getLabelNumber() {
            return labelNumber;
        }

        @Override
        public int compareTo(SemanticVersion v) {
            int comp;

            comp = Integer.compare(major, v.major);
            if (comp != 0) {
                return comp;
            }

            comp = Integer.compare(minor, v.minor);
            if (comp != 0) {
                return comp;
            }

            comp = Integer.compare(patch, v.patch);
            if (comp != 0) {
                return comp;
            }

            // Special case, no label should be sorted after non-empty label
            if (label.isEmpty() && !v.label.isEmpty()) {
                return 1;
            } else if (!label.isEmpty() && v.label.isEmpty()) {
                return -1;
            } else {
                comp = label.compareTo(v.label);
                if (comp != 0) {
                    return comp;
                }
            }

            comp = Integer.compare(labelNumber, v.labelNumber);
            return comp;
        }
    }
}
