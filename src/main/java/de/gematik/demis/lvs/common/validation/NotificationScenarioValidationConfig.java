package de.gematik.demis.lvs.common.validation;

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

import ca.uhn.fhir.context.FhirContext;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.externalchecks.AdditionalOperationExecuter;
import de.gematik.demis.lvs.common.fhirpath.Scenario;
import de.gematik.demis.lvs.disease.DiseaseBasicNotificationLifecycleValidationSrv;
import de.gematik.demis.lvs.labnotification.validation.NotificationBasicValidationService;
import de.gematik.demis.lvs.metrics.ValidationMetrics;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Explicit configuration that creates two differently parameterized
 * NotificationScenarioValidationService beans so Spring can disambiguate the scenario lists
 * (loadDiseaseScenarios vs. loadLaboratoryScenarios).
 */
@ConditionalOnProperty(name = "feature.flag.fhirpath.validation.enabled", havingValue = "true")
@Configuration
public class NotificationScenarioValidationConfig {

  private final ValidationMetrics validationMetrics;

  public NotificationScenarioValidationConfig(ValidationMetrics validationMetrics) {
    this.validationMetrics = validationMetrics;
  }

  @Bean
  public NotificationScenarioValidationService diseaseNotificationScenarioValidationService(
      FhirContext context,
      @Qualifier("loadDiseaseScenarios") List<Scenario> diseaseScenarios,
      ValidationMetrics validationMetrics,
      AdditionalOperationExecuter additionalOperationExecuter,
      FhirParser fhirParser) {
    return new NotificationScenarioValidationService(
        context, diseaseScenarios, validationMetrics, additionalOperationExecuter, fhirParser);
  }

  @Bean
  public NotificationScenarioValidationService laboratoryNotificationScenarioValidationService(
      FhirContext context,
      @Qualifier("loadLaboratoryScenarios") List<Scenario> laboratoryScenarios,
      ValidationMetrics validationMetrics,
      AdditionalOperationExecuter additionalOperationExecuter,
      FhirParser fhirParser) {
    return new NotificationScenarioValidationService(
        context, laboratoryScenarios, validationMetrics, additionalOperationExecuter, fhirParser);
  }

  @Bean
  public NotificationValidationService diseaseValidationService(
      DiseaseBasicNotificationLifecycleValidationSrv notificationBasicValidationService,
      NotificationScenarioValidationService diseaseNotificationScenarioValidationService,
      @Value("${feature.flag.return.disease.fhirpath.validation.in.responses}")
          boolean returnValidationInResponses) {
    return new NotificationValidationService(
        notificationBasicValidationService,
        diseaseNotificationScenarioValidationService,
        returnValidationInResponses,
        validationMetrics);
  }

  @Bean
  public NotificationValidationService laboratoryValidationService(
      NotificationBasicValidationService notificationBasicValidationService,
      NotificationScenarioValidationService laboratoryNotificationScenarioValidationService,
      @Value("${feature.flag.return.fhirpath.validation.in.responses}")
          boolean returnValidationInResponses) {
    return new NotificationValidationService(
        notificationBasicValidationService,
        laboratoryNotificationScenarioValidationService,
        returnValidationInResponses,
        validationMetrics);
  }
}
