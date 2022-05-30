package com.willowtree.vocable.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.willowtree.vocable.BaseViewModelFactory
import com.willowtree.vocable.R
import com.willowtree.vocable.room.Phrase
import java.util.*

class EditPhrasesKeyboardFragment : EditKeyboardFragment() {

    companion object {
        private const val KEY_PHRASE = "KEY_PHRASE"

        fun newInstance(phrase: Phrase?): EditPhrasesKeyboardFragment {
            return EditPhrasesKeyboardFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_PHRASE, phrase)
                }
            }
        }
    }

    private var phrase: Phrase? = null
    private var addNewPhrase = false
    private lateinit var viewModel: EditPhrasesViewModel
    private val args by navArgs<EditPhrasesKeyboardFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        phrase = args.phrase

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.action = {
            val textChanged =
                binding.keyboardInput.text.toString() != localizedResourceUtility.getTextFromPhrase(
                    phrase
                )
            if (!textChanged || isDefaultTextVisible() || addNewPhrase) {
                findNavController().popBackStack()
            } else {
                showConfirmationDialog()
            }
        }

        binding.saveButton.action = {
            if (!isDefaultTextVisible()) {
                binding.keyboardInput.text.let { text ->
                    if (text.isNotBlank()) {
                        val phraseUtterance =
                            phrase?.localizedUtterance?.toMutableMap()?.apply {
                                put(Locale.getDefault().toString(), text.toString())
                            }
                        phrase?.localizedUtterance = phraseUtterance ?: mapOf()
                        if (phrase == null) {
                            viewModel.addNewPhrase(text.toString())
                            addNewPhrase = true
                        } else {
                            phrase?.let { updatedPhrase ->
                                viewModel.updatePhrase(updatedPhrase)
                                addNewPhrase = false
                            }
                        }

                    }
                }
            }
        }

        val phraseText = localizedResourceUtility.getTextFromPhrase(phrase)
        val inputText = phraseText.ifEmpty { getString(R.string.keyboard_select_letters) }

        binding.keyboardInput.setText(inputText)

        viewModel = ViewModelProviders.of(
            requireActivity(),
            BaseViewModelFactory()
        ).get(EditPhrasesViewModel::class.java)

        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        viewModel.showPhraseAdded.observe(viewLifecycleOwner) {
            if (it) {
                if (phrase == null) {
                    Toast.makeText(context, R.string.new_phrase_saved, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.changes_saved, Toast.LENGTH_SHORT).show()
                }
                viewModel.phraseToFalse()
                findNavController().popBackStack()
            }
        }
    }
}