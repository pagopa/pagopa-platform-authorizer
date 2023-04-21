const {get} = require("../utility/axios_common");
const ip = require('ip');
const { debugLog } = require("../utility/helpers");

const payments_host = process.env.payments_host;
const ipAddress = ip.address();

function paymentsHealthCheck() {
    const host = `${payments_host}/info`;
    debugLog(`Calling endpoint: [${host}]`);
    return get(host, {
        headers: {
            "Host": process.env.host_header,
            "X-Forwarded-For": ipAddress,
            "Ocp-Apim-Subscription-Key": process.env.EXT_SUBSCRIPTION_KEY
        }
    })
}

function getReceiptList(organizationFiscalCode, subkey) {
    const host = `${payments_host}/payments/${organizationFiscalCode}/receipts?page=0`;
    debugLog(`Calling endpoint: [${host}]`);
    return get(host, {
        headers: {
            "Host": process.env.host_header,
            "X-Forwarded-For": ipAddress,
            "Ocp-Apim-Subscription-Key": subkey
        }
    })
}



module.exports = {
    paymentsHealthCheck,
    getReceiptList,
}