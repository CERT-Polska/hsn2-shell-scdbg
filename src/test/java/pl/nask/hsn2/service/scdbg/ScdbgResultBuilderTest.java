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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class ScdbgResultBuilderTest {
    private ScdbgResultBuilder builder;

    @BeforeMethod
    public void prepareBuilder() {
        builder = new ScdbgResultBuilder();
    }

    @Test
    public void tryToMatchOffsetPattern1() {
        tryToMatchOffsetPattern("4) offset= 0xad89       steps=2313       final_eip= 40be41", true);
    }

    @Test
    public void tryToMatchOffsetPattern2() {
        tryToMatchOffsetPattern("3) offset=0xc797       steps=MAX    final_eip=40d819", true);
    }

    @Test
    public void tryToMatchOffsetPatterBad() {
        tryToMatchOffsetPattern("3) offset= =0xc797       steps=MAX    final_eip=40d819", false);
    }

    private void tryToMatchOffsetPattern(String input, boolean matches) {
        Assert.assertEquals(builder.tryToMatchOffsetPattern(input), matches);
    }
}
