package fruitshop.review_service.exception;

public class DownstreamServiceException extends RuntimeException {
    public DownstreamServiceException(String message) {
        super(message);
    }
}
