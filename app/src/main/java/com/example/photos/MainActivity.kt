package com.example.photos

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val requestCodeImageGet = 100
    private var imageCount = 0
    private val imagePermissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val photoList = mutableListOf<Photo>()
        val moveDoodleBtn= findViewById<ImageButton>(R.id.ibtn_add_photo)

        moveDoodleBtn.setOnClickListener {
            println(downloadJson())
        }
        findViewById<Button>(R.id.btn_gallery).setOnClickListener {
            if (checkPermissions()) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                activityResultLauncher.launch(intent)
            }
        }
        val adapter = PhotoAdapter(PhotoDiffCallback())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data
                uri?.let{
                    val photo = Photo(imageCount.toString(), uri)
                    photoList.add(photo)
                    imageCount++
                    adapter.submitList(photoList)
                }

            }
        }
        CoroutineScope(Job() + Dispatchers.IO).launch {
            val jsonString = downloadJson()
            println(jsonString)

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            requestCodeImageGet -> {
                if (grantResults.isNotEmpty()) {
                    for ((i) in permissions.withIndex()) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.i("PermissionsResult", "finish")
                            finish()
                        }
                    }
                }
            }
        }
    }

    fun checkPermissions(): Boolean {
        val denyPermissions = ArrayList<String>()

        for (i in imagePermissions) {
            if (ContextCompat.checkSelfPermission(this, i) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(i)
            }
        }

        if (denyPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, denyPermissions.toTypedArray(), requestCodeImageGet)
            return false
        }
        return true
    }

    private fun downloadJson(): String {
        val loadJson = URL("https://public.codesquad.kr/jk/doodle.json").openStream()
        val reader = BufferedReader(InputStreamReader(loadJson, "UTF-8"))
        val buffer = StringBuffer()
        var endOfFile = true
        while (endOfFile) {
            val json = reader.readLine()
            if (json != null) {
                endOfFile = false
            }
            buffer.append(json)
        }
        return """$buffer""".trimIndent()

    }
}