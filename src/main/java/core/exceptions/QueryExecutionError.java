package core.exceptions;

public class QueryExecutionError extends ClientsideEncryptionError{
    public QueryExecutionError(String errorMessage) { super(errorMessage); }
}
