package api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    @GetMapping("/test")
    public Mono<CommonClass> test() {

        return Mono.just(new CommonClass("test!!"));
    }
}