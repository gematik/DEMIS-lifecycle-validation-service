package de.gematik.demis.lvs.common.fhir;

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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.*;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NotificationHelperTest {

  @Test
  void expectExtractionOfCompositionReturnsEmptyOnBadBundle() {
    final var result = NotificationHelper.extractComposition(new Bundle());
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void expectExtractionOfDiagnosticReportReturnsEmptyOnBadComposition() {
    final var result = NotificationHelper.extractDiagnosticReport(new Composition());
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void expectExtractionOfPathogenReturnsEmptyOnBadDiagnosticReport() {
    final var result =
        NotificationHelper.extractPathogenDetectionSetFromDiagnosticReport(new DiagnosticReport());
    Assertions.assertTrue(result.isEmpty());
  }
}
