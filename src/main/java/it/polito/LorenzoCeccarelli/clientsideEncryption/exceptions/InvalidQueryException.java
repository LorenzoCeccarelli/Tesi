package it.polito.LorenzoCeccarelli.clientsideEncryption.exceptions;


public class InvalidQueryException extends  Exception{
    public InvalidQueryException(String errorMessage){
        super(errorMessage);
    }
}
