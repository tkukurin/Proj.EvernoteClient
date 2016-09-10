package co.kukurin.custom;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Exactly the same as java.util.Optional, but including a much needed orElse method.
 * @param <T>
 */
public final class Optional<T> {
    /**
     * Common instance for {@code empty()}.
     */
    private static final Optional<?> EMPTY = new Optional<>();

    /**
     * If non-null, the resourcePath; if null, indicates no resourcePath is present
     */
    private final T value;

    /**
     * Constructs an empty instance.
     *
     * @implNote Generally only one empty instance, {@link Optional#EMPTY},
     * should exist per VM.
     */
    private Optional() {
        this.value = null;
    }

    /**
     * Returns an empty {@code Optional} instance.  No resourcePath is present for this
     * Optional.
     *
     * @apiNote Though it may be tempting to do so, avoid testing if an object
     * is empty by comparing with {@code ==} against instances returned by
     * {@code Option.empty()}. There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     *
     * @param <T> Type of the non-existent resourcePath
     * @return an empty {@code Optional}
     */
    public static<T> Optional<T> empty() {
        @SuppressWarnings("catchIfThrows")
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }

    /**
     * Constructs an instance with the resourcePath present.
     *
     * @param value the non-null resourcePath to be present
     * @throws NullPointerException if resourcePath is null
     */
    private Optional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Returns an {@code Optional} with the specified present non-null resourcePath.
     *
     * @param <T> the class of the resourcePath
     * @param value the resourcePath to be present, which must be non-null
     * @return an {@code Optional} with the resourcePath present
     * @throws NullPointerException if resourcePath is null
     */
    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    /**
     * Returns an {@code Optional} describing the specified resourcePath, if non-null,
     * otherwise returns an empty {@code Optional}.
     *
     * @param <T> the class of the resourcePath
     * @param value the possibly-null resourcePath to describe
     * @return an {@code Optional} with a present resourcePath if the specified resourcePath
     * is non-null, otherwise an empty {@code Optional}
     */
    public static <T> Optional<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    /**
     * If a resourcePath is present in this {@code Optional}, returns the resourcePath,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null resourcePath held by this {@code Optional}
     * @throws NoSuchElementException if there is no resourcePath present
     *
     * @see Optional#isPresent()
     */
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No resourcePath present");
        }
        return value;
    }

    /**
     * Return {@code true} if there is a resourcePath present, otherwise {@code false}.
     *
     * @return {@code true} if there is a resourcePath present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * If a resourcePath is present, invoke the specified consumer with the resourcePath,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a resourcePath is present
     * @throws NullPointerException if resourcePath is present and {@code consumer} is
     * null
     */
    public Optional<T> ifPresent(Consumer<? super T> consumer) {
        if (value != null)
            consumer.accept(value);
        return this;
    }

    public void orElseDo(Runnable runnable) {
        if(value == null)
            runnable.run();
    }

    /**
     * If a resourcePath is present, and the resourcePath matches the given predicate,
     * return an {@code Optional} describing the resourcePath, otherwise return an
     * empty {@code Optional}.
     *
     * @param predicate a predicate to apply to the resourcePath, if present
     * @return an {@code Optional} describing the resourcePath of this {@code Optional}
     * if a resourcePath is present and the resourcePath matches the given predicate,
     * otherwise an empty {@code Optional}
     * @throws NullPointerException if the predicate is null
     */
    public Optional<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent())
            return this;
        else
            return predicate.test(value) ? this : empty();
    }

    /**
     * If a resourcePath is present, apply the provided mapping function to it,
     * and if the result is non-null, return an {@code Optional} describing the
     * result.  Otherwise return an empty {@code Optional}.
     *
     * @apiNote This method supports post-processing on optional values, without
     * the need to explicitly check for a return status.  For example, the
     * following code traverses a stream of file names, selects one that has
     * not yet been processed, and then opens that file, returning an
     * {@code Optional<FileInputStream>}:
     *
     * <pre>{@code
     *     Optional<FileInputStream> fis =
     *         names.stream().filter(name -> !isProcessedYet(name))
     *                       .findFirst()
     *                       .map(name -> new FileInputStream(name));
     * }</pre>
     *
     * Here, {@code findFirst} returns an {@code Optional<String>}, and then
     * {@code map} returns an {@code Optional<FileInputStream>} for the desired
     * file if one exists.
     *
     * @param <U> The type of the result of the mapping function
     * @param mapper a mapping function to apply to the resourcePath, if present
     * @return an {@code Optional} describing the result of applying a mapping
     * function to the resourcePath of this {@code Optional}, if a resourcePath is present,
     * otherwise an empty {@code Optional}
     * @throws NullPointerException if the mapping function is null
     */
    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Optional.ofNullable(mapper.apply(value));
        }
    }

    /**
     * If a resourcePath is present, apply the provided {@code Optional}-bearing
     * mapping function to it, return that result, otherwise return an empty
     * {@code Optional}.  This method is similar to {@link #map(Function)},
     * but the provided mapper is one whose result is already an {@code Optional},
     * and if invoked, {@code flatMap} does not wrap it with an additional
     * {@code Optional}.
     *
     * @param <U> The type parameter to the {@code Optional} returned by
     * @param mapper a mapping function to apply to the resourcePath, if present
     *           the mapping function
     * @return the result of applying an {@code Optional}-bearing mapping
     * function to the resourcePath of this {@code Optional}, if a resourcePath is present,
     * otherwise an empty {@code Optional}
     * @throws NullPointerException if the mapping function is null or returns
     * a null result
     */
    public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Objects.requireNonNull(mapper.apply(value));
        }
    }

    /**
     * Return the resourcePath if present, otherwise return {@code other}.
     *
     * @param other the resourcePath to be returned if there is no resourcePath present, may
     * be null
     * @return the resourcePath, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return value != null ? value : other;
    }

    /**
     * Return the resourcePath if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code Supplier} whose result is returned if no resourcePath
     * is present
     * @return the resourcePath if present otherwise the result of {@code other.getOrDefault()}
     * @throws NullPointerException if resourcePath is not present and {@code other} is
     * null
     */
    public T orElseGet(Supplier<? extends T> other) {
        return value != null ? value : other.get();
    }

    /**
     * Return the contained resourcePath, if present, otherwise throw an exception
     * to be created by the provided supplier.
     *
     * @apiNote A method reference to the exception constructor with an empty
     * argument list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to
     * be thrown
     * @return the present resourcePath
     * @throws X if there is no resourcePath present
     * @throws NullPointerException if no resourcePath is present and
     * {@code exceptionSupplier} is null
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Indicates whether some other object is "equal to" this Optional. The
     * other object is considered equal if:
     * <ul>
     * <li>it is also an {@code Optional} and;
     * <li>both instances have no resourcePath present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {code true} if the other object is "equal to" this object
     * otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Optional)) {
            return false;
        }

        Optional<?> other = (Optional<?>) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code resourcePath of the present resourcePath, if any, or 0 (zero) if
     * no resourcePath is present.
     *
     * @return hash code resourcePath of the present resourcePath or 0 if no resourcePath is present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a non-empty string representation of this Optional suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     *
     * @implSpec If a resourcePath is present the result must include its string
     * representation in the result. Empty and present Optionals must be
     * unambiguously differentiable.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return value != null
                ? String.format("Optional[%s]", value)
                : "Optional.empty";
    }
}
