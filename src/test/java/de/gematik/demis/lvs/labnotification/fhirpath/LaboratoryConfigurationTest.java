package de.gematik.demis.lvs.labnotification.fhirpath;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.gematik.demis.lvs.common.fhirpath.ScenarioLoader;
import java.util.List;
import org.junit.jupiter.api.Test;

class LaboratoryConfigurationTest {
  @Test
  void testLoadLaboratoryScenarios() {
    final LaboratoryConfigurationProperties properties =
        new LaboratoryConfigurationProperties(
            "configuration/laboratoryScenarios.json", "configuration/keyToFhirPath.json", true);
    final List<LaboratoryScenario> scenarios =
        ScenarioLoader.loadScenarios(
            properties.fhirPathData(), properties.keyToFhirPathData(), LaboratoryScenario[].class);
    assertThat(scenarios.getFirst().getFhirPathExpression().getFirst().getFhirPath())
        .isEqualTo(
            "Bundle.where(meta.profile = 'https://demis.rki.de/fhir/StructureDefinition/NotificationBundleLaboratoryNegative').empty()");
    assertThat(scenarios.getLast().getFhirPathExpression().getFirst().getFhirPath())
        .isEqualTo(
            "Bundle.where(meta.profile = 'https://demis.rki.de/fhir/StructureDefinition/NotificationBundleLaboratoryNegative').exists()");
  }
}
