package core.exceptions;

public class InitializationError extends ClientsideEncryptionError{
    public InitializationError(String errorMessage){ super(errorMessage); }
}
