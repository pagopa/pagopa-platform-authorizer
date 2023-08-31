package it.gov.pagopa.authorizer.util;

import it.gov.pagopa.authorizer.entity.GenericPair;
import it.gov.pagopa.authorizer.entity.Metadata;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

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

    public static String extractMetadataAsString(@NonNull List<Metadata> metadata) {
        StringBuilder builder = new StringBuilder();
        for (Metadata singleMetadata : metadata) {
            builder.append(singleMetadata.getShortKey()).append("=");
            List<GenericPair> content = singleMetadata.getContent();
            if (content.size() == 1) {
                GenericPair metadataPair = content.get(0);
                builder.append(getMetadataValueAsString(metadataPair));
            } else {
                Iterator<GenericPair> it = content.iterator();
                while (it.hasNext()) {
                    GenericPair metadataPair = it.next();
                    builder.append(metadataPair.getKey()).append(":");
                    builder.append(getMetadataValueAsString(metadataPair));
                    if (it.hasNext()) {
                        builder.append(";");
                    }
                }
            }
            builder.append(";;");
        }

        return builder.toString();
    }

    private static String getMetadataValueAsString(GenericPair metadataPair) {
        return metadataPair.getValues() != null ? StringUtils.join(metadataPair.getValues(), ",") : metadataPair.getValue();
    }
}
