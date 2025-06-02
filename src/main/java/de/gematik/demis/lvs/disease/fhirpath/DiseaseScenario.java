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

import java.util.List;

/**
 * Represents a disease scenario, which includes a name and a list of FHIRPath expressions. The
 * names are based on the implementation guide available at:
 *
 * @see <a
 *     href="https://simplifier.net/guide/rki.demis.disease/Home/guide-lifecyclemanagement.guide.md?version=current">Implementation
 *     Guide</a>
 */
public class DiseaseScenario {
  private String name; // The name of the disease scenario
  private List<FhirPathExpression> fhirPathExpression; // List of associated FHIRPath expressions

  /**
   * Gets the name of the disease scenario.
   *
   * @return the name of the disease scenario
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the disease scenario.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the list of FHIRPath expressions associated with the disease scenario.
   *
   * @return the list of FHIRPath expressions
   */
  public List<FhirPathExpression> getFhirPathExpression() {
    return fhirPathExpression;
  }

  /**
   * Sets the list of FHIRPath expressions associated with the disease scenario.
   *
   * @param fhirPathExpression the list of FHIRPath expressions to set
   */
  public void setFhirPathExpression(List<FhirPathExpression> fhirPathExpression) {
    this.fhirPathExpression = fhirPathExpression;
  }

  /** Represents a FHIRPath expression with an optional description. */
  public static class FhirPathExpression {
    private String fhirPath; // The FHIRPath expression
    private String description; // A description of the FHIRPath expression

    /**
     * Gets the FHIRPath expression.
     *
     * @return the FHIRPath expression
     */
    public String getFhirPath() {
      return fhirPath;
    }

    /**
     * Sets the FHIRPath expression.
     *
     * @param fhirPath the FHIRPath expression to set
     */
    public void setFhirPath(String fhirPath) {
      this.fhirPath = fhirPath;
    }

    /**
     * Gets the description of the FHIRPath expression.
     *
     * @return the description of the FHIRPath expression
     */
    public String getDescription() {
      return description;
    }

    /**
     * Sets the description of the FHIRPath expression.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
      this.description = description;
    }
  }
}
