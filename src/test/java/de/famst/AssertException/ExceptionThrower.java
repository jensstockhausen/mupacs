package de.famst.AssertException;

/**
 * Created by jens on 08/10/2016.
 */
@FunctionalInterface
public interface ExceptionThrower
{
    void throwException() throws Throwable;
}
