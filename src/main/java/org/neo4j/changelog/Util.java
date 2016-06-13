package org.neo4j.changelog;

import org.eclipse.jgit.lib.Ref;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Random;

/**
 * Some utilities
 */
public class Util {
    public static @Nonnull String randomHex() {
        Random r = new Random();
        return Integer.toHexString(r.nextInt(Integer.MAX_VALUE));
    }

    /**
     * Indicate sort order of two Refs according to semantic versioning rules
     */
    public static int SemanticCompare(Ref ref1, Ref ref2) {
        return SemanticCompare(getTagName(ref1), getTagName(ref2));
    }

    /**
     * Indicate sort order of two versions according to semantic versioning rules.
     *
     * v1.0.0 is considered equivalent to 1.0.0
     */
    public static int SemanticCompare(String semanticVersion1, String semanticVersion2) {
        String[] semanticVersion1Parts = splitSemanticVersion(semanticVersion1);
        String[] semanticVersion2Parts = splitSemanticVersion(semanticVersion2);

        //compare major versions
        int majorComparison = Integer.compare(Integer.parseInt(semanticVersion1Parts[0]),
                Integer.parseInt(semanticVersion2Parts[0]));
        if (majorComparison != 0) {
            return majorComparison;
        }

        if (semanticVersion1Parts.length > 1 && semanticVersion2Parts.length > 1) {
            //compare minor versions
            int minorComparison = Integer.compare(Integer.parseInt(semanticVersion1Parts[1]),
                    Integer.parseInt(semanticVersion2Parts[1]));
            if (minorComparison != 0) {
                return minorComparison;
            }
        } else if (semanticVersion1Parts.length > semanticVersion2Parts.length) {
            return 1;
        } else if (semanticVersion1Parts.length < semanticVersion2Parts.length) {
            return -1;
        }

        if (semanticVersion1Parts.length > 2 && semanticVersion2Parts.length > 2) {
            //compare patches
            int patchComparison = Integer.compare(Integer.parseInt(semanticVersion1Parts[2]),
                    Integer.parseInt(semanticVersion2Parts[2]));
            if (patchComparison != 0) {
                return patchComparison;
            }
        } else if (semanticVersion1Parts.length > semanticVersion2Parts.length) {
            return 1;
        } else if (semanticVersion1Parts.length < semanticVersion2Parts.length) {
            return -1;
        }

        if (semanticVersion1Parts.length > 3 && semanticVersion2Parts.length > 3) {
            //compare labels
            int labelComparison = semanticVersion1Parts[3].compareTo(semanticVersion2Parts[3]);
            if (labelComparison != 0) {
                return labelComparison;
            }
        } else if (semanticVersion1Parts.length < semanticVersion2Parts.length) {
            return 1;
        } else if (semanticVersion1Parts.length > semanticVersion2Parts.length) {
            return -1;
        }

        if (semanticVersion1Parts.length > 4 && semanticVersion2Parts.length > 4) {
            //compare beta number
            int patchComparison = Integer.compare(Integer.parseInt(semanticVersion1Parts[4]),
                    Integer.parseInt(semanticVersion2Parts[4]));
            if (patchComparison != 0) {
                return patchComparison;
            }
        } else if (semanticVersion1Parts.length > semanticVersion2Parts.length) {
            return 1;
        } else if (semanticVersion1Parts.length < semanticVersion2Parts.length) {
            return -1;
        }

        // Will not check more parts
        return 0;
    }

    private static String[] splitSemanticVersion(String semanticVersion) {
        int metaDataStart = !semanticVersion.contains("+") ? semanticVersion.length() : semanticVersion.indexOf("+");
        int start = 0;
        if (semanticVersion.toLowerCase().startsWith("v")) {
            start = 1;
        }
        return semanticVersion.substring(start, metaDataStart).split("[.|-]");
    }

    public static String getTagName(Ref ref) {
        int i = ref.getName().lastIndexOf("/");
        return ref.getName().substring(i + 1);
    }

    public static Comparator<Ref> SemanticComparator() {
        return Util::SemanticCompare;
    }
}
