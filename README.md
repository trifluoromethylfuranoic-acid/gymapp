# Gym Application

## Building and Running

1. Create a `.env` file in the project root, in `main-service/` and in `report-service/`. Use `.env.example` as an example.
2. Generate an RSA key pair using `openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem` and `openssl rsa -pubout -in private.pem -out public.pem`. Put the keys into `keys/`.
3. Run `mvn spring-boot:build-image -Dmaven.test.skip=true`.
4. Run `docker compose -f docker-compose.yaml -f docker-compose.<profile>.yaml up`.

## Running integration tests
1. Run `mvn -q -pl integration-tests failsafe:integration-test`.
