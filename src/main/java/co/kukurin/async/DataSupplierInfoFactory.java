package co.kukurin.async;

public class DataSupplierInfoFactory {

    public static DataSupplierInfo getDataSupplier(int startIndex, int fetchSize) {
        return new DataSupplierInfo() {
            @Override
            public int getFetchStartIndex() {
                return startIndex;
            }

            @Override
            public int getFetchSize() {
                return fetchSize;
            }
        };
    }

}
