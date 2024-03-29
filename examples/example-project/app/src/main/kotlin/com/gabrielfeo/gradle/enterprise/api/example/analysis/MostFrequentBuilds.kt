package com.gabrielfeo.gradle.enterprise.api.example.analysis

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.extension.*
import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.*
import java.util.LinkedList

/**
 * See what builds are most commonly invoked by developers, e.g. 'clean assemble',
 * 'test' or 'check'. You can set up the URL and a token for your Gradle
 * Enterprise instance and run this notebook as-is for your own project. This is a
 * simple example of something you can do with the API. It could bring insights,
 * for example:
 *
 * - "Our developers frequently clean together with assemble. We should ask them why,
 *   because they shouldn't have to. Just an old habit from Maven or are they working
 *   around a build issue we don't know about?"
 *
 * - "Some are doing check builds locally, which we set up to trigger our notably slow
 *   legacy tests. We should suggest they run test instead, leaving check for CI to run."
 */
suspend fun mostFrequentBuilds(
    api: BuildsApi,
    startDate: LocalDate = LocalDate.now().minusWeeks(1),
    buildFilter: (GradleAttributes) -> Boolean = { build ->
        "LOCAL" in build.tags
    },
) {
    // Fetch builds from the API
    val startMilli = startDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    val builds: List<GradleAttributes> = api.getGradleAttributesFlow(since = startMilli)
        .filter(buildFilter)
        .toList(LinkedList())

    // Process builds and count how many times each was invoked
    val buildCounts = builds.groupBy { build ->
        val tasks = build.requestedTasks.joinToString(" ").trim(':')
        tasks.ifBlank { "IDE sync" }
    }.mapValues { (_, builds) ->
        builds.size
    }.entries.sortedByDescending { (_, count) ->
        count
    }

    // Print the top 5 as a pretty table
    val table = buildCounts.take(5).joinToString("\n") { (tasks, count) ->
        "${tasks.padEnd(100)} | $count"
    }
    println(
        """
            |---------------------
            |Most frequent builds:
            |
            |$table
        """.trimMargin()
    )
}
