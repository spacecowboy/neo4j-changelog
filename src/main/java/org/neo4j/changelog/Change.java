package org.neo4j.changelog;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A general entry in a changelog
 */
public interface Change {

    @Nonnull
    List<String> getLabels();

    @Nonnull
    String getVersion();
}
