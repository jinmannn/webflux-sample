package api.controller;

import api.CommonClass;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    @GetMapping("/test")
    public Mono<CommonClass> test() {

        return Mono.just(new CommonClass("test!!"));
    }

    @PostMapping("/test/decorate")
    public Mono<JsonNode> decorateTest(@RequestBody JsonNode requestBody) {
        return Mono.just(requestBody);
    }
}