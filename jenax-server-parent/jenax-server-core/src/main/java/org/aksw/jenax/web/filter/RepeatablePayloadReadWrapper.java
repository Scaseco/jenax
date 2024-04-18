package org.aksw.jenax.web.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class RepeatablePayloadReadWrapper
    extends HttpServletRequestWrapper
{
    protected Charset charset;
    protected Callable<InputStream> inputStreamSupplier;

    public RepeatablePayloadReadWrapper(HttpServletRequest request, Charset charset, Callable<InputStream> inputStreamSupplier) {
        super(request);
        this.charset = charset;
        this.inputStreamSupplier = inputStreamSupplier;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream raw;
        try {
            raw = inputStreamSupplier.call();
        } catch (Exception e) {
            throw new IOException(e);
        }

        return new ServletInputStream() {
            protected ReadListener readListener = null;
            protected boolean isFinished;

            @Override
            public int read() throws IOException {
                int result = raw.read();
                isFinished = result == -1;
                return result;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
//				this.readListener = readListener;
//				readListener.onDataAvailable();
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public boolean isFinished() {
                return isFinished;
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }
}
