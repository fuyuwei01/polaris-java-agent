package cn.polaris.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/call/{name}")
    public String callProvider(@PathVariable String name) {
        // 使用服务名进行调用，Eureka会自动进行负载均衡
        String result = restTemplate.getForObject(
            "http://eureka-provider/hello/" + name, String.class);
        return "Consumer received: " + result;
    }

    @GetMapping("/health")
    public String health() {
        return "Consumer is healthy!";
    }
}