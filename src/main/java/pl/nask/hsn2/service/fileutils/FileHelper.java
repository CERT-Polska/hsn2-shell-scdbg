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

package pl.nask.hsn2.service.fileutils;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);

	private FileHelper() {}

    public static File createTempDir() throws IOException {
       return createTempDir(null);
    }

    public static File createTempDir(File parentTmp) throws IOException {
        File tmpFile = File.createTempFile("tmp", "", parentTmp);
        File tmpMainDir = tmpFile.getParentFile();
        String tmpDirname = tmpFile.getName() + ".d";
        if (!tmpFile.delete()) {
        	LOGGER.warn("Cannot delete temp file: {}", tmpFile.getName());
        }
        File tmpDir = new File(tmpMainDir, tmpDirname);
        if (!tmpDir.mkdir()) {
        	LOGGER.error("Cannot create temp dir! {}", tmpDir.getName());
        	throw new IllegalStateException("Cannot create temporary directory");
        }

        return tmpDir;
    }

}
