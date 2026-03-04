package de.gematik.demis.lvs.api;

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

import static de.gematik.demis.lvs.common.exception.ExceptionMessages.EXCEPTION_MESSAGE_NOTIFICATION_CATEGORY_MISMATCH;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.gematik.demis.lvs.common.codemapping.FutsClient;
import de.gematik.demis.lvs.common.destination.DestinationLookupServiceClient;
import de.gematik.demis.lvs.common.destination.NotificationCategoryDTO;
import feign.FeignException;
import feign.Request;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureObservability
@EnableCaching
@TestPropertySource(
    properties = {
      "feature.flag.fhirpath.validation.enabled=true",
      "feature.flag.return.fhirpath.validation.in.responses=true",
      "feature.flag.return.disease.fhirpath.validation.in.responses=true"
    })
class LifecycleValidationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FutsClient futsClientMock;
  @MockitoBean private DestinationLookupServiceClient destinationLookupServiceClientMock;

  @BeforeEach
  void setUp() {
    Map<String, String> labMap = new HashMap<>();
    labMap.put("cvdp", "cvd");
    labMap.put("abvp", "abv");
    labMap.put("banp", "ban");
    when(futsClientMock.getConceptMap("NotificationCategoryToTransmissionCategory"))
        .thenReturn(labMap);
    Map<String, String> disMap = new HashMap<>();
    disMap.put("cvdd", "cvd");
    disMap.put("abvd", "abv");
    disMap.put("band", "ban");
    when(futsClientMock.getConceptMap("NotificationDiseaseCategoryToTransmissionCategory"))
        .thenReturn(disMap);
  }

  @Nested
  class DiseaseTests {

    @Test
    void shouldCallDiseaseNotificationLifecycleValidationSrv() throws Exception {

      String notification =
          Files.readString(
              Path.of("src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V.json"));

      when(destinationLookupServiceClientMock.getNotificationCategory(anyString()))
          .thenReturn(new NotificationCategoryDTO("band"));

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
  }

  @Nested
  class LaboratoryTests {
    @Test
    void shouldCallLaboratoryNotificationLifecycleValidationSrv() throws Exception {

      String notification =
          Files.readString(
              Path.of("src/test/resources/notifications/laboratory/scenarioExamples/M_POS.json"));

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
          .andExpect(content().string("M_POS"))
          .andReturn();
    }

    @Test
    void shouldReturnErrorForNotValid() throws Exception {

      String notification =
          Files.readString(
              Path.of(
                  "src/test/resources/notifications/laboratory/scenarioExamples/invalid/IM_NEG.json"));

      mockMvc
          .perform(
              post("/laboratory/$validate")
                  .header("Content-Type", "application/json")
                  .content(notification))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(content().string("No valid lifecycle scenario found"));
    }

    @Test
    void shouldThrowErrorNotificationCategoryMismatch() throws Exception {

      String notification =
          Files.readString(
              Path.of("src/test/resources/notifications/laboratory/scenarioExamples/FM_NEG.json"));

      when(destinationLookupServiceClientMock.getNotificationCategory(anyString()))
          .thenReturn(new NotificationCategoryDTO("abvp"));

      mockMvc
          .perform(
              post("/laboratory/$validate")
                  .header("Content-Type", "application/json")
                  .content(notification))
          .andExpect(status().isUnprocessableEntity())
          .andExpect(content().string(EXCEPTION_MESSAGE_NOTIFICATION_CATEGORY_MISMATCH));
    }
  }

  @Test
  void shouldThrowIllegalArgumentException_UnknownNotificationType() throws Exception {
    mockMvc
        .perform(
            post("/invalid/$validate").header("Content-Type", "application/json").content("any"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid or unknown notification type"));
  }
}
