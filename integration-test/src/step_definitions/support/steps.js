const { After, Before, Given, setDefaultTimeout, Then, When } = require('@cucumber/cucumber');
const { 
    assertStatusCodeEquals, 
    assertStatusCodeNotEquals,
    executeAfterAllStep,
    executeAuthorizedInvocation, 
    executeHealthCheckForGPDPayments, 
    generateAuthorization,
    executeGetEnrolledECInvocation,
    executeGetEnrolledStationsForECInvocation,
    assertResponseWithEnrolledCI,
    assertResponseWithEnrolledStations
} = require('./logic/common_logic');


/* Setting defaul timeout to 30s. */
setDefaultTimeout(30 * 1000);

let bundle = {
    authorization_id: undefined,
    domain: undefined,
    response: undefined
}

/* 
 *  'Given' precondition for health checks on various services. 
 */
Given('GPD-Payments service running', () => executeHealthCheckForGPDPayments());
Given('an authorization on entity {string} for the domain {string} related to subscription key "A" is added in the database', (entity, domain) => generateAuthorization(entity, domain, bundle));
When('the client execute a call for entity {string} with subscription key {string}', (entity, subkeyType) => executeAuthorizedInvocation(entity, subkeyType, bundle));
When('the client execute a call for the domain {string}', (domain) => executeGetEnrolledECInvocation(domain, bundle));
When('the client execute a call for the domain {string} for entity {creditorInstitutionCode}', (domain, creditorInstitutionCode) => executeGetEnrolledStationsForECInvocation(domain, creditorInstitutionCode, bundle));
Then('the client receives status code {int}', (statusCode) => assertStatusCodeEquals(bundle.response, statusCode));
Then('the client receives status code different from {int}', (statusCode) => assertStatusCodeNotEquals(bundle.response, statusCode));
Then('the client receives an object with enrolled creditor institutions', () => assertResponseWithEnrolledCI(bundle.response));
Then('the client receives an object with enrolled station for creditor institution', () => assertResponseWithEnrolledStations(bundle.response));


Before(function(scenario) {
    const header = `| Starting scenario "${scenario.pickle.name}" |`;
    let separator = "-".repeat(header.length);
    console.log(`\n${separator}`);
    console.log(`${header}`);
    console.log(`${separator}`);
});

After(() => {
    console.log(`\n\n--[Clear all created entities]--`);
    executeAfterAllStep(bundle);
});