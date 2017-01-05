package com.auth0;

import com.auth0.json.UserInfo;
import com.auth0.net.CustomRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.Assert.assertThat;

public class AuthAPITest {

    private static final String DOMAIN = "domain.auth0.com";
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";

    @Mock
    OkHttpClient client;
    private MockServer server;
    private AuthAPI api;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        server = new MockServer();
        api = new AuthAPI(server.getBaseUrl(), CLIENT_ID, CLIENT_SECRET);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    // Configuration

    @Test
    public void shouldAcceptDomainWithNoScheme() throws Exception {
        AuthAPI api = new AuthAPI("me.something.com", CLIENT_ID, CLIENT_SECRET);

        HttpUrl parsed = HttpUrl.parse(api.getBaseUrl());
        MatcherAssert.assertThat(parsed, CoreMatchers.is(notNullValue()));
        MatcherAssert.assertThat(parsed.host(), CoreMatchers.is("me.something.com"));
        MatcherAssert.assertThat(parsed.scheme(), CoreMatchers.is("https"));
    }

    @Test
    public void shouldAcceptDomainWithHttpScheme() throws Exception {
        AuthAPI api = new AuthAPI("http://me.something.com", CLIENT_ID, CLIENT_SECRET);

        HttpUrl parsed = HttpUrl.parse(api.getBaseUrl());
        MatcherAssert.assertThat(parsed, CoreMatchers.is(notNullValue()));
        MatcherAssert.assertThat(parsed.host(), CoreMatchers.is("me.something.com"));
        MatcherAssert.assertThat(parsed.scheme(), CoreMatchers.is("http"));
    }

