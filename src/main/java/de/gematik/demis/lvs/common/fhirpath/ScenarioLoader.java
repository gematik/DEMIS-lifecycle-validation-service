package de.gematik.demis.lvs.common.fhirpath;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ScenarioLoader {

  private ScenarioLoader() {}

  public static <S extends Scenario> List<S> loadScenarios(
      String fhirPathData, String keyToFhirPathData, Class<S[]> scenarioClass) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      final S[] scenarios = objectMapper.readValue(new File(fhirPathData), scenarioClass);
      final JsonNode keysToFhirPath = objectMapper.readTree(new File(keyToFhirPathData)).get(0);

      Arrays.stream(scenarios)
          .forEach(
              scenario -> {
                scenario
                    .getFhirPathExpression()
                    .forEach(
                        expression -> {
                          final String key = expression.getFhirPath();
                          String resolved = keysToFhirPath.get(key).asText();
                          expression.setFhirPath(resolved);
                        });
                scenario
                    .getExternalChecks()
                    .forEach(
                        check -> {
                          Map<String, Object> map = check.getInputs();
                          for (Map.Entry<String, Object> entry : map.entrySet()) {
                            final Object keyToFhirPath = entry.getValue();
                            if (keyToFhirPath instanceof String) {
                              final String path =
                                  keysToFhirPath.get((String) keyToFhirPath).asText();
                              map.put(entry.getKey(), path);
                            }
                          }
                        });
              });

      return List.of(scenarios);
    } catch (IOException e) {
      log.warn("Error while processing routing rules config file", e);
    }
    return Collections.emptyList();
  }
}
