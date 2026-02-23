<div style="text-align:right"><img src="https://raw.githubusercontent.com/gematik/gematik.github.io/master/Gematik_Logo_Flag_With_Background.png" width="250" height="47" alt="gematik GmbH Logo"/> <br/> </div> <br/> 
 
# Release notes

## Release 1.4.3
- updated spring-parent to 2.14.20
- added obligatory comparison of notification category for supplementary disease notifications
- added obligatory comparison of notification category for nominal negative laboratory notifications 
- removed validation of Composition and DiagnosticReport status for laboratory scenarios
- enhanced notification validation service with metrics tracking for disease and laboratory notifications

## Release 1.4.2
- added feature flag FEATURE_FLAG_CODEMAPPING_SERVICE_BASE
- added use of service base client for code mapping connection
- updated spring-parent to 2.14.19

## Release 1.4.1
- remove feature flag FEATURE_FLAG_ACCEPTING_ANONYMOUS_NOTIFICATIONS
- add support for disease follow up notifications
- add feature flag FEATURE_FLAG_RETURN_DISEASE_FHIRPATH_VALIDATION_IN_RESPONSES

## Release 1.4.0
- add feature flag FEATURE_FLAG_FHIRPATH_VALIDATION_ENABLED
- add feature flag FEATURE_FLAG_RETURN_FHIRPATH_VALIDATION_IN_RESPONSES
- remove feature flag FEATURE_FLAG_NOTIFICATIONS_7_3
- updated dependencies
- bump spring parent to 2.14.2
- add validation of notificationId to be UUID, before sending request to get notification category to DLS, skip scenario if invalid

## Release 1.3.2
- add default feature flag FEATURE_FLAG_NOTIFICATIONS_7_3 to values.yaml

## Release 1.3.1
- updated dependencies
- updated base image
- relax clinicalStatus validation for nominal Bundles

## Release 1.3.0
- Updated ospo-resources for adding additional notes and disclaimer
- setting new ressources in helm chart
- setting new timeouts and retries in helm chart
- updating dependencies
- switched processing of §7.3 disease notifications and §6.1 notifications to fhir path


## Release 1.2.1
- First official GitHub-Release
- Optional check for questionnaire responses.
- Support for §7.4 notifications
- Dependency-Updates (CVEs et al.)
- Update Base-Image to OSADL

## Release 1.0.0 (2023-XX-XX)

### added

- SpringBoot 3.1.3

### changed

- Creation of Service, added validation logic for Laboratory Notifications