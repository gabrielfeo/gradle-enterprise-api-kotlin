@file:Suppress("unused")

package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.API_MAX_BUILDS
import com.gabrielfeo.gradle.enterprise.api.internal.operator.pagedUntilLastBuild
import com.gabrielfeo.gradle.enterprise.api.internal.operator.withGradleAttributes
import com.gabrielfeo.gradle.enterprise.api.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*

/**
 * Gets builds on demand from the API, in as many requests as necessary. It allows
 * for queries of any size, as opposed to [GradleEnterpriseApi.getBuilds] which is limited by the
 * API itself to 1000.
 *
 * - Will request from the API until results end or an error occurs.
 * - Use [Sequence.take] and similar functions to stop collecting early.
 * - A subset of `getBuilds` params are supported
 */
fun GradleEnterpriseApi.getBuildsFlow(
    since: Long = 0,
    sinceBuild: String? = null,
    fromInstant: Long? = null,
    fromBuild: String? = null,
    buildsPerPage: Int = API_MAX_BUILDS,
): Flow<Build> {
    val api = this
    return flow {
        val firstBuilds = getBuilds(
            since = since,
            sinceBuild = sinceBuild,
            fromInstant = fromInstant,
            fromBuild = fromBuild,
            maxBuilds = buildsPerPage,
        )
        val pagedBuilds = firstBuilds.asFlow().pagedUntilLastBuild(api, buildsPerPage)
        emitAll(pagedBuilds)
    }
}

/**
 * Gets [GradleAttributes] of all builds from a given date. Queries [GradleEnterpriseApi.getBuilds]
 * first, since it's the only endpoint providing a timeline of builds, then maps each to
 * [GradleEnterpriseApi.getGradleAttributes].
 *
 * Don't expect client-side filtering to be efficient. Will request up to [Int.MAX_VALUE]
 * builds and their attributes concurrently and eagerly, with a buffer, in coroutines started in
 * [scope]. For other params, see [getBuildsFlow] and [GradleEnterpriseApi.getBuilds].
 *
 * @param scope CoroutineScope in which to create coroutines. Defaults to [GlobalScope].
 */
@OptIn(DelicateCoroutinesApi::class)
fun GradleEnterpriseApi.getGradleAttributesFlow(
    since: Long = 0,
    sinceBuild: String? = null,
    fromInstant: Long? = null,
    fromBuild: String? = null,
    scope: CoroutineScope = GlobalScope,
): Flow<GradleAttributes> =
    getBuildsFlow(
        since = since,
        sinceBuild = sinceBuild,
        fromInstant = fromInstant,
        fromBuild = fromBuild
    ).withGradleAttributes(scope, api = this).map { (_, attrs) ->
        attrs
    }
