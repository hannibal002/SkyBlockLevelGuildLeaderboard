import at.guiildscanner.utils.Utils

class GuildScanner(private val apiKey: String) {

    fun load(playerUuid: String) {
        println("Loading guild data.")
        val hypixelData = Utils.getJSONResponse("https://api.hypixel.net/guild?key=$apiKey&player=$playerUuid")
        val guildData = hypixelData["guild"]
        if (guildData.isJsonNull) {
            System.err.println("This player is not in an guild!")
            return
        }

        val guild = guildData.asJsonObject
        val listOfMembers = mutableListOf<String>()
        for (element in guild["members"].asJsonArray) {
            listOfMembers.add(element.asJsonObject["uuid"].asString)
        }

        //TODO remove
//        for (i in 0..45) {
//            listOfMembers.removeFirst()
//        }

        val guildName = guild["name"].asString
        val guildSize = listOfMembers.size
        println(" ")
        println("Loading skyblock levels for guild '$guildName' with $guildSize members.")
        val membersData = loadMembersData(listOfMembers, guildSize)

        println(" ")
        println("Found the SkyBlock level for ${membersData.size} of $guildSize members.")
        println("Sorting the result.")
        println(" ")
        println("$guildName SkyBlock Level Leaderboard")
        printResults(membersData)

    }

    private fun loadMembersData(
        listOfMembers: MutableList<String>,
        size: Int
    ): MutableMap<String, Int> {
        val dataMap = mutableMapOf<String, Int>()

        for ((i, member) in listOfMembers.withIndex()) {
            val experience = loadExperience(member)
            val name = getMinecraftName(member)
            val display = "  [${i + 1}/$size] " + name
            when (experience) {
                -1 -> System.err.println("$display: SkyBlock level not in API.")
                -2 -> System.err.println("$display: No SkyBlock profiles.")
                else -> {
                    println("$display: $experience experience")
                    dataMap[name] = experience
                }
            }
        }
        return dataMap
    }

    private fun printResults(dataMap: MutableMap<String, Int>) {
        val sorted = dataMap.toList().sortedBy { (_, value) -> -value }.toMap()
        var j = 0
        for ((name, exp) in sorted) {
            val lvl = exp.toDouble() / 100
            println("#${++j} $name: $lvl")
        }
    }

    private fun loadExperience(playerUuid: String): Int {
        val url = "https://api.hypixel.net/skyblock/profiles?key=$apiKey&uuid=$playerUuid"
        var highestExperience = -1
        val profilesData = Utils.getJSONResponse(url)
        val profiles = profilesData["profiles"]
        if (profiles.isJsonNull) return -2 // No SkyBlock profiles

        for (element in profiles.asJsonArray) {
            val profile = element.asJsonObject
            for (entry in profile["members"].asJsonObject.entrySet()) {
                val memberId = entry.key
                val value = entry.value.asJsonObject
                if (memberId == playerUuid) {
                    val jsonElement = value["leveling"]
                    if (jsonElement != null) {
                        val asJsonObject = jsonElement.asJsonObject
                        val jsonElement1 = asJsonObject["experience"]
                        val experience = jsonElement1.asInt
                        if (experience > highestExperience) {
                            highestExperience = experience
                        }
                    }
                }
            }
        }
        return highestExperience
    }

    private fun getMinecraftName(uuid: String) =
        Utils.getJSONResponse("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")["name"].asString
}
