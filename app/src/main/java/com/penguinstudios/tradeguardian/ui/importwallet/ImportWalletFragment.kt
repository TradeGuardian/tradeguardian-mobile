package com.penguinstudios.tradeguardian.ui.importwallet

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.databinding.ImportFragmentBinding
import com.penguinstudios.tradeguardian.ui.MainActivity
import com.penguinstudios.tradeguardian.ui.createwallet.password.PasswordStrength
import com.penguinstudios.tradeguardian.ui.createwallet.viewmodel.CreateWalletUIState
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ImportWalletFragment : DialogFragment() {

    private lateinit var binding: ImportFragmentBinding
    private lateinit var viewModel: ImportWalletViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.Theme_TradeGuardian)
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.attributes?.windowAnimations = R.style.FragmentSlideUpAnim
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ImportFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            dismiss()
        }

        binding.btnImport.setOnClickListener {
            viewModel.onImportBtnClick(
                binding.etSecretPhrase.text.toString(),
                binding.etNewPassword.text.toString(),
                binding.etConfirmPassword.text.toString()
            )
        }

        binding.etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    binding.tvPasswordStrength.visibility = View.INVISIBLE
                } else {
                    viewModel.onNewPasswordTextChange(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        viewModel = ViewModelProvider(this)[ImportWalletViewModel::class.java]
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is ImportWalletUIState.SuccessImportWallet -> {
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }

                    is ImportWalletUIState.UpdatePasswordStrength -> {
                        binding.tvPasswordStrength.visibility = View.VISIBLE
                        updatePasswordStrength(uiState.strength)
                    }

                    is ImportWalletUIState.Error -> {
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun updatePasswordStrength(strength: PasswordStrength) {
        val baseText = "Password strength: "
        val coloredText = strength.strengthName
        val spannableString = SpannableString(baseText + coloredText)

        val color = Color.parseColor(strength.hexColor)
        spannableString.setSpan(
            ForegroundColorSpan(color),
            baseText.length,
            (baseText + coloredText).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvPasswordStrength.text = spannableString
    }
}