package org.neo4j.changelog;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class ChangeLogTest {

    @Test
    public void whenTagEqualsVersion() throws Exception {
        ChangeLog cl = new ChangeLog(Arrays.asList(v1),
                "1.0.0",
                Arrays.asList("Kernel", "Cypher"));

        cl.addToChangeLog(simpleChange(3, "Kernel", "Fix the kernel [#3]"));
        cl.addToChangeLog(simpleChange(2, "Changelog", "Added a changelog [#2]"));
        cl.addToChangeLog(simpleChange(4, "Cypher", "Add a query language [#4]"));
        cl.addToChangeLog(simpleChange(1, "Kernel", "Add a kernel [#1]"));

        StringWriter sw = new StringWriter();

        cl.writeTo(sw);

        assertEquals(SIMPLE_CHANGELOG, sw.toString());
    }

    @Test
    public void simpleChangeLogGeneration() throws Exception {
        ChangeLog cl = new ChangeLog(Arrays.asList(v1),
                Arrays.asList("Kernel", "Cypher"));

        cl.addToChangeLog(simpleChange(3, "Kernel", "Fix the kernel [#3]"));
        cl.addToChangeLog(simpleChange(2, "Changelog", "Added a changelog [#2]"));
        cl.addToChangeLog(simpleChange(4, "Cypher", "Add a query language [#4]"));
        cl.addToChangeLog(simpleChange(1, "Kernel", "Add a kernel [#1]"));

        StringWriter sw = new StringWriter();

        cl.writeTo(sw);

        assertEquals(SIMPLE_CHANGELOG, sw.toString());
    }

    private Change simpleChange(int number, @Nonnull String label, @Nonnull String changeText) {
        return new Change() {
            @Override
            public int getSortingNumber() {
                return number;
            }

            @Nonnull
            @Override
            public List<String> getLabels() {
                return Arrays.asList(label);
            }

            @Nonnull
            @Override
            public String getVersion() {
                return "1.0.0";
            }

            @Override
            public String toString() {
                return changeText;
            }
        };
    }

    private static final String SIMPLE_CHANGELOG =
            "\n### 1.0.0\n\n" +
            "\n#### Kernel\n\n" +
            "- Add a kernel [#1]\n" +
            "- Fix the kernel [#3]\n" +
            "\n#### Cypher\n\n" +
            "- Add a query language [#4]\n" +
            "\n#### Misc\n\n" +
            "- Added a changelog [#2]\n";

    Ref v1 = new Ref() {
        @Override
        public String getName() {
            return "1.0.0";
        }

        @Override
        public boolean isSymbolic() {
            return false;
        }

        @Override
        public Ref getLeaf() {
            return null;
        }

        @Override
        public Ref getTarget() {
            return null;
        }

        @Override
        public ObjectId getObjectId() {
            return null;
        }

        @Override
        public ObjectId getPeeledObjectId() {
            return null;
        }

        @Override
        public boolean isPeeled() {
            return false;
        }

        @Override
        public Storage getStorage() {
            return null;
        }
    };
}
