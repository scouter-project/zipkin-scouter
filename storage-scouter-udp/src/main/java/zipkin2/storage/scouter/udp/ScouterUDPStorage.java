/*
 *  Copyright 2015-2018 the original author or authors.
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package zipkin2.storage.scouter.udp;

import zipkin2.Call;
import zipkin2.Callback;
import zipkin2.DependencyLink;
import zipkin2.Span;
import zipkin2.storage.QueryRequest;
import zipkin2.storage.SpanConsumer;
import zipkin2.storage.SpanStore;
import zipkin2.storage.StorageComponent;
import zipkin2.storage.scouter.udp.net.DataProxy;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ScouterUDPStorage extends StorageComponent implements SpanStore, SpanConsumer {
    private static final Logger logger = Logger.getLogger(ScouterUDPStorage.class.getName());
    private static ScouterConfig config;

    public static Builder newBuilder() {
        return new Builder();
    }

    private volatile DatagramSocket socket;
    private volatile boolean closeCalled;

    private ScouterUDPStorage(ScouterConfig config) {
        this.config = config;
    }

    public static ScouterConfig getConfig() {
        return config;
    }

    @Override
    public SpanStore spanStore() {
        return this;
    }

    @Override
    public SpanConsumer spanConsumer() {
        return this;
    }

    @Override
    public Call<Void> accept(List<Span> spans) {
        if (config.isDebug()) {
            logger.info("SPANS received : " + spans);
        }
        if (closeCalled) throw new IllegalStateException("closed");
        if (spans.isEmpty()) return Call.create(null);
        return new UDPCall(spans);
    }

    @Override
    public synchronized void close() {
        if (closeCalled) return;
        DatagramSocket socket = this.socket;
        if (socket != null) socket.close();
        closeCalled = true;
    }

    @Override
    public Call<List<List<Span>>> getTraces(QueryRequest queryRequest) {
        throw new UnsupportedOperationException("This is collector-only at the moment");
    }

    @Override
    public Call<List<Span>> getTrace(String s) {
        throw new UnsupportedOperationException("This is collector-only at the moment");
    }

    @Override
    public Call<List<String>> getServiceNames() {
        throw new UnsupportedOperationException("This is collector-only at the moment");
    }

    @Override
    public Call<List<String>> getSpanNames(String s) {
        throw new UnsupportedOperationException("This is collector-only at the moment");
    }

    @Override
    public Call<List<DependencyLink>> getDependencies(long l, long l1) {
        throw new UnsupportedOperationException("This is collector-only at the moment");
    }

    public static final class Builder extends StorageComponent.Builder {
        ScouterConfig config;

        /**
         * Ignored as Scouter doesn't accept 64-bit trace IDs. downgrade the ID in scouter.
         */
        @Override
        public Builder strictTraceId(boolean strictTraceId) {
            return this;
        }

        /**
         * Ignored as Scouter doesn't expose storage options
         */
        @Override
        public Builder searchEnabled(boolean searchEnabled) {
            return this;
        }

        /**
         * Defaults to the env variable
         */
        public Builder config(ScouterConfig config) {
            if (config == null) throw new IllegalArgumentException("config == null");
            this.config = config;
            return this;
        }

        @Override
        public ScouterUDPStorage build() {
            if (config == null) {
                config = new ScouterConfig(false, "127.0.0.1", 6100, 60000, new HashMap<>(), null);
            }

            return new ScouterUDPStorage(config);
        }

        Builder() {
        }
    }

    @Override
    public final String toString() {
        return "ScouterUDPStorage{address=" + config.getAddress() + ":" + config.getPort() + "}";
    }


    static class UDPCall extends Call.Base<Void> {
        private static final Logger logger = Logger.getLogger(UDPCall.class.getName());
        private final List<Span> spans;

        UDPCall(List<Span> spans) {
            this.spans = spans;
        }

        @Override
        protected Void doExecute() throws IOException {
            DataProxy.sendSpanContainer(spans, config);
            return null;
        }

        @Override
        protected void doEnqueue(Callback<Void> callback) {
            try {
                doExecute();
                callback.onSuccess(null);

            } catch (IOException | RuntimeException | Error e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                propagateIfFatal(e);
                callback.onError(e);
            }
        }

        @Override
        public Call<Void> clone() {
            return new UDPCall(spans);
        }
    }
}
