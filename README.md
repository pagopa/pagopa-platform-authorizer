# pagoPa Platform Authorizer

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-platform-authorizer&metric=alert_status)](https://sonarcloud.io/dashboard?id=pagopa_pagopa-platform-authorizer)

Azure Function that exposes a set of API that permits to handle authorization for creditor institution and retrieve the info about their enrollment.

## Api Documentation 📖

See the [OpenApi 3 here.](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-platform-authorizer/main/openapi/openapi.json)

- [pagoPa Platform Authorizer](#pagopa-platform-authorizer)
  * [Api Documentation 📖](#api-documentation---)
  * [Run locally with Docker](#run-locally-with-docker)
    + [Test](#test)
  * [Run locally with Maven](#run-locally-with-maven---)
  * [Contributors 👥](#contributors---)
    + [Mainteiners](#mainteiners)


---

## Run locally with Docker
`docker build -t pagopa-functions-template .`

`docker run -p 8999:80 pagopa-functions-template`

### Test
`curl http://localhost:8999/example`

## Run locally with Maven

`mvn clean package`

`mvn azure-functions:run`

### Test
`curl http://localhost:7071/example` 

---


## Contributors 👥

Made with ❤️ by PagoPa S.p.A.

### Mainteiners

See `CODEOWNERS` file