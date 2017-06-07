package io.geeny.sample.ui.main.clients.custom.slot

import android.os.Bundle
import io.geeny.sample.R
import io.geeny.sample.ui.common.presenter.BaseFragment
import io.geeny.sdk.clients.common.Client
import io.geeny.sdk.clients.custom.slots.Slot

class CustomResourceFragment : BaseFragment(), CustomResourceView {

    private lateinit var presenter: CustomResourcePresenter

    override fun layout(): Int = R.layout.view_custom_client_resource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = CustomResourcePresenter(arguments.getString(ARG_CLIENT_ID), arguments.getString(ARG_RESOURCE_ID), sdk(), component().ioScheduler, component().mainScheduler)
    }

    override fun onStart() {
        super.onStart()

        presenter.attach(this)
        presenter.load()
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }


    override fun onClientAndResourceLoaded(client: Client, slot: Slot) {
        setTitle(slot.name())
        (view as CustomClientResourceView).setValues(client, slot)
    }

    companion object {
        val TAG = CustomResourceFragment::class.java.simpleName
        val ARG_RESOURCE_ID = "ARG_RESOURCE_ID"
        val ARG_CLIENT_ID = "ARG_CLIENT_ID"
        fun newInstance(clientId: String, resourceId: String): CustomResourceFragment {
            val arg = Bundle()
            arg.putString(ARG_CLIENT_ID, clientId)
            arg.putString(ARG_RESOURCE_ID, resourceId)
            return CustomResourceFragment().apply { arguments = arg }
        }
    }
}