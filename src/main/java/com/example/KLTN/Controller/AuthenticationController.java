package com.example.KLTN.Controller;

import com.example.KLTN.DTO.IntrospectDTO;
import com.example.KLTN.DTO.ResponseDTO;
import com.example.KLTN.Request.AuthenticationRequest;
import com.example.KLTN.Request.IntrospectRequest;
import com.example.KLTN.Request.logoutRequest;
import com.example.KLTN.Service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("")
    public ResponseDTO authenticate(@RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(request);
    }
    @PostMapping("/logout")
    public ResponseDTO logout( @RequestBody logoutRequest request) throws ParseException, JOSEException {
        return authenticationService.logout(request);
    }

    @PostMapping("/introspect")
    IntrospectDTO introspect (@RequestBody IntrospectRequest introspectRequest) throws ParseException, JOSEException{
        return authenticationService.introspect(introspectRequest);
    }
}
