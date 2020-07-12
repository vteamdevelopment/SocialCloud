package com.vteam.testdemo.splash.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vteam.testdemo.splash.viewmodel.OnBoardingViewModel.UiMode.VERIFY_NUMBER

class OnBoardingViewModel(application: Application) : AndroidViewModel(application) {

    private var uiModel= MutableLiveData<Int>()

    fun getUiModel(): MutableLiveData<Int> {
        return uiModel
    }

    object Constant{
        const val TAG = "OnBoardingViewModel"
    }



    fun verifyPhoneNumber(){
        uiModel.value= VERIFY_NUMBER

    }

    object UiMode{
        const val VERIFY_NUMBER =1
    }

}