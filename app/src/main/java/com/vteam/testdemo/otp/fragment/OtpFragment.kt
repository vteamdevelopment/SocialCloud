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
import com.vteam.testdemo.landing.LandingActivity
import com.vteam.testdemo.profile.CreateProfileActivity
import kotlinx.android.synthetic.main.on_boarding_fragment.sign_up
import kotlinx.android.synthetic.main.otp_fragment.*

class OtpFragment : Fragment() {

    companion object {
        fun newInstance() = OtpFragment()
    }

    private var countryCode: String? = null
    private var mobileNumber: String? = null
    private lateinit var viewModel: OTPViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mobileNumber = it.getString(Constants.KEY.MOBILE_NUMBER)
            countryCode = it.getString(Constants.KEY.COUNTRY_CODE)
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
        mobileNumber?.let {
            activity?.let { act ->
                countryCode?.let { countryCode ->
                    viewModel.verifyPhoneNumber(act, countryCode + it)
                }
            }
        }
        viewModel.getUiModel().observe(viewLifecycleOwner, Observer {
            when (it) {
                OTPViewModel.UiMode.CREDENTIAL_RECEIVED -> {
                    activity?.let { activity -> viewModel.signInWithPhoneAuthCredential(activity) }
                }
                OTPViewModel.UiMode.STATE_SIGNIN_SUCCESS -> {


                    viewModel.verifyUserExistOrNot()

                }
                OTPViewModel.UiMode.STATE_LANDING_PAGE -> {
                    if(viewModel.existingUser) {
                        val intent = Intent(context, LandingActivity::class.java)
                        intent.putExtra("UserExist", viewModel.existingUser)
                        startActivity(intent)
                        activity?.finish()
                    }else{
                        val intent = Intent(context, CreateProfileActivity::class.java)
                        intent.putExtra("UserExist", viewModel.existingUser)
                        startActivity(intent)
                        activity?.finish()
                    }
                }
            }
        })

        sign_up.setOnClickListener(View.OnClickListener {
            activity?.let { activity -> viewModel.verifyOtp(activity, otp.text.toString()) }
        })

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}