package de.gematik.demis.lvs.common.codemapping;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class LegacyCodeMappingServiceTest {

  @Mock private FutsClient futsClient;
  @Mock private CacheManager cacheManager;
  @Mock private Cache cache;

  private LegacyCodeMappingService legacyCodeMappingService;

  private final Map<String, String> labMap = new HashMap<>();
  private final Map<String, String> diseaseMap = new HashMap<>();

  @BeforeEach
  void setUp() {
    labMap.clear();
    diseaseMap.clear();
    lenient().when(cacheManager.getCache(anyString())).thenReturn(cache);
    legacyCodeMappingService = new LegacyCodeMappingService(futsClient, cacheManager);
  }

  @Test
  void testLazyInitOnFirstGetSurvNetCode() {
    labMap.put("LAB1", "SN1");
    diseaseMap.put("DIS1", "SN2");
    when(futsClient.getConceptMap("NotificationCategoryToTransmissionCategory")).thenReturn(labMap);
    when(futsClient.getConceptMap("NotificationDiseaseCategoryToTransmissionCategory"))
        .thenReturn(diseaseMap);

    assertThat(legacyCodeMappingService.getSurvNetCode("LAB1")).isEqualTo("SN1");
    assertThat(legacyCodeMappingService.getSurvNetCode("DIS1")).isEqualTo("SN2");
    assertThat(legacyCodeMappingService.getSurvNetCode("UNKNOWN")).isNull();
    verify(futsClient, times(1)).getConceptMap("NotificationCategoryToTransmissionCategory");
    verify(futsClient, times(1)).getConceptMap("NotificationDiseaseCategoryToTransmissionCategory");
  }

  @Test
  void testReloadCachesWithEmptyMaps() {
    when(futsClient.getConceptMap(anyString())).thenReturn(new HashMap<>());
    legacyCodeMappingService.reloadCaches();
    assertThat(legacyCodeMappingService.getSurvNetCode("ANY")).isNull();
  }

  @Test
  void testRefreshCacheIsCalled() {
    labMap.put("LAB1", "SN1");
    when(futsClient.getConceptMap("NotificationCategoryToTransmissionCategory")).thenReturn(labMap);
    when(futsClient.getConceptMap("NotificationDiseaseCategoryToTransmissionCategory"))
        .thenReturn(new HashMap<>());
    legacyCodeMappingService.reloadCaches();
    verify(cacheManager, atLeastOnce()).getCache(anyString());
    verify(cache, atLeastOnce()).clear();
    verify(cache, atLeastOnce()).put("LAB1", "SN1");
  }

  @Test
  void shouldHandleRuntimeExceptions() {
    when(futsClient.getConceptMap("NotificationCategoryToTransmissionCategory"))
        .thenThrow(new RuntimeException("some runtime exception"));
    assertThatThrownBy(() -> legacyCodeMappingService.reloadCaches())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("some runtime exception");
  }

  @Test
  void shouldNotThrowExceptionsReturnValuesFromFutsAreNull() {
    when(futsClient.getConceptMap("NotificationCategoryToTransmissionCategory")).thenReturn(null);
    when(futsClient.getConceptMap("NotificationDiseaseCategoryToTransmissionCategory"))
        .thenReturn(null);
    assertDoesNotThrow(() -> legacyCodeMappingService.reloadCaches());
  }
}
