package de.gematik.demis.lvs.labnotification.validation;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

class NotificationBasicValidationServiceTest {

  private final BundleBuilder bundleBuilder = new BundleBuilder(FhirContext.forR4Cached());
  private NotificationBasicValidationService service;

  @BeforeEach
  void beforeEach() {
    service = new NotificationBasicValidationService(new FhirParser(FhirContext.forR4Cached()));
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

  @ParameterizedTest
  @ValueSource(
      strings = {
        "src/test/resources/notifications/laboratory/nonnominal-notifiedperson.json",
        "src/test/resources/notifications/laboratory/anonymous.json",
        "src/test/resources/notifications/laboratory/negative_covid19_notification_Dv2.json"
      })
  void processNotification(String notificationPath) {
    final String notification = FileLoaderHelper.loadResourceFile(notificationPath);

    service.validate(notification, MediaType.APPLICATION_JSON);
  }

  private void performValidation() {
    service.validate((Bundle) bundleBuilder.getBundle());
  }
}
