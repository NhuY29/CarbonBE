package com.example.KLTN.Service;

import com.example.KLTN.DTO.IntrospectDTO;
import com.example.KLTN.DTO.ResponseDTO;
import com.example.KLTN.Request.AuthenticationRequest;
import com.example.KLTN.Request.IntrospectRequest;
import com.example.KLTN.Request.logoutRequest;
import com.nimbusds.jose.JOSEException;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public interface AuthenticationService {
    ResponseDTO authenticate(AuthenticationRequest request);
    ResponseDTO logout(logoutRequest request) throws ParseException, JOSEException;
    IntrospectDTO introspect (IntrospectRequest introspectRequest) throws ParseException, JOSEException;
}
