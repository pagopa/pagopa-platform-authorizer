const assert = require("assert");
const { paymentsHealthCheck, getReceiptList } = require("../clients/payments_client");
const { getEnrolledEC } = require("../clients/enrolled_ec_client");
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

async function assertECListIsNotEmpty(response) {
    console.log(` - Then the client receives a non-empty list..`);
    assert.ok(response.data.creditor_institutions.length > 0)
}

async function assertECListIsEmpty(response) {
    console.log(` - Then the client receives an empty list..`);
    assert.strictEqual(0, response.data.creditor_institutions.length);
}

module.exports = {
    assertStatusCodeEquals,
    assertStatusCodeNotEquals,
    executeAfterAllStep,
    executeAuthorizedInvocation,
    executeHealthCheckForGPDPayments,
    generateAuthorization,
    executeGetEnrolledECInvocation,
    assertECListIsNotEmpty,
    assertECListIsEmpty
}