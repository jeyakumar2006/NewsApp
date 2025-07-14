package com.example.mynewsapp.Profile


import android.Manifest
import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
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
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
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

    val gpsResolutionLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                getUserLocation()
            } else {
                binding.location.text = "GPS is still disabled, Kindly enable the GPS"
            }
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
                    isGranted -> checkIfGPSEnabledThenGetLocation()
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

    private fun checkIfGPSEnabledThenGetLocation() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true) // Show GPS dialog even if previously declined

        val client = com.google.android.gms.location.LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getUserLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                Log.e(TAG, "checkIfGPSEnabledThenGetLocation:::::::1:::::: " )
                try {
                    Log.e(TAG, "checkIfGPSEnabledThenGetLocation:::::::2:::::: " )
                    exception.resolution?.let { intentSender ->
                        gpsResolutionLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build()
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "checkIfGPSEnabledThenGetLocation:::::::3::::: " )
                    e.printStackTrace()
                    binding.location.text = "Failed to prompt GPS settings"
                }
            } else {
                Log.e(TAG, "checkIfGPSEnabledThenGetLocation:::::::4:::::: " )
                binding.location.text = "GPS not supported on this device"
            }
        }
    }




    private fun checkAndRequestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkIfGPSEnabledThenGetLocation()
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
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    val location = result.lastLocation
                    if (location != null) {
                        val accuracy = location.accuracy
                        if (accuracy > 1000f) {
                            binding.location.text = "Approximate location: accuracy is ${accuracy.toInt()} meters"
                        } else {
                            getAddressFromLatLng(location.latitude, location.longitude)
                        }
                    } else {
                        binding.location.text = "Location not found"
                    }

                    fusedLocationClient.removeLocationUpdates(this)
                }

                override fun onLocationAvailability(availability: com.google.android.gms.location.LocationAvailability) {
                    if (!availability.isLocationAvailable) {
                        binding.location.text = "Location not available"
                    }
                }
            },
            Looper.getMainLooper()
        )
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
