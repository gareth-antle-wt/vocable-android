package com.willowtree.vocable.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.willowtree.vocable.BaseViewModel
import com.willowtree.vocable.presets.PresetCategories
import com.willowtree.vocable.presets.PresetsRepository
import com.willowtree.vocable.room.Category
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class EditCategoryMenuViewModel : BaseViewModel() {

    private val presetsRepository: PresetsRepository by inject()

    private val _currentCategory = MutableLiveData<Category>()
    val currentCategory: LiveData<Category> = _currentCategory

    private val _lastCategoryRemaining = MutableLiveData<Boolean>()
    val lastCategoryRemaining: LiveData<Boolean> = _lastCategoryRemaining

    fun updateCategoryById(categoryId: String) {
        backgroundScope.launch {
            _lastCategoryRemaining.postValue(presetsRepository.getAllCategories().size == 1)
            _currentCategory.postValue(presetsRepository.getCategoryById(categoryId))
        }
    }

    fun updateHiddenStatus(showCategoryStatus: Boolean) {
        backgroundScope.launch {
            _currentCategory.value?.hidden = !showCategoryStatus
            _currentCategory.value?.let { presetsRepository.updateCategory(it) }
        }
    }

    fun deleteCategory() {
        backgroundScope.launch {
            val category = _currentCategory.value

            // Delete any phrases whose only associated category is the one being deleted
            // First get the ids of all phrases associated with the category being deleted
            val phrasesForCategory = category?.let { categoryPhrase ->
                presetsRepository.getPhrasesForCategory(categoryPhrase.categoryId)
                    .sortedBy { it.sortOrder }
            }


            //Delete from Recents by utterance
            if (phrasesForCategory != null) {
                presetsRepository.deletePhrases(
                    presetsRepository.getPhrasesForCategory(PresetCategories.RECENTS.id)
                        .filter {
                            phrasesForCategory.map { phrase ->
                                phrase.localizedUtterance
                            }.contains(it.localizedUtterance)
                        }
                )
            }

            //Delete phrases
            presetsRepository.deletePhrases(
                phrasesForCategory ?: listOf()
            )

            // Delete the category
            if (category != null) {
                presetsRepository.deleteCategory(category)
            }

            // Update the sort order of remaining categories
            val overallCategories = presetsRepository.getAllCategories()
            val categoriesToUpdate =
                overallCategories.filter { it.sortOrder > category?.sortOrder ?: 0 }
            categoriesToUpdate.forEach {
                it.sortOrder--
            }
            if (categoriesToUpdate.isNotEmpty()) {
                presetsRepository.updateCategories(categoriesToUpdate)
            }
        }
    }
}
