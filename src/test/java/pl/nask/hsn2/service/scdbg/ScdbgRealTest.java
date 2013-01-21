/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.0.
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pl.nask.hsn2.ResourceException;

@Test(enabled=false)
public class ScdbgRealTest {

	private File tmpDir;
	private File bPdfFile;
	private File tPdfFile;
	private File urlsFile;
	// path to scdbg elf 
	private String scdbgPath = "/opt/hsn2/shell-scdbg/lib/scdbg";
	private ScdbgWrapper wrapper;
	private ScdbgTool tool;
	private ScdbgResultBuilder builder;

	@BeforeClass
	public void prepareTestFiles() throws FileNotFoundException, IOException {
		tmpDir = new File("testTmp");
		Assert.assertTrue(tmpDir.exists() || tmpDir.mkdir());
		bPdfFile = prepareTestFile("b.pdf");
		tPdfFile = prepareTestFile("sf86.pdf");
		urlsFile = prepareTestFile("file-with-urls");
		wrapper = new ScdbgLinuxBinaryWrapper(scdbgPath,"");
		tool = new ScdbgTool(wrapper, tmpDir);
	}

	private File prepareTestFile(String filename) throws FileNotFoundException, IOException {
		InputStream input = null;
		FileOutputStream fos = null;
		try {
			File f = new File(tmpDir, filename);
			fos = new FileOutputStream(f); 
			input = this.getClass().getResourceAsStream("/" + filename);
			Assert.assertNotNull(input);
			IOUtils.copy(input, fos);
			return f;
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(fos);
		}
	}

	@AfterClass
	public void removeTestFiles() throws IOException {
		if (tmpDir != null)
			FileUtils.deleteDirectory(tmpDir);
	}

	@BeforeMethod
	public void prepareBuilder() {
		builder = new ScdbgResultBuilder();
	}

	public void scanForOffsetsTest() throws ResourceException {
		tool.scanForOffsets(builder, bPdfFile.getAbsolutePath());
		ScdbgToolResult res = builder.build();

		Assert.assertEquals(res.getNumberOfShellcodes(), 8);
	}

	public void scanOffsetBasicTest() throws ResourceException {
		tool.scanOffset(builder, bPdfFile.getAbsolutePath(), new Offset("0xb4cf", "40c538"));

		ScdbgToolResult res = builder.build();
		Assert.assertEquals(res.getProcessedOffsets().size(), 1);
		ProcessedOffset offset = res.getProcessedOffsets().get(0);
		Assert.assertTrue(offset.getOutput().length() > 1000);
		Assert.assertTrue(offset.getOutput().indexOf("\n") >= 0 && offset.getOutput().indexOf("\n") != offset.getOutput().lastIndexOf("\n"));
		Assert.assertEquals(offset.getOffsetAsInt(), 0xb4cf); //46287
	}

	public void scanOffsetWithGraphFile() throws ResourceException {
		tool.scanOffset(builder, bPdfFile.getAbsolutePath(), new Offset("0xb4cf", "40c538"));

		ScdbgToolResult res = builder.build();
		ProcessedOffset offset = res.getProcessedOffsets().get(0);

		Assert.assertTrue(offset.hasGraphFile());
		Assert.assertTrue(offset.getGraphFile() != null);
	}


	public void scanWithUrls() throws ResourceException, IOException {
		ScdbgToolResult res = tool.runWithFile(urlsFile.getAbsolutePath());

		Assert.assertEquals(res.getNumberOfShellcodes(), 1);
		Assert.assertEquals(res.getOutgoingUrls().size(), 1);
		Assert.assertEquals(res.getOutgoingUrls().get(0), "http://user1.12-26.net/bak.css");
	}

	public void scanBpdfTest() throws IOException, ResourceException {
		ScdbgToolResult res = tool.runWithFile(bPdfFile.getAbsolutePath());
		Assert.assertEquals(res.getNumberOfShellcodes(), 8);
		Assert.assertEquals(res.getProcessedOffsets().size(), 8);
		Assert.assertEquals(res.getOutgoingUrls().size(), 0);
	}

	public void scanTpdfTest() throws IOException, ResourceException {        
		try {
			tool.runWithFile(tPdfFile.getAbsolutePath());
			Assert.fail(ResourceException.class + " expected to be thrown here");
		} catch (ResourceException e) {
			// expected
		}
	}

}
