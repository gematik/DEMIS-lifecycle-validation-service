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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.gematik.demis.lvs.common.codemapping.FutsClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureObservability
@TestPropertySource(
    properties = {
      "feature.flag.fhirpath.validation.enabled=true",
    })
class DiseaseNotificationLifecycleValidationCtrTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FutsClient futsClientMock;

  @BeforeEach
  void setUp() {
    when(futsClientMock.getConceptMap(anyString())).thenReturn(new HashMap<>());
  }

  @Test
  void shouldCallDiseaseNotificationLifecycleValidationSrv() throws Exception {

    String notification =
        Files.readString(
            Path.of("src/test/resources/notifications/disease/scenarioExamples/S_IM_V.json"));

    mockMvc
        .perform(
            post("/disease/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isOk())
        .andExpect(content().string("S_IM_V"))
        .andReturn();
  }

  @Test
  void shouldReturnErrorForNotValid() throws Exception {

    String notification =
        Files.readString(
            Path.of(
                "src/test/resources/notifications/disease/scenarioExamples/S_IM_V_not_valid.json"));

    mockMvc
        .perform(
            post("/disease/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isUnprocessableEntity());
  }
}
