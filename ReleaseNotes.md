<img align="right" width="250" height="47" src="media/Gematik_Logo_Flag.png"/> <br/> 
 
# Release notes

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
