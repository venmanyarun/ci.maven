/*******************************************************************************
 * (c) Copyright IBM Corporation 2018, 2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.wasdev.wlp.test.feature.it;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.util.Scanner;

public class InstallFeaturesServerTest extends BaseInstallFeature {


    static final String TOOLCHAIN_CONFIGURED_FOR_GOAL = "CWWKM4101I: The %s goal is using the configured toolchain JDK located at";

    @Test
    public void testInstalledFeatures() throws Exception {
        
        File f1 = new File(".libertyls");
        assertFalse(".libertyls directory should not exist", f1.exists());

        assertInstalled("appSecurityClient-1.0");
        assertNotInstalled("beanValidation-2.0");
        assertNotInstalled("couchdb-1.0");
        assertNotInstalled("distributedMap-1.0");

        Set<String> expectedFeatures = new HashSet<String>();
        expectedFeatures.add("ssl-1.0");
        expectedFeatures.add("appSecurityClient-1.0");
        
        assertTrue(buildLogCheckForInstalledFeatures("io.openliberty.tools.it:install-features-server-it", "The following features have been installed:", expectedFeatures));
        File buildLog = new File("../build.log");
        if (!buildLog.exists()) {
            buildLog = new File("../../build.log");
        }
        assertTrue(buildLog.exists());

        assertTrue("Did not find toolchain honored message for create goal in build.log", logContainsMessage(buildLog, String.format(TOOLCHAIN_CONFIGURED_FOR_GOAL, "install-feature")));
        assertTrue("Did not find toolchain passed message for installfeatureutil build.log", logContainsMessage(buildLog, "Passing toolchain JAVA_HOME to InstallFeatureUtil:"));
    }


    private boolean logContainsMessage( File logFile, String message) throws FileNotFoundException {

        assertTrue("Log file not found at location: "+ logFile.getPath(), logFile.exists());
        boolean found = false;

        try (Scanner scanner = new Scanner(logFile);) {
            while (scanner.hasNextLine()) {
                if(scanner.nextLine().contains(message)) {
                    found = true;
                }
            }
        }

        return found;
    }
}
