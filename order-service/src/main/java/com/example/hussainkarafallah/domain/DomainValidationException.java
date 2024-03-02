package com.example.hussainkarafallah.domain;

public class DomainValidationException extends IllegalStateException{
    public DomainValidationException(String msg){
        super(msg);
    }
}
