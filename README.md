# Idam java client

[![Build Status](https://travis-ci.com/hmcts/idam-java-client.svg?branch=master)](https://travis-ci.com/hmcts/idam-java-client)
[ ![Download](https://api.bintray.com/packages/hmcts/hmcts-maven/idam-client/images/download.svg) ](https://bintray.com/hmcts/hmcts-maven/idam-client/_latestVersion)

This is a client library for interacting with the idam application.

## Getting started

This library is hosted on Azure DevOps Artifacts and can be used in your project by adding the following to your `build.gradle` file:

```gradle
repositories {
    maven {
        url 'https://pkgs.dev.azure.com/hmcts/Artifacts/_packaging/hmcts-lib/maven/v1'
    }
}

dependencies {
  implementation 'com.github.hmcts:idam-java-client:LATEST_TAG'
}
```


### Prerequisites

- [JDK 8](https://www.oracle.com/java)

## Usage

Add the library as a dependency of your project and configure the spring application to scan for Feign clients in the `uk.gov.hmcts.reform.idam` package:

```java
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam"})
public class YourSpringApplication { }
```

You will also need to set the spring configuration property of `idam.api.url` 

Optionally if you are authenticating a user you can use provide client configuration:
```yaml
idam:
  api:
    url: http://localhost:8080
  client:
    id: client_id
    secret: 123456
    redirect_uri: https://localhost:3000/receiver 
```

A client (IdamClient) is provided for interacting with the IdamApi feign client to simplify the log in flow:
```java
@Service
class UserService {
    private final IdamClient idamClient;
    
    UserService(IdamClient idamClient) {
        this.idamClient = idamClient;
    }
    
    public UserDetails authenticateUser(String username, String password) {
        return idamClient.authenticateUser(username, password);
    }
    
}

```

Components provided by this library will get automatically configured in a Spring context if `idam.api.url` configuration property is defined and does not equal `false`. 

## Building

The project uses [Gradle](https://gradle.org) as a build tool but you don't have install it locally since there is a
`./gradlew` wrapper script.  

To build project please execute the following command:

```bash
    ./gradlew build
```

## Developing

### Coding style tests

To run all checks (including unit tests) please execute the following command:

```bash
    ./gradlew check
```

## Versioning

We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
