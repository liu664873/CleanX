package com.quickcleanpro.phonecleaner.presentation.common.route

/**
 * Minimal ad manager interface.
 * Call [showAd] to display an ad; [onComplete] is invoked when
 * the ad finishes (whether skipped, completed, or failed).
 *
 * Implement this with your ad SDK (AdMob, etc.).
 */
fun interface AdManager {
    /**
     * Show an ad for the given [placement].
     * @param onComplete called when the ad finishes — navigates to the target page.
     */
    fun showAd(placement: String, onComplete: () -> Unit)
}

/** Default no-op [AdManager] — navigation proceeds immediately. */
val NoOpAdManager = AdManager { _, onComplete -> onComplete() }
