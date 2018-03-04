/*
 * YS: When create a new customer but usename got conflict
 */
package util.exception;

/**
 *
 * @author yingshi
 */
public class CustomerExistException extends Exception {

    /**
     * Creates a new instance of <code>CustomerExistException</code> without
     * detail message.
     */
    public CustomerExistException() {
    }

    /**
     * Constructs an instance of <code>CustomerExistException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public CustomerExistException(String msg) {
        super(msg);
    }
}
