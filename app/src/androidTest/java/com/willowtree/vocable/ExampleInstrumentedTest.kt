package com.willowtree.vocable

import android.app.Instrumentation
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.listener.InstrumentationRunListener
import com.willowtree.vocable.ScreenRobot.Companion.withRobot
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
open class ExampleInstrumentedTest {

    @Rule
    @JvmField
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.willowtree.vocable.debug", appContext.packageName)
    }

    @Test
    fun assertBackArrow() {
        withRobot(VocableScreenRobot::class.java)
            .verifyBackArrowDisplayed()
    }

    @Test
    fun assertPleaseButton() {
        withRobot(VocableScreenRobot::class.java)
            .verifyPleaseButton()
    }

    @Test
    fun testForwardButton() {
        withRobot(VocableScreenRobot::class.java)
            .clickOnForwardButton()
            .verifyBasicNeeds()
    }

    // first big/complex test using screen robot pattern
    // Goal:
    // Go into settings and add a new category
    // questions:
    //      1. should this be one task/flow, or should i break it down?
    //      2. Should this make more use of mocks?
    //      3. is the pattern of click then verify too cumbersome, can i slim it down?
    //      4. Should clicks and verifies be grouped together in the screenRobot?
    @Test
    fun testSettingsMenu() {
        val categoryName = "Test category"
        withRobot(VocableScreenRobot::class.java)
            .clickOnSettingsButton()
            .verifyCategoriesAndPhrasesButtonDisplayed()
            .clickOnCategoriesAndPhrasesButton()
            .verifyCategoriesAndPhrasesMenuDisplayed()
            .clickOnAddCategoryButton()
            .verifyKeyboardInputScreen()
            .enterUserInput(categoryName)
            .clickOnSaveButton()
            .clickOnBackButton()
            .verifyCategoriesAndPhrasesMenuDisplayed()
            .verifyNewCategory(categoryName)
            /*.clickOnEditCategoryButton(categoryName)
            .verifyCategoryTitle(categoryName)
            .verifyRemoveCategoryButton()
            .clickOnRemoveCategoryButton()
            .verifyAreYouSureDialogue()
            .clickOnDialoguePositiveButton()
            .verifyTextNotPresent(categoryName)*/
    }

    @After
    fun tearDown() {

    }

    class VocableScreenRobot : ScreenRobot<VocableScreenRobot>() {

        fun verifyBackArrowDisplayed(): VocableScreenRobot {
            return checkIsDisplayedViewId(R.id.category_back_button)
        }

        fun verifyPleaseButton(): VocableScreenRobot {
            return checkIsDisplayedTextFromViewId(R.string.preset_please)
        }

        fun verifyCategoryTitle(categoryTitle: String): VocableScreenRobot {
            return checkIsDisplayedText(categoryTitle)
        }

        fun verifyRemoveCategoryButton(): VocableScreenRobot {
            return checkIsDisplayedViewId(R.id.remove_category_button)
        }

        fun verifyAreYouSureDialogue(): VocableScreenRobot {
            return checkIsDisplayedText("Are you sure?")
        }

        fun verifyTextNotPresent(text: String): VocableScreenRobot {
            return checkIsNotDisplayedText(text)
        }

        fun clickOnForwardButton(): VocableScreenRobot {
            return clickOnView(R.id.category_forward_button)
        }

        fun verifyBasicNeeds(): VocableScreenRobot {
            return checkIsDisplayedTextFromViewId(R.string.preset_basic_needs)
        }

        fun clickOnSettingsButton(): VocableScreenRobot {
            return clickOnView(R.id.settings_button)
        }

        fun verifyCategoriesAndPhrasesButtonDisplayed(): VocableScreenRobot {
            return checkIsDisplayedViewId(R.id.edit_categories_button)
        }

        fun clickOnCategoriesAndPhrasesButton(): VocableScreenRobot {
            return clickOnView(R.id.edit_categories_button)
        }

        fun verifyCategoriesAndPhrasesMenuDisplayed(): VocableScreenRobot {
            return checkIsDisplayedViewId(R.id.my_sayings_title)
        }

        fun clickOnAddCategoryButton(): VocableScreenRobot {
            return clickOnView(R.id.add_category_button)
        }

        fun verifyKeyboardInputScreen(): VocableScreenRobot {
            return checkIsDisplayedViewId(R.id.keyboard_input)
        }

        fun enterUserInput(inputString: String): VocableScreenRobot {
            return clickOnSequenceOfDisplayedText(inputString)
        }

        fun clickOnSaveButton(): VocableScreenRobot {
            return clickOnView(R.id.save_button)
        }

        fun clickOnBackButton(): VocableScreenRobot {
            return clickOnView(R.id.back_button)
        }

        fun verifyNewCategory(inputString: String): VocableScreenRobot {
            return checkDisplayedTextEndsWith(inputString)
        }

        fun clickOnEditCategoryButton(categoryName: String): VocableScreenRobot {
            return clickOnEditCategorySelectButton(R.id.edit_category_select_button, categoryName)
        }

        fun clickOnRemoveCategoryButton(): VocableScreenRobot {
            return clickOnView(R.id.remove_category_button)
        }

        fun clickOnDialoguePositiveButton(): VocableScreenRobot {
            return clickOnView(R.id.dialog_positive_button)
        }

    }

}