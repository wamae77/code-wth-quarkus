package org.acme.easyfindv8.resteasyjackson;

import java.io.IOException;

@FunctionalInterface
public interface TriFunction<T,U,S, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param s the third function argument
     * @return the function result
     */
    R apply(T t, U u, S s) throws IOException;
}