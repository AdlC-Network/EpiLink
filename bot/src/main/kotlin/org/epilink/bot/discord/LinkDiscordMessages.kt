/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package org.epilink.bot.discord

import org.epilink.bot.LinkException
import org.epilink.bot.config.LinkDiscordConfig
import org.epilink.bot.config.LinkDiscordServerSpec
import org.epilink.bot.config.LinkPrivacy
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * The Discord bot interface that generates embeds for various instances
 */
interface LinkDiscordMessages {
    /**
     * Get the embed to send to a user about why connecting failed
     */
    fun getCouldNotJoinEmbed(guildName: String, reason: String): DiscordEmbed

    /**
     * Get the initial server message sent upon connection with the server not knowing who the person is, or null if no
     * message should be sent.
     */
    fun getGreetingsEmbed(guildId: String, guildName: String): DiscordEmbed?

    /**
     * Send an identity access notification to the given Discord ID with the given information, or null if no message
     * should be sent. Whether a message should be sent or not is determined through the privacy configuration of
     * EpiLink.
     *
     * @param automated Whether the access was done automatically or not
     * @param author The author of the request (bot name or human name)
     * @param reason The reason behind this identity access
     */
    fun getIdentityAccessEmbed(automated: Boolean, author: String, reason: String): DiscordEmbed?

    /**
     * Get the ban notification embed, or null if privacy settings have ban notifications disabled.
     */
    fun getBanNotification(banReason: String, banExpiry: Instant?): DiscordEmbed?
}

private const val logoUrl = "https://raw.githubusercontent.com/EpiLink/EpiLink/dev/assets/epilink256.png"
private const val unknownUserLogoUrl = "https://raw.githubusercontent.com/EpiLink/EpiLink/dev/assets/unknownuser256.png"
private const val idNotifyLogoUrl = "https://raw.githubusercontent.com/EpiLink/EpiLink/dev/assets/idnotify256.png"
private const val banLogoUrl = "https://raw.githubusercontent.com/EpiLink/EpiLink/dev/assets/ban256.png"

private val poweredByEpiLink = DiscordEmbedFooter("Powered by EpiLink", logoUrl)

internal class LinkDiscordMessagesImpl : LinkDiscordMessages, KoinComponent {
    private val config: LinkDiscordConfig by inject()

    private val privacyConfig: LinkPrivacy by inject()

    override fun getCouldNotJoinEmbed(guildName: String, reason: String) =
        DiscordEmbed(
            title = ":x: Could not authenticate on $guildName",
            description = "Failed to authenticate you on $guildName. Please contact an administrator if you think that should not be happening.",
            fields = listOf(DiscordEmbedField("Reason", reason, true)),
            footer = poweredByEpiLink,
            color = "red"
        )

    override fun getGreetingsEmbed(guildId: String, guildName: String): DiscordEmbed? {
        val guildConfig = config.getConfigForGuild(guildId)
        if (!guildConfig.enableWelcomeMessage)
            return null
        return guildConfig.welcomeEmbed ?: DiscordEmbed(
            title = ":closed_lock_with_key: Authentication required for $guildName",
            description =
            """
                **Welcome to $guildName**. Access to this server is restricted. Please log in using the link below to get full access to the server's channels.
                """.trimIndent(),
            fields = run {
                val ml = mutableListOf<DiscordEmbedField>()
                val welcomeUrl = config.welcomeUrl
                if (welcomeUrl != null)
                    ml += DiscordEmbedField("Log in", welcomeUrl)
                ml += DiscordEmbedField(
                    "Need help?",
                    "Contact the administrators of $guildName if you need help with the procedure."
                )
                ml
            },
            thumbnail = unknownUserLogoUrl,
            footer = poweredByEpiLink,
            color = "#3771c8"
        )
    }

    override fun getIdentityAccessEmbed(
        automated: Boolean,
        author: String,
        reason: String
    ): DiscordEmbed? {
        if (privacyConfig.shouldNotify(automated)) {
            val str = buildString {
                append("Your identity was accessed")
                if (privacyConfig.shouldDiscloseIdentity(automated)) {
                    append(" by *$author*")
                }
                if (automated) {
                    append(" automatically")
                }
                appendln(".")
            }
            return DiscordEmbed(
                title = "Identity access notification",
                description = str,
                fields = listOf(
                    DiscordEmbedField("Reason", reason, false),
                    if (automated) {
                        DiscordEmbedField(
                            "Automated access",
                            "This access was conducted automatically by a bot. No administrator has accessed your identity.",
                            false
                        )
                    } else {
                        DiscordEmbedField(
                            "I need help!",
                            "Contact an administrator if you believe that this action was conducted against the Terms of Services.",
                            false
                        )
                    }
                ),
                color = "#ff6600",
                thumbnail = idNotifyLogoUrl,
                footer = poweredByEpiLink
            )
        } else return null
    }

    override fun getBanNotification(banReason: String, banExpiry: Instant?): DiscordEmbed? =
        if (privacyConfig.notifyBans) {
            DiscordEmbed(
                title = ":no_entry_sign: You have been banned",
                description = "You have been banned on EpiLink. All of your roles have been removed. For more" +
                        " information, please contact an administrator.",
                fields = listOf(
                    DiscordEmbedField("Reason", banReason),
                    DiscordEmbedField(
                        "Expires on",
                        banExpiry?.let { "Expires on ${it.getDate()} at ${it.getTime()} (UTC+0)" }
                            ?: "This ban does not expire."
                    )
                ),
                color = "#d40000",
                thumbnail = banLogoUrl,
                footer = poweredByEpiLink
            )
        } else {
            null
        }

}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun Instant.getDate(): String =
    DateTimeFormatter.ISO_LOCAL_DATE.format(this.atOffset(ZoneOffset.UTC))

private fun Instant.getTime(): String =
    timeFormatter.format(this.atOffset(ZoneOffset.UTC))

/**
 * Retrieve the configuration for a given guild, or throw an error if such a configuration could not be found.
 *
 * Expects the guild to be monitored (i.e. expects a configuration to be present).
 *
 * @throws LinkException If the configuration could not be found.
 */
fun LinkDiscordConfig.getConfigForGuild(guildId: String): LinkDiscordServerSpec =
    this.servers.firstOrNull { it.id == guildId }
        ?: throw LinkException("Configuration not found, but guild was expected to be monitored")
