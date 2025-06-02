package de.gematik.demis.lvs.disease;

/*-
 * #%L
 * lifecycle-validation-service
 * %%
 * Copyright (C) 2025 gematik GmbH
 * %%
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureObservability
// @TestPropertySource(locations = "classpath:application-test.properties")
class DiseaseNotificationLifecycleValidationCtrTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldCallDiseaseNotificationLifecycleValidationSrv() throws Exception {

    File file = new File("src/test/resources/notifications/disease/scenarioExamples/S_IM_V.json");
    FileInputStream inputStream = new FileInputStream(file);
    String notification = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    inputStream.close();

    mockMvc
        .perform(
            post("/disease/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isOk())
        .andExpect(content().json("[\"S_IM_V\",\"S_FM_V2V\"]"))
        .andReturn();
  }

  @Test
  void shouldReturnErrorForNotValid() throws Exception {

    File file =
        new File("src/test/resources/notifications/disease/scenarioExamples/S_IM_V_not_valid.json");
    FileInputStream inputStream = new FileInputStream(file);
    String notification = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    inputStream.close();

    mockMvc
        .perform(
            post("/disease/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isUnprocessableEntity());
  }
}
