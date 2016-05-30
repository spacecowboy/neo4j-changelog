package org.neo4j.changelog.github;

import okhttp3.*;

import java.io.IOException;

import static junit.framework.TestCase.fail;


public abstract class GitHubMockInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // Get Request URI.
        String url = chain.request().url().toString();

        try {
            return new Response.Builder()
                    .code(getCode(url))
                    .message(getMessage(url))
                    .request(original)
                    .protocol(Protocol.HTTP_1_0)
                    .body(ResponseBody.create(MediaType.parse("application/json"),
                            getBody(url).getBytes()))
                    .addHeader("content-type", "application/json")
                    .build();
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    /**
     *
     * @return http response body (in JSON format)
     * @param url
     */
    protected abstract String getBody(String url) throws Exception;

    /**
     *
     * @return http response message
     * @param url
     */
    protected String getMessage(String url) throws Exception {
        return "Success";
    }

    /**
     *
     * @return http error code (200 for success)
     * @param url
     */
    protected int getCode(String url) {
        return 200;
    }
}
