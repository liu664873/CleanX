package com.quickcleanpro.phonecleaner.config

import com.quickcleanpro.phonecleaner.navigation.AppRoute

fun FeatureKey.routeOrNull(): AppRoute? =
    FeatureCatalog.routeFor(this)
