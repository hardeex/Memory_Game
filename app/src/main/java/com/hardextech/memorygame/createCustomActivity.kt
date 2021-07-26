package com.hardextech.memorygame

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hardextech.memorygame.bitmap_scaler.BitmapScaler
import java.io.ByteArrayOutputStream

class createCustomActivity : AppCompatActivity() {
    companion object {
        private const val PICKCUSTOMIMAGE_REQUESTCODE = 123
        private const val READPHOTO_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val READPHOTO_PERMISSIONCODE = 321
        private const val TAG = "createCustomActivity"
        private const val MAX_GAMENAME_LENGTH = 15
        private const val MIN_GAMENAME_LENGTH = 5
    }

    private lateinit var rvCustomGame: RecyclerView
    private lateinit var edtCustomGameName: EditText
    private lateinit var btnSave: Button
    private lateinit var boardSize: BoardSize
    private lateinit var adapter: rvCustomGameAdapter

    private var numImagesRequired = -1
    private val choosenImageUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)

        // Initializing the variables
        rvCustomGame = findViewById<RecyclerView>(R.id.rvCustomGame)
        edtCustomGameName = findViewById<EditText>(R.id.edtCustomGameName)
        btnSave = findViewById<Button>(R.id.btnSave)




        btnSave.setOnClickListener {
            saveDataToFirebase()
        }
        edtCustomGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAMENAME_LENGTH))
        edtCustomGameName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                btnSave.isEnabled = saveButtonEnable()
            }

        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(CUSTOMGAME_BOARDSIZE) as BoardSize
        numImagesRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose Pictures (0/$numImagesRequired)"


        adapter = rvCustomGameAdapter(this, choosenImageUris, boardSize,
            object : rvCustomGameAdapter.CustomImageClickListener {
                override fun onPlaceHolderClick() {
                    if (userAllowPermission(this@createCustomActivity, READPHOTO_PERMISSION)) {
                        lauchIntentCustomGame()
                    } else {
                        requestPermission(
                            this@createCustomActivity,
                            READPHOTO_PERMISSION,
                            READPHOTO_PERMISSIONCODE
                        )
                    }

                }

            })
        rvCustomGame.adapter = adapter
        rvCustomGame.setHasFixedSize(true)
        rvCustomGame.layoutManager = GridLayoutManager(this, boardSize.getWidth())

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READPHOTO_PERMISSIONCODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lauchIntentCustomGame()
            } else {
                Toast.makeText(
                    this,
                    "Permission is required in order to create custom game",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /*
    Whenever we call startForResult, whatever is the result of the lauch activity comes back to the onActivityResult that needs to be override
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICKCUSTOMIMAGE_REQUESTCODE || resultCode != Activity.RESULT_OK || data == null) {
            Log.w(TAG, "Data couldn't be retrieved from the launch activity")
            return
        }
        val singleSelecteDdata = data.data // For selection of a single data
        val clipSelectedData = data.clipData // For selection of mulitple data
        if (clipSelectedData != null) {
            Log.i(
                TAG,
                "The number of clipData image ${clipSelectedData.itemCount}: $clipSelectedData"
            )
            for (clipDataCount in 0 until clipSelectedData.itemCount) {
                val clipItemCount = clipSelectedData.getItemAt(clipDataCount)
                if (choosenImageUris.size < numImagesRequired) {
                    choosenImageUris.add(clipItemCount.uri)
                }
            }
        } else {
            if (singleSelecteDdata != null) {
                Log.i(TAG, "The selected data: $singleSelecteDdata")
                choosenImageUris.add(singleSelecteDdata)
            }
            adapter.notifyDataSetChanged()
            supportActionBar?.title =
                "Choose Pictures (${choosenImageUris.size}/$numImagesRequired)"
            btnSave.isEnabled = saveButtonEnable()
        }
    }



    private fun saveButtonEnable(): Boolean {
        if (choosenImageUris.size != numImagesRequired) {
            return false
        }
        if (edtCustomGameName.text.isBlank() || edtCustomGameName.length() < MIN_GAMENAME_LENGTH) {
            return false
        }
        return true

    }

    private fun getImageByteArray(userByteImageUris: Uri): ByteArray {
        val originalUserImageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val createSource = ImageDecoder.createSource(contentResolver, userByteImageUris)
            ImageDecoder.decodeBitmap(createSource)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, userByteImageUris)
        }
        Log.i(
            TAG,
            "Original width ${originalUserImageBitmap.width}, Original height: ${originalUserImageBitmap.height}"
        )
        val scaleBitmap = BitmapScaler.scaleToFitHeight(originalUserImageBitmap, 250)
        Log.i(TAG, "Scaled width ${scaleBitmap.width}, Original height: ${scaleBitmap.height}")
        val byteOutputStream = ByteArrayOutputStream()
        scaleBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteOutputStream)
        return byteOutputStream.toByteArray()

    }

    private fun saveDataToFirebase() {
        Log.i(TAG, "Save files to Firebase")
        for ((index, userPhotoUris) in choosenImageUris.withIndex()) {
            val imageByteArray = getImageByteArray(userPhotoUris)
        }
    }

    private fun lauchIntentCustomGame() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(intent, "Choose Pictures"),
            PICKCUSTOMIMAGE_REQUESTCODE
        )

    }
}