package org.isen.jee.tennis.harness;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.isen.jee.tennis.servlet.TennisScoreServlet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class JettyHarness {

    private static final int BASE_PORT = 9090;
    private static final int MAX_PORT_DEVIATION = 0;
    private static final String CONTEXT_PATH = "app";

    private HttpClient httpClient;
    private static Server server;

    private static int port = BASE_PORT
            + (int) (Math.random() * MAX_PORT_DEVIATION);

    protected static final String SERVLET_PATH = "/score";
    protected static final String SERVLET_URI = getBaseUri() + SERVLET_PATH;

    @BeforeClass
    public static void startServer() throws Exception {
        server = new Server(port);
        server.setStopAtShutdown(true);

        WebAppContext context = new WebAppContext();

        String wardir = "src/main/webapp";
        context.setResourceBase(wardir);
        context.setDescriptor(wardir + "WEB-INF/web.xml");
        context.setContextPath("/" + CONTEXT_PATH);
        context.setParentLoaderPriority(true);

        context.addServlet(new ServletHolder(new TennisScoreServlet()),
                SERVLET_PATH);

        server.setHandler(context);
        server.start();

    }

    public static String getBaseUri() {
        return "http://localhost:" + port + "/" + CONTEXT_PATH;
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();

    }

    @Before
    public void doBefore() throws Exception {
        httpClient = new DefaultHttpClient();
    }

    @After
    public void doAfter() throws Exception {
        httpClient.getConnectionManager().closeIdleConnections(0,
                TimeUnit.SECONDS);
    }

    public String get(String uri) throws IOException, HttpException {
        HttpGet get = new HttpGet(uri);
        return executeAndReturnResult(get);
    }

    public String delete(String uri) {
        HttpDelete delete = new HttpDelete(uri);
        return executeAndReturnResult(delete);
    }

    public String post(String uri, Map<String, String> params) {

        HttpPost post = new HttpPost(uri);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        for (Entry<String, String> paramEntry : params.entrySet()) {
            nameValuePairs.add(new BasicNameValuePair(paramEntry.getKey(),
                    paramEntry.getValue()));
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return executeAndReturnResult(post);
    }

    private String executeAndReturnResult(HttpRequestBase method) {
        try {
            HttpResponse response = httpClient.execute(method);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode >= 400) {
                throw new WebRuntimeException(responseCode, "Bad request");
            }
            InputStream content = response.getEntity().getContent();
            return IOUtils.toString(content, "UTF-8");
        } catch (IOException e) {
            throw new WebRuntimeException(500, e.getMessage());
        } finally {
            method.releaseConnection();
        }
    }
}
