# Auth0 Java

[![Build-Circle][circle-ci-badge]][circle-ci-url]
[![MIT][mit-badge]][mit-url]
[![Maven][maven-badge]][maven-url]
[![JCenter][jcenter-badge]][jcenter-url]
[![codecov][codecov-badge]][codecov-url]

Java client library for the [Auth0](https://auth0.com) platform.

## Download

Get Auth0 Java via Maven:

```xml
<dependency>
  <groupId>com.auth0</groupId>
  <artifactId>auth0</artifactId>
  <version>1.5.1</version>
</dependency>
```

or Gradle:

```gradle
compile 'com.auth0:auth0:1.5.1'
```


### Android

The Auth0 Authentication API and User's Management API are available for Android in the `auth0.android` library. Check https://github.com/auth0/auth0.android for more information.


## Auth API

The implementation is based on the [Authentication API Docs](https://auth0.com/docs/api/authentication).

Create an `AuthAPI` instance by providing the client data from the [dashboard](https://manage.auth0.com/#/clients).

```java
AuthAPI auth = new AuthAPI("{YOUR_DOMAIN}", "{YOUR_CLIENT_ID}", "{YOUR_CLIENT_SECRET}");
```

### Authorize - /authorize

Creates an `AuthorizeUrlBuilder` to authenticate the user with an OAuth provider. The `redirectUri` must be white-listed in the "Allowed Callback URLs" section of the Client Settings. Parameters can be added to the final url by using the builder methods. When ready, call `build()` and obtain the Url.

```AuthorizeUrlBuilder authorizeUrl(String redirectUri)```

Example:
```java
String url = auth.authorizeUrl("https://me.auth0.com/callback")
    .withConnection("facebook")
    .withAudience("https://api.me.auth0.com/users")
    .withScope("openid contacts")
    .withState("state123")
    .build();
```

### Logout - /v2/logout
Creates a `LogoutUrlBuilder` to log out the user. The `returnToUrl` must be white-listed in the "Allowed Logout URLs" section of the Dashboard, depending on the value of `setClientId` this configuration should be set in the Client or in the Tenant. Parameters can be added to the final url by using the builder methods. When ready, call `build()` and obtain the Url.

`LogoutUrlBuilder logoutUrl(String returnToUrl, boolean setClientId)`

Example:
```java
String url = auth.logoutUrl("https://me.auth0.com/home", true)
    .useFederated(true)
    .build();
```

### UserInfo - /userinfo
Creates a request to get the user information associated to a given access token. This will only work if the token has been granted the `openid` scope.

`Request<UserInfo> userInfo(String accessToken)`

Example:
```java
Request<UserInfo> request = auth.userInfo("nisd1h9dk.....s1doWJOsaf");
try {
    UserInfo info = request.execute();
    // info.getValues();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

### Reset Password - /dbconnections/change_password
Creates a request to reset the user's password. This will only work for db connections.

`Request resetPassword(String email, String connection)`

Example:
```java
Request request = auth.resetPassword("user@domain.com", "Username-Password-Authentication");
try {
    request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```


### Sign Up - /dbconnections/signup
Creates a request to create a user. Up to 10 additional Sign Up fields can be added to the request. This will only work for db connections.

`SignUpRequest signUp(String email, String username, String password, String connection)`

`SignUpRequest signUp(String email, String password, String connection)`

Example:
```java
Map<String, String> fields = new HashMap<>();
fields.put("age", "25");
fields.put("city", "Buenos Aires");
SignUpRequest request = auth.signUp("user@domain.com", "username", "password123", "Username-Password-Authentication")
    .setCustomFields(fields);
try {
    CreatedUser user = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

### Exchange the Authorization Code - /oauth/token

Creates a request to exchange the `code` previously obtained by calling the /authorize endpoint. The redirect uri must be the one sent in the /authorize call.

`AuthRequest exchangeCode(String code, String redirectUri)`

Example:
```java
AuthRequest request = auth.exchangeCode("asdfgh", "https://me.auth0.com/callback")
    .setAudience("https://api.me.auth0.com/users")
    .setScope("openid contacts");
try {
    TokenHolder holder = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

### Log In with Password - /oauth/token

Creates a request to log in the user with `username` and `password`. The connection used is the one defined as "Default Directory" in the account settings.

`AuthRequest login(String emailOrUsername, String password)`

Example:
```java
AuthRequest request = auth.login("me@domain.com", "password123")
    .setAudience("https://api.me.auth0.com/users")
    .setScope("openid contacts");
try {
    TokenHolder holder = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

### Log In with Password Realm - /oauth/token

Creates a request to log in the user with `username` and `password` using the Password Realm.

`AuthRequest login(String emailOrUsername, String password, String realm)`

Example:
```java
AuthRequest request = auth.login("me@domain.com", "password123", "Username-Password-Authentication")
    .setAudience("https://api.me.auth0.com/users")
    .setScope("openid contacts");
try {
    TokenHolder holder = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

### Request Token for Audience - /oauth/token

Creates a request to get a Token for the given Audience.

`AuthRequest requestToken(String audience)`

Example:
```java
AuthRequest request = auth.requestToken("https://api.me.auth0.com/users")
    .setScope("openid contacts");
try {
    TokenHolder holder = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

### Revoke Refresh Token

Creates a request to revoke an existing Refresh Token.

`Request<Void> revokeToken(String refreshToken)`

Example:
```java
Request<Void> request = auth.revokeToken("nisd1h9dks1doWJOsaf");
try {
    request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

### Renew Authentication

Creates a request to renew the authentication and get fresh new credentials using a valid Refresh Token.

`AuthRequest renewAuth(String refreshToken)`

Example:
```java
AuthRequest request = auth.renewAuth("nisd1h9dks1doWJOsaf");
try {
    TokenHolder holder = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```


## Management API

The implementation is based on the [Management API Docs](https://auth0.com/docs/api/management/v2).

Create a `ManagementAPI` instance by providing the domain from the [client dashboard](https://manage.auth0.com/#/clients) and the API Token. Click [here](https://auth0.com/docs/api/management/v2#!/Introduction/Getting_an_API_token) for more information on how to obtain a valid API Token.

```java
ManagementAPI mgmt = new ManagementAPI("{YOUR_DOMAIN}", "{YOUR_API_TOKEN}");
```

The Management API is divided into different entities. Each of them have the list, create, update, delete and update methods plus a few more if corresponds. The calls are authenticated using the API Token given in the `ManagementAPI` instance creation and must contain the `scope` required by each entity. See the javadoc for details on which `scope` is expected for each call.

* **Client Grants:** See [Docs](https://auth0.com/docs/api/management/v2#!/Client_Grants/get_client_grants). Access the methods by calling `mgmt.clientGrants()`.
* **Clients:** See [Docs](https://auth0.com/docs/api/management/v2#!/Clients/get_clients). Access the methods by calling `mgmt.clients()`.
* **Connections:** See [Docs](https://auth0.com/docs/api/management/v2#!/Connections/get_connections). Access the methods by calling `mgmt.connections()`.
* **Device Credentials:** See [Docs](https://auth0.com/docs/api/management/v2#!/Device_Credentials/get_device_credentials). Access the methods by calling `mgmt.deviceCredentials()`.
* **Logs:** See [Docs](https://auth0.com/docs/api/management/v2#!/Logs/get_logs). Access the methods by calling `mgmt.logEvents()`.
* **Rules:** See [Docs](https://auth0.com/docs/api/management/v2#!/Rules/get_rules). Access the methods by calling `mgmt.rules()`.
* **User Blocks:** See [Docs](https://auth0.com/docs/api/management/v2#!/User_Blocks/get_user_blocks). Access the methods by calling `mgmt.userBlocks()`.
* **Users:** See [this](https://auth0.com/docs/api/management/v2#!/Users/get_users) and [this](https://auth0.com/docs/api/management/v2#!/Users_By_Email) Docs. Access the methods by calling `mgmt.users()`.
* **Blacklists:** See [Docs](https://auth0.com/docs/api/management/v2#!/Blacklists/get_tokens). Access the methods by calling `mgmt.blacklists()`.
* **Emails:** See [Docs](https://auth0.com/docs/api/management/v2#!/Emails/get_provider). Access the methods by calling `mgmt.emailProvider()`.
* **Guardian:** See [Docs](https://auth0.com/docs/api/management/v2#!/Guardian/get_factors). Access the methods by calling `mgmt.guardian()`.
* **Stats:** See [Docs](https://auth0.com/docs/api/management/v2#!/Stats/get_active_users). Access the methods by calling `mgmt.stats()`.
* **Tenants:** See [Docs](https://auth0.com/docs/api/management/v2#!/Tenants/get_settings). Access the methods by calling `mgmt.tenants()`.
* **Tickets:** See [Docs](https://auth0.com/docs/api/management/v2#!/Tickets/post_email_verification). Access the methods by calling `mgmt.tickets()`.
* **Resource Servers:** See [Docs](https://auth0.com/docs/api/management/v2#!/Resource_Servers/get_resource_servers). Access the methods by calling `mgmt.resourceServers()`.


### Users

#### List by Email

Creates a request to list the Users by Email. This is the preferred and fastest way to query Users by Email, and should be used instead of calling the generic list method with an email query. An API Token with scope `read:users` is needed. If you want the identities.access_token property to be included, you will also need the scope `read:user_idp_tokens`.
You can pass an optional Filter to narrow the results in the response.

`Request<UsersPage> listByEmail(String email, UserFilter filter)`

Example:
```java
FieldsFilter filter = new FieldsFilter(...);
Request<UsersPage> request = mgmt.users().listByEmail("johndoe@auth0.com", filter);
try {
    UsersPage response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### List

Creates a request to list the Users. An API Token with scope `read:users` is needed. If you want the identities.access_token property to be included, you will also need the scope `read:user_idp_tokens`.
You can pass an optional Filter to narrow the results in the response.

`Request<UsersPage> list(UserFilter filter)`

Example:
```java
UserFilter filter = new UserFilter(...);
Request<UsersPage> request = mgmt.users().list(filter);
try {
    UsersPage response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### Get

Creates a request to get a User. An API Token with scope `read:users` is needed. If you want the identities.access_token property to be included, you will also need the scope `read:user_idp_tokens`.
You can pass an optional Filter to narrow the results in the response.

`Request<User> get(String userId, UserFilter filter)`

Example:
```java
UserFilter filter = new UserFilter(...);
Request<User> request = mgmt.users().get("auth0|123", filter);
try {
    User response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### Create

Creates a request to create a User. An API Token with scope `create:users` is needed.

`Request<User> create(User user)`

Example:
```java
User data = new User(...);
Request<User> request = mgmt.users().create(data);
try {
    User response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### Delete

Creates a request to delete a User. An API Token with scope `delete:users` is needed.

`Request delete(String userId)`

Example:
```java
Request request = mgmt.users().delete("auth0|123");
try {
    request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### Update

Creates a request to update a User. An API Token with scope `update:users` is needed. If you're updating app_metadata you'll also need `update:users_app_metadata` scope.

`Request<User> update(String userId, User user)`

Example:
```java
User data = new User(...);
Request request = mgmt.users().update("auth0|123", data);
try {
    User response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### Get Guardian Enrollments

Creates a request to list the User's Guardian Enrollments. An API Token with scope `read:users` is needed.

`Request<List<Enrollment>> getEnrollments(String userId)`

Example:
```java
Request<List<Enrollment>> request = mgmt.users().getEnrollments("auth0|123");
try {
    List<Enrollment> response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### Get Log Events

Creates a request to list the User's Log Events. An API Token with scope `read:logs` is needed.
You can pass an optional Filter to narrow the results in the response.

`Request<LogEventsPage> getLogEvents(String userId, LogEventFilter filter)`

Example:
```java
LogEventFilter filter = new LogEventFilter(...);
Request<LogEventsPage> request = mgmt.users().getLogEvents("auth0|123", filter);
try {
    LogEventsPage response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```


#### Delete Multifactor Provider

Creates a request to delete the User's Multifactor Provider. An API Token with scope `update:users` is needed.

`Request deleteMultifactorProvider(String userId, String provider)`

Example:
```java
Request request = mgmt.users().deleteMultifactorProvider("auth0|123", "duo");
try {
    request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### Rotate Recovery Code

Creates a request to rotate the User's Recovery Code. An API Token with scope `update:users` is needed.

`Request<RecoveryCode> rotateRecoveryCode(String userId)`

Example:
```java
Request<RecoveryCode> request = mgmt.users().rotateRecoveryCode("auth0|123");
try {
    RecoveryCode response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### Link Identities

Creates a request to link two User identities. An API Token with scope `update:users` is needed.

`Request<List<Identity>> linkIdentity(String primaryUserId, String secondaryUserId, String provider, String connectionId)`

Example:
```java
Request<List<Identities>> request = mgmt.users().linkIdentity("auth0|123", "124", "facebook", "c90");
try {
    List<Identities> response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```

#### Un-Link Identities

Creates a request to un-link two User identities. An API Token with scope `update:users` is needed.

`Request<List<Identity>> unlinkIdentity(String primaryUserId, String secondaryUserId, String provider)`

Example:
```java
Request<List<Identities>> request = mgmt.users().unlinkIdentity("auth0|123", "124", "facebook");
try {
    List<Identities> response = request.execute();
} catch (APIException exception) {
    // api error
} catch (Auth0Exception exception) {
    // request error
}
```



## Error Handling

The API Clients throw `Auth0Exception` when an unexpected error happens on a request execution, i.e. Connectivity or Timeout error.

If you need to handle different error scenarios you need to catch first `APIException`, which provides methods to get a clue of what went wrong.

The APIExplorer includes a list of response messages for each endpoint. You can get a clue of what went wrong by asking the Http status code: `exception.getStatusCode()`. i.e. a `status_code=403` would mean that the token has an insufficient scope.

An error code will be included to categorize the type of error, you can get it by calling `exception.getError()`. If you want to see a user friendly description of what happened and why the request is failing check the `exception.getDescription()`.


```
Example exception data
{
  statusCode: 400,
  description: "Query validation error: 'String 'users' does not match pattern. Must be a comma separated list of the following values: name,strategy,options,enabled_clients,id,provisioning_ticket_url' on property fields (A comma separated list of fields to include or exclude (depending on include_fields) from the result, empty to retrieve all fields).",
  error: "invalid_query_string"
}
```

## Documentation

For more information about [auth0](http://auth0.com) check our [documentation page](http://docs.auth0.com/).

## What is Auth0?

Auth0 helps you to:

* Add authentication with [multiple authentication sources](https://docs.auth0.com/identityproviders), either social like **Google, Facebook, Microsoft Account, LinkedIn, GitHub, Twitter, Box, Salesforce, amont others**, or enterprise identity systems like **Windows Azure AD, Google Apps, Active Directory, ADFS or any SAML Identity Provider**.
* Add authentication through more traditional **[username/password databases](https://docs.auth0.com/mysql-connection-tutorial)**.
* Add support for **[linking different user accounts](https://docs.auth0.com/link-accounts)** with the same user.
* Support for generating signed [Json Web Tokens](https://docs.auth0.com/jwt) to call your APIs and **flow the user identity** securely.
* Analytics of how, when and where users are logging in.
* Pull data from other sources and add it to the user profile, through [JavaScript rules](https://docs.auth0.com/rules).

## Create a free Auth0 Account

1. Go to [Auth0](https://auth0.com) and click Sign Up.
2. Use Google, GitHub or Microsoft Account to login.

## Issue Reporting

If you have found a bug or if you have a feature request, please report them at this repository issues section. Please do not report security vulnerabilities on the public GitHub issue tracker. The [Responsible Disclosure Program](https://auth0.com/whitehat) details the procedure for disclosing security issues.

## Author

[Auth0](https://auth0.com)

## License

This project is licensed under the MIT license. See the [LICENSE](LICENSE) file for more info.


<!-- Vars -->

[circle-ci-badge]: https://img.shields.io/circleci/project/github/auth0/auth0-java.svg?style=flat
[circle-ci-url]: https://circleci.com/gh/auth0/auth0-java/tree/master
[mit-badge]: http://img.shields.io/:license-mit-blue.svg?style=flat
[mit-url]: https://raw.githubusercontent.com/auth0/auth0-java/master/LICENSE
[maven-badge]: https://img.shields.io/maven-central/v/com.auth0/auth0.svg
[maven-url]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.auth0%22%20AND%20a%3A%22auth0%22
[jcenter-badge]: https://api.bintray.com/packages/auth0/java/auth0/images/download.svg
[jcenter-url]: https://bintray.com/auth0/java/auth0/_latestVersion
[codecov-badge]: https://codecov.io/gh/auth0/auth0-java/branch/master/graph/badge.svg
[codecov-url]: https://codecov.io/gh/auth0/auth0-java
