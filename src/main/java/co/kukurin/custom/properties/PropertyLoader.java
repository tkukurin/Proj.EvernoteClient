package co.kukurin.custom.properties;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class PropertyLoader<T> {

    private static final Map<Class<?>, Function<String, ?>> fieldTypeToItsConverterFromString;
    static {
        fieldTypeToItsConverterFromString = new HashMap<>();
        fieldTypeToItsConverterFromString.put(Integer.class, Integer::parseInt);
        fieldTypeToItsConverterFromString.put(String.class, Function.identity());
    }

    private final Class<T> clazz;
    private PropertyLoader(Class<T> clazz) { this.clazz = clazz; }

    public static <T> PropertyLoader<T> forClass(Class<T> classWhoseFieldsShouldBeLoadedFromProperties) {
        return new PropertyLoader<>(classWhoseFieldsShouldBeLoadedFromProperties);
    }

    public T initFromSystemResourceFiles(String ... resourceNames) throws IllegalAccessException, InstantiationException {
        Map<String, String> fieldNameToValue = loadFieldNameToValueMapFromResources(resourceNames);
        T clazzInstance = clazz.newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            if(isStatic(field))
                continue;

            field.setAccessible(true);
            String fieldName = field.getName();
            String fieldValue = fieldNameToValue.get(fieldName);

            if(fieldValue != null) {
                Class<?> fieldType = field.getType();
                Object remappedValue = Optional
                        .ofNullable(fieldTypeToItsConverterFromString.get(fieldType))
                        .map(converter -> converter.apply(fieldValue))
                        .orElseThrow(mappingFailedException(fieldValue, fieldType));

                field.set(clazzInstance, remappedValue);
            } else if(mustBePresent(field)) {
                throw new RuntimeException("required field not present: " + fieldName);
            } else {
                log.info("optional field not present: " + fieldName);
            }
        }

        return clazzInstance;
    }

    private Supplier<RuntimeException> mappingFailedException(String fieldValue, Class<?> fieldType) {
        return () -> new RuntimeException("cannot parse " + fieldValue + " as " + fieldType.getName());
    }

    private boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    private boolean mustBePresent(Field field) {
        return field.isAnnotationPresent(NotNull.class);
    }

    private static Map<String, String> loadFieldNameToValueMapFromResources(String[] resourceNames) {
        Map<String, String> map = new HashMap<>();

        Arrays.stream(resourceNames)
                .map(ClassLoader::getSystemResourceAsStream)
                .map(PropertyLoader::propertiesFromInputStream)
                .forEach(properties -> {
                    properties.forEach((key, value) -> map.put((String)key, (String)value));
                });

        return map;
    }

    private static Properties propertiesFromInputStream(InputStream inputStream) {
        Properties properties = new Properties();

        try {
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException("failed loading properties", e);
        }

        return properties;
    }

}
