package com.penguinstudios.tradeguardian.ui.tradeinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.penguinstudios.tradeguardian.data.model.UserRole
import com.penguinstudios.tradeguardian.databinding.ConfirmDepositFragmentBinding
import com.penguinstudios.tradeguardian.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmDepositFragment(

) : DialogFragment() {

    private lateinit var binding: ConfirmDepositFragmentBinding
    private val viewModel: TradeInfoViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ConfirmDepositFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvItemPrice.text = viewModel.trade.getFormattedItemPrice()
        val userRole: UserRole = UserRole.getUserRoleById(viewModel.trade.userRoleId)
        if (userRole == UserRole.SELLER) {
            binding.tvDepositDescription.text =
                "The seller must deposit ${Constants.SELLER_DEPOSIT_MULTIPLIER}x the item price."
            binding.tvDepositAmount.text = viewModel.getFormattedSellerDepositAmount()
        } else {
            binding.tvDepositDescription.text =
                "The buyer must deposit ${Constants.BUYER_DEPOSIT_MULTIPLIER}x the item price."
            binding.tvDepositAmount.text = viewModel.getFormattedBuyerDepositAmount()
        }

        binding.btnConfirm.setOnClickListener {
            viewModel.deposit()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
}