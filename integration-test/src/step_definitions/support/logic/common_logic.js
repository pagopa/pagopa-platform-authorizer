const assert = require("assert");
const { paymentsHealthCheck, getReceiptList } = require("../clients/payments_client");
const { getEnrolledEC, getEnrolledStationForEC } = require("../clients/enrolled_ec_client");
const { debugLog, makeidMix } = require("../utility/helpers");
const { CosmosClient } = require("@azure/cosmos");

const key = process.env.COSMOSDB_KEY
const endpoint = process.env.COSMOSDB_URI
const subkeyA = process.env.SUBKEY_A
const subkeyB = process.env.SUBKEY_B

async function executeHealthCheckForGPDPayments() {
    console.log(" - Given GPD-Payments service running...");
    const response = await paymentsHealthCheck();
    debugLog(`GPD Payments Health check API invocation returned HTTP status code: ${response?.status}`);
    assert.strictEqual(response.status, 200);
}

async function generateAuthorization(authEntity, authDomain, bundle) {
    console.log(` - Given an authorization on entity ${authEntity} for the domain ${authDomain} related to subscription key A is added in the database..`);
    bundle.authorization_id = makeidMix(30);
    bundle.domain = authDomain;
    const newAuthorization = {
        id: bundle.authorization_id,
        domain: authDomain,
        subkey: subkeyA,
        authorization: [ authEntity ]    
    };
    const client = new CosmosClient({ endpoint, key });
    await client.database("authorizer").container("skeydomains").items.create(newAuthorization);    
    await new Promise(resolve => setTimeout(resolve, process.env.wait_execution_sec * 1000));
    debugLog(`Forcing insertion of new authorization for domain ${authDomain} including entity [${authEntity}]`);
}

async function executeAuthorizedInvocation(entity, subkeyType, bundle) {
    console.log(` - When the client execute a call for entity ${entity} with subscription key ${subkeyType}..`);
    let response = await ("A" == subkeyType ? getReceiptList(entity, subkeyA) : getReceiptList(entity, subkeyB));
    debugLog(`API invocation returned HTTP status code: ${response?.status}`);
    bundle.response = response;
}

async function executeGetEnrolledECInvocation(domain, bundle) {
    console.log(` - When the client execute a call for the domain ${domain}...`);
    let response = await getEnrolledEC(domain);
    debugLog(`API invocation returned HTTP status code: ${response?.status}`);
    bundle.response = response;
}

async function executeGetEnrolledStationsForECInvocation(domain, creditorInstitutionCode, bundle) {
    console.log(` - When the client execute a call for the domain ${domain} for entity ${creditorInstitutionCode}...`);
    let response = await getEnrolledStationForEC(domain, creditorInstitutionCode);
    debugLog(`API invocation returned HTTP status code: ${response?.status}`);
    bundle.response = response;
}

async function executeAfterAllStep(bundle) {
    console.log(` - Deleting authorization with id ${bundle.authorization_id}..`);
    const client = new CosmosClient({ endpoint, key });
    await client.database("authorizer").container("skeydomains").item(bundle.authorization_id, bundle.domain).delete();    
}

async function assertStatusCodeEquals(response, statusCode) {
    console.log(` - Then the client receives status code [${statusCode}]..`);
    assert.strictEqual(response.status, statusCode);
}

async function assertStatusCodeNotEquals(response, statusCode) {
    console.log(` - Then the client receives status code different from [${statusCode}]..`);
    assert.ok(response.status !== statusCode);
}

async function assertResponseWithEnrolledCI(response) {
    console.log(` - Then the client receives an object with enrolled creditor institutions..`);
    assert.ok(response.data.creditor_institutions !== undefined);
}

async function assertResponseWithEnrolledStations(response) {
    console.log(` - Then the client receives an object with enrolled stations for creditor institution..`);
    assert.ok(response.data.stations.length > 0);
}

module.exports = {
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
}