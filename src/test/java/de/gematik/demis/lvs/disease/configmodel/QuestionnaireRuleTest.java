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

import de.gematik.demis.lvs.disease.NotificationData;
import java.util.Collections;
import java.util.List;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.Test;

class QuestionnaireRuleTest {

  @Test
  void shouldCheckCorrectQuestionnaireResponseValues() {

    QuestionnaireRule questionnaireRule =
        new QuestionnaireRule(
            "profileUrl",
            List.of("completed"),
            List.of(new QuestionRule("linkId", List.of("value"))));

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
    questionnaireResponse.setMeta(new Meta().addProfile("profileUrl"));
    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent.setLinkId("linkId");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent.setValue(new Coding("system", "value", "display"));

    NotificationData notificationData =
        new NotificationData(null, null, List.of(questionnaireResponse));

    assertThat(questionnaireRule.isQuestionnaireValid(notificationData)).isTrue();
  }

  @Test
  void shouldReturnFalseWhenValueIsNotCorrect() {

    QuestionnaireRule questionnaireRule =
        new QuestionnaireRule(
            "profileUrl",
            List.of("completed"),
            List.of(new QuestionRule("linkId", List.of("value"))));

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
    questionnaireResponse.setMeta(new Meta().addProfile("profileUrl"));
    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent.setLinkId("linkId");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent.setValue(
        new Coding("system", "wrong_value", "display"));

    NotificationData notificationData =
        new NotificationData(null, null, List.of(questionnaireResponse));

    assertThat(questionnaireRule.isQuestionnaireValid(notificationData)).isFalse();
  }

  @Test
  void shouldReturnTrueWhenOneOfMultipleQuestionsIsCorrect() {

    QuestionnaireRule questionnaireRule =
        new QuestionnaireRule(
            "profileUrl",
            List.of("completed"),
            List.of(new QuestionRule("linkId", List.of("value"))));

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
    questionnaireResponse.setMeta(new Meta().addProfile("profileUrl"));

    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent.setLinkId("linkId");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent.setValue(new Coding("system", "value", "display"));

    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent2 =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent2.setLinkId("linkId2");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent2 = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent2.setValue(new StringType("value2"));

    NotificationData notificationData =
        new NotificationData(null, null, List.of(questionnaireResponse));

    assertThat(questionnaireRule.isQuestionnaireValid(notificationData)).isTrue();
  }

  @Test
  void shouldReturnTrueWhenOneQuestionnaireResponsesIsCorrect() {

    QuestionnaireRule questionnaireRule =
        new QuestionnaireRule(
            "profileUrl",
            List.of("completed"),
            List.of(new QuestionRule("linkId", List.of("value"))));

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
    questionnaireResponse.setMeta(new Meta().addProfile("profileUrl"));

    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent.setLinkId("linkId");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent.setValue(new Coding("system", "value", "display"));

    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent2 =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent2.setLinkId("linkId2");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent2 = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent2.setValue(new StringType("value2"));

    QuestionnaireResponse questionnaireResponse2 = new QuestionnaireResponse();
    questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
    questionnaireResponse.setMeta(new Meta().addProfile("profileUrl2"));

    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent3 =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent3.setLinkId("linkId3");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent3 = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent3.setValue(new Coding("system", "value", "display"));

    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent4 =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent4.setLinkId("linkId4");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent4 = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent4.setValue(new StringType("value2"));

    NotificationData notificationData =
        new NotificationData(null, null, List.of(questionnaireResponse, questionnaireResponse2));

    assertThat(questionnaireRule.isQuestionnaireValid(notificationData)).isTrue();
  }

  @Test
  void shouldHandleNoQuestionnaireResponseGracefully() {

    QuestionnaireRule questionnaireRule =
        new QuestionnaireRule(
            "profileUrl",
            List.of("completed"),
            List.of(new QuestionRule("linkId", List.of("value"))));

    NotificationData notificationData = new NotificationData(null, null, Collections.emptyList());

    assertThat(questionnaireRule.isQuestionnaireValid(notificationData)).isTrue();
  }

  @Test
  void shouldHandleStatusCorrectly() {
    QuestionnaireRule questionnaireRule =
        new QuestionnaireRule(
            "profileUrl",
            List.of("completed"),
            List.of(new QuestionRule("linkId", List.of("value"))));

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.AMENDED);
    questionnaireResponse.setMeta(new Meta().addProfile("profileUrl"));
    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent.setLinkId("linkId");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent.setValue(new Coding("system", "value", "display"));

    NotificationData notificationData =
        new NotificationData(null, null, List.of(questionnaireResponse));

    assertThat(questionnaireRule.isQuestionnaireValid(notificationData)).isFalse();
  }

  @Test
  void shouldNotLookIntoQuestionnairesResponseItemWhenNoQuestionRules() {
    QuestionnaireRule questionnaireRule =
        new QuestionnaireRule("profileUrl", List.of("completed"), Collections.emptyList());

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
    questionnaireResponse.setMeta(new Meta().addProfile("profileUrl"));

    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent.setLinkId("someId");

    NotificationData notificationData =
        new NotificationData(null, null, List.of(questionnaireResponse));

    assertThat(questionnaireRule.isQuestionnaireValid(notificationData)).isTrue();
  }

  @Test
  void shouldHandleMultipleProfileUrls() {

    QuestionnaireRule questionnaireRule =
        new QuestionnaireRule(
            "profileUrl",
            List.of("completed"),
            List.of(new QuestionRule("linkId", List.of("value"))));

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
    questionnaireResponse.setMeta(new Meta().addProfile("profileUrl2"));
    questionnaireResponse.setMeta(new Meta().addProfile("profileUrl"));
    QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent =
        questionnaireResponse.addItem();
    questionnaireResponseItemComponent.setLinkId("linkId");
    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent
        questionnaireResponseItemAnswerComponent = questionnaireResponseItemComponent.addAnswer();
    questionnaireResponseItemAnswerComponent.setValue(new Coding("system", "value", "display"));

    NotificationData notificationData =
        new NotificationData(null, null, List.of(questionnaireResponse));

    assertThat(questionnaireRule.isQuestionnaireValid(notificationData)).isTrue();
  }
}