    @Test
    public void shouldThrowWhenDomainIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'domain' cannot be null!");
        new AuthAPI(null, CLIENT_ID, CLIENT_SECRET);
    }

    @Test
    public void shouldThrowWhenClientIdIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'client id' cannot be null!");
        new AuthAPI(DOMAIN, null, CLIENT_SECRET);
    }

    @Test
    public void shouldThrowWhenClientSecretIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'client secret' cannot be null!");
        new AuthAPI(DOMAIN, CLIENT_ID, null);
    }


    //Authorize

    @Test
    public void shouldThrowWhenAuthorizeUrlBuilderConnectionIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'connection' cannot be null!");
        api.authorize(null, "https://domain.auth0.com/callback");
    }

    @Test
    public void shouldThrowWhenAuthorizeUrlBuilderRedirectUriIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'redirect uri' cannot be null!");
        api.authorize("my-connection", null);
    }

    @Test
    public void shouldGetAuthorizeUrlBuilder() throws Exception {
        AuthorizeUrlBuilder builder = api.authorize("my-connection", "https://domain.auth0.com/callback");
        assertThat(builder, is(notNullValue()));
    }

    @Test
    public void shouldSetAuthorizeUrlBuilderDefaultValues() throws Exception {
        AuthAPI api = new AuthAPI("domain.auth0.com", CLIENT_ID, CLIENT_SECRET);
        String url = api.authorize("my-connection", "https://domain.auth0.com/callback").build();
        HttpUrl parsed = HttpUrl.parse(url);

        MatcherAssert.assertThat(url, not(isEmptyOrNullString()));
        MatcherAssert.assertThat(parsed, is(notNullValue()));
        MatcherAssert.assertThat(parsed.scheme(), is("https"));
        MatcherAssert.assertThat(parsed.host(), is("domain.auth0.com"));
        MatcherAssert.assertThat(parsed.pathSegments().size(), is(1));
        MatcherAssert.assertThat(parsed.pathSegments().get(0), is("authorize"));

        MatcherAssert.assertThat(parsed.queryParameter("response_type"), is("code"));
        MatcherAssert.assertThat(parsed.queryParameter("client_id"), is(CLIENT_ID));
        MatcherAssert.assertThat(parsed.queryParameter("redirect_uri"), is("https://domain.auth0.com/callback"));
        MatcherAssert.assertThat(parsed.queryParameter("connection"), is("my-connection"));
    }


    //Logout

    @Test
    public void shouldThrowWhenLogoutUrlBuilderReturnToUrlIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("'return to url' cannot be null!");
        api.logout(null, true);
    }

    @Test
    public void shouldGetLogoutUrlBuilder() throws Exception {
        LogoutUrlBuilder builder = api.logout("https://domain.auth0.com/callback", true);
        assertThat(builder, is(notNullValue()));
    }

    @Test
    public void shouldSetLogoutUrlBuilderDefaultValues() throws Exception {
        AuthAPI api = new AuthAPI("domain.auth0.com", CLIENT_ID, CLIENT_SECRET);
        String url = api.logout("https://my.domain.com/welcome", false).build();
        HttpUrl parsed = HttpUrl.parse(url);

        MatcherAssert.assertThat(url, not(isEmptyOrNullString()));
        MatcherAssert.assertThat(parsed, is(notNullValue()));
        MatcherAssert.assertThat(parsed.scheme(), is("https"));
        MatcherAssert.assertThat(parsed.host(), is("domain.auth0.com"));
        MatcherAssert.assertThat(parsed.pathSegments().size(), is(2));
        MatcherAssert.assertThat(parsed.pathSegments().get(0), is("v2"));
        MatcherAssert.assertThat(parsed.pathSegments().get(1), is("logout"));

        MatcherAssert.assertThat(parsed.queryParameter("client_id"), is(nullValue()));
        MatcherAssert.assertThat(parsed.queryParameter("returnTo"), is("https://my.domain.com/welcome"));
    }

    @Test
    public void shouldSetLogoutUrlBuilderDefaultValuesAndClientId() throws Exception {
        AuthAPI api = new AuthAPI("domain.auth0.com", CLIENT_ID, CLIENT_SECRET);
        String url = api.logout("https://my.domain.com/welcome", true).build();
        HttpUrl parsed = HttpUrl.parse(url);

        MatcherAssert.assertThat(url, not(isEmptyOrNullString()));
        MatcherAssert.assertThat(parsed, is(notNullValue()));
        MatcherAssert.assertThat(parsed.scheme(), is("https"));
        MatcherAssert.assertThat(parsed.host(), is("domain.auth0.com"));
        MatcherAssert.assertThat(parsed.pathSegments().size(), is(2));
        MatcherAssert.assertThat(parsed.pathSegments().get(0), is("v2"));
        MatcherAssert.assertThat(parsed.pathSegments().get(1), is("logout"));

        MatcherAssert.assertThat(parsed.queryParameter("client_id"), is(CLIENT_ID));
        MatcherAssert.assertThat(parsed.queryParameter("returnTo"), is("https://my.domain.com/welcome"));
    }

    //Userinfo

    @Test
    public void shouldCreateUserInfoRequest() throws Exception {
        CustomRequest<UserInfo> request = (CustomRequest<UserInfo>) api.userInfo("accessToken");
        assertThat(request, is(notNullValue()));

        server.userInfoResponse();
        UserInfo response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest.getMethod(), is("GET"));
        assertThat(recordedRequest.getPath(), is("/userinfo"));
        assertThat(recordedRequest.getHeader("Content-Type"), is("application/json"));
        assertThat(recordedRequest.getHeader("Authorization"), is("Bearer accessToken"));

        assertThat(response, is(notNullValue()));
        assertThat(response.getValues(), is(notNullValue()));
        assertThat(response.getValues(), hasEntry("email_verified", (Object) false));
        assertThat(response.getValues(), hasEntry("email", (Object) "test.account@userinfo.com"));
        assertThat(response.getValues(), hasEntry("clientID", (Object) "q2hnj2iu..."));
        assertThat(response.getValues(), hasEntry("updated_at", (Object) "2016-12-05T15:15:40.545Z"));
        assertThat(response.getValues(), hasEntry("name", (Object) "test.account@userinfo.com"));
        assertThat(response.getValues(), hasEntry("picture", (Object) "https://s.gravatar.com/avatar/dummy.png"));
        assertThat(response.getValues(), hasEntry("user_id", (Object) "auth0|58454..."));
        assertThat(response.getValues(), hasEntry("nickname", (Object) "test.account"));
        assertThat(response.getValues(), hasEntry("created_at", (Object) "2016-12-05T11:16:59.640Z"));
        assertThat(response.getValues(), hasEntry("sub", (Object) "auth0|58454..."));
        assertThat(response.getValues(), hasKey("identities"));
        List<Map<String, Object>> identities = (List<Map<String, Object>>) response.getValues().get("identities");
        assertThat(identities, hasSize(1));
        assertThat(identities.get(0), hasEntry("user_id", (Object) "58454..."));
        assertThat(identities.get(0), hasEntry("provider", (Object) "auth0"));
        assertThat(identities.get(0), hasEntry("connection", (Object) "Username-Password-Authentication"));
        assertThat(identities.get(0), hasEntry("isSocial", (Object) false));
    }

    @Test
    public void shouldCreateResetPasswordRequest() throws Exception {
        CustomRequest<Void> request = (CustomRequest<Void>) api.resetPassword("me@auth0.com", "db-connection");
        assertThat(request, is(notNullValue()));

        server.resetPasswordRequest();
        Void response = request.execute();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest.getMethod(), is("POST"));
        assertThat(recordedRequest.getPath(), is("/dbconnections/change_password"));
        assertThat(recordedRequest.getHeader("Content-Type"), is("application/json"));
        Map<String, String> body = bodyFromRequest(recordedRequest);
        assertThat(body, hasEntry("email", "me@auth0.com"));
        assertThat(body, hasEntry("connection", "db-connection"));
        assertThat(body, hasEntry("client_id", CLIENT_ID));

        assertThat(response, is(nullValue()));
    }
    
    private Map<String, String> bodyFromRequest(RecordedRequest request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MapType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        Buffer body = request.getBody();
        try {
            return mapper.readValue(body.inputStream(), mapType);
        } catch (IOException e) {
            throw e;
        } finally {
            body.close();
        }
    }
}