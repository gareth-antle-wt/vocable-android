/**

These tests confirm that users can successfully upgrade from older versions of Vocable to newer versions.
All new migrations should have a test confirming the migration.

**/

package com.willowtree.vocable.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.willowtree.vocable.presets.PresetCategories
import com.willowtree.vocable.utils.VocableSharedPreferences
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    companion object {
        private const val TEST_DB = "migration-test"
    }

    private val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        VocableDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        val mySayingsCategoryNameV2 = "My Sayings"
        val mySayingTestPhraseV2 = "Test Phrase"

        helper.createDatabase(TEST_DB, 2).apply {
            // Create mock My Sayings category
            val catId = System.currentTimeMillis()
            execSQL("INSERT INTO Category (identifier, creation_date, is_user_generated, name) VALUES ($catId, ${System.currentTimeMillis()}, 0, '$mySayingsCategoryNameV2')")

            // Add a custom phrase to My Sayings category
            val phraseId = System.currentTimeMillis()
            execSQL("INSERT INTO Phrase (identifier, creation_date, is_user_generated, last_spoken_date, utterance, category_id) VALUES ($phraseId, ${System.currentTimeMillis()}, 0, ${System.currentTimeMillis()}, '$mySayingTestPhraseV2', $catId)")

            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 3, true, VocableDatabaseMigrations.MIGRATION_2_3)
            .apply {
                // Verify that V2 custom phrase was saved in SharedPreferences
                val sharedPrefs = VocableSharedPreferences()
                val v2MySayings = sharedPrefs.getMySayings()
                Assert.assertEquals(1, v2MySayings.size)
                Assert.assertEquals(mySayingTestPhraseV2, v2MySayings.first())

                // Verify that new schema is as expected
                val categoryIdV3 = "Category_V3"
                val categoryNameV3 = "Category_Name_V3"
                execSQL("INSERT INTO Category (category_id, creation_date, is_user_generated, localized_name, hidden, sort_order) VALUES ('$categoryIdV3', ${System.currentTimeMillis()}, 0, '$categoryNameV3', 0, 0)")

                val phraseIdV3 = "Phrase_V3"
                val phraseUtteranceV3 = "Phrase_Utterance_V3"
                execSQL("INSERT INTO Phrase (phrase_id, creation_date, is_user_generated, last_spoken_date, localized_utterance, sort_order) VALUES ('$phraseIdV3', ${System.currentTimeMillis()}, 0, ${System.currentTimeMillis()}, '$phraseUtteranceV3', 0)")

                close()
            }
    }

    @Test
    @Throws(IOException::class)
    fun migrate3to4() {
        val categoryNameMap = HashMap<String, String>()
        categoryNameMap["en_US"] = "My Sayings"

        val phraseOneMap = HashMap<String, String>()
        phraseOneMap["en_US"] = "Saying 1"

        val phraseTwoMap = HashMap<String, String>()
        phraseTwoMap["en_US"] = "Saying 2"

        val mySayingsCategoryNameV3 = Converters.stringMapToJson(categoryNameMap)
        val mySayingTestPhraseOneV3 = Converters.stringMapToJson(phraseOneMap)
        val mySayingTestPhraseTwoV3 = Converters.stringMapToJson(phraseTwoMap)

        val phraseOneId = UUID.randomUUID().toString()
        val creationDateOne = System.currentTimeMillis()

        val phraseTwoId = UUID.randomUUID().toString()
        val creationDateTwo = System.currentTimeMillis()

        helper.createDatabase(TEST_DB, 3).apply {
            // Create mock My Sayings category
            execSQL("INSERT INTO Category (category_id, creation_date, is_user_generated, localized_name, hidden, sort_order) VALUES ('${PresetCategories.MY_SAYINGS.id}', ${System.currentTimeMillis()}, 0, '$mySayingsCategoryNameV3', 0, ${PresetCategories.MY_SAYINGS.initialSortOrder})")

            // Add custom phrases to My Sayings category
            execSQL("INSERT INTO Phrase (phrase_id, creation_date, is_user_generated, last_spoken_date, localized_utterance, sort_order) VALUES ('$phraseOneId', $creationDateOne, 1, $creationDateOne, '$mySayingTestPhraseOneV3', 0)")
            execSQL("INSERT INTO CategoryPhraseCrossRef (category_id, phrase_id) VALUES ('${PresetCategories.MY_SAYINGS.id}', '$phraseOneId')")

            execSQL("INSERT INTO Phrase (phrase_id, creation_date, is_user_generated, last_spoken_date, localized_utterance, sort_order) VALUES ('$phraseTwoId', $creationDateTwo, 1, $creationDateTwo, '$mySayingTestPhraseTwoV3', 1)")
            execSQL("INSERT INTO CategoryPhraseCrossRef (category_id, phrase_id) VALUES ('${PresetCategories.MY_SAYINGS.id}', '$phraseTwoId')")
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true, VocableDatabaseMigrations.MIGRATION_3_4)
            .apply {
                // Verify that new schema is as expected
                val categoryId = PresetCategories.MY_SAYINGS.id
                val crossRefCursor =
                    query("SELECT phrase_id FROM CategoryPhraseCrossRef WHERE category_id = '$categoryId'")
                val myLocalizedSayings = arrayListOf<String>()
                val phraseIds =  mutableListOf<String>()
                while (crossRefCursor.moveToNext()) {
                    val phraseId = crossRefCursor.getString(crossRefCursor.getColumnIndex("phrase_id"))
                    phraseIds.add(phraseId)
                }
                crossRefCursor.close()

                phraseIds.forEach {
                    val phraseCursor = query("SELECT localized_utterance FROM Phrase WHERE phrase_id = '$it'")
                    while (phraseCursor.moveToNext()) {
                        val saying = phraseCursor.getString(phraseCursor.getColumnIndex("localized_utterance"))
                        myLocalizedSayings.add(saying)
                    }
                    phraseCursor.close()
                }
                Assert.assertTrue(myLocalizedSayings.contains(mySayingTestPhraseOneV3))
                Assert.assertTrue(myLocalizedSayings.contains(mySayingTestPhraseTwoV3))
                close()
            }
    }

    @Test
    @Throws(IOException::class)
    fun migrateSharedPreferenceSayingsDuring3to4() {
        val categoryNameMap = HashMap<String, String>()
        categoryNameMap["en_US"] = "My Sayings"

        val mySayingsCategoryNameV3 = Converters.stringMapToJson(categoryNameMap)

        val mySayingLegacyPhrase = "Legacy Phrase"
        val legacyMyPhrases = LinkedHashSet<String>()
        legacyMyPhrases.add(mySayingLegacyPhrase)
        VocableSharedPreferences().setMySayings(legacyMyPhrases)

        helper.createDatabase(TEST_DB, 3).apply {
            // Create mock My Sayings category
            execSQL("INSERT INTO Category (category_id, creation_date, is_user_generated, localized_name, hidden, sort_order) VALUES ('${PresetCategories.MY_SAYINGS.id}', ${System.currentTimeMillis()}, 0, '$mySayingsCategoryNameV3', 0, ${PresetCategories.MY_SAYINGS.initialSortOrder})")

            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true, VocableDatabaseMigrations.MIGRATION_3_4)
            .apply {
                // Verify that new schema is as expected
                val categoryId = PresetCategories.MY_SAYINGS.id
                val crossRefCursor =
                    query("SELECT phrase_id FROM CategoryPhraseCrossRef WHERE category_id = '$categoryId'")
                val myLocalizedSayings = arrayListOf<String>()
                val phraseIds =  mutableListOf<String>()
                while (crossRefCursor.moveToNext()) {
                    val phraseId = crossRefCursor.getString(crossRefCursor.getColumnIndex("phrase_id"))
                    phraseIds.add(phraseId)
                }
                crossRefCursor.close()

                phraseIds.forEach {
                    val phraseCursor = query("SELECT localized_utterance FROM Phrase WHERE phrase_id = '$it'")
                    while (phraseCursor.moveToNext()) {
                        val saying = phraseCursor.getString(phraseCursor.getColumnIndex("localized_utterance"))
                        myLocalizedSayings.add(saying)
                    }
                    phraseCursor.close()
                }
                val hashMap = HashMap<String, String>()
                hashMap["en_US"] = mySayingLegacyPhrase
                val localizedLegacyPhrase = Converters.stringMapToJson(hashMap)
                Assert.assertTrue(myLocalizedSayings.contains(localizedLegacyPhrase))
                close()
            }
    }

    @Test
    @Throws(IOException::class)
    fun migrate4to5() {
        // This migration adds a timestamp to phrases.

        val categoryNameMap = HashMap<String, String>()
        categoryNameMap["en_US"] = "My Sayings"

        val phraseOneMap = HashMap<String, String>()
        phraseOneMap["en_US"] = "Saying 1"

        val categoryNameV4 = Converters.stringMapToJson(categoryNameMap)
        val testPhraseV4 = Converters.stringMapToJson(phraseOneMap)

        val phraseId = UUID.randomUUID().toString()
        val creationDate = System.currentTimeMillis()

        helper.createDatabase(TEST_DB, 4).apply {
            // Create mock My Sayings category. This replicates a user who's added phrases before Custom Categories was a thing.
            execSQL("INSERT INTO Category (category_id, creation_date, is_user_generated, localized_name, hidden, sort_order) VALUES ('${PresetCategories.MY_SAYINGS.id}', ${System.currentTimeMillis()}, 0, '$categoryNameV4', 0, ${PresetCategories.MY_SAYINGS.initialSortOrder})")

            // Add custom phrase to the category you just created.
            execSQL("INSERT INTO Phrase (phrase_id, creation_date, is_user_generated, last_spoken_date, localized_utterance, sort_order) VALUES ('$phraseId', $creationDate, 1, $creationDate, '$testPhraseV4', 0)")
            execSQL("INSERT INTO CategoryPhraseCrossRef (category_id, phrase_id) VALUES ('${PresetCategories.MY_SAYINGS.id}', '$phraseId')")

            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 5, true, VocableDatabaseMigrations.MIGRATION_4_5)
            .apply {
                // Verify that new schema is as expected.
                val categoryId = PresetCategories.MY_SAYINGS.id
                val crossRefCursor =
                    query("SELECT * FROM CategoryPhraseCrossRef WHERE category_id = '$categoryId'")
                val phrasesAdded = arrayListOf<String>()
                val phraseIds =  mutableListOf<String>()
                var timestamp: Long? = null
                while (crossRefCursor.moveToNext()) {
                    val phraseId = crossRefCursor.getString(crossRefCursor.getColumnIndex("phrase_id"))
                    timestamp = crossRefCursor.getLong(crossRefCursor.getColumnIndex("timestamp"))
                    phraseIds.add(phraseId)
                }
                crossRefCursor.close()

                    val phraseCursor = query("SELECT localized_utterance FROM Phrase WHERE phrase_id = '$phraseId'")
                    while (phraseCursor.moveToNext()) {
                        val saying = phraseCursor.getString(phraseCursor.getColumnIndex("localized_utterance"))
                        phrasesAdded.add(saying)
                    }
                    phraseCursor.close()

                Assert.assertTrue(phrasesAdded.contains(testPhraseV4))
                Assert.assertEquals(0L, timestamp)
               close()
            }
    }

}