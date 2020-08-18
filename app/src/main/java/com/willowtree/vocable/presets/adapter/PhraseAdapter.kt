package com.willowtree.vocable.presets.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.willowtree.vocable.R
import com.willowtree.vocable.databinding.PhraseButtonBinding
import com.willowtree.vocable.room.Phrase
import com.willowtree.vocable.utils.LocalizedResourceUtility
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class PhraseAdapter(private val phrases: List<Phrase>, private val numRows: Int) :
    RecyclerView.Adapter<PhraseAdapter.PhraseItemViewHolder>(), KoinComponent {

    class PhraseItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = PhraseButtonBinding.bind(itemView)

        fun bind(text: String) {
            binding.root.setText(text, Locale.getDefault())
        }
    }

    private val localizedResourceUtility: LocalizedResourceUtility by inject()

    private var _minHeight: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhraseItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.phrase_button, parent, false)
        parent.post {
            itemView.minimumHeight = getMinHeight(parent)
        }
        return PhraseItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PhraseItemViewHolder, position: Int) {
        val text = localizedResourceUtility.getTextFromPhrase(phrases[position])
        holder.bind(text)
    }

    private fun getMinHeight(parent: ViewGroup): Int {
        if (_minHeight == null) {
            val offset =
                parent.context.resources.getDimensionPixelSize(R.dimen.speech_button_margin)
            _minHeight = (parent.measuredHeight / numRows) - ((numRows - 1) * offset / numRows)
        }
        return _minHeight ?: 0
    }

    override fun getItemCount(): Int = phrases.size
}