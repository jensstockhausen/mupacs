package de.famst.AssertException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

public class ThrowableAssertion
{

    public static ThrowableAssertion assertThrown(ExceptionThrower exceptionThrower)
    {
        try
        {
            exceptionThrower.throwException();
        }
        catch (Throwable caught)
        {
            return new ThrowableAssertion(caught);
        }
        throw new ExceptionNotThrownAssertionError();
    }

    private final Throwable caught;

    public ThrowableAssertion(Throwable caught)
    {
        this.caught = caught;
    }

    public ThrowableAssertion isInstanceOf(Class<? extends Throwable> exceptionClass)
    {
        MatcherAssert.assertThat(caught, Matchers.isA((Class<Throwable>) exceptionClass));
        return this;
    }

    public ThrowableAssertion hasMessage(String expectedMessage)
    {
        MatcherAssert.assertThat(caught.getMessage(), Matchers.equalTo(expectedMessage));
        return this;
    }

    public ThrowableAssertion hasNoCause()
    {
        MatcherAssert.assertThat(caught.getCause(), Matchers.nullValue());
        return this;
    }

    public ThrowableAssertion hasCauseInstanceOf(Class<? extends Throwable> exceptionClass)
    {
        MatcherAssert.assertThat(caught.getCause(), Matchers.notNullValue());
        MatcherAssert.assertThat(caught.getCause(), Matchers.isA((Class<Throwable>) exceptionClass));
        return this;
    }
}