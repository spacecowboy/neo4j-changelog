package org.neo4j.changelog;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Test utilities
 */
public class Util {
    public static @Nonnull String randomHex() {
        Random r = new Random();
        return Integer.toHexString(r.nextInt(Integer.MAX_VALUE));
    }
}
