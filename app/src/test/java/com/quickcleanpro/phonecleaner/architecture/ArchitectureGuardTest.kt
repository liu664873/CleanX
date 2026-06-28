package com.quickcleanpro.phonecleaner.architecture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ArchitectureGuardTest {
    private val root = File(requireNotNull(System.getProperty("user.dir")))
    private val mainSource = File(root, "src/main/java/com/quickcleanpro/phonecleaner")

    @Test
    fun mainConfigDoesNotReferenceFlavorUiResourcesOrLegacyScreen() {
        val configDir = File(mainSource, "config")
        val offenders =
            configDir.kotlinFiles()
                .filter { file ->
                    val text = file.readText()
                    "R.drawable" in text ||
                        "R.string" in text ||
                        "presentation.common.route.Screen" in text
                }

        assertTrue("main/config must not depend on UI resources or legacy Screen: $offenders", offenders.isEmpty())
    }

    @Test
    fun advertiseSdkIsOnlyUsedInsideAdvertisePackageOrApplicationStartup() {
        val offenders =
            mainSource.kotlinFiles()
                .filterNot { file ->
                    file.inPath("advertise") ||
                        file.name == "QuickCleanApplication.kt"
                }.filter { file ->
                    "AdvertiseSdk" in file.readText()
                }

        assertTrue("AdvertiseSdk must stay behind app/advertise mediator: $offenders", offenders.isEmpty())
    }

    @Test
    fun legacyScreenIsDocumentedAsCompatibilityLayer() {
        val screenFile =
            File(
                mainSource,
                "presentation/common/route/Screen.kt",
            )
        assertTrue(screenFile.exists())
        assertTrue(
            "Screen.kt should clearly be marked legacy compatibility.",
            "legacy" in screenFile.readText().lowercase(),
        )
    }

    @Test
    fun originalFlavorSourceSetIsRemoved() {
        assertFalse(File(root, "src/original").exists())
    }

    @Test
    fun routeRegistrationLivesInFlavorSourceSet() {
        val mainRouteDir = File(mainSource, "presentation/common/route")
        val routeRegistrationFiles =
            mainRouteDir.kotlinFiles()
                .filter { file ->
                    file.name.endsWith("Routes.kt") &&
                        file.name !in setOf("NavigationActions.kt", "RoutePlaceholder.kt")
                }

        assertTrue("Route registration files should live in the flavor UI source set: $routeRegistrationFiles", routeRegistrationFiles.isEmpty())
    }

    @Test
    fun mainRoutePackageDoesNotContainFlavorComposeUi() {
        val mainRouteDir = File(mainSource, "presentation/common/route")
        val allowedComposeFiles = setOf("AppNavigation.kt", "AdManager.kt", "RouteManager.kt")
        val offenders =
            mainRouteDir.kotlinFiles()
                .filterNot { file -> file.name in allowedComposeFiles }
                .filter { file ->
                    val text = file.readText()
                    "@Composable" in text ||
                        "androidx.compose" in text
                }

        assertTrue("Flavor route UI should live in flavor source sets: $offenders", offenders.isEmpty())
    }

    @Test
    fun legacyScreenIsOnlyUsedByCompatibilityLayerInMain() {
        val offenders =
            mainSource.kotlinFiles()
                .filterNot { file -> file.name == "Screen.kt" }
                .filter { file ->
                    val text = file.readText()
                    "presentation.common.route.Screen" in text ||
                        "Screen." in text
                }

        assertTrue("main code should use AppRoute instead of legacy Screen: $offenders", offenders.isEmpty())
    }

    @Test
    fun mainPermissionFlowDoesNotReferenceFlavorPopupComponents() {
        val permissionDir = File(mainSource, "presentation/common/permission")
        val offenders =
            permissionDir.kotlinFiles()
                .filter { file ->
                    "presentation.common.components.popups" in file.readText()
                }

        assertTrue("main permission flow must delegate popup UI to VariantPermissionUi: $offenders", offenders.isEmpty())
    }

    @Test
    fun mainPermissionFlowDoesNotOwnFlavorPermissionPromptCopy() {
        val permissionDir = File(mainSource, "presentation/common/permission")
        val offenders =
            permissionDir.kotlinFiles()
                .filter { file ->
                    val text = file.readText()
                    "copyFor(" in text ||
                        "permission_storage_desc" in text ||
                        "permission_hint_junk_deleted" in text ||
                        "permission_whatsapp_storage_desc" in text ||
                        "permission_app_lock_usage_desc" in text
                }

        assertTrue("permission prompt copy should be owned by flavor UI: $offenders", offenders.isEmpty())
    }

    @Test
    fun mainPermissionFlowDoesNotOwnFlavorPermissionLabels() {
        val permissionDir = File(mainSource, "presentation/common/permission")
        val offenders =
            permissionDir.kotlinFiles()
                .filter { file ->
                    val text = file.readText()
                    "R.string" in text ||
                        "ManageItem" in text ||
                        "manageItems" in text
                }

        assertTrue("permission labels should be owned by flavor UI: $offenders", offenders.isEmpty())
    }

    @Test
    fun flavorSpecificViewModelsStayOutOfMainPresentationModule() {
        val presentationModule = File(mainSource, "di/PresentationModule.kt")
        val text = presentationModule.readText()

        assertFalse("ManagePermissionsViewModel should be provided by flavor DI.", "ManagePermissionsViewModel" in text)
    }

    @Test
    fun mainPresentationScreenDoesNotContainFlavorComposeScreens() {
        val screenDir = File(mainSource, "presentation/screen")
        val offenders =
            screenDir.kotlinFiles()
                .filter { file ->
                    val text = file.readText()
                    file.name.endsWith("Screen.kt") ||
                        "@Composable" in text
                }

        assertTrue("Compose screens should live in flavor UI source sets: $offenders", offenders.isEmpty())
    }

    @Test
    fun mainCommonComponentsDoesNotContainFlavorComposeUi() {
        val commonComponentsDir = File(mainSource, "presentation/common/components")
        val offenders =
            commonComponentsDir.kotlinFiles()
                .filter { file ->
                    val text = file.readText()
                    "@Composable" in text ||
                        "androidx.compose" in text
                }

        assertTrue("Flavor Compose components should live in flavor UI source sets: $offenders", offenders.isEmpty())
    }

    private fun File.kotlinFiles(): List<File> =
        walkTopDown()
            .filter { file -> file.isFile && file.extension == "kt" }
            .toList()

    private fun File.inPath(segment: String): Boolean =
        invariantSeparatorsPath.split('/').any { it == segment }
}
