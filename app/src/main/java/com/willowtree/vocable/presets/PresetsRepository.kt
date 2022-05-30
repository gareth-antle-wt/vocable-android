package com.willowtree.vocable.presets

import android.content.Context
import com.willowtree.vocable.room.Category
import com.willowtree.vocable.room.Phrase
import com.willowtree.vocable.room.VocableDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.*

class PresetsRepository(val context: Context) : KoinComponent {

    private val database = VocableDatabase.getVocableDatabase(context)

    suspend fun getAllCategories(): List<Category> {
        return database.categoryDao().getAllCategories()
    }

    suspend fun getUserGeneratedCategories(): List<Category> {
        return database.categoryDao().getAllCategories()
    }

    suspend fun getPhrasesForCategory(categoryId: String): List<Phrase> {
        return database.categoryDao().getCategoryWithPhrases(categoryId)?.phrases ?: listOf()
    }

    suspend fun addPhrase(phrase: Phrase) {
        database.phraseDao().insertPhrase(phrase)
    }

    suspend fun addPhraseToRecents(phrase: Phrase) {

        val phrases = getPhrasesForCategory(
            PresetCategories.RECENTS.id
        )
        val recentPhrase = phrases.firstOrNull {
            it.localizedUtterance == phrase.localizedUtterance
        }
        if (recentPhrase == null) {
            addPhrase(
                Phrase(
                    phraseId = 0L,
                    parentCategoryId = PresetCategories.RECENTS.id,
                    creationDate = Calendar.getInstance().timeInMillis,
                    lastSpokenDate = Calendar.getInstance().timeInMillis,
                    localizedUtterance = phrase.localizedUtterance,
                    sortOrder = phrase.sortOrder
                )
            )
            if (phrases.size > 7) {
                phrases.minByOrNull {
                    it.lastSpokenDate
                }?.let {
                    deletePhrase(
                        it
                    )
                }
            }
        } else {
            updatePhrase(
                Phrase(
                    recentPhrase.phraseId,
                    PresetCategories.RECENTS.id,
                    phrase.creationDate,
                    Calendar.getInstance().timeInMillis,
                    phrase.localizedUtterance,
                    phrase.sortOrder
                )
            )
        }
    }

    suspend fun addCategory(category: Category) {
        database.categoryDao().insertCategory(category)
    }

    suspend fun populateCategories(categories: List<Category>) {
        database.categoryDao().insertCategories(*categories.toTypedArray())
    }

    suspend fun populatePhrases(phrases: List<Phrase>) {
        database.phraseDao().insertPhrases(*phrases.toTypedArray())
    }

    suspend fun deletePhrase(phrase: Phrase) {
        database.phraseDao().deletePhrase(phrase)
    }

    suspend fun deletePhrases(phrases: List<Phrase>) {
        database.phraseDao().deletePhrases(*phrases.toTypedArray())
    }

    suspend fun deleteCategory(category: Category) {
        database.categoryDao().deleteCategory(category)
    }

    suspend fun updatePhrase(phrase: Phrase) {
        database.phraseDao().updatePhrase(phrase)
    }

    suspend fun updateCategory(category: Category) {
        database.categoryDao().updateCategory(category)
    }

    suspend fun updateCategories(categories: List<Category>) {
        database.categoryDao().updateCategories(*categories.toTypedArray())
    }

    suspend fun getCategoryById(categoryId: String): Category {
        return database.categoryDao().getCategoryById(categoryId)
    }

    suspend fun getNumberOfShownCategories(): Int {
        return database.categoryDao().getNumberOfShownCategories()
    }

    //Initial DB populate
    suspend fun populateDatabase() {
        PresetCategories.values().forEach { presetCategory ->
            if (presetCategory != PresetCategories.RECENTS && presetCategory != PresetCategories.MY_SAYINGS) {
                val phrasesIds =
                    get<Context>().resources.obtainTypedArray(presetCategory.getArrayId())
                val phraseObjects = mutableListOf<Phrase>()
                for (index in 0 until phrasesIds.length()) {
                    phraseObjects.add(
                        Phrase(
                            0L,
                            presetCategory.id,
                            System.currentTimeMillis(),
                            System.currentTimeMillis(),
                            mapOf(Pair(Locale.getDefault().toString(), context.getString(phrasesIds.getResourceId(index, -1)))),
                            phraseObjects.size
                        )
                    )
                }
                phrasesIds.recycle()
                populatePhrases(phraseObjects)
            }
            database.categoryDao().insertCategories(
                Category(
                    presetCategory.id,
                    System.currentTimeMillis(),
                    presetCategory.getNameId(),
                    null,
                    false,
                    presetCategory.initialSortOrder
                )

            )
        }
    }
}
