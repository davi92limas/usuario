package usuario.infrastructure.exceptions;

public class ResourceNotFoundExeception extends RuntimeException {
  public ResourceNotFoundExeception(String message) {
    super(message);
  }
  
  public ResourceNotFoundExeception(String message, Throwable throwable) {
    super(message);
  }
}
