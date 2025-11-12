package de.gematik.demis.lvs.common.externalchecks;

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

import static org.assertj.core.api.Assertions.assertThat;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.IFhirPath;
import de.gematik.demis.lvs.common.codemapping.CodeMappingService;
import de.gematik.demis.lvs.common.destination.DestinationLookupServiceClient;
import de.gematik.demis.lvs.disease.fhirpath.DiseaseScenario;
import de.gematik.demis.lvs.labnotification.fhirpath.LaboratoryScenario;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class AdditionalOperationExecuterTest {

  @Mock CodeMappingService codeMappingServiceMock;

  @Mock DestinationLookupServiceClient destinationLookupServiceClientMock;

  IFhirPath fhirpath;

  @Test
  @DisplayName("gracefully return true for empty list")
  void gracefullyReturnTrueForEmptyList() {

    fhirpath = FhirContext.forR4Cached().newFhirPath();

    AdditionalOperationExecuter additionalOperationExecuter =
        new AdditionalOperationExecuter(codeMappingServiceMock, destinationLookupServiceClientMock);

    assertThat(
            additionalOperationExecuter.checkAllExternalChecks(
                new Bundle(),
                new LaboratoryScenario("fakeScenario", List.of(), List.of()),
                fhirpath))
        .isTrue();
    assertThat(
            additionalOperationExecuter.checkAllExternalChecks(
                new Bundle(), new DiseaseScenario("fakeScenario", List.of(), List.of()), fhirpath))
        .isTrue();
  }
}
