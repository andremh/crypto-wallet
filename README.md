# Crypto Wallet Challenge

This is my implementation of the SPICE-P code challenge, creating a cryptocurrency wallet management system. The application lets users create wallets, add crypto assets, and track their performance over time.

## Features

- Create wallets linked to user email addresses
- Add crypto assets to wallets (BTC, ETH, etc.)
- Track current asset prices with auto-updates from CoinCap API
- View wallet contents with up-to-date valuations
- Evaluate portfolio performance against historical prices

## Tech Stack

- Java 21
- Spring Boot 3.x
- PostgreSQL (containerized with Docker)
- Maven
- RESTful API

## Running the Project

### Start the Database

```
docker-compose up -d
```

### Launch the Application

```
./mvnw spring-boot:run
```

The API will be available at http://localhost:8080

## API Endpoints

- `POST /api/wallets` - Create a new wallet
- `POST /api/wallets/{id}/assets` - Add asset to wallet
- `GET /api/wallets/{id}` - Get wallet information
- `POST /api/wallets/evaluate` - Evaluate wallet performance

## Testing

The project includes unit and integration tests for services, controllers, and repositories. Run them with:

```
./mvnw test
```

Tests use an in-memory H2 database to avoid affecting development data.

## Some Implementation Notes

- Price updates run on a configurable schedule (default: every minute)
- Up to 3 tokens are processed concurrently during updates
- Spring profiles are being used to separate test and development environments