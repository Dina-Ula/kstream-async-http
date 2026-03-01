package demo.rcbs;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public final class HttpXmlClient implements AutoCloseable {
    private final CloseableHttpClient http;

    public HttpXmlClient(int maxTotal, int maxPerRoute, int connectMs, int readMs) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(maxPerRoute);

        RequestConfig rc = RequestConfig.custom()
                .setConnectTimeout(connectMs)
                .setConnectionRequestTimeout(connectMs) // pool wait
                .setSocketTimeout(readMs)
                .build();

        this.http = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(rc)
                .evictExpiredConnections()
                .evictIdleConnections(30, TimeUnit.SECONDS)
                .build();
    }

    public String postXml(String url, String xml) throws Exception {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/xml; charset=UTF-8");
        post.setEntity(new StringEntity(xml, StandardCharsets.UTF_8));

        try (CloseableHttpResponse resp = http.execute(post)) {
            int code = resp.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
            if (code >= 200 && code < 300) return body;
            throw new RuntimeException("HTTP " + code + " body=" + body);
        }
    }

    @Override public void close() throws Exception {
        http.close();
    }
}