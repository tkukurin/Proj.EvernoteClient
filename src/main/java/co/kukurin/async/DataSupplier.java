package co.kukurin.async;

import java.util.Collection;

@FunctionalInterface
public interface DataSupplier<T> {

    T getData(DataSupplierInfo dataSupplierInfo);

}
