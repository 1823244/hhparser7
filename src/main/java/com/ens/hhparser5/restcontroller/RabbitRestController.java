package com.ens.hhparser5.restcontroller;

import com.ens.hhparser5.exceptions.HhparserException;
import com.ens.hhparser5.service.RabbitConnector;
import com.ens.hhparser5.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SuppressWarnings(value = "unused")
@RestController
@RequestMapping("rabbit")
public class RabbitRestController {

    @Autowired
    RabbitService rabbitService;
    @Autowired
    RabbitConnector rabbitConnector;

    @GetMapping("mocksend")
    public void rabbitMockSend(){
        rabbitService.mockRabbitSend();
    }

    @GetMapping("mockreceive")
    public void rabbitMockReceive() throws IOException, TimeoutException {
        rabbitService.mockRabbitReceive();
    }

    @GetMapping("truncate")
    public void rabbitTruncate() throws HhparserException {
        rabbitConnector.rabbitReceive();
    }
}
