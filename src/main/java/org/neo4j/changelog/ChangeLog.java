package org.neo4j.changelog;


import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.lib.Ref;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ChangeLog {

    private static final String VERSION_FMT = "\n### %s\n\n";
    private static final String CATEGORY_FMT = "\n#### %s\n\n";
    private static final String CHANGE_FMT = "- %s\n";
    private final Map<String, Map<String, List<Change>>> versions = new HashMap<>();
    private final ArrayList<String> tags = new ArrayList<>();
    private final LinkedHashSet<String> categories = new LinkedHashSet<>();
    private String catchAllSubHeader = "Misc";

    public ChangeLog(@Nonnull List<Ref> tags, @Nonnull List<String> categories) {
        this(tags, null, categories);
    }

    public ChangeLog(@Nonnull List<Ref> tags, @Nullable String nextHeader, @Nonnull List<String> categories) {
        this.categories.addAll(categories);

        this.tags.addAll(tags.stream().map(Util::getTagName).collect(Collectors.toList()));
        if (nextHeader != null && !this.tags.contains(nextHeader)) {
            this.tags.add(0, nextHeader);
        }
    }

    public void addToChangeLog(@Nonnull Change change) {
        String version = change.getVersion();
        String subheader = getCategoryFor(change);

        Map<String, List<Change>> headers = defaultGet(versions, version, HashMap::new);
        defaultGet(headers, subheader, ArrayList::new).add(change);
    }

    @Nonnull
    private String getCategoryFor(@Nonnull Change change) {
        List<String> labels = change.getLabels();

        for (String category: categories) {
            if (labels.stream().anyMatch(s -> s.equalsIgnoreCase(category))) {
                return category;
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
        for (String version: tags) {
            Map<String, List<Change>> catMap = defaultGet(versions, version, HashMap::new);

            if (containsNoChanges(catMap)) {
                // Don't output versions with no changes
                continue;
            }

            w.write(String.format(VERSION_FMT, version));

            if (!categories.contains(catchAllSubHeader)) {
                categories.add(catchAllSubHeader);
            }

            for (String category: categories) {
                List<Change> changes = defaultGet(catMap, category, ArrayList::new);

                if (changes.isEmpty()) {
                    // Don't output categories with no changes
                    continue;
                }

                w.write(String.format(CATEGORY_FMT, category));

                changes.sort((c1, c2) -> {
                    if (c1.getSortingNumber() == c2.getSortingNumber()) {
                        return 0;
                    } else if (c1.getSortingNumber() < c2.getSortingNumber()) {
                        return -1;
                    } else {
                        return 1;
                    }
                });

                for (Change change: changes) {
                    w.write(String.format(CHANGE_FMT, change.toString()));
                }
            }
        }
    }

    private static boolean containsNoChanges(@Nonnull final Map<String, List<Change>> catMap) {
        for (String category: catMap.keySet()) {
            if (!catMap.getOrDefault(category, Collections.emptyList()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private <T> T defaultGet(@Nonnull Map<String, T> map, @Nonnull String key, @Nonnull Supplier<T> supplier) {
        if (!map.containsKey(key)) {
            map.put(key, supplier.get());
        }
        return map.get(key);
    }
}
