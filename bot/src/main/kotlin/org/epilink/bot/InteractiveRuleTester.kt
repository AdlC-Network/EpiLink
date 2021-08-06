/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package org.epilink.bot

import kotlinx.coroutines.runBlocking
import org.epilink.bot.rulebook.*
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

/*
 * NOTE: I know this file looks like crap, but it is just a quick tester that's not intended to be super solid and
 * fancy.
 *
 * Groups in the regex:
 * 1. Rule name
 * 2. Discord ID
 * 3. Discord username (without discriminator)
 * 4. Discord discriminator
 * 5. (optional) E-mail address
 */
private val queryRegex = Regex("""(.+?)\[(\d+);(.+?);(\d{4})(?:;(.+?))?]""")

/**
 * A simple rule testing program
 */
fun ruleTester(rulebookFile: String) = runBlocking {
    println("-- EpiLink -- Interactive Rule Tester --")
    var rulebook = loadRulebook(rulebookFile) ?: Rulebook(mapOf()) { true }.also {
        println("<!> Failed to load the rulebook. An empty rulebook has been loaded instead.")
    }
    println("(?) Enter your query. Format: RuleName[discordId;discordUsername;discordDiscriminator;email]")
    println("    Enter exit to quit. Enter load: followed by a file path to load a new rulebook.")
    println("    Help: https://epilink.zoroark.guru/#/IRT")
    do {
        print(">>> ")
        val l = readLine() ?: exitProcess(0)
        handleLine(l, rulebook) {
            if (it != null)
                rulebook = it
            else
                println("<!> Loading failed. The rulebook was not changed.")
        }

    } while (true)
}

private suspend fun handleLine(l: String, rulebook: Rulebook, rulebookSetter: (Rulebook?) -> Unit) {
    // Exit command
    if (l == "exit") exitProcess(0)
    // Load command
    if (l.startsWith("load:")) {
        rulebookSetter(loadRulebook(l.substring(5)))
        return
    }
    // E-mail validation command
    if (l.startsWith("validate:")) {
        val validator = rulebook.validator
        if (validator == null) {
            println("<!> No e-mail validator is defined in the rulebook")
        } else {
            print("(i) Running e-mail validator... ")
            val result = runCatching { validator(l.substring(9)) }
            result.fold(onSuccess = {
                if (it) {
                    println("OK, e-mail passes (returned true)")
                } else {
                    println("NOT OK, e-mail rejected (returned false)")
                }
            }, onFailure = {
                println("error")
                it.printStackTrace(System.out)
            })
        }
        return
    }
    // Actual query
    val query = toQuery(l)
    if (query == null) {
        println("<!> Invalid query. Please try again.")
        return
    }
    val rule = rulebook.rules[query.ruleName]
    if (rule == null) {
        println("<!> No rule exists named ${query.ruleName}")
        return
    }
    print("(i) Running rule ${query.ruleName}... ")
    runCatching {
        when (rule) {
            is StrongIdentityRule -> {
                if (query.email == null) {
                    println("<!> Rule ${query.ruleName} requires an e-mail address (it is a strong identity rule).")
                    error("E-mail address required")
                }

                rule.determineRoles(
                    query.discordId,
                    query.discordUsername,
                    query.discordDiscriminator,
                    query.email
                )
            }
            is WeakIdentityRule -> rule.determineRoles(
                query.discordId,
                query.discordUsername,
                query.discordDiscriminator
            )
        }
    }.fold(onSuccess = { result ->
        result.joinToString(", ").ifEmpty { "(nothing found)" }.let {
            println("found roles: $it")
        }
    }, onFailure = { error ->
        println("error")
        error.printStackTrace(System.out)
    })
}


private fun loadRulebook(fileName: String): Rulebook? {
    println("(i) Loading rulebook, please wait...")
    val result = runCatching {
        val rulebookContent = Files.readString(Paths.get(fileName))
        runBlocking { loadRules(rulebookContent) }.also {
            println("(i) Rulebook loaded with ${it.rules.size} rule(s).")
        }
    }
    return result.getOrElse { it.printStackTrace(System.out); null }
}

private data class Query(
    val ruleName: String,
    val discordId: String,
    val discordUsername: String,
    val discordDiscriminator: String,
    val email: String?
)

@Suppress("UnsafeCallOnNullableType") // Impossible for groups to be null on match due to the Regex.
private fun toQuery(queryString: String): Query? {
    val match = queryRegex.matchEntire(queryString) ?: return null
    return Query(
        match.groups[1]!!.value,
        match.groups[2]!!.value,
        match.groups[3]!!.value,
        match.groups[4]!!.value,
        match.groups[5]?.value
    )
}