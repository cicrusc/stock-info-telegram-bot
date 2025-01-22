// Import necessary libraries and modules for Telegram bot functionality and HTTP requests.
// These libraries allow for handling bot commands, making HTTP requests, and parsing JSON responses.
// ... [other imports]

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import com.google.gson.JsonParser
import io.github.cdimascio.dotenv.Dotenv
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.util.*

// Function to save user feedback in a text file for later review or analysis.
// This allows for continuous improvement of the bot based on user suggestions.
fun saveFeedback(userId: Long, feedback: String) {
    val feedbackFile = File("feedback.txt")

    if (!feedbackFile.exists()) {
        feedbackFile.createNewFile()
    }
    feedbackFile.appendText("Feedback da $userId: $feedback\n")
}


// Function to load the count of searches each user has made from a properties file.
// Helps in tracking usage and implementing any rate limits to prevent abuse.
fun loadSearchCounts(): MutableMap<Long, Int> {
    val file = File("searchCounts.properties")
    val properties = Properties()

    if (!file.exists()) {
        file.createNewFile()
    } else {
        properties.load(file.reader())
    }

    return properties.entries.associate { (key, value) ->
        key.toString().toLong() to value.toString().toInt()
    }.toMutableMap()
}

// Function to save the current count of searches per user back to the properties file.
// Ensures that the usage data persists across bot restarts and is available for analysis.
fun saveSearchCounts(counts: Map<Long, Int>) {
    val properties = Properties()
    counts.forEach { (key, value) -> properties[key.toString()] = value.toString() }
    properties.store(File("searchCounts.properties").writer(), null)
}

// Function to search a locally stored CSV for a ticker symbol based on a company name.
// Utilizes a simple matching mechanism; could be expanded for more complex matching or fuzzy logic.
fun searchTickerInCSV(companyName: String): String {
    val csvFilePath = object {}.javaClass.classLoader.getResource("data.csv")?.file
        ?: throw FileNotFoundException("CSV file not found")

    BufferedReader(FileReader(csvFilePath)).use { br ->
        var line: String?
        while (br.readLine().also { line = it } != null) {
            val values = line!!.split(',')
            if (values.size > 1 && values[1].contains(companyName, ignoreCase = true)) {
                return values[0] // Return the Symbol if a partial match is found
            }
        }
    }
    return "" // Return empty string if no match is found, indicating the need for an API search.
}

// Function to search for a company's ticker symbol using an external API.
// This method is used if the local CSV doesn't contain the desired information, ensuring comprehensive coverage.
fun searchTickerByName(companyName: String, apiKey: String): String {
    // ... [Code for API call and response handling]
    // The method handles various scenarios including no ticker found, or API errors, ensuring robustness.
    val tickerFromCSV = searchTickerInCSV(companyName)
    if (tickerFromCSV.isNotEmpty()) {
        return tickerFromCSV // Return the ticker if found in the CSV
    }

    // If not found in CSV, proceed with API call
    val client = OkHttpClient()
    val url = "http://api.marketstack.com/v1/tickers?access_key=$apiKey&search=$companyName"

    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseBody = response.body()?.string() ?: throw IOException("Response body is null")
        println("Response: $responseBody")  // Print the full response for debugging


        val jsonObject = JsonParser().parse(responseBody).asJsonObject
        val data = jsonObject.getAsJsonArray("data")

        if (data.size() == 0) {
            throw Exception("No ticker found for $companyName")
        }

        val results = data.map { it.asJsonObject }.toList()

        // Prefer short symbols
        val bestMatch = results.minByOrNull {
            it.get("symbol")?.asString?.length ?: Int.MAX_VALUE
        } ?: throw Exception("No suitable ticker found for $companyName")

        return bestMatch.get("symbol")?.asString ?: throw Exception("Symbol is missing from the result")
    }
}

// Function to fetch the latest market data for a given ticker symbol.
// Retrieves and calculates necessary information to provide a quick stock overview, like price changes.
fun fetchMarketData(symbol: String, apiKey: String): String {
    // ... [Code for market data retrieval and processing]
    // The method includes error handling and data formatting for user-friendly display.
    val client = OkHttpClient()
    val url = "http://api.marketstack.com/v1/eod?access_key=$apiKey&symbols=$symbol"

    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseBody = response.body()?.string() ?: "No response received"

        val parser = JsonParser()
        val element = parser.parse(responseBody)
        val jsonObject = element.asJsonObject

        val data = jsonObject.getAsJsonArray("data").first().asJsonObject

        //  Obtaining last update
        val lastClose = data.get("close").asString.toDouble()
        val previousClose = data.get("open").asString.toDouble()

        // Calculating percentage
        val percentageChange = ((lastClose - previousClose) / previousClose) * 100

        // Output
        val formattedResponse = "Stock: $symbol\n" +
                "Last Price: ${"%.2f".format(lastClose)}$\n" +
                "Daily Change: ${"%.2f".format(percentageChange)}%"

        // Check the message length
        return if (formattedResponse.length > 4000) formattedResponse.take(4000) + "..." else formattedResponse
    }
}

