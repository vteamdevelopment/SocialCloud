package com.vteam.testdemo.splash.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.vteam.testdemo.R
import com.vteam.testdemo.SplashActivity
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.otp.fragment.OtpFragment
import com.vteam.testdemo.splash.viewmodel.OnBoardingViewModel
import kotlinx.android.synthetic.main.on_boarding_fragment.*

class OnBoardingFragment : Fragment() {

    companion object {
        fun newInstance() = OnBoardingFragment()
    }

    private lateinit var viewModel: OnBoardingViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.on_boarding_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OnBoardingViewModel::class.java)

        viewModel.getUiModel().observe(viewLifecycleOwner, Observer {
            when(it){
                OnBoardingViewModel.UiMode.VERIFY_NUMBER -> {
                    addOtpScreen()
                }
                else -> {

                }
            }
        })

        sign_up.setOnClickListener(View.OnClickListener {
            viewModel.verifyPhoneNumber()
        })

    }



    private fun addOtpScreen() {
        val mobileNumber = mobile_number.text.toString()
        val countryCode = country_code.text.toString()
        if (mobileNumber.length==10){

            if(activity is SplashActivity){
                (activity as SplashActivity).addFragment(OtpFragment.newInstance(),
                    SplashActivity.TransactionType.REPLACE,OtpFragment.javaClass.simpleName,R.id.container,
                    bundle = bundleOf(Constants.KEY.MOBILE_NUMBER to mobileNumber,Constants.KEY.COUNTRY_CODE to countryCode)
                )
            }
        }
    }


}