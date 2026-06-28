package com.pdffox.adv.safe

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pdffox.adv.SafeConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SafeCheckerPackageNameTest {

	@Test
	fun checkPackageNameReturnsTrueWhenPackageMatches() {
		val baseContext = InstrumentationRegistry.getInstrumentation().targetContext
		val context = object : ContextWrapper(baseContext) {
			override fun getPackageName(): String = "com.datatool.photorecovery"
		}

		assertTrue(invokeCheckPackageName(context))
	}

	@Test
	fun checkPackageNameReturnsFalseWhenPackageMismatches() {
		val baseContext = InstrumentationRegistry.getInstrumentation().targetContext
		val context = object : ContextWrapper(baseContext) {
			override fun getPackageName(): String = "com.example.attacker"
		}

		assertFalse(invokeCheckPackageName(context))
	}

	private fun invokeCheckPackageName(context: Context): Boolean {
		val method = SafeChecker::class.java.getDeclaredMethod(
			"checkPackageName",
			Context::class.java,
			SafeConfig::class.java,
		)
		method.isAccessible = true
		return method.invoke(
			SafeChecker,
			context,
			SafeConfig(expectedPackageName = "com.datatool.photorecovery"),
		) as Boolean
	}
}
