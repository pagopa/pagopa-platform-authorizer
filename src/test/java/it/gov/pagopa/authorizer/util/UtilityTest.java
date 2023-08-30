package it.gov.pagopa.authorizer.util;

import it.gov.pagopa.authorizer.entity.GenericPair;
import it.gov.pagopa.authorizer.entity.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UtilityTest {

    private static final String FIRST_SEPARATOR = ",";

    private static final String SECOND_SEPARATOR = "|";

    private static final String EMPTY_STRING = "";

    private final List<String> contentList = List.of("first", "second", "third");

    @Test
    void convertListToString_OK_fullList() {
        assertEquals("first,second,third", Utility.convertListToString(contentList, FIRST_SEPARATOR));
        assertEquals("first|second|third", Utility.convertListToString(contentList, SECOND_SEPARATOR));
    }

    @Test
    void convertListToString_OK_emptyList() {
        List<String> emptyContentList = List.of();
        String result = Utility.convertListToString(emptyContentList, FIRST_SEPARATOR);
        assertEquals(EMPTY_STRING, result);
        assertEquals(0, result.length());
        result = Utility.convertListToString(emptyContentList, SECOND_SEPARATOR);
        assertEquals(EMPTY_STRING, result);
        assertEquals(0, result.length());
    }


    @Test
    void convertListToString_OK_nullElementInFullList() {
        List<String> refactoredContentList = new ArrayList<>(this.contentList);
        refactoredContentList.add(null);
        assertEquals(4, refactoredContentList.size());
        assertEquals("first,second,third", Utility.convertListToString(refactoredContentList, FIRST_SEPARATOR, false));
        assertEquals("first|second|third", Utility.convertListToString(refactoredContentList, SECOND_SEPARATOR, false));
        assertEquals(4, refactoredContentList.size()); // check if no element was removed after conversion
        assertEquals("first,second,third,null", Utility.convertListToString(refactoredContentList, FIRST_SEPARATOR, true));
        assertEquals("first|second|third|null", Utility.convertListToString(refactoredContentList, SECOND_SEPARATOR, true));
    }

    @Test
    void convertListToString_KO_nullParameter() {
        assertThrows(IllegalArgumentException.class, () -> Utility.convertListToString(null, FIRST_SEPARATOR, false));
        assertThrows(IllegalArgumentException.class, () -> Utility.convertListToString(null, FIRST_SEPARATOR));
        assertThrows(IllegalArgumentException.class, () -> Utility.convertListToString(contentList, null, false));
        assertThrows(IllegalArgumentException.class, () -> Utility.convertListToString(contentList, null));
    }

    @Test
    void extractMetadataAsString_OK() {
        String expectedResult = "_md1=single-value;;_md2=value1,value2;;_md3=multiple-object-1:single-value;multiple-object-2:value1,value2;;";
        List<Metadata> metadata = List.of(
                Metadata.builder()
                        .name("metadata-1")
                        .shortKey("_md1")
                        .content(List.of(
                                GenericPair.builder()
                                        .key("single-object")
                                        .value("single-value")
                                        .build()))
                        .build(),
                Metadata.builder()
                        .name("metadata-2")
                        .shortKey("_md2")
                        .content(List.of(
                                GenericPair.builder()
                                        .key("single-object-with-multiple-values")
                                        .values(List.of("value1", "value2"))
                                        .build()))
                        .build(),
                Metadata.builder()
                        .name("metadata-3")
                        .shortKey("_md3")
                        .content(List.of(
                                GenericPair.builder()
                                        .key("multiple-object-1")
                                        .value("single-value")
                                        .build(),
                                GenericPair.builder()
                                        .key("multiple-object-2")
                                        .values(List.of("value1", "value2"))
                                        .build()))
                        .build()
        );

        String result = Utility.extractMetadataAsString(metadata);
        assertEquals(expectedResult, result);
    }
}
