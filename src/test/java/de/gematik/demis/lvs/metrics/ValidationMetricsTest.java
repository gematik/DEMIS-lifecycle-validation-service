package de.gematik.demis.lvs.metrics;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ValidationMetricsTest {

  @Mock private MeterRegistry meterRegistry;

  @Mock private Counter counter;

  private ValidationMetrics validationMetrics;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    validationMetrics = new ValidationMetrics(meterRegistry);
  }

  @Test
  void log_shouldLogCounterName() {
    // Just invoke to cover the @PostConstruct method
    assertDoesNotThrow(() -> validationMetrics.log());
  }

  @Test
  void saveScenarios_shouldIncrementCounter() {
    List<String> scenarios = List.of("S1", "S2");
    when(meterRegistry.counter("DEMIS_COUNTER_VALIDATION", "scenario_list", scenarios.toString()))
        .thenReturn(counter);

    validationMetrics.saveScenarios(scenarios);

    verify(meterRegistry).counter("DEMIS_COUNTER_VALIDATION", "scenario_list", "[S1, S2]");
    verify(counter).increment();
  }

  @Test
  void saveScenarios_shouldSwallowExceptionFromMeterRegistry() {
    when(meterRegistry.counter(anyString(), anyString(), anyString()))
        .thenThrow(new RuntimeException("boom"));

    assertDoesNotThrow(() -> validationMetrics.saveScenarios(List.of("X")));
  }

  @Test
  void incUnsuccessfulValidationCount_shouldIncrementErrorTypeMetric() {
    String sender = "sender123";
    String errorType = "FORMAT";
    when(meterRegistry.counter(
            "validation_error_type", "principal_id", sender, "error_type", errorType))
        .thenReturn(counter);

    validationMetrics.incUnsuccessfulValidationCount(sender, errorType);

    verify(meterRegistry)
        .counter("validation_error_type", "principal_id", sender, "error_type", errorType);
    verify(counter).increment();
  }
}
