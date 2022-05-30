package com.willowtree.vocable.presets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.willowtree.vocable.BaseViewModel
import com.willowtree.vocable.room.Category
import com.willowtree.vocable.room.Phrase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class PresetsViewModel : BaseViewModel() {

    private val presetsRepository: PresetsRepository by inject()

    private val liveCategoryList = MutableLiveData<List<Category>>()
    val categoryList: LiveData<List<Category>> = liveCategoryList

    private val liveSelectedCategory = MutableLiveData<Category>()
    val selectedCategory: LiveData<Category> = liveSelectedCategory

    private val liveCurrentPhrases = MutableLiveData<List<Phrase?>>()
    val currentPhrases: LiveData<List<Phrase?>> = liveCurrentPhrases

    private val liveNavToAddPhrase = MutableLiveData<Boolean>()
    val navToAddPhrase: LiveData<Boolean> = liveNavToAddPhrase

    init {
        populateCategories()
    }

    fun populateCategories() {
        backgroundScope.launch {
            val categories = presetsRepository.getAllCategories().filter { !it.hidden }
            liveCategoryList.postValue(categories)
            val currentCategory = if (categories.contains(liveSelectedCategory.value) && liveSelectedCategory.value != null) {
                liveSelectedCategory.value ?: categories.first()
            } else {
                categories.first()
            }

            onCategorySelected(currentCategory)
        }
    }

    fun onCategorySelected(category: Category) {
        liveSelectedCategory.postValue(category)

        backgroundScope.launch {
            val catId = presetsRepository.getCategoryById(category.categoryId)

            // make sure the category wasn't deleted before getting its phrases
            if (catId != null) {

                // if the selected category was the Recents category, we need to invert the sort so
                // the most recently added phrases are at the top
                val phrases: MutableList<Phrase?> = if (catId.categoryId == PresetCategories.RECENTS.id) {
                    presetsRepository.getPhrasesForCategory(category.categoryId)
                        .sortedBy { it.lastSpokenDate }.reversed().toMutableList()
                } else {
                    presetsRepository.getPhrasesForCategory(category.categoryId)
                        .sortedBy { it.sortOrder }.toMutableList()
                }
                //Add null to end of normal non empty category phrase list for the "+ Add Phrase" button
                if (catId.categoryId != PresetCategories.RECENTS.id && catId.categoryId != PresetCategories.USER_KEYPAD.id && phrases.isNotEmpty()) {
                    phrases.add(null)
                }
                liveCurrentPhrases.postValue(phrases)
            } else { // if the category has been deleted, select the first available category to show
                val categories = presetsRepository.getAllCategories().filter { !it.hidden }
                onCategorySelected(categories[0])
            }
        }
    }

    fun addToRecents(phrase: Phrase) {
        backgroundScope.launch {
            presetsRepository.addPhraseToRecents(phrase)
        }
    }

    fun navToAddPhrase() {
        liveNavToAddPhrase.value = true
        liveNavToAddPhrase.value = false
    }
}
