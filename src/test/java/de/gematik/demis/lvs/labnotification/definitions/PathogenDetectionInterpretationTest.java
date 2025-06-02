package de.gematik.demis.lvs.labnotification.definitions;

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

import java.util.List;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PathogenDetectionInterpretationTest {

  private static Observation createObservationForCode(final String code) {
    final var codeableConcept = new CodeableConcept();
    codeableConcept.setCoding(List.of(new Coding("Test", code, "Test")));
    final var observation = new Observation();
    observation.addInterpretation(codeableConcept);
    return observation;
  }

  @Test
  void expectEnumIsPositiveInterpretation() {
    final var result =
        PathogenDetectionInterpretation.getEnumFromObservation(createObservationForCode("POS"));
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(PathogenDetectionInterpretation.POSITIVE, result.get());
  }

  @Test
  void expectEnumIsNegativeInterpretation() {
    final var codeableConcept = new CodeableConcept();
    codeableConcept.setCoding(List.of(new Coding("Test", "NEG", "Test")));
    final var observation = new Observation();
    observation.addInterpretation(codeableConcept);
    final var result =
        PathogenDetectionInterpretation.getEnumFromObservation(createObservationForCode("NEG"));
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(PathogenDetectionInterpretation.NEGATIVE, result.get());
  }

  @Test
  void expectEnumIsIndeterminateInterpretation() {
    final var result =
        PathogenDetectionInterpretation.getEnumFromObservation(createObservationForCode("IND"));
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(PathogenDetectionInterpretation.INDETERMINATE, result.get());
  }

  @Test
  void expectIsPositiveOrNot() {
    Assertions.assertTrue(PathogenDetectionInterpretation.POSITIVE.isPositive());
    Assertions.assertFalse(PathogenDetectionInterpretation.NEGATIVE.isPositive());
    Assertions.assertFalse(PathogenDetectionInterpretation.INDETERMINATE.isPositive());
  }
}
