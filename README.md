# Cryptocurrency Wallet Manager 💰

A Java-based client-server application that simulates a personal cryptocurrency wallet. Users can register, manage their funds, browse available cryptocurrencies, buy and sell assets, and track their investment performance using real-time market data from CoinAPI.

## Features

### User Management
- **Register** – Create an account using a username and password.
- **Login** – Authenticate and access your personal wallet.
- Passwords are stored securely as hashes rather than plain text.
- User data is persisted on the server and restored after restarting the application.

### Wallet Management
- **Deposit Money** – Add funds to your wallet in USD.
- **Buy Cryptocurrency** – Purchase a cryptocurrency using a specified amount of available funds.
- **Sell Cryptocurrency** – Sell an existing cryptocurrency investment and receive the proceeds in your wallet.
- **Wallet Summary** – View your available balance and current cryptocurrency investments.
- **Overall Investment Summary** – Track the overall profit or loss by comparing purchase prices with current market prices.

### Cryptocurrency Market Data
- **List Offerings** – Browse available cryptocurrencies and their market information.
- Integration with the [CoinAPI](https://www.coinapi.io/) REST API.
- Cryptocurrency filtering using asset information.
- Market data caching with a 30-minute validity period to reduce unnecessary API requests.

### Additional Functionality
- Console-based client with intuitive commands.
- Input validation and user-friendly error messages.
- Logging of technical errors and stack traces to files.
- Support for multiple concurrent clients.
- Persistent storage of user accounts and wallet information.

## Architecture

The application follows a client-server architecture:

- **Client** – Accepts user commands, sends requests to the server, and displays responses in a human-readable format.
- **Server** – Processes client requests, handles authentication and wallet operations, manages persistent data, and communicates with CoinAPI.
- **CoinAPI** – Provides cryptocurrency asset information and market prices.

## Available Commands

| Command | Description |
|---|---|
| `register` | Register a new user |
| `login` | Log in to an existing account |
| `deposit-money <amount>` | Deposit USD into your wallet |
| `list-offerings` | Display available cryptocurrencies |
| `buy --offering=<code> --money=<amount>` | Buy a cryptocurrency |
| `sell --offering=<code>` | Sell a cryptocurrency |
| `get-wallet-summary` | Display your current wallet balance and investments |
| `get-wallet-overall-summary` | Display your overall investment profit or loss |
| `help` | Display available commands |

## Technologies

- **Java** – Core application logic and client-server communication.
- **CoinAPI** – External REST API for cryptocurrency market data.
- **File-based persistence** – Stores user accounts and wallet information.
- **Multithreading** – Enables the server to handle multiple clients concurrently.

## Getting Started

### Prerequisites

- Java Development Kit (JDK)
- A CoinAPI API key
- Git

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/Bacekalki/CryptocurrencyWallet.git
   ```

2. Navigate to the project directory:

   ```bash
   cd CryptocurrencyWallet
   ```

3. Configure your CoinAPI API key using the configuration mechanism implemented by the application.

4. Start the server.

5. Start the client in a separate terminal.

> **Note:** The server must be running before starting the client. The exact commands for compiling and running the applications depend on the project configuration.

## Error Handling and Validation

The application validates user input and handles common failure scenarios, including:

- Invalid commands or command arguments.
- Invalid usernames and passwords.
- Insufficient wallet balance.
- Attempts to buy or sell unsupported cryptocurrencies.
- Invalid deposit or purchase amounts.
- Network communication failures.
- External API errors.

Technical exceptions and stack traces are recorded in log files, while users receive clear and understandable error messages.

## Project Structure

The repository contains two independent Java applications:

```text
CryptocurrencyWallet/
├── CryptoWalletClient/
│   └── ...
│
├── CryptocurrencyWalletManager/
│   └── ...
│
└── README.md
```

- `CryptoWalletClient` – Console application responsible for user interaction.
- `CryptocurrencyWalletManager` – Server application responsible for processing requests and managing wallet data.

## Disclaimer

This project is developed for educational purposes. It simulates cryptocurrency investments and does not perform actual cryptocurrency transactions or provide financial advice.
