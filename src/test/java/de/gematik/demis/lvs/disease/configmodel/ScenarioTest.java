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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import de.gematik.demis.lvs.disease.NotificationData;
import java.util.List;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScenarioTest {

  private final Composition composition = new Composition();
  private final Condition condition = new Condition();
  private final List<QuestionnaireResponse> questionnaireResponseList =
      List.of(new QuestionnaireResponse(), new QuestionnaireResponse());
  @Mock private CompositionRule compositionRuleMock;
  @Mock private ConditionRule conditionRuleMock;
  @Mock private QuestionnaireRule questionnaireRuleMock1;
  @Mock private QuestionnaireRule questionnaireRuleMock2;

  static Stream<Arguments> provideMockValues() {
    return Stream.of(
        Arguments.of(true, true, true, true, true),
        Arguments.of(false, true, true, true, false),
        Arguments.of(true, false, true, true, false),
        Arguments.of(true, true, false, true, false),
        Arguments.of(true, true, true, false, false),
        Arguments.of(false, false, true, true, false),
        Arguments.of(false, false, false, true, false),
        Arguments.of(false, false, true, false, false),
        Arguments.of(false, false, false, false, false));
  }

  @ParameterizedTest
  @MethodSource("provideMockValues")
  void shouldCreateScenarioAndCallSubObjectMethodsForValidation(
      boolean compositionValid,
      boolean conditionValid,
      boolean questionnaire1Valid,
      boolean questionnaire2Valid,
      boolean expectedValue) {
    NotificationData notificationData =
        new NotificationData(composition, condition, questionnaireResponseList);
    lenient()
        .when(compositionRuleMock.compareCompositionData(composition))
        .thenReturn(compositionValid);
    lenient().when(conditionRuleMock.compareConditionData(condition)).thenReturn(conditionValid);
    QuestionnaireWrapper questionnaireWrapperMock1 =
        new QuestionnaireWrapper(questionnaireRuleMock1);
    QuestionnaireWrapper questionnaireWrapperMock2 =
        new QuestionnaireWrapper(questionnaireRuleMock2);
    lenient()
        .when(questionnaireRuleMock1.isQuestionnaireValid(notificationData))
        .thenReturn(questionnaire1Valid);
    lenient()
        .when(questionnaireRuleMock2.isQuestionnaireValid(notificationData))
        .thenReturn(questionnaire2Valid);

    Scenario scenario =
        new Scenario(
            "scenarioId",
            "description",
            compositionRuleMock,
            conditionRuleMock,
            List.of(questionnaireWrapperMock1, questionnaireWrapperMock2));

    assertThat(scenario.isScenarioValid(notificationData))
        .as(
            "Scenario should be valid, when all parts are valid, but invalid when any part is invalid")
        .isEqualTo(expectedValue);
  }

  @Test
  void shouldReturnFalseIfAnyFieldOfNotificationDataDataIsNull() {
    NotificationData notificationData =
        new NotificationData(null, new Condition(), singletonList(new QuestionnaireResponse()));
    NotificationData notificationData2 =
        new NotificationData(new Composition(), null, singletonList(new QuestionnaireResponse()));
    NotificationData notificationData3 =
        new NotificationData(new Composition(), new Condition(), emptyList());

    Scenario scenario =
        new Scenario(
            "scenarioId",
            "description",
            compositionRuleMock,
            conditionRuleMock,
            List.of(new QuestionnaireWrapper(questionnaireRuleMock1)));
    assertThat(scenario.isScenarioValid(notificationData)).isFalse();
    assertThat(scenario.isScenarioValid(notificationData2)).isFalse();
    assertThat(scenario.isScenarioValid(notificationData3)).isFalse();
  }
}
