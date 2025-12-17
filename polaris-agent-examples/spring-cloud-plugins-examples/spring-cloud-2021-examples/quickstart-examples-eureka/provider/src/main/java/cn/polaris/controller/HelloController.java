package cn.polaris.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {
    @Value("${server.port}")
    private String serverPort;
    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        try{
            Thread.sleep(50);
            return "Hello " + name + " from provider! Port:" + serverPort;
        }catch (Exception e){
            return e.getMessage();
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Provider is healthy!";
    }

    @GetMapping("/echo-headers")
    public Map<String, Object> echoHeaders(
            @RequestHeader Map<String, String> headers,
            @RequestParam(defaultValue = "50") long delay) {
        try {
            // 延迟指定的毫秒数
            Thread.sleep(delay);
            
            // 解析并构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("headers", headers);
            result.put("delay", delay + "ms");
            result.put("port", System.getProperty("server.port"));
            
            return result;
        } catch (InterruptedException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
}