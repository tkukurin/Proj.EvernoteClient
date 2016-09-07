package co.kukurin.custom.properties;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

public class PropertyLoaderTest {

    static class CustomClass {
        @NotNull
        private String requiredString;

        @NotNull
        private Integer requiredInteger;
        private String optionalString;
    }

    @Test
    public void shouldCorrectlyLoadItems() throws Exception {
        // given
        CustomClass customClass;

        // when
        customClass = PropertyLoader
                .forClass(CustomClass.class)
                .initFromSystemResourceFiles("shouldCorrectlyLoadItems.properties");

        // then
        then(customClass.requiredInteger).isEqualTo(new Integer(1));
        then(customClass.requiredString).isEqualTo("string");
        then(customClass.optionalString).isNullOrEmpty();
    }

    @NotNull
    static class NotNullAnnotatedClass {
        private String string1;
        private String string2;
    }

    @Test
    public void shouldCorrectlyInterpretNotNullOnClass() throws Exception {
        // given
        NotNullAnnotatedClass notNullAnnotatedClass;

        // when
        Throwable throwable = catchThrowable(() ->
                PropertyLoader
                        .forClass(NotNullAnnotatedClass.class)
                        .initFromSystemResourceFiles("shouldCorrectlyInterpretNotNullOnClass.properties"));

        // then
        then(throwable).isNotNull();
        then(throwable.getLocalizedMessage()).contains("required field not present");
    }

    static class CollectionClass {
        private Set<String> stringSet;
        private Set<Integer> integerSet;
    }

    @Test
    public void shouldCorrectlyMapToSet() throws Exception {
        // given
        CollectionClass collectionClass;

        // when
        collectionClass = PropertyLoader.forClass(CollectionClass.class)
                .initFromSystemResourceFiles("shouldCorrectlyMapToSet.properties");

        // then
        then(collectionClass.stringSet).contains("firstString", "secondString", "thirdString");
        then(collectionClass.integerSet).contains(1, 2, 3);
    }
}
