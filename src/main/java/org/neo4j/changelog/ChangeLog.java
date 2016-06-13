package org.neo4j.changelog;


import org.eclipse.jgit.lib.Ref;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ChangeLog {

    private static final String VERSION_FMT = "### %s\n";
    private static final String CATEGORY_FMT = "#### %s\n";
    private static final String CHANGE_FMT = "- %s\n";
    private final Map<String, Map<String, List<Change>>> versions;
    private final List<Ref> tags;
    private final List<String> subHeaders;
    private String catchAllSubHeader = "Misc";

    public ChangeLog(@Nonnull List<Ref> tags, @Nonnull List<String> subHeaders) {
        this.subHeaders = subHeaders;
        this.tags = tags;
        this.tags.sort(Util::SemanticCompare);
        versions = new HashMap<>();
    }

    public void addToChangeLog(@Nonnull Change change) {
        String version = change.getVersion();
        String subheader = getSubHeaderFor(change);

        Map<String, List<Change>> headers = defaultGet(versions, version, HashMap::new);
        defaultGet(headers, subheader, ArrayList::new).add(change);
    }

    @Nonnull
    private String getSubHeaderFor(@Nonnull Change change) {
        List<String> labels = change.getLabels();

        for (String subHeader: subHeaders) {
            if (labels.contains(subHeader)) {
                return subHeader;
            }
        }
        return catchAllSubHeader;
    }

    public void write(@Nonnull Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path)) {
            writeTo(w);
        }
    }

    void writeTo(@Nonnull Writer w) throws IOException {
        for (String version: versions.keySet()) {
            w.write(String.format(VERSION_FMT, version));

            for (String category: versions.get(version).keySet()) {
                w.write(String.format(CATEGORY_FMT, category));

                for (Change change: versions.get(version).get(category)) {
                    w.write(String.format(CHANGE_FMT, change.toString()));
                }
            }
        }
    }

    private <T> T defaultGet(@Nonnull Map<String, T> map, @Nonnull String key, @Nonnull Supplier<T> supplier) {
        if (!map.containsKey(key)) {
            map.put(key, supplier.get());
        }
        return map.get(key);
    }
}
