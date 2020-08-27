# notebook:note-service

The `note-service` Spring Boot Application is the api service code of the notebook application. 

It is responsible for access control, CRUD operations, notifications on user note operations.

## Building

The application can be built in several ways:

- Locally using Maven
- Locally using [pack](https://pack.io/)
- Locally using [skaffold](https://skaffold.dev/)
- Cloud using Google Cloud Build

### Maven

To build locally using maven, ensure that you have a Java 11+ JDK configured.

Use the following command to build and run tests:
 
```shell script
$ ./mvnw package
``` 

### Pack

Building with `pack` is used to build a container image for the service to run.
`pack` uses [CNCF buildpacks](https://buildpacks.io/) to build the application with all the required dependencies included.

The following command will build the application, and a container image to run the application.
```shell script
$ pack build notebook/noteservice --path . --builder gcr.io/buildpacks/builder:v1
``` 

### Skaffold

Building with `skaffold` is similar to building with `pack`.

```shell script
$ skaffold build
``` 

## Developing with [Minikube](https://minikube.sigs.k8s.io/docs/)

To build images to work with `minikube`, the docker client must be pointed at the minikube docker service.

The following command can be run to achieve this:

```shell script
$ eval "$(minikube docker-env)"
```

## Design

The note service is a [Spring Boot]() Reactive Web Application.
It is created using Spring Boot release `2.3.3.RELEASE`.
 
It uses the following thirdparty components:

1. spring-boot-starter-webflux
   - This includes all dependencies to create reactive REST applications.
2. spring-boot-starter-data-mongodb-reactive
   - Includes all dependencies used to create interact with mongodb in a reactive way.
3. lombok
   - Allows for the generation of boilerplate java code for data object creation.
   
## API

| Method | End Point | Accept | Description |
| ------ | --------- | ------ | ----------- |
| `POST` | `/note`     | application/json | Create a new note |
| `GET`  | `/note/{id}` | - | Get an existing note |
| `PUT`  | `/note/{id}` | application/json | Update an existing note |
| `DELETE` | `/note/{id}` | - | Delete an existing note |
| `GET`  | `/note` | - | Get all notes for the authenticated user. |

## Running the application locally

Launch a mongodb instance using docker

```shell script
$ docker run --name mongo --rm --detach -p 27017:27017 mongo:4.4.0-bionic
```

Launch the application using spring-boot maven plugin
```shell script
$ ./mvnw spring-boot:run
```

Create a new note using curl
```shell script
$ curl -X POST --url http://localhost:8080/note \
   --header 'Authorization: Bearer <jwt-token>' \
   --header 'Content-Type: application/json'  \
   --data-raw '{
           "content": "My First Note"
       }'
```

The result should be a `JSON` document describing the new note:
```json
{
  "id":"5f3ea2a82855e00a0dd21fcf",
  "owner":"fergus",
  "created":"2020-08-20T16:19:52.251271Z",
  "modified":"2020-08-20T16:19:52.251271Z",
  "content":"My First Note"
}
```

## Deployment

See [notebook-deployment.git](https://github.com/fbyrne/notebook-deployment) for notes on deployment to a kubernetes cluster.

## Dev lifecycle on minikube

```
$ eval $(minikube docker-env)
$ skaffold build && kubectl -n notebook-dev rollout restart deployment notes
```