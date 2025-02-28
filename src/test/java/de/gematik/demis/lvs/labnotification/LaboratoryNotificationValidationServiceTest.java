package de.gematik.demis.lvs.labnotification;

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
 * #L%
 */

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleBuilder;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.integration.FileLoaderHelper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class LaboratoryNotificationValidationServiceTest {

  private final BundleBuilder bundleBuilder = new BundleBuilder(FhirContext.forR4Cached());
  private LaboratoryNotificationValidationService service;

  @BeforeEach
  void beforeEach() {
    service =
        new LaboratoryNotificationValidationService(
            new FhirParser(FhirContext.forR4Cached()), true);
  }

  @Test
  void expectThatEmptyBundleThrowsExceptionOnValidation() {
    Assertions.assertThrows(LifecycleValidationException.class, this::performValidation);
  }

  @Test
  void expectThatCompositionWithoutStatusThrowsExceptionOnValidation() {
    bundleBuilder.addDocumentEntry(new Composition());

    Assertions.assertThrows(LifecycleValidationException.class, this::performValidation);
  }

  @Test
  void expectThatCompositionWithStatusAmendedWorks() {
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.AMENDED);
    final Reference diagnosticReportRef = new Reference(new DiagnosticReport());
    composition.addSection(new Composition.SectionComponent().addEntry(diagnosticReportRef));
    bundleBuilder.addDocumentEntry(composition);

    Assertions.assertDoesNotThrow(this::performValidation);
  }

  @Test
  void expectThatCompositionWithStatusEnteredInErrorThrowsException() {
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.ENTEREDINERROR);
    bundleBuilder.addDocumentEntry(composition);

    Assertions.assertThrows(LifecycleValidationException.class, this::performValidation);
  }

  @Test
  void expectThatCompositionWithStatusNullThrowsException() {
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.ENTEREDINERROR);
    bundleBuilder.addDocumentEntry(composition);

    Assertions.assertThrows(LifecycleValidationException.class, this::performValidation);
  }

  @Test
  void expectThatCompositionWithStatusPreliminaryThrowsExceptionOnEmptyComposition() {
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.PRELIMINARY);
    bundleBuilder.addDocumentEntry(composition);

    Assertions.assertThrows(LifecycleValidationException.class, this::performValidation);
  }

  @Test
  void expectThatCompositionWithStatusFinalThrowsExceptionOnEmptyComposition() {
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.FINAL);
    bundleBuilder.addDocumentEntry(composition);

    Assertions.assertThrows(LifecycleValidationException.class, this::performValidation);
  }

  @Test
  void regression_expectThat74CompositionWithNegativeCOVPIsRejected() {
    final LaboratoryNotificationValidationService rejectingAnonymousNotifications =
        new LaboratoryNotificationValidationService(
            new FhirParser(FhirContext.forR4Cached()), false);
    // GIVEN isPositive 7.4 notification for Covid-19
    final String notification =
        FileLoaderHelper.loadResourceFile(
            "src/test/resources/notifications/laboratory/negative_covid19_notification_Dv2.json");

    assertThrows(
        LifecycleValidationException.class,
        () -> rejectingAnonymousNotifications.validate(notification, MediaType.APPLICATION_JSON));
  }

  @Test
  void expectThat74CompositionWithNegativeCOVPIsAccepted() {
    // GIVEN isPositive 7.4 notification for Covid-19
    final String notification =
        FileLoaderHelper.loadResourceFile(
            "src/test/resources/notifications/laboratory/negative_covid19_notification_Dv2.json");

    service.validate(notification, MediaType.APPLICATION_JSON);
  }

  private void performValidation() {
    service.validate((Bundle) bundleBuilder.getBundle());
  }
}
