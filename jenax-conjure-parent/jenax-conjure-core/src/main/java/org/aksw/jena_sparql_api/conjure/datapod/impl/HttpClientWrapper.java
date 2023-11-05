package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

public class HttpClientWrapper
    extends HttpClient
{
    protected HttpClient delegate;

    public HttpClientWrapper(HttpClient delegate) {
        super();
        this.delegate = delegate;
    }

    public HttpClient getDelegate() {
        return delegate;
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return getDelegate().cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return getDelegate().connectTimeout();
    }

    @Override
    public Redirect followRedirects() {
        return getDelegate().followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return getDelegate().proxy();
    }

    @Override
    public SSLContext sslContext() {
        return getDelegate().sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return getDelegate().sslParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return getDelegate().authenticator();
    }

    @Override
    public Version version() {
        return getDelegate().version();
    }

    @Override
    public Optional<Executor> executor() {
        return getDelegate().executor();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return getDelegate().send(request, responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler) {
        return sendAsync(request, responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler,
            PushPromiseHandler<T> pushPromiseHandler) {
        return sendAsync(request, responseBodyHandler, pushPromiseHandler);
    }
}
