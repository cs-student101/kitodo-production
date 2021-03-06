/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.services.dataformat;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;

public class MetsServiceIT {

    /**
     * Tests loading a workpiece from a METS file.
     */
    @Test
    public void testReadXML() throws Exception {
        MetsXmlElementAccessInterface workpiece = MetsService.getInstance().createMetsXmlElementAccess();
        workpiece.read(new FileInputStream(new File("../Kitodo-DataFormat/src/test/resources/meta.xml")));

        // METS file has 183 associated images
        assertEquals(183, workpiece.getFileGrp().size());

        // all pages are linked to the root element
        assertEquals(workpiece.getFileGrp().size(), workpiece.getStructMap().getAreas().size());

        // root node has 16 children
        assertEquals(16, workpiece.getStructMap().getChildren().size());

        // root node has 11 meta-data entries
        assertEquals(11, workpiece.getStructMap().getMetadata().size());

        // file URIs can be read
        assertEquals(new URI("images/ThomPhar_644901748_media/00000001.tif"),
            workpiece.getFileGrp().get(0).getAllUsesWithFLocats().iterator().next().getValue().getUri());

        // pagination can be read
        assertEquals(
            Arrays.asList("uncounted", "uncounted", "uncounted", "uncounted", "[I]", "[II]", "[III]", "[IV]", "V", "VI",
                "[VII]", "[VIII]", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
                "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32",
                "33", "34", "35", "36", "37", "38", "39", "[40]", "uncounted", "uncounted", "41", "42", "43", "44",
                "45", "46", "uncounted", "uncounted", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56",
                "[57]", "[58]", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "[72]",
                "73", "74", "[75]", "[76]", "[77]", "[78]", "79", "80", "uncounted", "uncounted", "uncounted",
                "uncounted", "81", "82", "uncounted", "uncounted", "83", "84", "uncounted", "uncounted", "85", "86",
                "[87]", "uncounted", "[88]", "uncounted", "89", "90", "91", "92", "uncounted", "[93]", "[94]",
                "uncounted", "95", "96", "uncounted", "uncounted", "97", "uncounted", "[98]", "uncounted", "99", "100",
                "uncounted", "uncounted", "uncounted", "uncounted", "101", "102", "103", "104", "105", "106",
                "uncounted", "uncounted", "107", "108", "109", "[110]", "111", "112", "uncounted", "uncounted",
                "uncounted", "uncounted", "113", "114", "115", "116", "117", "118", "uncounted", "uncounted", "119",
                "120", "uncounted", "uncounted", "121", "122", "123", "124", "125", "126", "127", "128", "129", "130",
                "131", "132", "133", "134", "uncounted", "uncounted", "uncounted"),
            workpiece.getFileGrp().stream().map(FileXmlElementAccessInterface::getOrderlabel)
                    .collect(Collectors.toList()));
    }
}
