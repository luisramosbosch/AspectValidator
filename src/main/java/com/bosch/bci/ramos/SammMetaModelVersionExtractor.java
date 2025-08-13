package com.bosch.bci.ramos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SammMetaModelVersionExtractor {

    // Regex to capture the full SAMM meta-model URN, including the version.
    // It handles cases like:
    // - <urn:samm:org.eclipse.esmf.samm:meta-model:2.2.0#>
    // - urn:samm:org.eclipse.esmf.samm:meta-model:2.2.0#
    // - "urn:samm:org.eclipse.esmf.samm:meta-model:2.2.0#"
    // The key part is the capturing group for the URN: (urn:samm:org.eclipse.esmf.samm:meta-model:[0-9]+\\.[0-9]+\\.[0-9]+#)
    private static final Pattern SAMM_META_MODEL_URN_PATTERN =
            Pattern.compile(".*?(urn:samm:org\\.eclipse\\.esmf\\.samm:meta-model:([0-9]+)\\.([0-9]+)\\.([0-9]+)#).*?");

    /**
     * Represents a parsed version number for comparison.
     */
    private static class Version implements Comparable<Version> {
        final int major;
        final int minor;
        final int patch;

        public Version(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public int compareTo(Version other) {
            if (this.major != other.major) {
                return Integer.compare(this.major, other.major);
            }
            if (this.minor != other.minor) {
                return Integer.compare(this.minor, other.minor);
            }
            return Integer.compare(this.patch, other.patch);
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }
    }


    /**
     * Reads a .ttl file, extracts all SAMM meta-model URNs, and returns the one
     * corresponding to the highest version found.
     *
     * @param ttlFilePath The path to the .ttl file.
     * @return An Optional containing the highest version SAMM meta-model URN if found,
     *         otherwise an empty Optional.
     * @throws IOException If an I/O error occurs reading the file.
     */
    public static Optional<String> getHighestSammMetaModelUrn(String ttlFilePath) throws IOException {
        if (!Files.exists(Paths.get(ttlFilePath))) {
            // Log this instead of printing to System.err in a library function
            // Logger.getLogger(SammMetaModelVersionExtractor.class.getName()).warning("File not found: " + ttlFilePath);
            return Optional.empty();
        }

        String highestUrnFound = null;
        Version highestVersionFound = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(ttlFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = SAMM_META_MODEL_URN_PATTERN.matcher(line);
                if (matcher.find()) {
                    // Group 1 is the full URN: urn:samm:org.eclipse.esmf.samm:meta-model:X.Y.Z#
                    String currentUrn = matcher.group(1);
                    // Groups 2, 3, 4 are major, minor, patch respectively
                    int major = Integer.parseInt(matcher.group(2));
                    int minor = Integer.parseInt(matcher.group(3));
                    int patch = Integer.parseInt(matcher.group(4));

                    Version currentVersion = new Version(major, minor, patch);

                    if (highestVersionFound == null || currentVersion.compareTo(highestVersionFound) > 0) {
                        highestVersionFound = currentVersion;
                        highestUrnFound = currentUrn;
                    }
                }
            }
        }

        return Optional.ofNullable(highestUrnFound);
    }
}
