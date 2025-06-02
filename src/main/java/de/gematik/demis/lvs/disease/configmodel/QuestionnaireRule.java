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

import de.gematik.demis.lvs.disease.NotificationData;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

public record QuestionnaireRule(
    String profileName, List<String> status, List<QuestionRule> question) {

  public QuestionnaireRule(String profileName, List<String> status, List<QuestionRule> question) {
    this.profileName = profileName;
    this.status = status;
    this.question = question != null ? question : List.of();
  }

  public boolean isQuestionnaireValid(NotificationData data) {

    Optional<QuestionnaireResponse> questionnaireResponse =
        getQuestionnaireResponseForName(data.questionnaireResponseList());

    return questionnaireResponse.isEmpty()
        || questionnaireResponse.filter(this::compareData).isPresent();
  }

  private boolean compareData(QuestionnaireResponse questionnaireResponse) {

    boolean statusValid =
        status.contains(questionnaireResponse.getStatus().toString().toLowerCase());
    boolean questionsValid = compareQuestionnaireResponseData(questionnaireResponse);

    return statusValid && questionsValid;
  }

  private Optional<QuestionnaireResponse> getQuestionnaireResponseForName(
      List<QuestionnaireResponse> questionnaireResponseList) {
    for (QuestionnaireResponse questionnaireResponse : questionnaireResponseList) {
      Pattern pattern = Pattern.compile(profileName);
      List<CanonicalType> profile = questionnaireResponse.getMeta().getProfile();
      for (CanonicalType profileType : profile) {
        String profileUrl = profileType.getValue();
        if (pattern.matcher(profileUrl).find()) {
          return Optional.of(questionnaireResponse);
        }
      }
    }
    return Optional.empty();
  }

  private boolean compareQuestionnaireResponseData(QuestionnaireResponse questionnaireResponse) {
    if (question.isEmpty()) {
      return true;
    }
    boolean returnValue = true;
    for (QuestionRule questionRule : question) {
      String questionId = questionRule.question();
      for (QuestionnaireResponse.QuestionnaireResponseItemComponent item :
          questionnaireResponse.getItem()) {
        if (item.getLinkId().equals(questionId)) {
          Coding coding = (Coding) item.getAnswer().getFirst().getValue();
          returnValue = returnValue && questionRule.value().contains(coding.getCode());
        }
      }
    }
    return returnValue;
  }
}
