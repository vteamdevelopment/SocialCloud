package com.vteam.testdemo.otp.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vteam.testdemo.common.ConstantNodes
import com.vteam.testdemo.otp.viewmodel.OTPViewModel.Constant.TAG
import com.vteam.testdemo.otp.viewmodel.OTPViewModel.UiMode.CREDENTIAL_RECEIVED
import com.vteam.testdemo.otp.viewmodel.OTPViewModel.UiMode.STATE_LANDING_PAGE
import com.vteam.testdemo.otp.viewmodel.OTPViewModel.UiMode.STATE_SIGNIN_FAILED
import com.vteam.testdemo.otp.viewmodel.OTPViewModel.UiMode.STATE_SIGNIN_SUCCESS

class OTPViewModel : ViewModel() {

    object Constant {
        const val TAG = "OTPViewModel"
    }

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var credential: PhoneAuthCredential? = null

    private var auth: FirebaseAuth

    var existingUser : Boolean = false

    private var uiModel = MutableLiveData<Int>()

    fun getUiModel(): MutableLiveData<Int> {
        return uiModel
    }


    init {
        auth = FirebaseAuth.getInstance()
    }

    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                this@OTPViewModel.credential = credential
                uiModel.value = CREDENTIAL_RECEIVED
//            signInWithPhoneAuthCredential(credential)
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                Log.d(TAG, "onCodeAutoRetrievalTimeOut:$p0")
                storedVerificationId = p0

            }

            override fun onVerificationFailed(p0: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", p0)

                if (p0 is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (p0 is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }


            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the c  ode and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent: verificationId : $verificationId")
                Log.d(TAG, "onCodeSent: token : $token")


                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

            }
        }

    fun verifyPhoneNumber(activity: Activity, phoneNumber: String) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            java.util.concurrent.TimeUnit.SECONDS, // Unit of timeout
            activity, // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks

    }

    fun verifyUserExistOrNot(){
        var rootRef = FirebaseDatabase.getInstance().reference
        var currentUserID = auth.getCurrentUser()?.getUid()
        currentUserID?.let {
            rootRef.child(ConstantNodes.NODES.USER_NODE).child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.child("name").exists()) {
                            existingUser = true
                        } else {
                            existingUser = false
                        }
                        uiModel.value = STATE_LANDING_PAGE

                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
    }
    fun verifyOtp(activity: Activity, code: String) {
        Log.d(TAG, "code:$code  ")

        credential = storedVerificationId?.let { PhoneAuthProvider.getCredential(it, code) }
        signInWithPhoneAuthCredential(activity)
    }


    fun signInWithPhoneAuthCredential(activity: Activity) {

        credential?.let {
            auth.signInWithCredential(it)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        val user = task.result?.user

                        uiModel.value = STATE_SIGNIN_SUCCESS
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            uiModel.value = STATE_SIGNIN_FAILED
                        }
                        // [START_EXCLUDE silent]
                        // Update UI
                        //                    updateUI(STATE_SIGNIN_FAILED)
                        // [END_EXCLUDE]
                    }
                }
        }

    }


    object UiMode {
        const val CREDENTIAL_RECEIVED = 1
        const val STATE_SIGNIN_SUCCESS = 2
        const val STATE_SIGNIN_FAILED = 3
        const val STATE_LANDING_PAGE = 4
        const val STATE_PROFILE_PAGE = 5

    }

}