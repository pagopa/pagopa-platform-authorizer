const {get} = require("../utility/axios_common");
const ip = require('ip');
const { debugLog } = require("../utility/helpers");

const enrolled_ec_host = process.env.enrolled_ec_host;
const ipAddress = ip.address();

function getEnrolledEC(domain) {
    const host = `${enrolled_ec_host}/organizations/domains/${domain}`;
    debugLog(`Calling endpoint: [${host}]`);
    return get(host, {
        headers: {
            "Host": process.env.host_header,
            "X-Forwarded-For": ipAddress,
            "Ocp-Apim-Subscription-Key": process.env.EXT_SUBSCRIPTION_KEY
        }
    })
}

function getEnrolledStationForEC(domain, creditorInstitutionCode) {
    const host = `${enrolled_ec_host}/organizations/${creditorInstitutionCode}/domains/${domain}`;
    debugLog(`Calling endpoint: [${host}]`);
    return get(host, {
        headers: {
            "Host": process.env.host_header,
            "X-Forwarded-For": ipAddress,
            "Ocp-Apim-Subscription-Key": process.env.EXT_SUBSCRIPTION_KEY
        }
    })
}

module.exports = {
    getEnrolledEC
}