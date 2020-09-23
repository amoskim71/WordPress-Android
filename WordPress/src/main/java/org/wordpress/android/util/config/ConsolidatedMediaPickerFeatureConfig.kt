package org.wordpress.android.util.config

import org.wordpress.android.BuildConfig
import org.wordpress.android.annotation.FeatureInDevelopment
import javax.inject.Inject

/**
 * Configuration of the Consolidated media picker
 */
@FeatureInDevelopment
class ConsolidatedMediaPickerFeatureConfig
@Inject constructor(appConfig: AppConfig) : FeatureConfig(
        appConfig,
        BuildConfig.CONSOLIDATED_MEDIA_PICKER
)
