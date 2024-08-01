/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tensorflow.lite.examples.objectdetection.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.tensorflow.lite.examples.objectdetection.NearbyPlacesPage
import org.tensorflow.lite.examples.objectdetection.ObjectDetectorHelper
import org.tensorflow.lite.examples.objectdetection.R
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentCameraBinding
import org.tensorflow.lite.examples.objectdetection.detailsPage
import org.tensorflow.lite.task.vision.detector.Detection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max


class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener {




    var switchlocn = false
    private val TAG = "ObjectDetection"

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null





    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()

        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }

    }

//    override fun onPause() {
//        super.onPause()
//        cameraExecutor.shutdown()
//    }


    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()
    }
    private companion object{
        //PERMISSION request constant, assign any value
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }
    override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this)

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Attach listeners to UI control widgets
        initBottomSheetControls()

    }

    private fun initBottomSheetControls() {


        fragmentCameraBinding.bottomSheetLayout.btncamera.setOnClickListener {
            val intent = Intent(context, NearbyPlacesPage::class.java)
            intent.putExtra("locnSwitch", switchlocn.toString())
            startActivity(intent)
        }


        fragmentCameraBinding.bottomSheetLayout.thresholdMinus.setOnClickListener {
            if (objectDetectorHelper.threshold >= 0.2) {
                objectDetectorHelper.threshold -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise detection score threshold floor
        fragmentCameraBinding.bottomSheetLayout.thresholdPlus.setOnClickListener {
            if (objectDetectorHelper.threshold <= 0.9) {
                objectDetectorHelper.threshold += 0.1f
                updateControlsUi()
            }
        }




        // When clicked, decrease the number of threads used for detection
        fragmentCameraBinding.bottomSheetLayout.threadsMinus.setOnClickListener {
            if (objectDetectorHelper.numThreads > 1) {
                objectDetectorHelper.numThreads--
                updateControlsUi()
            }
        }

        // When clicked, increase the number of threads used for detection
        fragmentCameraBinding.bottomSheetLayout.threadsPlus.setOnClickListener {
            if (objectDetectorHelper.numThreads < 4) {
                objectDetectorHelper.numThreads++
                updateControlsUi()
            }
        }

        fragmentCameraBinding.bottomSheetLayout.locnSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchlocn = true
                Toast.makeText(context, "Current Location Turned On", Toast.LENGTH_SHORT).show()
            } else {
                switchlocn = false
                Toast.makeText(context, "Current Location Turned Off", Toast.LENGTH_SHORT).show()
            }

        }

        // When clicked, change the underlying hardware used for inference. Current options are CPU
        // GPU, and NNAPI
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(0, false)
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    objectDetectorHelper.currentDelegate = p2
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }

        // When clicked, change the underlying model used for object detection
        fragmentCameraBinding.bottomSheetLayout.spinnerModel.setSelection(0, false)
        fragmentCameraBinding.bottomSheetLayout.spinnerModel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    objectDetectorHelper.currentModel = p2
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }
    }

    // Update the values displayed in the bottom sheet. Reset detector.
    private fun updateControlsUi() {

        fragmentCameraBinding.bottomSheetLayout.thresholdValue.text =
            String.format("%.2f", objectDetectorHelper.threshold)
        fragmentCameraBinding.bottomSheetLayout.threadsValue.text =
            objectDetectorHelper.numThreads.toString()

        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when applicable
        objectDetectorHelper.clearObjectDetector()
        fragmentCameraBinding.overlay.clear()
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview =
            Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)

                .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            // The image rotation and RGB image buffer are initialized only once
                            // the analyzer has started running
                            bitmapBuffer = Bitmap.createBitmap(
                              image.width,
                              image.height,
                              Bitmap.Config.ARGB_8888
                            )
                        }

                        detectObjects(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectObjects(image: ImageProxy) {

        // Copy out RGB bits to the shared bitmap buffer
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }


        val imageRotation = image.imageInfo.rotationDegrees
        // Pass Bitmap and rotation to the object detector helper for processing and detection
        objectDetectorHelper.detect(bitmapBuffer, imageRotation)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after objects have been detected. Extracts original image height/width
    // to scale and place bounding boxes properly through OverlayView
    override fun onResults(
        image : Bitmap,
      results: MutableList<Detection>?,
      inferenceTime: Long,
      imageHeight: Int,
      imageWidth: Int
    ) {
        activity?.runOnUiThread {
            fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
                            String.format("%d ms", inferenceTime)

            // Pass necessary information to OverlayView for drawing on the canvas

            fragmentCameraBinding.overlay.setResults(
                results ?: LinkedList<Detection>(),
                imageHeight,
                imageWidth
            )
            fragmentCameraBinding.overlay.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {



                    val matrix = Matrix()

                    matrix.postRotate(-270F)

                    val scaledBitmap = Bitmap.createScaledBitmap(image, image.width, image.height, true)

                    val rotatedBitmap = Bitmap.createBitmap(
                        scaledBitmap,
                        0,
                        0,
                        scaledBitmap.width,
                        scaledBitmap.height,
                        matrix,
                        true
                    )


                   val xValue = event.getX()
                  val  yValue = event.getY()
                    var top = 0.0f
                    var bottom = 0.0f
                    var right = 0.0f
                    var left = 0.0f
                    var croppedHeight = 0.0f
                    var croppedWidth = 0.0f
                    val width = v.width
                    val height = v.height

                    val  scaleFactor = (max(width * 1f / imageWidth, height * 1f / imageHeight))
                    var tf = false
                    if (results != null && results.size > 0) {
                        val result = results[0]
                        val boundingBox = result.boundingBox
                        top = boundingBox.top
                        bottom = boundingBox.bottom
                        left = boundingBox.left
                        right = boundingBox.right

                        if( top > 0 && bottom > 0 && left > 0 && right > 0) {


                        croppedHeight = abs(((bottom) - (top)))
                        if (croppedHeight + top >= image.width){
                            croppedHeight = image.width - top
                        }
                        croppedWidth = abs(((right) - (left)))
                        if (croppedWidth + left >= image.height){
                            croppedWidth = image.height - left
                        }


                        val rect = RectF(left * scaleFactor, top * scaleFactor, right * scaleFactor, bottom * scaleFactor)
                        tf = rect.contains(xValue, yValue)
                        }
                    }


                      if (tf){

                          val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, abs(left.toInt()), abs(top.toInt()), croppedWidth.toInt(), croppedHeight.toInt())
                          showAndSaveImagePopup(croppedBitmap, results)







                      }


                    return v?.onTouchEvent(event) ?: true
                }
            })







            // Force a redraw

            fragmentCameraBinding.overlay.invalidate()
        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.context?.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
            catch (e: Exception){
                Log.e(TAG, "requestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        }
        else{
            //Android is below 11(R)

        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.d(TAG, "storageActivityResultLauncher: ")
        //here we will handle the result of our intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            if (Environment.isExternalStorageManager()){
                //Manage External Storage Permission is granted
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is granted")

            }
            else{
                //Manage External Storage Permission is denied....
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is denied....")

            }
        }
        else{
            //Android is below 11(R)
        }
    }

    private fun checkPermission(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            Environment.isExternalStorageManager()
        }
        else{
            //Android is below 11(R)
            val write = this.context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) }
            val read = this.context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) }
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()){
                //check each permission if granted or not
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read){
                    //External Storage Permission granted
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission granted")

                }
                else{
                    //External Storage Permission denied...
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission denied...")

                }
            }
        }
    }


    private fun showAndSaveImagePopup( bitmap: Bitmap, results: MutableList<Detection>?) {


        val builder = AlertDialog.Builder(context)
        val imageView = ImageView(context)
        imageView.setPadding(20,50,20,20)
        imageView.setImageBitmap(bitmap)
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.setColor(Color.BLACK)
//        gradientDrawable.cornerRadius = 30f
        imageView.background = gradientDrawable
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(imageView)










        val progressBar = ProgressBar(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(20,50,20,50) // here you set the margin you want
        layoutParams.gravity = Gravity.CENTER
        progressBar.layoutParams = layoutParams
        layout.addView(progressBar)
        builder.setView(layout)

        builder.setPositiveButton("Save") { _, _ ->
            // Code to save the image goes here
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false
            saveButton.isClickable = false

            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)


            var location = "(27.6714,85.4293)" //default Nyatapola coordinates
            if(switchlocn) {
                location = getDeviceLocation()
            }

            val retrofit = Retrofit.Builder()
                .baseUrl("https://mlrecog-062v.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()


            val service = retrofit.create(API::class.java)
            val imageBytes = stream.toByteArray()
            val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes)
            val fileimg = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)
            val ltlg = RequestBody.create(MediaType.parse("text/plain"), location)
            val startTime = System.currentTimeMillis()
            val call = service.sendData(ltlg, fileimg)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {


                        val endTime = System.currentTimeMillis()
                        val elapsedTime = endTime - startTime
//                        Toast.makeText(context, "$elapsedTime ms", Toast.LENGTH_SHORT).show()

                        val imageData = response.body()!!.bytes()
                        val bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)

                        val responseData = response.headers()

                        if (response.body() != null && responseData.get("status").toString() != "server error") {
                           val imgView: ImageView = (layout.getChildAt(0)) as ImageView

                            imgView.setImageBitmap(bmp)
                            layout.removeView(progressBar)
                            val infoTextTitle = TextView(context)
                            val spanTitle = "Predicted Data"
                            val spannableString = SpannableString(spanTitle)
                            spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, spanTitle.length, 0)
                            infoTextTitle.text = spannableString
                            infoTextTitle.setPadding(80, 65, 20, 20)
                            layout.addView(infoTextTitle)
                            val infoTextView = TextView(context)
                            val info =  "Name: " + responseData.get("predname").toString().capitalize() + "\nPred Conf: " + ((responseData.get("predconf").toString().toFloat() * 100).toInt()).toString() + " %"  + "\nInf Time: " + responseData.get("inftime").toString()
                            infoTextView.setText(info)
                            infoTextView.setPadding(80, 10, 20, 20)
                            layout.addView(infoTextView)



                            val myButton = Button(context)
                            myButton.setTextColor(Color.rgb(183,121,0))
//                            myButton.setTextSize(20f)
                            myButton.text = "  More Detail  "
                            myButton.isAllCaps = false

                            val shape = GradientDrawable()
                            shape.shape = GradientDrawable.RECTANGLE
                            shape.setColor(Color.WHITE)
                            shape.cornerRadius = 18F
                            myButton.background = shape



                            layout.addView(myButton)
                            val layoutParamss = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                            layoutParamss.setMargins(75, 20, 20, 20)
                            layoutParamss.gravity = Gravity.START
                            myButton.layoutParams = layoutParamss
                            myButton.setOnClickListener {
                                val intent = Intent(context, detailsPage::class.java)
                                intent.putExtra("DataKey", responseData.get("predname").toString())
                                startActivity(intent)
                            }




                            saveButton.isEnabled = true
                            saveButton.isClickable = true


                            saveButton.setOnClickListener {



                                val pictureFile: File? = getOutputMediaFile(responseData.get("predname").toString() + "_" + ((responseData.get("predconf").toString().toFloat() * 100).toInt()).toString())
                                if (pictureFile == null) {
                                    println("Check Permission")

                                }
                                try {
                                    val fos = FileOutputStream(pictureFile)








                                    try {
                                        if (checkPermission()) {
                                            Log.d(TAG, "onCreate: Permission already granted, create folder")

                                        } else {
                                            Log.d(TAG, "onCreate: Permission was not granted, request")
                                            requestPermission()
                                        }





                                        fos.write(imageData)

                                        fos.flush();
                                        fos.close()

                                    } catch (e: java.lang.Exception) {
                                        Log.e(TAG, e.message!!)
                                    }



                                    fos.flush();
                                    fos.close()


                                } catch (e: FileNotFoundException) {
                                    println("File not found: ")
                                } catch (e: IOException) {
                                    println("Error accessing file: ")
                                }
                                Toast.makeText(requireContext(), "Image Saved", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()


                            }
                        } else {

                            var label = results!!.first().categories.first().label.toString().capitalize().replace("\r", "")
                            if (label == "Temple1"){
                                label = "Gopinath"
                            }else if (label == "Siddhilaxmi"){
                                label = "Vatshala Devi"
                            }

                            layout.removeView(progressBar)
                            val infoTextTitle = TextView(context)
                            val spanTitle = "Predicted Data"
                            val spannableString = SpannableString(spanTitle)
                            spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, spanTitle.length, 0)
                            infoTextTitle.text = spannableString
                            infoTextTitle.setPadding(80, 65, 20, 20)
                            layout.addView(infoTextTitle)
                            val infoTextView = TextView(context)
                            val info =  "Name: " + label + "\nPred Conf: " + ((results!!.first().categories.first().score.toString().toFloat() * 100).toInt()).toString() + " %"
                            infoTextView.setText(info)
                            infoTextView.setPadding(80, 10, 20, 20)
                            layout.addView(infoTextView)
                            Toast.makeText(requireContext(), "Remote Server Offline", Toast.LENGTH_SHORT).show()



                            val myButton = Button(context)
                            myButton.setTextColor(Color.rgb(183,121,0))
//                            myButton.setTextSize(20f)
                            myButton.text = "  More Detail  "
                            myButton.isAllCaps = false

                            val shape = GradientDrawable()
                            shape.shape = GradientDrawable.RECTANGLE
                            shape.setColor(Color.WHITE)
                            shape.cornerRadius = 18F
                            myButton.background = shape



                            layout.addView(myButton)
                            val layoutParamss = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                            layoutParamss.setMargins(75, 20, 20, 20)
                            layoutParamss.gravity = Gravity.START
                            myButton.layoutParams = layoutParamss
                            myButton.setOnClickListener {
                                val intent = Intent(context, detailsPage::class.java)
                                intent.putExtra("DataKey", label)
                                startActivity(intent)
                            }




                            saveButton.isEnabled = true
                            saveButton.isClickable = true


                            saveButton.setOnClickListener {



                                val pictureFile: File? = getOutputMediaFile(label + "_" + ((results!!.first().categories.first().score.toString().toFloat() * 100).toInt()).toString())
                                if (pictureFile == null) {
                                    println("Check Permission")

                                }
                                try {
                                    val fos = FileOutputStream(pictureFile)








                                    try {
                                        if (checkPermission()) {
                                            Log.d(TAG, "onCreate: Permission already granted, create folder")

                                        } else {
                                            Log.d(TAG, "onCreate: Permission was not granted, request")
                                            requestPermission()
                                        }




                                        val streamtemp = ByteArrayOutputStream()
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, streamtemp)
                                        val imagetemp = streamtemp.toByteArray()
                                        fos.write(imagetemp)

                                        fos.flush();
                                        fos.close()

                                    } catch (e: java.lang.Exception) {
                                        Log.e(TAG, e.message!!)
                                    }



                                    fos.flush();
                                    fos.close()


                                } catch (e: FileNotFoundException) {
                                    println("File not found: ")
                                } catch (e: IOException) {
                                    println("Error accessing file: ")
                                }
                                Toast.makeText(requireContext(), "Image Saved", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()


                            }
                        }


                    } else {
                        var label = results!!.first().categories.first().label.toString().capitalize().replace("\r", "")
                        if (label == "Temple1"){
                            label = "Gopinath"
                        }else if (label == "Siddhilaxmi"){
                            label = "Vatshala Devi"
                        }

                        layout.removeView(progressBar)
                        val infoTextTitle = TextView(context)
                        val spanTitle = "Predicted Data"
                        val spannableString = SpannableString(spanTitle)
                        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, spanTitle.length, 0)
                        infoTextTitle.text = spannableString
                        infoTextTitle.setPadding(80, 65, 20, 20)
                        layout.addView(infoTextTitle)
                        val infoTextView = TextView(context)
                        val info =  "Name: " + label + "\nPred Conf: " + ((results!!.first().categories.first().score.toString().toFloat() * 100).toInt()).toString() + " %"
                        infoTextView.setText(info)
                        infoTextView.setPadding(80, 10, 20, 20)
                        layout.addView(infoTextView)
                        Toast.makeText(requireContext(), "Remote Server Offline", Toast.LENGTH_SHORT).show()



                        val myButton = Button(context)
                        myButton.setTextColor(Color.rgb(183,121,0))
