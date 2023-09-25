package com.hifi.redeal.account

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAddressSearchBinding

class AddressSearchFragment : Fragment() {

    lateinit var mainActivity: MainActivity
    lateinit var fragmentAddressSearchBinding: FragmentAddressSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        fragmentAddressSearchBinding = FragmentAddressSearchBinding.inflate(layoutInflater)

        fragmentAddressSearchBinding.run {
            materialToolbarAddressSearch.run {
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.ADDRESS_SEARCH_FRAGMENT)
                }
            }

            webViewAddressSearch.run {
                settings.javaScriptEnabled = true
                addJavascriptInterface(BridgeInterface(), "Android")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        loadUrl("javascript:sample2_execDaumPostcode();")
                    }
                }

                loadUrl("https://redealaddresssearch.web.app")
            }
        }

        return fragmentAddressSearchBinding.root
    }

    inner class BridgeInterface {
        @JavascriptInterface
        fun processDATA(data: String){
            setFragmentResult("addressSearchResult", bundleOf("address" to data))
            //findNavController().popBackStack()
            mainActivity.removeFragment(MainActivity.ADDRESS_SEARCH_FRAGMENT)
        }
    }
}