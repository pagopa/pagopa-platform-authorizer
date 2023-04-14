package it.gov.pagopa.authorizer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DataAccessObjectTest {

    @Test
    void findAuthorizationByDomain_OK_someRowFound() {
        assertTrue(true); // TODO to be defined
    }

    @Test
    void findAuthorizationByDomain_OK_noRowFound() {
        assertTrue(true); // TODO to be defined
    }

    @Test
    void findAuthorizationByDomain_KO_invalidDatabase() {
        assertTrue(true); // TODO to be defined
    }

    @Test
    void findAuthorizationByDomain_KO_invalidContainer() {
        assertTrue(true); // TODO to be defined
    }
}
