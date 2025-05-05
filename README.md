# Maven Plugin to manage secrets in Google Cloud Secret Manager

## Prerequisites

To use this plugin, you must provide credentials in a form to authenticate Google Cloud Secret Manager SDK. For instance:
```shell
gcloud auth application-default login
```

## Running integration tests

```shell
mvn -Prun-its integration-test
```

## License

This project is licensed under the Apache License, version 2.0. See the [LICENSE](LICENSE) file for details.

## References
* [Create and access a secret using Secret Manager](https://cloud.google.com/secret-manager/docs/create-secret-quickstart)