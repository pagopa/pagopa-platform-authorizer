# example: sh ./run_integration_test.sh <smoke|integration> <local|dev|uat|prod>
environment=$1
containerName="node-container"

docker stop node-container || true
docker rm node-container || true

# please see https://github.com/andrea-deri/prebuilt-img-yarn-base for yarn-testing-base image content
docker pull ${CONTAINER_NAMESPACE}/yarn-testing-base:latest
docker run -dit --name ${containerName} ${CONTAINER_NAMESPACE}/yarn-testing-base:latest

# run integration tests with yarn
docker cp -a ./src/. ${containerName}:/test
docker exec -i ${containerName} /bin/bash -c " \
cd ./test
export COSMOSDB_URI=${COSMOSDB_URI} \
export COSMOSDB_KEY=${COSMOSDB_KEY} \
export EXT_SUBSCRIPTION_KEY=${INTEGRTEST_EXT_SUBSCRIPTION_KEY} \
export SUBKEY_A=${INTEGRTEST_VALID_SUBKEY} \
export SUBKEY_B=${INTEGRTEST_INVALID_SUBKEY}
yarn test:${environment}"

# clean up container
docker stop ${containerName} && docker rm ${containerName}