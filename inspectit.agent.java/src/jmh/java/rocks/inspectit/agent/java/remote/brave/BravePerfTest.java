package rocks.inspectit.agent.java.remote.brave;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollector;
import com.github.kristofa.brave.LoggingSpanCollector;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpClientRequest;
import com.github.kristofa.brave.http.HttpClientRequestAdapter;
import com.github.kristofa.brave.http.HttpClientResponseAdapter;
import com.github.kristofa.brave.http.HttpResponse;
import com.github.kristofa.brave.http.HttpServerRequest;
import com.github.kristofa.brave.http.HttpServerRequestAdapter;
import com.github.kristofa.brave.http.HttpServerResponseAdapter;
import com.github.kristofa.brave.http.SpanNameProvider;

/**
 * @author Ivan Senic
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(value = 1)
@State(Scope.Benchmark)
public class BravePerfTest {

	private final static SpanNameProvider SPAN_NAME_PROVIDER = new DefaultSpanNameProvider();

	private Brave brave;

	@Param({ "empty" })
	String collectorType;

	@Setup
	public void setup() {
		SpanCollector collector;
		if ("empty".equals(collectorType)) {
			collector = new EmptySpanCollector();
		} else {
			collector = new LoggingSpanCollector();
		}
		Brave.Builder builder = new Brave.Builder("perf-test");
		brave = builder.spanCollector(collector).traceSampler(Sampler.create(0f)).build();
	}

	@Benchmark
	public void clientRequestResponse() {
		HttpClientRequest request = new HttpClientRequest() {
			public URI getUri() {
				try {
					return new URI("http://localhost");
				} catch (URISyntaxException e) {
					return null;
				}
			}
			public String getHttpMethod() {
				return "GET";
			}
			public void addHeader(String header, String value) {
			}
		};
		HttpClientRequestAdapter requestAdapter = new HttpClientRequestAdapter(request, SPAN_NAME_PROVIDER);
		brave.clientRequestInterceptor().handle(requestAdapter);

		HttpResponse response = new HttpResponse() {
			public int getHttpStatusCode() {
				return 0;
			}
		};
		HttpClientResponseAdapter responseAdapter = new HttpClientResponseAdapter(response);
		brave.clientResponseInterceptor().handle(responseAdapter);
	}

	@Benchmark
	public void serverRequestResponse() {
		HttpServerRequest serverRequest = new HttpServerRequest() {
			public URI getUri() {
				try {
					return new URI("http://localhost");
				} catch (URISyntaxException e) {
					return null;
				}
			}
			public String getHttpMethod() {
				return "GET";
			}
			public String getHttpHeaderValue(String headerName) {
				return null;
			}
		};
		HttpServerRequestAdapter requestAdapter = new HttpServerRequestAdapter(serverRequest, SPAN_NAME_PROVIDER);
		brave.serverRequestInterceptor().handle(requestAdapter);

		HttpResponse response = new HttpResponse() {
			public int getHttpStatusCode() {
				return 0;
			}
		};
		HttpServerResponseAdapter responseAdapter = new HttpServerResponseAdapter(response);
		brave.serverResponseInterceptor().handle(responseAdapter);
	}

	@Benchmark
	public void cleintServerRequestResponse() {
		final Map<String, String> headers = new HashMap<String, String>();
		HttpServerRequest serverRequest = new HttpServerRequest() {
			public URI getUri() {
				try {
					return new URI("http://localhost");
				} catch (URISyntaxException e) {
					return null;
				}
			}

			public String getHttpMethod() {
				return "GET";
			}

			public String getHttpHeaderValue(String headerName) {
				return headers.get(headerName);
			}
		};
		HttpResponse response = new HttpResponse() {
			public int getHttpStatusCode() {
				return 0;
			}
		};
		HttpClientRequest clientRequest = new HttpClientRequest() {
			public URI getUri() {
				try {
					return new URI("http://localhost");
				} catch (URISyntaxException e) {
					return null;
				}
			}

			public String getHttpMethod() {
				return "GET";
			}

			public void addHeader(String header, String value) {
				headers.put(header, value);
			}
		};

		HttpClientRequestAdapter clientRequestAdapter = new HttpClientRequestAdapter(clientRequest, SPAN_NAME_PROVIDER);
		brave.clientRequestInterceptor().handle(clientRequestAdapter);

		HttpServerRequestAdapter requestAdapter = new HttpServerRequestAdapter(serverRequest, SPAN_NAME_PROVIDER);
		brave.serverRequestInterceptor().handle(requestAdapter);

		HttpServerResponseAdapter responseAdapter = new HttpServerResponseAdapter(response);
		brave.serverResponseInterceptor().handle(responseAdapter);

		HttpClientResponseAdapter clientResponseAdapter = new HttpClientResponseAdapter(response);
		brave.clientResponseInterceptor().handle(clientResponseAdapter);
	}

}
