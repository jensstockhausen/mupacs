package de.famst.AssertException;

/**
 * Created by jens on 08/10/2016.
 */
public class ExceptionNotThrownAssertionError extends AssertionError
{
    public ExceptionNotThrownAssertionError()
    {
        super("Expected exception was not thrown.");
    }
}
