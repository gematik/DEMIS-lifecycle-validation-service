package de.gematik.demis.lvs.metrics;

/*-
 * #%L
 * validation-service
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

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationMetrics {

  private static final String COUNTER_VALIDATION = "DEMIS_COUNTER_VALIDATION";

  private static final String TAG_PRINCIPAL = "principal_id";
  private static final String TAG_ERROR_TYPE = "error_type";
  private static final String TAG_SCENARIO_LIST = "scenario_list";
  private static final String VALIDATION_ERROR_TYPE_METRIC = "validation_error_type";

  private static final String COUNTER_LAB_VAL_RESULT = "lvs_lab_val_res";
  private static final String COUNTER_DIS_VAL_RESULT = "lvs_disease_val_res";

  private final MeterRegistry meterRegistry;

  @PostConstruct
  public void log() {
    log.info("Metrics: {}", COUNTER_VALIDATION);
  }

  public void countLabValResult(final boolean isSuccessLegacy, final boolean isSuccessFhirpath) {
    final String equalString = isSuccessLegacy == isSuccessFhirpath ? "_equal" : "_unequal";
    final String resultString = getResultString(isSuccessLegacy, isSuccessFhirpath);
    meterRegistry.counter(COUNTER_LAB_VAL_RESULT + equalString + resultString).increment();
  }

  public void countDisValResult(final boolean isSuccessLegacy, final boolean isSuccessFhirPath) {
    final String equalString = isSuccessLegacy == isSuccessFhirPath ? "_equal" : "_unequal";
    final String resultString = getResultString(isSuccessLegacy, isSuccessFhirPath);
    meterRegistry.counter(COUNTER_DIS_VAL_RESULT + equalString + resultString).increment();
  }

  private String getResultString(final boolean isSuccessLegacy, final boolean isSuccessFhirPath) {
    if (isSuccessLegacy && isSuccessFhirPath) {
      return "_success";
    } else if (!isSuccessLegacy && !isSuccessFhirPath) {
      return "_fail";
    } else if (isSuccessLegacy) {
      return "_legacy_success_fhirpath_fail";
    } else {
      return "_legacy_fail_fhirpath_success";
    }
  }

  public void saveScenario(final String scenario) {
    try {
      meterRegistry.counter(COUNTER_VALIDATION, TAG_SCENARIO_LIST, scenario).increment();
    } catch (final RuntimeException e) {
      log.error("error incrementing counter", e);
    }
  }

  public void saveScenarios(final List<String> scenarios) {
    try {
      meterRegistry
          .counter(COUNTER_VALIDATION, TAG_SCENARIO_LIST, scenarios.toString())
          .increment();
    } catch (final RuntimeException e) {
      log.error("error incrementing counter", e);
    }
  }

  /** Store the list of findings for the given sender */
  public void incUnsuccessfulValidationCount(
      @Nonnull final String senderId, @Nonnull final String errorType) {
    meterRegistry
        .counter(VALIDATION_ERROR_TYPE_METRIC, TAG_PRINCIPAL, senderId, TAG_ERROR_TYPE, errorType)
        .increment();
  }
}
