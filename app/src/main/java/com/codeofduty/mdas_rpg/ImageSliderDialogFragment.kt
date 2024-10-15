package com.codeofduty.mdas_rpg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.codeofduty.mdas_rpg.databinding.DialogImageSliderBinding

class ImageSliderDialogFragment : DialogFragment() {

    private lateinit var binding: DialogImageSliderBinding
    private val imageList = listOf(
        R.drawable.guide_1,
        R.drawable.guide_2,
        R.drawable.guide_3,
        R.drawable.guide_4,
        R.drawable.guide_5,
        R.drawable.guide_6,
        R.drawable.guide_7
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogImageSliderBinding.inflate(inflater, container, false)

        val adapter = ImageSliderAdapter(requireContext(), imageList)
        binding.viewPager.adapter = adapter

        // Close dialog when 'X' button is clicked
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // Set the background of the dialog to be fully transparent
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
