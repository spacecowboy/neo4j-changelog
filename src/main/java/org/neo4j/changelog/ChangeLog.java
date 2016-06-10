package org.neo4j.changelog;


import org.eclipse.jgit.lib.Ref;
import org.neo4j.changelog.git.GitHelper;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ChangeLog {

    private final Map<String, Map<String, List<Main.Change>>> versions;
    private final List<Ref> tags;
    private final List<String> subHeaders;
    private String catchAllSubHeader = "Misc";

    public ChangeLog(@Nonnull List<Ref> tags, @Nonnull List<String> subHeaders) {
        this.subHeaders = subHeaders;
        this.tags = tags;
        this.tags.sort(Util::SemanticCompare);
        versions = new HashMap<>();
    }

    public void addToChangeLog(@Nonnull Main.Change change) {
        String version = change.getVersion();
        String subheader = getSubHeaderFor(change);

        Map<String, List<Main.Change>> headers = defaultGet(versions, version, HashMap::new);
        defaultGet(headers, subheader, ArrayList::new).add(change);
    }

    @Nonnull
    private String getSubHeaderFor(@Nonnull Main.Change change) {
        List<String> labels = change.getLabels();

        for (String subHeader: subHeaders) {
            if (labels.contains(subHeader)) {
                return subHeader;
            }
        }
        return catchAllSubHeader;
    }

    public void write(@Nonnull File file) {
    }

    private <T> T defaultGet(@Nonnull Map<String, T> map, @Nonnull String key, @Nonnull Supplier<T> supplier) {
        if (!map.containsKey(key)) {
            map.put(key, supplier.get());
        }
        return map.get(key);
    }
}
