<img align="right" width="250" height="47" src="media/Gematik_Logo_Flag.png"/> <br/> 

# Lifecycle-Validation-Service

[![Quality Gate Status](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Alifecycle-validation-service&metric=alert_status&token=sqb_599b9780a2f1ee3af06b499e16588d68d506a13c)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Alifecycle-validation-service)[![Vulnerabilities](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Alifecycle-validation-service&metric=vulnerabilities&token=sqb_599b9780a2f1ee3af06b499e16588d68d506a13c)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Alifecycle-validation-service)[![Bugs](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Alifecycle-validation-service&metric=bugs&token=sqb_599b9780a2f1ee3af06b499e16588d68d506a13c)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Alifecycle-validation-service)[![Code Smells](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Alifecycle-validation-service&metric=code_smells&token=sqb_599b9780a2f1ee3af06b499e16588d68d506a13c)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Alifecycle-validation-service)[![Lines of Code](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Alifecycle-validation-service&metric=ncloc&token=sqb_599b9780a2f1ee3af06b499e16588d68d506a13c)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Alifecycle-validation-service)[![Coverage](https://sonar.prod.ccs.gematik.solutions/api/project_badges/measure?project=de.gematik.demis%3Alifecycle-validation-service&metric=coverage&token=sqb_599b9780a2f1ee3af06b499e16588d68d506a13c)](https://sonar.prod.ccs.gematik.solutions/dashboard?id=de.gematik.demis%3Alifecycle-validation-service)

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#release-notes">Release Notes</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a>
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>

## About The Project

The Lifecycle-Validation-Service (LVS) validates the lifecycle rules of a pathogen notification according to
[Implementierungsleitfaden für DEMIS Erregernachweismeldung](https://simplifier.net/guide/rki.demis.laboratory/Home/guide-lifecyclemanagement.guide.md?version=current).

### Release Notes

See [ReleaseNotes.md](./ReleaseNotes.md) for all information regarding the (newest) releases.

## Getting Started

### Prerequisites

The Project requires Java 21 and Maven 3.8+.

### Installation

The Project can be built with the following command:

```sh
mvn clean install
```

The Docker Image associated to the service can be built with the extra profile `docker`:

```sh
mvn clean install -Pdocker
```

## Usage

The application can be executed from a JAR file or a Docker Image:

```sh
# As JAR Application
java -jar target/lifecycle-validation-service.jar
# As Docker Image
docker run --rm -it -p 8080:8080 lifecycle-validation-service:latest
```

It can also be deployed on Kubernetes by using the Helm Chart defined in the
folder `deployment/helm/lifecycle-validation-service`:

```ssh
helm install lifecycle-validation-service ./deployment/helm/lifecycle-validation-service
```

## Security Policy

If you want to see the security policy, please check our [SECURITY.md](.github/SECURITY.md).

## Contributing

If you want to contribute, please check our [CONTRIBUTING.md](.github/CONTRIBUTING.md).

## License

EUROPEAN UNION PUBLIC LICENCE v. 1.2

EUPL © the European Union 2007, 2016

Copyright (c) 2023 gematik GmbH

See [LICENSE](LICENSE.md).

## Contact

E-Mail to [DEMIS Entwicklung](mailto:demis-entwicklung@gematik.de?subject=[GitHub]%20VLifecycle-Validation-Service)
