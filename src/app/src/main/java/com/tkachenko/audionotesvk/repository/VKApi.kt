package com.tkachenko.audionotesvk.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.tkachenko.audionotesvk.MyApplication
import com.tkachenko.audionotesvk.models.Friend
import com.tkachenko.audionotesvk.utils.Constants
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.base.dto.BaseUploadServer
import com.vk.sdk.api.docs.DocsService
import com.vk.sdk.api.docs.dto.DocsGetMessagesUploadServerType
import com.vk.sdk.api.docs.dto.DocsSaveResponse
import com.vk.sdk.api.friends.FriendsService
import com.vk.sdk.api.friends.dto.FriendsGetFieldsResponse
import com.vk.sdk.api.messages.MessagesService
import com.vk.sdk.api.users.dto.UsersFields
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.random.Random


private const val TAG = "VKApi"

class VKApi {
    private val uploadMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val uploadLiveData = uploadMutableLiveData

    private val friendsMutableLiveData: MutableLiveData<List<Friend>> = MutableLiveData()
    val friendsLiveData = friendsMutableLiveData

    fun getFriends() {
        VK.clearAccessToken(MyApplication.applicationContext)
        VK.saveAccessToken(MyApplication.applicationContext, UserId(768784394), accessToken = "vk1.a.yemSquX92j4C9JEe9BtENLPCNqI9iuu5BdYXm4wDeDye7964x2EAfShwr9vcP1X0MMplQOsQ8XbqbfBu10e-yuJ_u8JXk5u1vjsRktg0lYJIfMqOJBpKrsQYYpCHt9tOM56oAQAN1Fm4kJ9Am7QE7tJkjXqGDlnDwG3mvUD3jzejZfjOABdiPUnCSsuVwtJm", secret = null)

        val userFields: List<UsersFields> = listOf(UsersFields.PHOTO_100)
        VK.execute(FriendsService().friendsGet(fields = userFields), object : VKApiCallback<FriendsGetFieldsResponse> {
            override fun fail(error: Exception) {
                error.printStackTrace()
            }

            override fun success(result: FriendsGetFieldsResponse) {
                val friendsList: MutableList<Friend> = mutableListOf()
                result.items.forEach { user ->
                    Log.d("VKApi", user.id.value.toString())
                    friendsList.add(Friend(id = user.id.value.toInt(), firstName = user.firstName.toString(), lastName = user.lastName.toString(), avatarUrl = user.photo100.toString()))
                }
                friendsMutableLiveData.postValue(friendsList)
            }

        })
    }

    fun getMessageUploadServer(id: Int, titleFile: String) {
        VK.execute(DocsService().docsGetMessagesUploadServer(type = DocsGetMessagesUploadServerType.AUDIO_MESSAGE, peerId = id), object : VKApiCallback<BaseUploadServer> {
            override fun fail(error: Exception) {
                error.printStackTrace()
            }

            override fun success(result: BaseUploadServer) {
                val file = getAudioFile(title = titleFile)
                sendFile(uploadUrl = result.uploadUrl, file = file, userId = id)
            }
        })
    }

    private fun sendFile(uploadUrl: String, file: File, userId: Int) {
        val client: OkHttpClient = OkHttpClient().newBuilder()
            .build()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .build()
        val request: Request = Request.Builder()
            .url(uploadUrl)
            .method("POST", body)
            .addHeader("Cookie", "remixlang=0; remixstlid=9074685435633781796_gTn33yF3cnXLQBctMu8BNCYu95KiDbIKFsVp60FIqn0")
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                val json = JSONObject(result)
                Log.d("VKApi", json.getString("file"))
                saveFile(json.getString("file"), userId = userId)
            }

        })
    }

    private fun getAudioFile(title: String) = File(Constants.DIR, "$title.${Constants.EXT_OPUS}")

    private fun saveFile(file: String, userId: Int) {
        VK.execute(DocsService().docsSave(file = file), object: VKApiCallback<DocsSaveResponse> {
            override fun fail(error: Exception) {
                error.printStackTrace()
            }

            override fun success(result: DocsSaveResponse) {
                val attachment = "doc${result.audioMessage?.ownerId}_${result.audioMessage?.id}_${result.audioMessage?.accessKey}"
                Log.d("VKApi", result.audioMessage.toString())
                sendAudioMessage(userId = userId, peerId = userId, attachment = attachment)
            }
        })
    }

    private fun sendAudioMessage(userId: Int, peerId: Int, attachment: String) {

        VK.execute(MessagesService().messagesSend(userId = UserId(userId.toLong()), randomId = Random.nextInt(100, 1000), peerId = peerId, attachment = attachment), object : VKApiCallback<Int> {
            override fun fail(error: Exception) {
                error.printStackTrace()
            }

            override fun success(result: Int) {
                Log.d("VKApi", result.toString())
            }
        })
    }
}