fun main() {

    // Load environment variables from the .env file
    // This allows us to securely store sensitive information like the bot token and API key
    val dotenv = Dotenv.load()

// Use the token and API key from the environment variables
    val botToken = dotenv["TELEGRAM_BOT_TOKEN"] ?: throw IllegalArgumentException("Bot token is missing!")
    val marketApiKey = dotenv["MARKET_API_KEY"] ?: throw IllegalArgumentException("API key is missing!")

    val bot = bot {
        // Bot setup with token and log level configuration. Replace with the actual token of your bot.
        // The token for your bot is passed from the .env file to keep it secure.
        token = botToken // Replace with the actual token of your bot
        logLevel = LogLevel.Network.Body // Detailed logging enabled for troubleshooting.

        val userSearchCount = loadSearchCounts()

        // Define the list of supported commands for easy reference and maintenance.
        val commands = setOf("/start", "/help", "/search", "/recent", "/empty", "/feedback")  // List of commands

        // Set a limit for the maximum number of searches a user can perform.
        val maxSearches = 5 // Maximum search limit per user

        dispatch {
            text { // Process only text messages received by the bot for simplicity and focus.

                // ... [Code for processing text messages, commands, and feedback]
                // Includes conditional checks and handling for different types of user inputs.
                val userId = message.chat.id

                // Trim the user's input to avoid issues with leading/trailing spaces.
                val input = message.text?.trim() ?: return@text  // Receive input from the user and remove whitespace

                // Command handling section: Determine if the input matches one of the predefined commands.
                if (input in commands) {
                    // ... [commands processing]
                    when (input) {
                        "/start" -> bot.sendMessage(
                            ChatId.fromId(userId),
                            text = "Welcome to the Stock Info Bot! Use /search to get stock prices, /recent to view your recent searches, and /help for more commands."
                        )

                        "/help" -> bot.sendMessage(
                            ChatId.fromId(userId), text = """
                             Welcome to Stock Info Bot! Here's how you can interact with me:
                             - Simply type a company's name or ticker symbol (like 'Apple' or 'AAPL') to get its latest stock information.
                             - Use /search followed by a company's name or ticker to initiate a detailed search (e.g., '/search Apple').
                             - Use /recent to see your recent searches.
                             - Use /empty to clear your recent searches.
                             For any assistance, type /help.
                        """.trimIndent()
                        )

                        "/search" -> bot.sendMessage(
                            ChatId.fromId(userId), text = """
                            To perform a detailed search, type /search followed by the company's name or ticker symbol (e.g., '/search Tesla' or '/search TSLA').
                            Alternatively, you can also simply type the name or ticker of the stock (like 'Nike' or 'NKE') for a quick search.
                        """.trimIndent()
                        )

                        "/recent" -> bot.sendMessage(
                            ChatId.fromId(userId),
                            text = "Your recent searches: [list of recent searches]. Use /search to find more stocks."
                        )

                        "/empty" -> bot.sendMessage(
                            ChatId.fromId(userId),
                            text = "Your search history has been cleared."
                        )

                        "/feedback" -> bot.sendMessage(
                            ChatId.fromId(userId),
                            text = "Please type your feedback after /feedback command. For example: /feedback I love this bot!"
                        )
                    }
                    return@text  // Stop processing further if a command is detected and handled.
                }

                // Feedback processing: Check if the user is trying to submit feedback.
                if (input.startsWith("/feedback")) {
                    val feedback = input.removePrefix("/feedback").trim()
                    if (feedback.isNotEmpty()) {
                        // ... [feedback processing]
                        saveFeedback(userId, feedback)
                        // Here you can save the feedback to a file or a database, or simply print it to the console
                        //println("Feedback da $userId: $feedback")
                        bot.sendMessage(ChatId.fromId(userId), text = "Thank you for your feedback!")
                    } else {
                        bot.sendMessage(
                            ChatId.fromId(userId),
                            text = "Please provide your feedback after the command. For example: /feedback I love this bot!"
                        )
                    }
                    return@text
                }

                val apiKey = marketApiKey // Replace with your API key
                var tickerSymbol = input.uppercase() // Convert to uppercase to standardize

                if (input.isNotBlank()) {

                    val searches = userSearchCount.getOrDefault(userId, 0)
                    if (searches >= maxSearches) {
                        // Send the limit reached message and ask for feedback
                        bot.sendMessage(
                            ChatId.fromId(userId), text = """
                            You have reached the limit of searches. Thank you for testing the bot!
                            If you liked the bot or want to help us improve, we invite you to leave feedback.
                            Click on the menu and select /feedback or simply type /feedback followed by your message.
                            """.trimIndent()
                        )
                        return@text  // Terminate the function to prevent further proceeding
                    }

                    /*Standard search processing: This section is triggered if the user's input is neither a recognized command nor a feedback submission.
                    It handles the searching of stock ticker symbols based on the user's input, updating the search count, fetching market data, and handling potential errors.
                    This ensures a robust and informative user experience by providing relevant stock information or appropriate error messages.*/
                    try {
                        // Check if the input is already a ticker symbol (e.g., short length, letters/numbers only)
                        // This is a simple heuristic; more robust checking may be necessary
                        if (!input.matches(Regex("^[A-Z0-9.]{1,5}$"))) {
                            // Search for the ticker corresponding to the company name
                            tickerSymbol = searchTickerByName(input, apiKey)
                            // Update the search count for the user
                            userSearchCount[userId] = searches + 1
                        }

                        // Use the found ticker to retrieve market data
                        val marketData = fetchMarketData(tickerSymbol, apiKey)
                        // Update the search count for the user
                        userSearchCount[userId] = searches + 1
                        // Save the search count to a file
                        saveSearchCounts(userSearchCount)
                        // Send the market data to the user
                        bot.sendMessage(ChatId.fromId(message.chat.id), text = marketData)
                    } catch (e: Exception) {
                        // General error handling: Captures and responds to any unexpected issues during the search.
                        bot.sendMessage(ChatId.fromId(message.chat.id), text = "An error occurred: ${e.message}")
                    }
                } else {
                    bot.sendMessage(ChatId.fromId(message.chat.id), text = "Please provide a valid input.")
                }
            }
        }
    }
    // Start the bot: Begins listening for incoming messages to process and interact with users.
    bot.startPolling()
}





