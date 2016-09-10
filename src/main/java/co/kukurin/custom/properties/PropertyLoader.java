package co.kukurin.custom.properties;

import co.kukurin.custom.exception.ExceptionHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Class made for playing with the reflection API for a bit.
 *
 * <ul>
 *     <li>Pros: less overhead compared to {@link Properties} once instantiated</li>
 *     <li>Cons: slower loading because reflection is used</li>
 * </ul>
 *
 * <p>
 *     Possibly useful in situations where properties are external/user-defined; otherwise induces unnecessary
 *     coupling between a class and property files.
 * </p>
 *
 * <p>
 *     <b>NOTE</b> Currently does not allow collection items to contain commas (since collection items are expected to be
 *     comma-split, e.g. "first,second" will produce collection with elements ["first", "second"]).
 *
 *     Also does not parse complex structures such as Set&lt;Set&lt;Object&gt;&gt;.
 * </p>
 *
 * @param <T>
 */
public class PropertyLoader<T> {

    @FunctionalInterface
    interface Converter { Object apply(String propertyValueAsString, Class<?> typeOfGenericIfFieldIsParametrized); }
    private static final Map<Class<?>, Converter> fieldTypeToConverter;

    static {
        fieldTypeToConverter = new HashMap<>();

        fieldTypeToConverter.put(Integer.class, (string, cannotBeGeneric) -> Integer.parseInt(string));
        fieldTypeToConverter.put(int.class, (string, cannotBeGeneric) -> Integer.parseInt(string));

        fieldTypeToConverter.put(String.class, (string, cannotBeGeneric) -> string);

        fieldTypeToConverter.put(Set.class, (string, typeOfGeneric) -> collect(Collectors.toSet(), typeOfGeneric, string));
        fieldTypeToConverter.put(List.class, (string, typeOfGeneric) -> collect(Collectors.toList(), typeOfGeneric, string));
    }

    @SuppressWarnings("unchecked")
    private static <T, A, R> R collect(Collector<T, A, R> collector,
                                       Class<?> typeOfGeneric,
                                       String commaSeparatedString) {
        Converter remappingFunctionForEachStringInCollection = fieldTypeToConverter.get(typeOfGeneric);
        return Arrays.stream(commaSeparatedStringToArray(commaSeparatedString))
                .map(arrayItem -> (T)remappingFunctionForEachStringInCollection.apply(arrayItem, null))
                .collect(collector);
    }

    private static String[] commaSeparatedStringToArray(String s) {
        return s.split(",");
    }

    private final Class<T> propertyClass;
    private PropertyLoader(Class<T> propertyClass) {
        this.propertyClass = propertyClass;
    }

    public static <T> PropertyLoader<T> forClass(Class<T> classWhoseFieldsShouldBeLoadedFromProperties) {
        return new PropertyLoader<>(classWhoseFieldsShouldBeLoadedFromProperties);
    }

    public T initFromSystemResourceFiles(String... resourceNames) throws IllegalAccessException, InstantiationException {
        Map<String, String> fieldNameToValue = loadFieldNameToValueMapFromResources(resourceNames);
        boolean allFieldsMustBePresent = requiresValuePresence(propertyClass);
        T propertyClassInstance = propertyClass.newInstance();

        for (Field field : propertyClass.getDeclaredFields()) {
            if (isStatic(field))
                continue;

            field.setAccessible(true);
            String fieldName = field.getName();
            String fieldValue = fieldNameToValue.get(fieldName);

            if (fieldValue != null) {
                convertPropertyAndSetFieldValue(propertyClassInstance, field, fieldValue);
            } else if (allFieldsMustBePresent || requiresValuePresence(field)) {
                throw new RuntimeException("required field not present: " + fieldName);
            }
        }

        return propertyClassInstance;
    }

    private void convertPropertyAndSetFieldValue(T propertyClassInstance, Field field, String fieldValueAsString) throws IllegalAccessException {
        Class<?> fieldType = field.getType();

        Object remappedValue = Optional
                .ofNullable(fieldTypeToConverter.get(fieldType))
                .map(converter -> tryToApplyConverter(fieldValueAsString, converter, getTypeOfGenericIfFieldIsParametrized(field)))
                .orElseThrow(mappingFailedException(fieldValueAsString, fieldType));

        field.set(propertyClassInstance, remappedValue);
    }

    private Class<?> getTypeOfGenericIfFieldIsParametrized(Field field) {
        return ExceptionHelper
                .tryGetValue(() -> (ParameterizedType) field.getGenericType())
                .map(ParameterizedType::getActualTypeArguments)
                .map(typeArguments -> (Class<?>) typeArguments[0])
                .orElse(null);
    }

    private Object tryToApplyConverter(String fieldValue, Converter converter, Class<?> typeOfGenericIfPresent) {
        return ExceptionHelper
                .tryGetValue(() -> converter.apply(fieldValue, typeOfGenericIfPresent))
                .orElse(null);
    }

    private Supplier<RuntimeException> mappingFailedException(String fieldValue, Class<?> fieldType) {
        return () -> new RuntimeException("cannot parse " + fieldValue + " as " + fieldType.getName());
    }

    private boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    private boolean requiresValuePresence(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(NotNull.class);
    }

    private static Map<String, String> loadFieldNameToValueMapFromResources(String[] resourceNames) {
        Map<String, String> map = new HashMap<>();

        Arrays.stream(resourceNames)
                .map(ClassLoader::getSystemResourceAsStream)
                .map(PropertyLoader::propertiesFromInputStream)
                .forEach(properties -> {
                    properties.forEach((key, value) -> map.put((String) key, (String) value));
                });

        return map;
    }

    private static Properties propertiesFromInputStream(InputStream inputStream) {
        return ExceptionHelper
                .remappingOnException(PropertyLoader::getAsUncheckedIfThrowsIOException)
                .tryGetValue(() -> {
                    Properties properties = new Properties();
                    properties.load(inputStream);
                    inputStream.close();
                    return properties;
                });
    }

    private static RuntimeException getAsUncheckedIfThrowsIOException(Exception e) {
        if(e instanceof RuntimeException)
            return (RuntimeException) e;
        return new UncheckedIOException("failed loading properties", (IOException) e);
    }

}
