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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegacyCodeMappingService {

  private static final String CONCEPT_MAP_LABORATORY = "NotificationCategoryToTransmissionCategory";
  private static final String CONCEPT_MAP_DISEASE =
      "NotificationDiseaseCategoryToTransmissionCategory";

  private final FutsClient futsClient;
  private final CacheManager cacheManager;
  private final AtomicReference<Map<String, String>> codeToSurvNet =
      new AtomicReference<>(new ConcurrentHashMap<>());
  private final Object cacheInitLock = new Object();

  /**
   * Returns the SurvNet code for the given source code. The internal cache is initialized lazily
   * and in a thread-safe manner on the first access. No @PostConstruct is used, because the FUTS
   * client might not be ready at application startup. Multiple parallel initializations are
   * prevented by double-checked locking.
   *
   * @param code The source code to be mapped
   * @return The corresponding SurvNet code or null if not found
   */
  @Cacheable(cacheNames = "codeToSurvNet", key = "#code")
  public String getSurvNetCode(String code) {
    if (codeToSurvNet.get().isEmpty()) {
      synchronized (cacheInitLock) {
        if (codeToSurvNet.get().isEmpty()) {
          reloadCaches();
        }
      }
    }
    return codeToSurvNet.get().get(code);
  }

  @Scheduled(cron = "${lvs.codemapping.cache.reload.cron}")
  protected void reloadCaches() {
    try {
      Map<String, String> newLab = futsClient.getConceptMap(CONCEPT_MAP_LABORATORY);
      Map<String, String> newDis = futsClient.getConceptMap(CONCEPT_MAP_DISEASE);

      Map<String, String> snapshot = new ConcurrentHashMap<>();

      if (newLab != null) {
        snapshot.putAll(newLab);
        log.info("laboratory conceptmap reloaded, entries={}", newLab.size());
      } else {
        log.warn("laboratory conceptmap reload skipped (empty/null)");
      }

      if (newDis != null) {
        snapshot.putAll(newDis);
        log.info("disease conceptmap reloaded, entries={}", newDis.size());
      } else {
        log.warn("disease conceptmap reload skipped (empty/null)");
      }

      codeToSurvNet.set(snapshot);
      refreshCache(snapshot);
    } catch (RuntimeException e) {
      log.error("Error reloading concept maps", e);
      throw e;
    }
  }

  private void refreshCache(Map<String, String> snapshot) {
    final String cacheName = "codeToSurvNet";
    Cache cache = cacheManager.getCache(cacheName);
    if (cache == null) {
      log.error("Cache {} not found", cacheName);
      return;
    }

    cache.clear();
    snapshot.forEach(cache::put);
  }
}
