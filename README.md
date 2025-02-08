![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Telegram](https://img.shields.io/badge/Telegram-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white)
![API](https://img.shields.io/badge/API-External_Integration-FF6F00?style=for-the-badge&logo=api&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![OOP](https://img.shields.io/badge/OOP-Object_Oriented_Programming-FF6F00?style=for-the-badge&logo=java&logoColor=white)
---

# Stock Info Telegram Bot

A Telegram bot that helps users get real-time stock market data, search for company ticker symbols, and track recent searches. The bot also supports user feedback and has rate-limiting functionality to prevent abuse.

## Features

- **Search by Company Name or Ticker**: Users can search for stock data by typing the company name or its ticker symbol.
- **Real-Time Market Data**: Fetch the latest stock price, daily change, and other market data for a given ticker symbol.
- **Recent Searches**: Users can view their recent searches.
- **Rate-Limiting**: Users are limited to a certain number of searches, after which they are prompted to leave feedback.
- **Feedback Submission**: Users can provide feedback that is saved for analysis.

## Technologies Used

- **Kotlin**: The programming language used to write the bot.
- **Telegram Bot API**: Used to interact with users and send messages.
- **OkHttp**: For making HTTP requests to fetch market data from an external API.
- **MarketStack API**: External API used to retrieve real-time stock market data.
- **Dotenv**: For securely managing environment variables such as the Telegram bot token and API key.
- **Gson**: For parsing JSON responses from the API.

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/stock-info-bot.git
cd stock-info-bot
```

### 2. Install Dependencies

You need to have **Kotlin** and **Gradle** installed on your machine. If you don't have them yet, you can download them from the official websites:

- [Kotlin](https://kotlinlang.org/)
- [Gradle](https://gradle.org/)

After installing them, run the following command to set up your project:

```bash
gradle build
```

### 3. Set Up Environment Variables

Create a `.env` file at the root of the project and add your Telegram Bot Token and MarketStack API key:

```ini
TELEGRAM_BOT_TOKEN=your-telegram-bot-token
MARKET_API_KEY=your-marketstack-api-key
```

You can obtain your **Telegram Bot Token** by creating a bot on Telegram through the [BotFather](https://core.telegram.org/bots#botfather).

To get your **MarketStack API Key**, sign up at [MarketStack](https://marketstack.com/) and create an account.

### 4. Run the Bot

Run the bot using the following command:

```bash
gradle run
```

The bot will now be listening for commands on Telegram.

## Usage

Once the bot is running, you can interact with it on Telegram by typing the following commands:

- **/start**: Start interacting with the bot and get an introduction.
- **/help**: Get a list of available commands.
- **/search [company name or ticker]**: Search for a company's stock data by name or ticker symbol (e.g., `/search Apple` or `/search AAPL`).
- **/recent**: See your recent searches.
- **/empty**: Clear your search history.
- **/feedback [your feedback]**: Submit feedback to the bot.

### Example Commands:

- `/search Tesla`
- `/recent`
- `/feedback I love this bot!`

## Files in the Project

- **src/main/kotlin/Main.kt**: The main source file containing the logic for the Telegram bot.
- **src/main/resources/data.csv**: A CSV file containing company ticker symbols and their names, used for quick lookup.
- **feedback.txt**: A text file where user feedback is saved for later review. This file is **auto-generated** when users provide feedback. It is not part of the repository.
- **searchCounts.properties**: A file that tracks the number of searches per user to implement rate limits. This file is **auto-generated** when the bot is run.

## Contributing

Feel free to fork the repository and contribute by submitting pull requests. All improvements and bug fixes are welcome.

## License

This project is open-source and available under the [MIT License](LICENSE).

---
