package de.gematik.demis.lvs.disease;

/*-
 * #%L
 * lifecycle-validation-service
 * %%
 * Copyright (C) 2025 - 2026 gematik GmbH
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
 * For additional notes and disclaimer from gematik and in case of changes by gematik,
 * find details in the "Readme" file.
 * #L%
 */

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.gematik.demis.lvs.common.destination.DestinationLookupServiceClient;
import de.gematik.demis.lvs.common.destination.NotificationCategoryDTO;
import de.gematik.demis.service.base.clients.mapping.CodeMappingService;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
class DiseaseNotificationLifecycleValidationCtrTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DestinationLookupServiceClient destinationLookupServiceClientMock;
  @MockitoBean private CodeMappingService codeMappingServiceMock;

  @Test
  void shouldCallDiseaseNotificationLifecycleValidationSrv() throws Exception {

    String notification =
        Files.readString(
            Path.of("src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V.json"));

    when(destinationLookupServiceClientMock.getNotificationCategory(anyString()))
        .thenReturn(new NotificationCategoryDTO("band"));

    when(codeMappingServiceMock.mapCode("band")).thenReturn("ban");

    mockMvc
        .perform(
            post("/disease/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isOk())
        .andExpect(content().string("S_FM_V2V"))
        .andReturn();
  }

  @Test
  void shouldReturnErrorForNotValid() throws Exception {

    String notification =
        Files.readString(
            Path.of(
                "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V_not_valid.json"));

    mockMvc
        .perform(
            post("/disease/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void shouldUseServiceBaseFunctionToMapPathogenCode() throws Exception {
    String notification =
        Files.readString(
            Path.of(
                "src/test/resources/notifications/disease/scenarioExamples/anonymous/S_FM_confirmed.json"));

    when(destinationLookupServiceClientMock.getNotificationCategory(
            "92d99f62-fe4f-4337-b833-351751db12dc"))
        .thenReturn(new NotificationCategoryDTO("band"));

    when(codeMappingServiceMock.mapCode("band")).thenReturn("ban");

    mockMvc
        .perform(
            post("/disease/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isOk())
        .andExpect(content().string("S_FM_Anonymous"))
        .andReturn();
  }
}
