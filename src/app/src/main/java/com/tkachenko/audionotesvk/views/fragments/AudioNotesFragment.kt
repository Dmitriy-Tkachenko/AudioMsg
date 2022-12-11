package com.tkachenko.audionotesvk.views.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.audioplayer.AudioNotePlayer
import com.tkachenko.audionotesvk.views.adapters.AudioNotesAdapter
import com.tkachenko.audionotesvk.views.viewmodels.AudioNotesViewModel
import java.net.URL


private const val TAG = "AudioNotesFragment"

class AudioNotesFragment: Fragment() {
    private lateinit var audioNotePlayer: AudioNotePlayer
    private lateinit var recyclerView: RecyclerView
    private lateinit var textWelcome: TextView
    private lateinit var mAdapter: AudioNotesAdapter
    private var holder: AudioNotesAdapter.AudioNoteHolder? = null

    private val audioNotesViewModel: AudioNotesViewModel by lazy { ViewModelProvider(this)[AudioNotesViewModel::class.java] }

    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioNotePlayer = AudioNotePlayer()
        mAdapter = AudioNotesAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_audio_notes, container, false)
        textWelcome = view.findViewById(R.id.text_welcome)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
        recyclerView.itemAnimator = null
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioNotesViewModel.fetchAudioNotes().observe(viewLifecycleOwner) { audioNotes ->
            if (audioNotes.isNotEmpty()) {
                mAdapter.setData(audioNotes)
                textWelcome.visibility = View.GONE
            }
        }

        mAdapter.attachCallback(object: AudioNotesAdapter.Callback {
            @SuppressLint("FragmentLiveDataObserve")
            override fun onClickBtnPlay(title: String, position: Int) {
                audioNotePlayer.playStart(title = title)
                holder = recyclerView.findViewHolderForAdapterPosition(position) as AudioNotesAdapter.AudioNoteHolder
                mAdapter.startPlay(position)

                audioNotePlayer.maxProgressLiveData.observe(viewLifecycleOwner) {
                    if (it != null && !it.equals("null")) {
                        holder?.setProgressIndicatorMax(it.toInt())
                    }
                }
                audioNotePlayer.currentPositionLiveData.observe(viewLifecycleOwner) {
                    if (it != null && !it.equals("null")) {
                        holder?.updateProgressIndicator(it.toInt() + 1)
                    }
                }
                audioNotePlayer.playFinishedLiveData.observe(viewLifecycleOwner) {
                    if (it == true) mAdapter.stopPlay(position)
                }
            }

            override fun onClickBtnStop(title: String, position: Int) {
                isPlaying = false
                audioNotePlayer.playStop()
                mAdapter.stopPlay(position)
            }

            override fun onClickBtnUpload(title: String, position: Int) {
                if (isPlaying) {
                    isPlaying = false
                    mAdapter.stopPlay(position)
                }

                val friendsDialogFragment = FriendsDialogFragment.newInstance(title = title)
                friendsDialogFragment.show(requireActivity().supportFragmentManager, TAG)
            }
        })

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                val title = mAdapter.removeData(position)
                audioNotePlayer.deleteAudioFile(title)
                Toast.makeText(requireActivity(), "Аудиозапись \"$title\" удалена", Toast.LENGTH_LONG).show()
                if (mAdapter.itemCount == 0) {
                    textWelcome.visibility = View.VISIBLE
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    companion object {
        fun newInstance() = AudioNotesFragment()
    }
}