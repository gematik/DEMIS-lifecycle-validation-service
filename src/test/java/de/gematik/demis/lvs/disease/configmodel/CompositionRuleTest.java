package de.gematik.demis.lvs.disease.configmodel;

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

import java.util.Arrays;
import java.util.Collections;
import org.hl7.fhir.r4.model.Composition;
import org.junit.jupiter.api.Test;

class CompositionRuleTest {

  @Test
  void shouldReturnTrueWhenStatusMatches() {
    CompositionRule rule = new CompositionRule(Arrays.asList("preliminary", "final"));
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.PRELIMINARY);

    assertTrue(rule.compareCompositionData(composition));
  }

  @Test
  void shouldReturnFalseWhenStatusDoesNotMatch() {
    CompositionRule rule = new CompositionRule(Arrays.asList("preliminary", "final"));
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.AMENDED);

    assertFalse(rule.compareCompositionData(composition));
  }

  @Test
  void shouldReturnFalseWhenCompositionIsEmpty() {
    CompositionRule rule = new CompositionRule(Arrays.asList("preliminary", "final"));
    Composition composition = new Composition();

    assertFalse(rule.compareCompositionData(composition));
  }

  @Test
  void shouldReturnFalseWhenStatusListIsEmpty() {
    CompositionRule rule = new CompositionRule(Collections.emptyList());
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.PRELIMINARY);

    assertFalse(rule.compareCompositionData(composition));
  }
}
