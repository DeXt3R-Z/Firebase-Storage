package com.example.firebasestorage

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestorage.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding

    private var storageReference = Firebase.storage.reference

    private var collection = Firebase.firestore.collection("counter")
    private lateinit var adpter: imageAdapter
    private var imageUrlList = mutableListOf<String>()
    private var randomNumber = 0L

    private val contract = registerForActivityResult(ActivityResultContracts.GetContent())
    {imageUri ->
        if(imageUri!=null)
        {
            bind.imageView.setImageURI(imageUri)
            uploadImage(imageUri)
//            if(imageUrlList.size != 0) {
//                adpter.notifyItemInserted(imageUrlList.size - 1)
//            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        CoroutineScope(Dispatchers.IO).launch {
            val size  = getRandomNumberFromSize()
            downloadImage("image${size}")
        }

//        Log.d("RandomNumber",size.toString())

        bind.btnAddImage.setOnClickListener {
            contract.launch("image/*")
        }

        listImage()
    }

    private suspend fun getRandomNumberFromSize(): Long{
        var sze: Long = 0
        val dataReturned = collection.document("counterTrack").get().await()
        sze = dataReturned.getLong("imageCounter")!!
        randomNumber = (0..sze).random()
        Log.d("checkFunction",randomNumber.toString())
        return randomNumber
    }

    //Code to upload image to the firestore
    private fun uploadImage(imageUri: Uri)
    {
        Firebase.firestore.runTransaction { transaction ->
            var docRef = collection.document("counterTrack")
            val snapshot = transaction.get(docRef)
            var newVal = snapshot.getLong("imageCounter") as Long + 1
            transaction.update(docRef,"imageCounter",newVal)
            newVal
        }.addOnSuccessListener {
            val imageName = "image$it"
            storageReference.child("images/${imageName}").putFile(imageUri)
        }
    }

    //code to download image from firestore
    private fun downloadImage(imageName: String)
    {
        val maxSize: Long = 5 * 1024 * 1024
        storageReference.child("images/${imageName}").getBytes(maxSize).addOnSuccessListener {
            val bmap = BitmapFactory.decodeByteArray(it,0,it.size)
            bind.imageView.setImageBitmap(bmap)
        }.addOnFailureListener{
            Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
        }
    }

    //code to show list the images
    private fun listImage() = CoroutineScope(Dispatchers.IO).launch {
        var imageUrlList = mutableListOf<String>()
        try {
            val imageList = storageReference.child("images/").listAll().await()
            for(images in imageList.items) //Looping through all the images object as returned by the firebase reference which contain
            // various data like the download URL
            {
                //Getting the download URL using the downloadUrl object on each image in the list of all images as returned by Firebase Storage
                val url = images.downloadUrl.await()
                //Log.d("Image Listing",url.toString())
                imageUrlList.add(url.toString())
            }
            withContext(Dispatchers.Main)
            {
                adpter = imageAdapter(imageUrlList)
                bind.recyclerView.apply {
                    adapter = adpter
                    layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }
        }catch (e: Exception)
        {
            withContext(Dispatchers.Main)
            {
                Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Code to delete images is used in the imageAdapter

}