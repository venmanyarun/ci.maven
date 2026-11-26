/*******************************************************************************
 * (c) Copyright IBM Corporation 2026.
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
package net.wasdev.wlp.test.dev.it;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Verifies that dev mode finds the server start message (CWWKF0011I) when the
 * server output directory is relocated with WLP_OUTPUT_DIR in server.env.
 * See https://github.com/OpenLiberty/ci.maven/issues/2151
 */
public class DevCustomOutputDirectoryTest extends BaseDevTest {

    private static final String CUSTOM_OUTPUT_DIR_NAME = "custom-server-output";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        setUpBeforeClass(null, "../resources/basic-dev-project", true, false, null, null);

        // Relocate the server output directory (logs, workarea) away from <userDirectory>/servers
        File customOutputDir = new File(tempProj, "target/" + CUSTOM_OUTPUT_DIR_NAME);
        File serverEnv = new File(tempProj, "src/main/liberty/config/server.env");
        String content = "WLP_OUTPUT_DIR=" + customOutputDir.getCanonicalPath().replace('\\', '/') + "\n";
        Files.write(serverEnv.toPath(), content.getBytes(StandardCharsets.UTF_8));

        // startProcess verifies that CWWKF0011I shows up, and fails if dev mode never finds it
        startProcess(null, true);
    }

    @AfterClass
    public static void cleanUpAfterClass() throws Exception {
        BaseDevTest.cleanUpAfterClass();
    }

    @Test
    public void serverLogsAreInCustomOutputDirectory() throws Exception {
        File customMessagesLog = new File(targetDir, CUSTOM_OUTPUT_DIR_NAME + "/defaultServer/logs/messages.log");
        assertTrue("messages.log was not created in the custom output directory: " + customMessagesLog.getAbsolutePath(),
                customMessagesLog.exists());

        File defaultMessagesLog = new File(targetDir, "liberty/wlp/usr/servers/defaultServer/logs/messages.log");
        assertFalse("messages.log should not exist in the default server directory: " + defaultMessagesLog.getAbsolutePath(),
                defaultMessagesLog.exists());
    }

    @Test
    public void devModeFindsServerStartedMessage() throws Exception {
        assertFalse("Dev mode timed out waiting for the server start message: " + getLogTail(),
                verifyLogMessageExists("The server has not started within", 1000));
    }
}
