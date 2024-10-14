package com.example.KLTN.Controller;

import com.example.KLTN.DTO.ClientSdi;
import com.example.KLTN.Service.ClientServe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client")
public class ClientController {
    @Autowired
    ClientServe clientServe;
    @PostMapping("/sendmail")
   public Boolean create(@RequestBody ClientSdi clientSdi){
        return clientServe.creat(clientSdi);
    }
}
