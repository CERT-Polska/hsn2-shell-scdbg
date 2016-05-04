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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import pl.nask.hsn2.ResourceException;

public class ScdbgWrapperMock implements ScdbgWrapper {
    Map<String, ScanResult> results = new HashMap<String, ScdbgWrapperMock.ScanResult>();

    public ScdbgWrapperMock() {
        try {
            addResult("b.pdf", "0xb4cf", "0xad89", "0xadb6", "0xadb8", "0xadcf", "0xc16d", "0xc78d", "0xc797");
            addResult("file-with-dump", "0x012c");
            setDump("file-with-dump", "0x012c", "/tmp/file-with-dump.unpack");
            addResult("file-with-urls", "0x003b");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDump(String filename, String offset, String dumpPath) {
        getResult(filename).hasDumpFile(offset, dumpPath);
    }

    private void addResult(String filename, String... offset) throws IOException {
        String output = IOUtils.toString(getClass().getResourceAsStream("/" + filename + "-output.txt"));
        ScanResult r = new ScanResult(output);
        for (String o: offset) {
            r.addOffset(o, IOUtils.toString(getClass().getResourceAsStream("/" + filename + "-" + o + "-output.txt")));
        }
        results.put(filename, r);
    }

    @Override
    public InputStream executeForOffsets(String filename, File localTemp) throws ResourceException {

        return getResult(filename).getOutput();
    }

    private ScanResult getResult(String filename) {
        File f = new File(filename);

        if (!results.containsKey(f.getName())) {
            throw new IllegalArgumentException("unknown file: " + f.getName());
        }

        return results.get(f.getName());
    }

    @Override
    public InputStream executeWithOffset(String filename, File graphFile, String offset, File localTemp) throws ResourceException {
        try {
            ScanResult res = getResult(filename);

            String f = res.getDumpFile(offset);
            if (f != null)
                createDumpFile(f);
            return res.getOutput(offset);
        } catch (IOException e) {
            throw new ResourceException("Could not create file", e);
        }
    }

    private void createDumpFile(String f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(new byte[2000]);
        fos.close();
    }

    private class ScanResult {
        private String output;
        private Map<String, String> offsets = new HashMap<String, String>();
        private Map<String, String> offsetsWithDumpFiles = new HashMap<String, String>();

        public ScanResult(String output) {
            this.output = output;
        }

        void addOffset(String offset, String outputForOffset) {
            offsets.put(offset, outputForOffset);
        }

        InputStream getOutput() {
            return IOUtils.toInputStream(output);
        }

        InputStream getOutput(String offset) {
            if (!offsets.containsKey(offset)) {
                throw new IllegalArgumentException("offset not recognized: " + offset);
            }
            return IOUtils.toInputStream(offsets.get(offset));
        }

        String getDumpFile(String offset) {
            return offsetsWithDumpFiles.get(offset);
        }

        void hasDumpFile(String offset, String pathToFile) {
            offsetsWithDumpFiles.put(offset, pathToFile);
        }
    }

	@Override
	public void setMemoryLimit(int memoryLimitInMb) {
	}
}
