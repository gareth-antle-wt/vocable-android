package com.willowtree.vocable.settings

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.willowtree.vocable.BaseViewModelFactory
import com.willowtree.vocable.BindingInflater
import com.willowtree.vocable.R
import com.willowtree.vocable.databinding.FragmentEditKeyboardBinding
import com.willowtree.vocable.room.Category

class EditCategoriesKeyboardFragment : EditKeyboardFragment() {

    override val bindingInflater: BindingInflater<FragmentEditKeyboardBinding> =
        FragmentEditKeyboardBinding::inflate

    private lateinit var viewModel: AddUpdateCategoryViewModel

    private val args: EditCategoriesKeyboardFragmentArgs by navArgs()

    private var currentCategory: Category? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentCategory = args.category

        binding.backButton.action = {
            val categoryTextChanged =
                binding.keyboardInput.text.toString() != localizedResourceUtility.getTextFromCategory(
                    currentCategory
                )
            if (!categoryTextChanged || isDefaultTextVisible()) {
                findNavController().popBackStack()
            } else {
                showConfirmationDialog()
            }
        }


        binding.saveButton.action = {
            if (!isDefaultTextVisible()) {
                if (currentCategory == null) {
                    // Add new category
                    binding.keyboardInput.text?.let { text ->
                        if (text.isNotBlank()) {
                            viewModel.addCategory(text.toString())
                        }
                    }
                } else {
                    // Update category
                    currentCategory?.let {
                        binding.keyboardInput.text.let { text ->
                            viewModel.updateCategory(it.categoryId, text.toString())
                        }
                    }
                }
            }
        }

        val categoryText = localizedResourceUtility.getTextFromCategory(currentCategory)
        val inputText = categoryText.ifEmpty { getString(R.string.keyboard_select_letters) }

        binding.keyboardInput.setText(inputText)

        viewModel = ViewModelProviders.of(
            this,
            BaseViewModelFactory()
        ).get(AddUpdateCategoryViewModel::class.java)

        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        viewModel.showCategoryUpdateMessage.observe(viewLifecycleOwner, Observer {
            with(binding.phraseSavedView.root) {
                isVisible = it == true
                if (currentCategory != null) {
                    setText(R.string.changes_saved)
                } else {
                    setText(R.string.category_created)
                }
            }
        })

        viewModel.currentCategory.observe(viewLifecycleOwner, Observer {
            it?.let {
                currentCategory = it
            }
        })
    }
}