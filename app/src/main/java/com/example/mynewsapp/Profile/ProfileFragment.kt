package com.example.mynewsapp.Profile


import android.Manifest
import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mynewsapp.R
import com.example.mynewsapp.Utils.SharedPrefHelper
import com.example.mynewsapp.databinding.FragmentProfileBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private var capturedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.profileImg.editlay.visibility = View.VISIBLE

        setupPermissionLauncher()
        checkAndRequestLocationPermission()
        retriveProfileImage()

        binding.location.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showEnablePreciseLocationDialog()
            }
        }

        binding.profileImg.profilelay.setOnClickListener {
            showBottomSheetForGallery()
        }

        return binding.root
    }


    private fun retriveProfileImage() {
        binding.name.text = SharedPrefHelper.getString("UserName") ?: "User"
        binding.email.text = SharedPrefHelper.getString("Email") ?: "User"

        val savedUriString = SharedPrefHelper.getString("ProfileImage")
        if (!savedUriString.isNullOrEmpty()) {
            val savedUri = Uri.parse(savedUriString)
            Glide.with(this)
                .load(savedUri)
                .error(R.drawable.ic_launcher_background)
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.profileImg.profilelay)
        }
    }


    private fun showEnablePreciseLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Enable Precise Location")
            .setMessage("For better results, please enable precise location in your device settings.")
            .setPositiveButton("Open Settings") { _, _ ->
//                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                Log.d(TAG, "Selected image URI: $uri")
                SharedPrefHelper.saveString("ProfileImage", uri.toString())
                binding.profileImg.profilelay.setImageURI(uri)
            } else {
                Log.d(TAG, "No image selected")
            }
        }

    private fun showPermissionAlertDialog() {
        val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        alertDialogBuilder.apply {
            setMessage("Enable Storage & Camera Permissions For Better Experience!")
            setPositiveButton("Yes") { dialog, which ->


                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showBottomSheetForGallery() {
        val bottomSheetView =
            LayoutInflater.from(requireContext()).inflate(com.example.mynewsapp.R.layout.bottom_sheet_options, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(bottomSheetView)

        val btnCamera = bottomSheetView.findViewById<LinearLayout>(com.example.mynewsapp.R.id.cameraLay)
        val btnGallery =
            bottomSheetView.findViewById<LinearLayout>(com.example.mynewsapp.R.id.GalleryLay)

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                launchCameraIntent()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            dialog.dismiss()
        }


        btnGallery.setOnClickListener {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {


                Log.e(TAG, "showBottomSheetForGallery:::above33" )
                val isGranted = ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED


                if (isGranted) {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    Log.e(TAG, "Requesting permission")
                    mediaImagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                val isGranted = ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                if (isGranted) {
                    startGalleryIntent()
                } else {
                    requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            dialog.dismiss()
        }
        dialog.show()
    }


    private fun launchCameraIntent() {
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "captured_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        capturedImageUri = imageUri

        takePictureLauncher.launch(imageUri)
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCameraIntent()
            } else {
                showPermissionAlertDialog()
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && capturedImageUri != null) {
                Log.d(TAG, "Image captured:::++::: $capturedImageUri")
                SharedPrefHelper.saveString("ProfileImage", capturedImageUri.toString())
                binding.profileImg.profilelay.setImageURI(capturedImageUri)
            } else {
                Log.d(TAG, "Camera capture cancelled!")
            }
        }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d(TAG, "Storage permission granted - launching gallery")
                startGalleryIntent()
            } else {
                Log.d(TAG, "Storage permission denied")
                showPermissionAlertDialog()
            }
        }


    private val mediaImagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d(TAG, "Permission granted - open image picker")
                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                Log.d(TAG, "Permission denied")
                showPermissionAlertDialog()
            }
        }


    private fun startGalleryIntent() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        legacyGalleryLauncher.launch(galleryIntent)
    }


    private val legacyGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    Log.e(TAG, "Legacy image URI: $selectedImageUri")
                    SharedPrefHelper.saveString("ProfileImage", selectedImageUri.toString())
                    binding.profileImg.profilelay.setImageURI(selectedImageUri)
                } else {
                    Log.e(TAG, "No image ")
                }
            }
        }


    private fun setupPermissionLauncher() {
        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                when {
                    isGranted -> getUserLocation()
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                        -> {
                        showEnablePreciseLocationDialog()
                        binding.location.text = "Enable Precise Location"
                    }


                    else -> {
                        showPermissionRationaleDialog()
                        binding.location.text = "Permission required to get location"
                    }


                }
            }
    }


    private fun checkAndRequestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getUserLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionRationaleDialog()
            }

            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }


    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Needed")
            .setMessage("This app requires access to your fine location to show your current address.")
            .setPositiveButton("Allow") { _, _ ->
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                binding.location.text = "Permission required to get location"
            }
            .show()
    }



    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                Log.e(TAG, "getUserLocation:::::::::::location:::::::::: $location" )
                if (location != null) {
                    val accuracy = location.accuracy
                    if (accuracy > 1000f) { // >1km = very approximate
                        binding.location.text =
                            "Approximate location: accuracy is ${accuracy.toInt()} meters"
                    } else {
                        getAddressFromLatLng(location.latitude, location.longitude)
                    }
                } else {
                    binding.location.text = "Location not found"
                }
            }
            .addOnFailureListener {
                binding.location.text = "Failed to get location"
            }
    }


    private fun getAddressFromLatLng(lat: Double, lon: Double) {

        Log.e(TAG, "getAddressFromLatLng::::"+ lat )
        Log.e(TAG, "getAddressFromLatLng::::"+ lon )
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    val address = addresses.firstOrNull()?.getAddressLine(0) ?: "No address found"
                    requireActivity().runOnUiThread {
                        binding.location.text = address
                    }
                }


                override fun onError(errorMessage: String?) {
                    requireActivity().runOnUiThread {
                        binding.location.text = "Geocoder error: ${errorMessage ?: "Unknown error"}"
                    }
                }
            })
        } else {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val addressText =
                        addresses?.firstOrNull()?.getAddressLine(0) ?: "No address found"


                    withContext(Dispatchers.Main) {
                        binding.location.text = addressText
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        binding.location.text = "Unable to get address"
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}







//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.location.Address
//import android.location.Geocoder
//import android.location.Location
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.Settings
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import com.bumptech.glide.Glide
//import com.example.mynewsapp.Utils.SharedPrefHelper
//import com.example.mynewsapp.databinding.FragmentProfileBinding
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.IOException
//import java.util.Locale
//
//class ProfileFragment : Fragment() {
//
//    private var _binding: FragmentProfileBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentProfileBinding.inflate(inflater, container, false)
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//
//        setupPermissionLauncher()
//        checkAndRequestLocationPermission()
//
//        binding.name.text = SharedPrefHelper.getString("UserName") ?: "User"
//        binding.email.text = SharedPrefHelper.getString("Email") ?: "User"
//        Glide.with(requireContext()).load(SharedPrefHelper.getString("ProfileImage")).into(binding.profilelay)
//
//        binding.location.setOnClickListener {
//            if (ContextCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                showEnablePreciseLocationDialog()
//            }
//        }
//        return binding.root
//    }
//
//
//    private fun showEnablePreciseLocationDialog() {
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("Enable Precise Location")
//            .setMessage("For better results, please enable precise location in your device settings.")
//            .setPositiveButton("Open Settings") { _, _ ->
////                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//                val intent = Intent().apply {
//                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    data = Uri.fromParts("package", requireActivity().packageName, null)
//                }
//                startActivity(intent)
//
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//
//    private fun setupPermissionLauncher() {
//        locationPermissionLauncher =
//            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//                when {
//                    isGranted -> getUserLocation()
//                    ContextCompat.checkSelfPermission(
//                        requireContext(),
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    ) == PackageManager.PERMISSION_GRANTED
//                        -> {
//                        showEnablePreciseLocationDialog()
//                        binding.location.text = "Enable Precise Location"
//                    }
//
//                    else -> {
//                        showPermissionRationaleDialog()
//                        binding.location.text = "Permission required to get location"
//                    }
//
//                }
//            }
//    }
//
//    private fun checkAndRequestLocationPermission() {
//        when {
//            ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                getUserLocation()
//            }
//
//            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
//                showPermissionRationaleDialog()
//            }
//
//            else -> {
//                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            }
//        }
//    }
//
//    private fun showPermissionRationaleDialog() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Location Permission Needed")
//            .setMessage("This app requires access to your fine location to show your current address.")
//            .setPositiveButton("Allow") { _, _ ->
//                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            }
//            .setNegativeButton("Cancel") { dialog, _ ->
//                dialog.dismiss()
//                binding.location.text = "Permission required to get location"
//            }
//            .show()
//    }
//
////    private fun checkAndRequestLocationPermission() {
////        if (ContextCompat.checkSelfPermission(
////                requireContext(),
////                Manifest.permission.ACCESS_FINE_LOCATION
////            ) == PackageManager.PERMISSION_GRANTED
////        ) {
////            getUserLocation()
////        } else {
////            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
////        }
////    }
//
//
//    @SuppressLint("MissingPermission") // permission is checked before calling
//    private fun getUserLocation() {
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                if (location != null) {
//                    val accuracy = location.accuracy
//                    if (accuracy > 1000f) { // >1km = very approximate
//                        binding.location.text =
//                            "Approximate location: accuracy is ${accuracy.toInt()} meters"
//                    } else {
//                        getAddressFromLatLng(location.latitude, location.longitude)
//                    }
//                } else {
//                    binding.location.text = "Location not found"
//                }
//            }
//            .addOnFailureListener {
//                binding.location.text = "Failed to get location"
//            }
//    }
//
////    @SuppressLint("MissingPermission")
////    private fun getUserLocation() {
////        fusedLocationClient.lastLocation
////            .addOnSuccessListener { location: Location? ->
////                if (location != null) {
////                    getAddressFromLatLng(location.latitude, location.longitude)
////                } else {
////                    binding.location.text = "Location not found"
////                }
////            }
////            .addOnFailureListener {
////                binding.location.text = "Failed to get location"
////            }
////    }
//
//    private fun getAddressFromLatLng(lat: Double, lon: Double) {
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
//                override fun onGeocode(addresses: MutableList<Address>) {
//                    val address = addresses.firstOrNull()?.getAddressLine(0) ?: "No address found"
//                    requireActivity().runOnUiThread {
//                        binding.location.text = address
//                    }
//                }
//
//                override fun onError(errorMessage: String?) {
//                    requireActivity().runOnUiThread {
//                        binding.location.text = "Geocoder error: ${errorMessage ?: "Unknown error"}"
//                    }
//                }
//            })
//        } else {
//            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//                try {
//                    val addresses = geocoder.getFromLocation(lat, lon, 1)
//                    val addressText =
//                        addresses?.firstOrNull()?.getAddressLine(0) ?: "No address found"
//
//                    withContext(Dispatchers.Main) {
//                        binding.location.text = addressText
//                    }
//                } catch (e: IOException) {
//                    withContext(Dispatchers.Main) {
//                        binding.location.text = "Unable to get address"
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}