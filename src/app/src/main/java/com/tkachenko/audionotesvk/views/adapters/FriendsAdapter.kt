package com.tkachenko.audionotesvk.views.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.models.Friend

class FriendsAdapter: RecyclerView.Adapter<FriendsAdapter.FriendHolder>() {
    interface Callback {
        fun clickBtnSend(id: Int)
    }

    private val friends: MutableList<Friend> = mutableListOf()
    private var callback: Callback? = null

    fun attachCallback(callback: Callback) {
        this.callback = callback
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(friends: List<Friend>) {
        this.friends.addAll(friends)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent,false)
        return FriendHolder(view = view)
    }

    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        val audioNote = friends[position]
        holder.bind(audioNote)
    }

    override fun getItemCount(): Int = friends.size

    inner class FriendHolder(view: View): RecyclerView.ViewHolder(view), View.OnTouchListener {
        private val tvFirstName: TextView = view.findViewById(R.id.tv_firstName)
        private val tvLastName: TextView = view.findViewById(R.id.tv_lastName)
        private val sivAvatar: ShapeableImageView = view.findViewById(R.id.siv_avatar)
        private val btnSend: MaterialButton = view.findViewById(R.id.mBtn_send)

        fun bind(model: Friend) {
            tvFirstName.text = model.firstName
            tvLastName.text = model.lastName
            Picasso.get().load(model.avatarUrl).into(sivAvatar)

            btnSend.setOnClickListener {
                callback!!.clickBtnSend(model.id)
            }
            btnSend.setOnTouchListener(this)
        }

        @SuppressLint("ClickableViewAccessibility", "Range")
        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
            when (view?.id) {
                R.id.mBtn_send -> {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            (view as MaterialButton).alpha = 200.0F
                        }
                        MotionEvent.ACTION_UP -> {
                            (view as MaterialButton).alpha = 255.0F
                            view.invalidate()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            (view as MaterialButton).alpha = 255.0F
                            view.invalidate()
                        }
                    }
                }
            }
            return false
        }
    }
}