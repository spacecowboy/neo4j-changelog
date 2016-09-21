package org.neo4j.changelog;

import org.eclipse.jgit.lib.Ref;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some utilities
 */
public class Util {
    public static boolean isSameMajorMinorVersion(@Nonnull String v1, @Nonnull String v2) {
        if (v1.startsWith("v")) {
            v1 = v1.substring(1);
        }
        if (v2.startsWith("v")) {
            v2 = v2.substring(1);
        }

        String[] first = v1.split("\\.");
        String[] second = v2.split("\\.");

        return first.length >= 2 && second.length >= 2 && first[0].equals(second[0]) && first[1].equals(second[1]);
    }

    public static boolean versionLiesBetween(@Nonnull String version, @Nonnull String from, @Nonnull String to) {
        if (SemanticCompare(from, to) > 0) {
            throw new IllegalArgumentException("From must be Less or Equal than To");
        }
        return SemanticCompare(version, from) >= 0 && SemanticCompare(version, to) <= 0;
    }

    /**
     * Indicate sort order of two Refs according to semantic versioning rules
     */
    public static int SemanticCompare(@Nonnull Ref ref1, @Nonnull Ref ref2) {
        return SemanticCompare(getTagName(ref1), getTagName(ref2));
    }

    /**
     * Indicate sort order of two versions according to semantic versioning rules.
     *
     * v1.0.0 is considered equivalent to 1.0.0
     */
    public static int SemanticCompare(@Nonnull String semanticVersion1, @Nonnull String semanticVersion2) {
        SemanticVersion v1;
        try {
            v1 = asSemanticVersion(semanticVersion1);
        } catch (IllegalArgumentException e) {
            v1 = null;
        }

        SemanticVersion v2;
        try {
            v2 = asSemanticVersion(semanticVersion2);
        } catch (IllegalArgumentException e) {
            v2 = null;
        }

        if (v1 == null && v2 == null) {
            throw new IllegalArgumentException("At least ONE of the arguments must be a semantic version");
        } else if (v1 == null) {
            return 1;
        } else if (v2 == null) {
            return -1;
        } else {
            return v1.compareTo(v2);
        }
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
    public static Comparator<Ref> SemanticComparator(Pattern pattern) {
        //return Util::SemanticCompare;
        return (ref1, ref2) -> {
            Matcher m1 = pattern.matcher(Util.getTagName(ref1));
            if (!m1.matches()) {
                throw new IllegalArgumentException("Could not compare order for: " + Util.getTagName(ref1));
            }
            Matcher m2 = pattern.matcher(Util.getTagName(ref2));
            if (!m2.matches()) {
                throw new IllegalArgumentException("Could not compare order for: " + Util.getTagName(ref2));
            }

            return Util.SemanticCompare(m1.group(1), m2.group(1));
        };
    }

    @Nonnull
    public static SemanticVersion asSemanticVersion(@Nonnull String version) throws IllegalArgumentException {
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
