package api.web_filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Order(1)
@Service
@RequiredArgsConstructor
public class DecorateWebFilter implements WebFilter {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        Flux<DataBuffer> requestDecorateFlux = DataBufferUtils.join(exchange.getRequest().getBody())
                .flux()
                .map(dataBuffer -> {
                    byte[] buffer = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(buffer);
                    if (!DataBufferUtils.release(dataBuffer)) {
                        log.error("release request buffer");
                    }

                    String requestMessage = new String(buffer, UTF_8);

                    // todo something...

                    ObjectNode decorateJson;
                    try {
                        decorateJson = (ObjectNode) objectMapper.readTree(requestMessage);
                        decorateJson.put("request", "decorate");
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("request is not json type", e);
                    }

                    // todo something...

                    byte[] decorateBytes = decorateJson.toString().getBytes(UTF_8);
                    DataBuffer decoratedDataBuffer = exchange.getResponse().bufferFactory().wrap(decorateBytes);
                    DataBufferUtils.retain(decoratedDataBuffer);
                    return decoratedDataBuffer;
                });

        ServerHttpRequest mutatedRequest =
                new ServerHttpRequestDecorator(exchange.getRequest()) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return requestDecorateFlux;
                    }
                };

        ServerHttpResponse mutatedResponse =
                new ServerHttpResponseDecorator(exchange.getResponse()) {
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        Mono<? extends DataBuffer> monoBody = (Mono<? extends DataBuffer>) body;
                        return super.writeWith(monoBody.map(dataBuffer -> {
                            byte[] buffer = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(buffer);
                            if (!DataBufferUtils.release(dataBuffer)) {
                                log.error("release request buffer");
                            }

                            String responseMessage = new String(buffer, UTF_8);

                            // todo something...

                            ObjectNode decorateJson;
                            try {
                                decorateJson = (ObjectNode) objectMapper.readTree(responseMessage);
                                decorateJson.put("response", "decorate");
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException("response is not json type", e);
                            }

                            // todo something...

                            byte[] decorateBytes = decorateJson.toString().getBytes(UTF_8);
                            exchange.getResponse().getHeaders().set("Content-Length", String.valueOf(decorateBytes.length));
                            return exchange.getResponse().bufferFactory().wrap(decorateBytes);
                        }));
                    }
                };

        ServerWebExchange mutateExchange = exchange.mutate()
                .request(mutatedRequest)
                .response(mutatedResponse)
                .build();

        return chain.filter(mutateExchange);
    }
}
