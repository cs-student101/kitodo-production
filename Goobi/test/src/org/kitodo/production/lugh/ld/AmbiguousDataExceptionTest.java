/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General private License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.lugh.ld;

import org.junit.Test;

public class AmbiguousDataExceptionTest {

    @Test(expected = AmbiguousDataException.class)
    public void testAmbiguousDataExceptionCanBeThrown() throws AmbiguousDataException {
        throw new AmbiguousDataException();
    }

}