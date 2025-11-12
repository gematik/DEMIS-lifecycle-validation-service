package de.gematik.demis.lvs.disease.fhirpath;

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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.gematik.demis.lvs.common.fhirpath.Scenario;
import java.util.List;

/**
 * Represents a disease scenario, which includes a name and a list of FHIRPath expressions. The
 * names are based on the implementation guide available at:
 *
 * @see <a
 *     href="https://simplifier.net/guide/rki.demis.disease/Home/guide-lifecyclemanagement.guide.md?version=current">Implementation
 *     Guide</a>
 */
public class DiseaseScenario extends Scenario {
  @JsonCreator
  public DiseaseScenario(
      @JsonProperty("name") String name,
      @JsonProperty("fhirPathExpression") List<FhirPathExpression> fhirPathExpression,
      @JsonProperty("externalChecks") List<ExternalCheckConfig> externalCheckConfigs) {
    super(name, fhirPathExpression, externalCheckConfigs);
  }
}
