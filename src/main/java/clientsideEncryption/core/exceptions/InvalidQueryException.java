package clientsideEncryption.core.exceptions;

public class InvalidQueryException extends ClientsideEncryptionError{
    public InvalidQueryException(String errorMessage){
        super(errorMessage);
    }
}
