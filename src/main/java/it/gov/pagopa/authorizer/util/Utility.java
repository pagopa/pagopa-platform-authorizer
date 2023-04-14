package it.gov.pagopa.authorizer.util;

import java.util.*;

public class Utility {

    private Utility() {}


    public static String convertListToString(List<String> list, String separator) {
        return convertListToString(list, separator, true);
    }

    public static String convertListToString(List<String> list, String separator, boolean includeNull) {
        if (list == null || separator == null) {
            throw new IllegalArgumentException("Passed null parameter");
        }
        List<String> elements = new ArrayList<>(list);
        if (!includeNull) {
            elements.removeAll(Collections.singleton(null));
        }
        StringBuilder builder = new StringBuilder();
        Iterator<String> it = elements.iterator();
        while (it.hasNext()) {
            String element = it.next();
            builder.append(element);
            if (it.hasNext()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }
}
