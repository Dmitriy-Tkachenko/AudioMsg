package com.tkachenko.audionotesvk.views.fragments

import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.repository.VKApi
import com.tkachenko.audionotesvk.views.adapters.FriendsAdapter

private const val TAG = "FriendsDialogFragment"

class FriendsDialogFragment: DialogFragment() {
    private val vkApi: VKApi by lazy { VKApi() }
    private lateinit var rvFriends: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter
    private var titleFile = ""

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) dismiss()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleFile = arguments?.getString("TITLE").toString()
        vkApi.getFriends()
        friendsAdapter = FriendsAdapter()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dialog_send_audio, container)
        rvFriends = view.findViewById(R.id.rv_friends)
        rvFriends.layoutManager = LinearLayoutManager(context)
        rvFriends.adapter = friendsAdapter
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vkApi.friendsLiveData.observe(this) {
            friendsAdapter.setData(it)
        }

        friendsAdapter.attachCallback(object : FriendsAdapter.Callback {
            override fun clickBtnSend(id: Int) {
                vkApi.getMessageUploadServer(id = id, titleFile = titleFile)
                Toast.makeText(requireActivity(), "Аудиосообщение отправлено", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }

    companion object {
        fun newInstance(title: String): FriendsDialogFragment {
            val fragment = FriendsDialogFragment()
            val bundle = Bundle()
            bundle.putString("TITLE", title)
            fragment.arguments = bundle
            return fragment
        }
    }
}