package org.neo4j.changelog;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A general entry in a changelog
 */
public interface Change extends Comparable {

    @Nonnull
    List<String> getLabels();

    @Nonnull
    String getVersion();

    @Override
    default int compareTo(@Nonnull Object o) {
        if (!(o instanceof Change)) {
            throw new IllegalArgumentException("Don't know how to compare against " + o.getClass());
        }

        // TODO, not sure if actually needed
        return 0;
    }
}
