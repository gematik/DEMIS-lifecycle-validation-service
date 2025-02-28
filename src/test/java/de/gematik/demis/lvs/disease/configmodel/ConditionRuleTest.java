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
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MarkdownType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConditionRuleTest {

  static Stream<Arguments> provideConditionRuleAndCondition() {
    return Stream.of(
        Arguments.of(
            new Condition()
                .setClinicalStatus(
                    new CodeableConcept(new Coding("system", "clinicalStatus", "display")))
                .setVerificationStatus(
                    new CodeableConcept(new Coding("system", "verificationStatus", "display")))
                .addNote(new Annotation(new MarkdownType("some note"))),
            new ConditionRule(
                List.of("clinicalstatus"), List.of("verificationstatus"), "not_empty"),
            true),
        Arguments.of(
            new Condition()
                .setClinicalStatus(
                    new CodeableConcept(new Coding("system", "clinicalStatus", "display")))
                .setVerificationStatus(
                    new CodeableConcept(new Coding("system", "otherStatus", "display")))
                .addNote(new Annotation(new MarkdownType("some note"))),
            new ConditionRule(
                List.of("clinicalstatus"), List.of("verificationstatus"), "not_empty"),
            false),
        Arguments.of(
            new Condition()
                .setClinicalStatus(
                    new CodeableConcept(new Coding("system", "otherStatus", "display")))
                .setVerificationStatus(
                    new CodeableConcept(new Coding("system", "verificationStatus", "display")))
                .addNote(new Annotation(new MarkdownType("some note"))),
            new ConditionRule(
                List.of("clinicalstatus"), List.of("verificationstatus"), "not_empty"),
            false),
        Arguments.of(
            new Condition()
                .setClinicalStatus(
                    new CodeableConcept(new Coding("system", "clinicalStatus", "display")))
                .setVerificationStatus(
                    new CodeableConcept(new Coding("system", "verificationStatus", "display")))
                .addNote(new Annotation()),
            new ConditionRule(
                List.of("clinicalstatus"), List.of("verificationstatus"), "not_empty"),
            false),
        Arguments.of(
            new Condition()
                .setClinicalStatus(
                    new CodeableConcept(new Coding("system", "clinicalStatus", "display")))
                .setVerificationStatus(
                    new CodeableConcept(new Coding("system", "verificationStatus", "display")))
                .addNote(new Annotation(new MarkdownType())),
            new ConditionRule(
                List.of("clinicalstatus"), List.of("verificationstatus"), "not_empty"),
            false),
        Arguments.of(
            new Condition()
                .setClinicalStatus(
                    new CodeableConcept(new Coding("system", "otherStatus", "display")))
                .setVerificationStatus(
                    new CodeableConcept(new Coding("system", "otherStatus", "display")))
                .addNote(new Annotation(new MarkdownType("some note"))),
            new ConditionRule(
                List.of("clinicalstatus"), List.of("verificationstatus"), "not_empty"),
            false),
        Arguments.of(
            new Condition()
                .setClinicalStatus(
                    new CodeableConcept(new Coding("system", "clinicalStatus", "display")))
                .setVerificationStatus(
                    new CodeableConcept(new Coding("system", "otherStatus", "display")))
                .addNote(new Annotation(new MarkdownType())),
            new ConditionRule(
                List.of("clinicalstatus"), List.of("verificationstatus"), "not_empty"),
            false),
        Arguments.of(
            new Condition()
                .setClinicalStatus(
                    new CodeableConcept(new Coding("system", "otherStatus", "display")))
                .setVerificationStatus(
                    new CodeableConcept(new Coding("system", "verificationStatus", "display")))
                .addNote(new Annotation(new MarkdownType())),
            new ConditionRule(
                List.of("clinicalstatus"), List.of("verificationstatus"), "not_empty"),
            false),
        Arguments.of(
            new Condition()
                .setClinicalStatus(
                    new CodeableConcept(new Coding("system", "otherStatus", "display")))
                .setVerificationStatus(
                    new CodeableConcept(new Coding("system", "otherStatus", "display")))
                .addNote(new Annotation(new MarkdownType())),
            new ConditionRule(
                List.of("clinicalstatus"), List.of("verificationstatus"), "not_empty"),
            false));
  }

  @ParameterizedTest
  @MethodSource("provideConditionRuleAndCondition")
  void testCompareConditionDataAllConditionsMatchExactly(
      Condition condition, ConditionRule conditionRule, boolean result) {
    assertThat(conditionRule.compareConditionData(condition)).isEqualTo(result);
  }
}
