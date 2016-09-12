package co.kukurin.async;

import java.util.Collection;

@FunctionalInterface
public interface DataSupplier<T> {

    Collection<T> getData(DataSupplierInfo dataSupplierInfo);

}
