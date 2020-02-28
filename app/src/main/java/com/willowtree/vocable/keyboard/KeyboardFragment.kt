package com.willowtree.vocable.keyboard

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.willowtree.vocable.BaseFragment
import com.willowtree.vocable.R
import com.willowtree.vocable.customviews.ActionButton
import com.willowtree.vocable.customviews.PointerListener
import com.willowtree.vocable.databinding.FragmentKeyboardBinding
import com.willowtree.vocable.databinding.KeyboardKeyLayoutBinding
import com.willowtree.vocable.presets.PresetsFragment
import com.willowtree.vocable.settings.SettingsActivity
import com.willowtree.vocable.utils.VocableTextToSpeech
import java.util.*


class KeyboardFragment : BaseFragment() {

    private lateinit var viewModel: KeyboardViewModel
    private var binding: FragmentKeyboardBinding? = null

    companion object {
        private val KEYS = listOf(
            "Q",
            "W",
            "E",
            "R",
            "T",
            "Y",
            "U",
            "I",
            "O",
            "P",
            "A",
            "S",
            "D",
            "F",
            "G",
            "H",
            "J",
            "K",
            "L",
            "'",
            "Z",
            "X",
            "C",
            "V",
            "B",
            "N",
            "M",
            ",",
            ".",
            "?"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentKeyboardBinding.inflate(inflater, container, false)

        populateKeys()

        return binding?.root
    }

    private fun populateKeys() {
        KEYS.withIndex().forEach {
            with(
                KeyboardKeyLayoutBinding.inflate(
                    layoutInflater,
                    binding?.keyboardKeyHolder,
                    true
                ).root as ActionButton
            ) {
                text = it.value
                action = {
                    val currentText = binding?.keyboardInput?.text?.toString() ?: ""
                    if (isDefaultTextVisible()) {
                        binding?.keyboardInput?.text = null
                        binding?.keyboardInput?.append(text?.toString())
                    } else if (currentText.endsWith(". ") || currentText.endsWith("? ")) {
                        binding?.keyboardInput?.append(text?.toString())
                    } else {
                        binding?.keyboardInput?.append(text?.toString()?.toLowerCase(Locale.getDefault()))
                    }
                }
            }
        }
    }

    private fun isDefaultTextVisible(): Boolean {
        if (binding?.keyboardInput?.text.toString() == getString(R.string.keyboard_select_letters)) {
            return true
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        VocableTextToSpeech.isSpeaking.observe(viewLifecycleOwner, Observer {
            binding?.speakerIcon?.isVisible = it ?: false
        })

        binding?.actionButtonContainer?.presetsButton?.let {
            it.setIconWithNoText(R.drawable.ic_presets)
            it.action = {
                fragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.fragment_container, PresetsFragment())
                    ?.commit()
            }
        }

        binding?.actionButtonContainer?.settingsButton?.let {
            it.setIconWithNoText(R.drawable.ic_settings_light_48dp)
            it.action = {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        binding?.actionButtonContainer?.saveButton?.let {
            it.setIconWithNoText(R.drawable.ic_heart_border_blue)
            it.action = {
                if (!isDefaultTextVisible()) {
                    binding?.keyboardInput?.text?.let { text ->
                        if (text.isNotBlank()) {
                            viewModel.addNewPhrase(text.toString())
                        }
                    }
                }
            }
        }

        binding?.keyboardClearButton?.let {
            it.setIconWithNoText(R.drawable.ic_delete)
            it.action = {
                binding?.keyboardInput?.setText(R.string.keyboard_select_letters)
            }
        }

        binding?.keyboardSpaceButton?.let {
            it.setIconWithNoText(R.drawable.ic_space_bar_56dp)
            it.action = {
                if (!isDefaultTextVisible() && binding?.keyboardInput?.text?.endsWith(' ') == false) {
                    binding?.keyboardInput?.append(" ")
                }
            }
        }

        binding?.keyboardBackspaceButton?.let {
            it.setIconWithNoText(R.drawable.ic_backspace)
            it.action = {
                if (!isDefaultTextVisible()) {
                    binding?.keyboardInput?.let { keyboardInput ->
                        keyboardInput.setText(keyboardInput.text.toString().dropLast(1))
                        keyboardInput.setSelection(
                            keyboardInput.text?.length ?: 0
                        )
                        if (keyboardInput.text.isNullOrEmpty()) {
                            keyboardInput.setText(R.string.keyboard_select_letters)
                        }
                    }
                }
            }
        }

        binding?.keyboardSpeakButton?.let {
            it.setIconWithNoText(R.drawable.ic_speak_40dp)
            it.action = {
                if (!isDefaultTextVisible()) {
                    VocableTextToSpeech.getTextToSpeech()?.speak(
                        binding?.keyboardInput?.text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        id.toString()
                    )
                }
            }
        }

        binding?.phraseSavedView?.root?.let {
            buildTextWithIcon(
                getString(R.string.saved_successfully),
                getString(R.string.category_saved_to),
                iconCharStart = 9,
                iconCharEnd = 10,
                view = it as TextView,
                icon = R.drawable.ic_heart_solid_blue
            )
        }

        viewModel = ViewModelProviders.of(this).get(KeyboardViewModel::class.java)
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        viewModel.showPhraseAdded.observe(viewLifecycleOwner, Observer {
            binding?.phraseSavedView?.root?.isVisible = it ?: false
        })
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private val allViews = mutableListOf<View>()

    override fun getAllViews(): List<View> {
        if (allViews.isEmpty()) {
            getAllChildViews(binding?.keyboardParent)
        }
        return allViews
    }

    private fun getAllChildViews(viewGroup: ViewGroup?) {
        viewGroup?.children?.forEach {
            if (it is PointerListener) {
                allViews.add(it)
            } else if (it is ViewGroup) {
                getAllChildViews(it)
            }
        }
    }
}