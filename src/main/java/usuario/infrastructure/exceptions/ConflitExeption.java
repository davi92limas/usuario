package usuario.infrastructure.exceptions;

public class ConflitExeption extends RuntimeException {
  
  public ConflitExeption(String message) {
    super(message);
  }
  
  public ConflitExeption(String message, Throwable throwable) {
    super(message);
  }
}
