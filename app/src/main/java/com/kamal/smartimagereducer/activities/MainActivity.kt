package com.kamal.smartimagereducer.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.kamal.smartimagereducer.databinding.ActivityMainBinding
import com.kamal.smartimagereducer.utils.CompressImage
import com.kamal.smartimagereducer.utils.DismissSetting
import com.kamal.smartimagereducer.utils.FileUtil.from
import com.kamal.smartimagereducer.utils.Utility
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.Random

class MainActivity : AppCompatActivity(),DismissSetting {
    private lateinit var binding: ActivityMainBinding
    var insertImage: File? = null
    var compressImage: File? = null
    private var interstitialAd: InterstitialAd? = null
    // Permissions
    private var permissionsCount: Int = 0
    private var permissionsList: ArrayList<String> = ArrayList()
    private var permissionsStr: Array<String> = Utility.permissionArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#36D080")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AdMob
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        loadInterstitialAd()
        //  imageInsert.setBackgroundColor(getRandomColor())
        setClickButton()
        setClearImage()
    }

    @SuppressLint("CheckResult")
    private fun setClickButton() {
        //insert gambar
        binding.btnInsert.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        //kompres gambar
        binding.btnCompress.setOnClickListener {
            if (insertImage == null) {
                Toast.makeText(
                    this@MainActivity, "Please choose an image!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                CompressImage(this@MainActivity)
                    .setDestinationDirectoryPath(
                        File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DCIM
                            ), ""
                        ).absolutePath
                    )
                    .compressToFileAsFlowable(insertImage!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ file ->
                        compressImage = file
                        val bitmapImage = BitmapFactory.decodeFile(compressImage!!.absolutePath)
                        binding.imageCompress.setImageBitmap(bitmapImage)

                        binding.tvSizeAfter.text = String.format(
                            "Size : %s",
                            getReadableFileSize(compressImage!!.length())
                        )

                        Toast.makeText(
                            this@MainActivity,
                            "Compressed image save in " + compressImage!!.path, Toast.LENGTH_LONG
                        ).show()
                    }) { throwable ->
                        throwable.printStackTrace()
                        Toast.makeText(this@MainActivity, throwable.message, Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        //custom
        binding.btnShare.setOnClickListener {
            if (insertImage == null) {
                Toast.makeText(this, "Please choose an image!", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    compressImage = CompressImage(this)
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(
                            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "").absolutePath
                        )
                        .compressToFile(insertImage!!)

                    val bitmapImage = BitmapFactory.decodeFile(compressImage!!.absolutePath)
                    binding.imageCompress.setImageBitmap(bitmapImage)
                    binding.tvSizeAfter.text = String.format("Size : %s", getReadableFileSize(compressImage!!.length()))

                   // Toast.makeText(this, "Compressed image saved in ${compressImage!!.path}", Toast.LENGTH_LONG).show()

                    showInterstitialThenShare() // Show full screen ad, then share

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setClearImage() {
        //  imageInsert.setBackgroundColor(getRandomColor())
        // imageCompress.setImageDrawable(null)
        // imageCompress.setBackgroundColor(getRandomColor())
        binding.tvSizeAfter.text = "Size : -"
    }

    private fun getRandomColor(): Int {
        val random = Random()
        return Color.argb(
            100, random.nextInt(256),
            random.nextInt(256), random.nextInt(256)
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to open picture!", Toast.LENGTH_SHORT
                ).show()
                return
            }
            try {
                insertImage = from(this, data.data!!)
                binding.imageInsert.setImageBitmap(BitmapFactory.decodeFile(insertImage?.absolutePath))
                binding.tvSizeBefore.text =
                    String.format("Size : %s", getReadableFileSize(insertImage!!.length()))

                // imageInsert.setBackgroundColor(getRandomColor())
                //imageCompress.setImageDrawable(null)
                // imageCompress.setBackgroundColor(getRandomColor())
                binding.tvSizeAfter.text = "Size : -"
            } catch (e: IOException) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to read picture data!", Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / Math.pow(1024.0, digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }


    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-9928088726335462/3686931665",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    Log.d("AdMob", "Interstitial failed: ${loadAdError.message}")
                }
            }
        )
    }

    private fun showInterstitialThenShare() {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    shareCompressedImage()
                    loadInterstitialAd() // Load next ad
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    shareCompressedImage()
                }
            }
            ad.show(this)
        } ?: run {
            shareCompressedImage()
        }
    }

    private fun shareCompressedImage() {
        compressImage?.let { file ->
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        askForPermissions(permissionsList)
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun askForPermissions(permissionsList: ArrayList<String>) {
        // Filter only ungranted permissions
        val ungrantedPermissions = permissionsList.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungrantedPermissions.isNotEmpty()) {
            permissionsLauncher.launch(ungrantedPermissions.toTypedArray())
        } else {
            Log.d("MainActivity", "All permissions already granted")
            // All permissions are granted – proceed with your app logic
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    var permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val list = ArrayList<Boolean>(it.values)
        permissionsList.clear()
        permissionsCount = 0
        for (i in list.indices) {
            if (shouldShowRequestPermissionRationale(permissionsStr[i])) {
                permissionsList.add(permissionsStr[i])
            } else if (!hasPermission(this, permissionsStr[i])) {
                permissionsCount++
            } else {
                //  showpermissiondialog()
            }
        }
        if (permissionsList.size > 0) {
            //some permissions are denied and can be asked again.

        } else if (permissionsCount > 0) {
            //Show alert dialog
            showPermissionDialog()
        } else {
            //All permissions are granted
            Log.d("MainActivity", "All permissions granted")
            // You can proceed with your app logic here
        }
    }


    private fun hasPermission(context: Context, permissionStr: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context, permissionStr
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun showPermissionDialog() {
        Utility.showSettingsDialog(this, this)
    }

    fun checkPermission(): Boolean {
        val result1 =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return result1 == PackageManager.PERMISSION_GRANTED
    }

    override fun alertSetting() {

    }

}