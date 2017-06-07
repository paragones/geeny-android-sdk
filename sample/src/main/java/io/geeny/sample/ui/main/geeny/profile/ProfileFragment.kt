package io.geeny.sample.ui.main.geeny.profile

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment

class ProfileFragment : BaseFragment(), ProfileView {

    var presenter: ProfilePresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ProfilePresenter()
    }

    override fun layout(): Int = R.layout.fragment_profile

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
        setTitle("Profile")
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }

    companion object {
        val TAG = ProfileFragment::class.java.simpleName
        fun newInstance(): Fragment? = ProfileFragment()

    }
}