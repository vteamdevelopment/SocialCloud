package com.vteam.testdemo.otp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.vteam.testdemo.R
import com.vteam.testdemo.common.Constants
import com.vteam.testdemo.otp.viewmodel.OTPViewModel
import com.vteam.testdemo.top.LandingActivity
import kotlinx.android.synthetic.main.on_boarding_fragment.sign_up
import kotlinx.android.synthetic.main.otp_fragment.*

class OtpFragment : Fragment() {

    companion object {
        fun newInstance() = OtpFragment()
    }

    private var mCountryCode: String?= null
    private var mMobileNumber: String?= null
    private lateinit var viewModel: OTPViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mMobileNumber = it.getString(Constants.KEY.MOBILE_NUMBER)
            mCountryCode = it.getString(Constants.KEY.COUNTRY_CODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.otp_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OTPViewModel::class.java)
        mMobileNumber?.let {
            activity?.let { act ->
                mCountryCode?.let {countryCode ->
                    viewModel.verifyPhoneNumber(act,countryCode+it) }
                }
        }
        viewModel.getUiModel().observe(viewLifecycleOwner, Observer {
            when(it){
                OTPViewModel.UiMode.CREDENTIAL_RECEIVED -> {
                    activity?.let { activity -> viewModel.signInWithPhoneAuthCredential(activity) }
                }
                OTPViewModel.UiMode.STATE_SIGNIN_SUCCESS -> {
                   val intent= Intent(context,LandingActivity::class.java)
                    startActivity(intent)
                }
            }
        })

        sign_up.setOnClickListener(View.OnClickListener {
            activity?.let { activity -> viewModel.verifyOtp(activity,otp.text.toString()) }
        })

    }

}