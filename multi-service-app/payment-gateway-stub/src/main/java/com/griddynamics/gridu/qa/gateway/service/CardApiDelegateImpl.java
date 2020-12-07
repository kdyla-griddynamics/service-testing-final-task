package com.griddynamics.gridu.qa.gateway.service;

import com.griddynamics.gridu.qa.gateway.api.CardApiDelegate;
import com.griddynamics.gridu.qa.gateway.api.model.Card;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CardApiDelegateImpl implements CardApiDelegate {

    public ResponseEntity<String> verifyCard(Card body) {
        return new ResponseEntity<>("someCardTokenReturnedHere", HttpStatus.OK);
    }
}
