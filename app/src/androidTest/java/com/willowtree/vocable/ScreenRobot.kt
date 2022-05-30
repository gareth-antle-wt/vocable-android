package com.willowtree.vocable

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.*

abstract class ScreenRobot<T: ScreenRobot<T>> {

    fun checkIsDisplayedViewId(viewId: Int): T {
        onView(withId(viewId))
            .check(matches(isDisplayed()))
        return this as T
    }

    fun checkIsDisplayedTextFromViewId(viewId: Int): T {
        onView(withText(viewId))
            .check(matches(isDisplayed()))
        return this as T
    }

    // NOTE: This function is case agnostic
    fun checkIsDisplayedText(string: String): T {
        onView(withText(equalToIgnoringCase(string)))
            .check(matches(isDisplayed()))
        return this as T
    }

    fun checkIsNotDisplayedText(string: String): T {
        onView(withText(equalToIgnoringCase(string)))
            .check(doesNotExist())
        return this as T
    }

    fun checkDisplayedTextEndsWith(string: String): T {
        onView(withText(endsWith(string)))
            .check(matches(isDisplayed()))
        return this as T
    }

    fun clickOnView(viewId: Int): T {
        onView(withId(viewId))
            .perform(click())
        return this as T
    }

    fun clickOnDisplayedText(viewId: Int): T {
        onView(withText(viewId))
            .perform(click())
        return this as T
    }

    fun clickOnEditCategorySelectButton(viewId: Int, categoryName: String): T {
        onView(allOf(withId(viewId), withParent(withChild(withText(endsWith(categoryName))))))
            .perform(click())
        return this as T
    }

    // Note: this method will ignore case
    fun clickOnSequenceOfDisplayedText(string: String): T {
        for (char in string) {
            if (char.toString() == " ") {
                onView(withId(R.id.keyboard_space_button))
                    .perform(click())
            } else {
                onView(withText(equalToIgnoringCase(char.toString())))
                    .perform(click())
            }
        }
        return this as T
    }

    companion object {

        fun <T : ScreenRobot<*>> withRobot(screenRobotClass: Class<T>?): T {
            if (screenRobotClass == null) {
                throw IllegalArgumentException("instance class == null")
            }

            try {
                return screenRobotClass.newInstance()
            } catch (iae: IllegalAccessException) {
                throw RuntimeException("IllegalAccessException", iae)
            } catch (ie: InstantiationException) {
                throw RuntimeException("InstantiationException", ie)
            }

        }
    }

}