//                            myButton.setTextSize(20f)
                        myButton.text = "  More Detail  "
                        myButton.isAllCaps = false

                        val shape = GradientDrawable()
                        shape.shape = GradientDrawable.RECTANGLE
                        shape.setColor(Color.WHITE)
                        shape.cornerRadius = 18F
                        myButton.background = shape



                        layout.addView(myButton)
                        val layoutParamss = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                        layoutParamss.setMargins(75, 20, 20, 20)
                        layoutParamss.gravity = Gravity.START
                        myButton.layoutParams = layoutParamss
                        myButton.setOnClickListener {
                            val intent = Intent(context, detailsPage::class.java)
                            intent.putExtra("DataKey", label)
                            startActivity(intent)
                        }




                        saveButton.isEnabled = true
                        saveButton.isClickable = true


                        saveButton.setOnClickListener {



                            val pictureFile: File? = getOutputMediaFile(label + "_" + ((results!!.first().categories.first().score.toString().toFloat() * 100).toInt()).toString())
                            if (pictureFile == null) {
                                println("Check Permission")

                            }
                            try {
                                val fos = FileOutputStream(pictureFile)








                                try {
                                    if (checkPermission()) {
                                        Log.d(TAG, "onCreate: Permission already granted, create folder")

                                    } else {
                                        Log.d(TAG, "onCreate: Permission was not granted, request")
                                        requestPermission()
                                    }




                                    val streamtemp = ByteArrayOutputStream()
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, streamtemp)
                                    val imagetemp = streamtemp.toByteArray()
                                    fos.write(imagetemp)

                                    fos.flush();
                                    fos.close()

                                } catch (e: java.lang.Exception) {
                                    Log.e(TAG, e.message!!)
                                }



                                fos.flush();
                                fos.close()


                            } catch (e: FileNotFoundException) {
                                println("File not found: ")
                            } catch (e: IOException) {
                                println("Error accessing file: ")
                            }
                            Toast.makeText(requireContext(), "Image Saved", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()


                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    layout.removeView(progressBar)
                    val infoTextTitle = TextView(context)
                    val spanTitle = "Server Error or Check Internet Connection"
                    val spannableString = SpannableString(spanTitle)
                    spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, spanTitle.length, 0)
                    infoTextTitle.text = spannableString
                    infoTextTitle.setPadding(0, 80, 0, 80)
                    val infoTextTitleLayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                    infoTextTitleLayoutParams.gravity = Gravity.CENTER
                    infoTextTitle.layoutParams = infoTextTitleLayoutParams
                    layout.addView(infoTextTitle)
                }
            })


            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

        }



        dialog.setCancelable(false)

        dialog.show()



    }


    interface API {
        @Multipart
        @POST("predict")
        fun sendData(@Part("ltlg") String: RequestBody, @Part image: MultipartBody.Part ): Call<ResponseBody>
    }

    private fun getDeviceLocation(): String {
        // Get the current device location

        val location = getLastKnownLocation()
        if(location != null) {
            val locn = "(" + String.format("%.5f", location.latitude) + "," + String.format(
                "%.5f",
                location.longitude
            ) + ")"

            return locn
        }else{

          Toast.makeText(requireContext(), "Please Turn On Location", Toast.LENGTH_SHORT).show()
            return "(27.6714,85.4293)"
        }
    }

    private fun getOutputMediaFile( name: String ): File? {

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        val mediaStorageDir = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/DCIM/od/"

        )


        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {

                return null
            }
        }
        // Create a media file name
        val timeStamp: String = SimpleDateFormat("ddMMyyyy_HHmm").format(Date())
        val mediaFile: File
        val mImageName = "MR_" + name + "_" + timeStamp +".jpg"
        mediaFile = File(mediaStorageDir.path + File.separator + mImageName)

        return mediaFile
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }


    }



    var mLocationManager: LocationManager? = null


    private fun getLastKnownLocation(): Location? {
        mLocationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mLocationManager!!.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(requireContext(), "Location Permission Not Granted", Toast.LENGTH_SHORT).show()
                return Location("")
            }else {
                mLocationManager!!.getLastKnownLocation(provider) ?: continue
            }
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        return bestLocation
    }


}
