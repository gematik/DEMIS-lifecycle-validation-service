package de.gematik.demis.lvs.labnotification;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.gematik.demis.lvs.common.codemapping.LegacyCodeMappingService;
import de.gematik.demis.lvs.common.destination.DestinationLookupServiceClient;
import de.gematik.demis.lvs.common.destination.NotificationCategoryDTO;
import de.gematik.demis.service.base.clients.mapping.CodeMappingService;
import feign.FeignException;
import feign.Request;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
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
      "feature.flag.return.fhirpath.validation.in.responses=true",
      "feature.flag.codemapping.service.base=true",
      "lvs.client.futs.address=http://localhost:9999"
    })
class LaboratoryNotificationRestControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean DestinationLookupServiceClient destinationLookupServiceClientMock;

  @MockitoBean LegacyCodeMappingService legacyCodeMappingServiceMock;

  @MockitoBean CodeMappingService codeMappingServiceMock;

  @Test
  void shouldCallLaboratoryNotificationLifecycleValidationSrv() throws Exception {
    String notification =
        Files.readString(
            Path.of("src/test/resources/notifications/laboratory/scenarioExamples/S1.json"));

    byte[] body = new byte[] {};
    when(destinationLookupServiceClientMock.getNotificationCategory(anyString()))
        .thenThrow(
            new FeignException.FeignClientException(
                404,
                "mocked Exception for S1 Test",
                mock(Request.class),
                body,
                Collections.emptyMap()));

    mockMvc
        .perform(
            post("/laboratory/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isOk())
        .andExpect(content().string("1"))
        .andReturn();
  }

  @Test
  void shouldReturnErrorForNotValid() throws Exception {
    String notification =
        Files.readString(
            Path.of("src/test/resources/notifications/laboratory/InvalidNotification.json"));

    byte[] body = new byte[] {};
    when(destinationLookupServiceClientMock.getNotificationCategory(anyString()))
        .thenThrow(
            new FeignException.FeignClientException(
                404,
                "mocked Exception for S1 Test",
                mock(Request.class),
                body,
                Collections.emptyMap()));

    mockMvc
        .perform(
            post("/laboratory/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void shouldUseServiceBaseFunctionToMapPathogenCode() throws Exception {
    String notification =
        Files.readString(
            Path.of("src/test/resources/notifications/laboratory/scenarioExamples/S2C.json"));

    when(destinationLookupServiceClientMock.getNotificationCategory(
            "92d99f62-fe4f-4337-b833-351751db12dc"))
        .thenReturn(new NotificationCategoryDTO("cvdp"));

    when(codeMappingServiceMock.mapCode("cvdp")).thenReturn("cvd");

    mockMvc
        .perform(
            post("/laboratory/$validate")
                .header("Content-Type", "application/json")
                .content(notification))
        .andExpect(status().isOk())
        .andExpect(content().string("2C"))
        .andReturn();

    verifyNoInteractions(legacyCodeMappingServiceMock);
  }
}
