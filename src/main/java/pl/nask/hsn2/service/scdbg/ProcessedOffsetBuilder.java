/*
 * Copyright (c) NASK, NCSC
 *
 * This file is part of HoneySpider Network 2.1.
 *
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.nask.hsn2.service.scdbg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import pl.nask.hsn2.ResourceException;

public class ProcessedOffsetBuilder {
    private static final Pattern DUMP_PATTERN = Pattern.compile("Change found at .* dumping to (.*)");

    private StringBuilder outputBuilder = new StringBuilder();
    private Offset offset;
    private File graphFile;
    private File dumpFile;
    private File localTempDir;
    private List<String> outgoingUrls = new ArrayList<String>();

    public ProcessedOffsetBuilder(Offset offset, File localTempDir) {
        this.offset = offset;
        this.localTempDir = localTempDir;
    }

    public final void setGraphFile(File graphFile) {
        this.graphFile = graphFile;
    }

    public final void setDumpFile(File dumpFile) {
        this.dumpFile = dumpFile;
    }

    public final ProcessedOffset build() {
        return new ProcessedOffset(offset, outputBuilder.toString(), graphFile, dumpFile, outgoingUrls);
    }

    public final void handleOutputLine(String line) throws ResourceException {
        tryToMatchDumpPattern(line);
        tryToMatchUrlPattern(line);

        outputBuilder.append(line).append("\n");
    }

    private void tryToMatchUrlPattern(String line) {
        String l = line.toLowerCase();
        int start = l.indexOf("http");
        if (start == -1) {
            start = l.indexOf("ftp");
        }

        if (start != -1) {
            int end = l.lastIndexOf(", ");
            if (end == -1) {
                end = l.length();
            }

            outgoingUrls.add(line.substring(start, end));
        }
    }

    private void tryToMatchDumpPattern(String line) throws ResourceException {
        Matcher matcher = DUMP_PATTERN.matcher(line);
        if (matcher.matches()) {
            try {
                String originalDumpFilePath = matcher.group(1);

                File originalDumpFile = new File(originalDumpFilePath);
                FileUtils.copyFileToDirectory(originalDumpFile, localTempDir);
                dumpFile = new File(localTempDir, originalDumpFile.getName());
            } catch (IOException e) {
                throw new ResourceException("Could not preserve dump file", e);
            }
        }
    }

}
