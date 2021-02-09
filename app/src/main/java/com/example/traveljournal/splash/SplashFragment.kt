package com.example.traveljournal.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.traveljournal.AppPreferences
import com.example.traveljournal.R
import kotlinx.android.synthetic.main.fragment_splash.*


class SplashFragment : Fragment() {
    lateinit var retripImage: ImageView
    lateinit var retrieve: TextView
    lateinit var remember: TextView
    lateinit var relive: TextView
    lateinit var retrip: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retripImage = retrip_image
        retrieve = retrieve_text
        remember = remember_text
        relive = relieve_text
        retrip = retrip_text

        faderRetripImage()
        faderRetrieve()
        faderRemember()
        faderRelive()
        faderRetrip()

        Handler().postDelayed({
            context?.let {
                findNavController().navigate(SplashFragmentDirections.actionSplashDestinationToJourneysDestination())
            }
        }, 3800)

        AppPreferences.firstTimeOpeningApp = false
    }

    private fun faderRetripImage() {
        val animator = ObjectAnimator.ofFloat(retripImage, View.ALPHA, 0f, 1f)
        animator.repeatCount = 0
        animator.duration = 1500
        animator.start()
    }

    private fun faderRetrieve() {
        val animator = ObjectAnimator.ofFloat(retrieve, View.ALPHA, 0f, 1f)
        animator.repeatCount = 0
        animator.duration = 1500
        animator.addListener(onStart = {
            retrieve.visibility = View.VISIBLE
        })
        animator.startDelay = 1200
        animator.start()
    }

    private fun faderRemember() {
        val animator = ObjectAnimator.ofFloat(remember, View.ALPHA, 0f, 1f)
        animator.repeatCount = 0
        animator.duration = 1500
        animator.addListener(onStart = {
            remember.visibility = View.VISIBLE
        })
        animator.startDelay = 1200
        animator.start()
    }

    private fun faderRelive() {
        val animator = ObjectAnimator.ofFloat(relive, View.ALPHA, 0f, 1f)
        animator.repeatCount = 0
        animator.duration = 1500
        animator.addListener(onStart = {
            relive.visibility = View.VISIBLE
        })
        animator.startDelay = 1200
        animator.start()
    }

    private fun faderRetrip() {
        val animator = ObjectAnimator.ofFloat(retrip, View.ALPHA, 0f, 1f)
        animator.repeatCount = 0
        animator.duration = 1500
        animator.addListener(onStart = {
            retrip.visibility = View.VISIBLE
        })
        animator.startDelay = 2000
        animator.start()
    }
}