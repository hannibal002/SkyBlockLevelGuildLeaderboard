fun main(args: Array<String>) {
    val apiKey = args[0]
    val playerUuid = args[1]
    GuildScanner(apiKey).load(playerUuid)
}