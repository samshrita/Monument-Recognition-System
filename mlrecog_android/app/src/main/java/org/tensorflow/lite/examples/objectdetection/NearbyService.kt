package org.tensorflow.lite.examples.objectdetection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class NearbyService : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)







        val receivedData = intent.getStringExtra("myDataKey")
        val receivedData2 = intent.getStringExtra("locnSwitch")

        var drawableimg = R.drawable.monument
        var category = "monument"
        setContentView(R.layout.loading_progress_bar)


        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)

        when (receivedData){
            "3" -> {supportActionBar?.setCustomView(R.layout.custom_title_layout3); drawableimg = R.drawable.monument; category = "monument"; }
            "4" -> {supportActionBar?.setCustomView(R.layout.custom_title_layout4);  drawableimg = R.drawable.hotel; category = "hotel";}
            "5" -> {supportActionBar?.setCustomView(R.layout.custom_title_layout5);  drawableimg = R.drawable.bus; category = "bustop";}
            "6" -> {supportActionBar?.setCustomView(R.layout.custom_title_layout6);  drawableimg = R.drawable.toilet; category = "restroom";}
            "7" -> {supportActionBar?.setCustomView(R.layout.custom_title_layout7);  drawableimg = R.drawable.exchange; category = "exchange";}
            "8" -> {supportActionBar?.setCustomView(R.layout.custom_title_layout8);  drawableimg = R.drawable.atm; category = "atm";}
            else -> {supportActionBar?.setCustomView(R.layout.custom_title_layout3);  drawableimg = R.drawable.monument; category = "monument";}

        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_minus) // set custom icon for back button

        }

        progressBar = findViewById(R.id.progressBar)





        var location = Location("")
        var lat1 = "27.6714"
        var lng1 = "85.4293" //default Nyatapola coordinates

        var locationvar = "($lat1,$lng1)"


        if(receivedData2.toBoolean()){
            // Get the current device location



            val tlocation = getLastKnownLocation()
            if(tlocation != null){
                location = tlocation

                lat1 = String.format("%.5f", location.latitude)
                lng1 = String.format("%.5f", location.longitude)
                locationvar = "($lat1,$lng1)"
            }else{
                Toast.makeText(this, "Please Turn On Location", Toast.LENGTH_SHORT).show()
            }





        }



        // Make API request using Volley
        val queue = Volley.newRequestQueue(this)
        val url = "https://mlrecog-062v.onrender.com/nearbyplaces"

        val jsonObject = JSONObject()
        jsonObject.put("category", category)
        jsonObject.put("latilongi",  locationvar)


        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->

                // Handle response received from API request
                // Set ProgressBar visibility to gone and show response to the user
                progressBar.visibility = View.GONE
                setContentView(R.layout.activity_nearby_service)


                val name = response.getJSONArray("name")
                val latlng = response.getJSONArray("latlng")
                val addr = response.getJSONArray("address")

                if (name.length() == 0){
                    val tcategory = category.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                    val textView = TextView(this)
                    textView.text = "No $tcategory Found Near You"
                    textView.gravity = Gravity.CENTER
                    setContentView(textView)
                }


                for (i in 0 until name.length()) {

                    val pattern = "\\((\\s*-?\\d+\\.\\d+\\s*),\\s*(\\s*-?\\d+\\.\\d+\\s*)\\)".toRegex()
                    val matchResult = pattern.find(latlng.getString(i))
                    val  (tlat2, tlng2) = matchResult!!.destructured
                    println("Latitude: $tlat2, Longitude: $tlng2")
                    val lat2 = tlat2.replace(" ", "")
                    val lng2 = tlng2.replace(" ", "")


//




                    val inflater = LayoutInflater.from(this)
                    val cardView = inflater.inflate(R.layout.cardbutton, null) as CardView
                    val showimg = cardView.findViewById<ImageView>(R.id.showimage)
                    val titlename = cardView.findViewById<TextView>(R.id.titlename)
                    val getdirbtn = cardView.findViewById<Button>(R.id.getdirection)
                    val distance = cardView.findViewById<TextView>(R.id.distance)
                    val address = cardView.findViewById<TextView>(R.id.address)
                    showimg.setImageResource(drawableimg)
                    titlename.text = name.getString(i)
                    address.text = addr.getString(i)
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 16, 0, 16) // Set margins for the CardView
                    val parent = findViewById<LinearLayout>(R.id.activitynearbyservice)
                    parent.addView(cardView, layoutParams)


                    getdirbtn.setOnClickListener {
                        if (receivedData2.toBoolean()) {
                            val uri =
                                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat2,$lng2")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        }else{
                            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$lat1,$lng1&destination=$lat2,$lng2")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)

                        }
                    }


                }










            },
            { error ->
                // Handle error received from API request
                // Set ProgressBar visibility to gone and show error message to the user
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: Server Req Failed", Toast.LENGTH_SHORT).show()
            })

        queue.add(request)




























    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    var mLocationManager: LocationManager? = null


    private fun getLastKnownLocation(): Location? {
        mLocationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        val providers = mLocationManager!!.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Location Permission Not Granted", Toast.LENGTH_SHORT).show()

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