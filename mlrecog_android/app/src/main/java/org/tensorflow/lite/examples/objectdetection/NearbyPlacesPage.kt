package org.tensorflow.lite.examples.objectdetection


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.TypefaceSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.core.content.res.ResourcesCompat
import org.tensorflow.lite.examples.objectdetection.R
import java.util.*

class NearbyPlacesPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receivedData = intent.getStringExtra("locnSwitch")

        setContentView(R.layout.activity_nearby_places_page)




        var location = Location("")
        location.latitude = 27.6714
        location.longitude = 85.4293 //default Nyatapola coordinates




        if(receivedData.toBoolean()) {
            // Get the current device location



            val tlocation = getLastKnownLocation()
            if (tlocation != null){
                location = tlocation


            }else{
                Toast.makeText(this, "Please Turn On Location", Toast.LENGTH_SHORT).show()
            }


        }





        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)

        supportActionBar?.setCustomView(R.layout.custom_title_layout2)


        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_minus) // set custom icon for back button

        }


        val textView = findViewById<TextView>(R.id.card7textview)
        val drawable = ContextCompat.getDrawable(this, R.drawable.icons8_map_marker_48)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)




        // Create a Geocoder instance
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(location.latitude, location.longitude, 1)


// Get the address using reverse geocoding
        // Check if there is at least one address found
        if (addresses.isNotEmpty()) {
            // Get the first address from the list
            val address = addresses[0]

            // Get the address line
            val addressLine = address.getAddressLine(0)
            textView.text = addressLine.toString()


            // Use the address line in your application
            Log.d("ReverseGeocoding", "Address: $addressLine")
        } else {
            textView.text = "No Address Found"
            Log.d("ReverseGeocoding", "No address found")
        }









        val cardbtn1 = findViewById<CardView>(R.id.card1)
        val cardbtn2= findViewById<CardView>(R.id.card2)
        val cardbtn3 = findViewById<CardView>(R.id.card3)
        val cardbtn4 = findViewById<CardView>(R.id.card4)
        val cardbtn5 = findViewById<CardView>(R.id.card5)
        val cardbtn6 = findViewById<CardView>(R.id.card6)


        cardbtn1.setOnClickListener {

            val intent = Intent(this, NearbyService::class.java)
            intent.putExtra("myDataKey", "3")
            intent.putExtra("locnSwitch", receivedData)

            startActivity(intent)
        }

        cardbtn2.setOnClickListener {

            val intent = Intent(this, NearbyService::class.java)
            intent.putExtra("myDataKey", "4")
            intent.putExtra("locnSwitch", receivedData)
            startActivity(intent)
        }

        cardbtn3.setOnClickListener {

            val intent = Intent(this, NearbyService::class.java)
            intent.putExtra("myDataKey", "5")
            intent.putExtra("locnSwitch", receivedData)
            startActivity(intent)
        }


        cardbtn4.setOnClickListener {

            val intent = Intent(this, NearbyService::class.java)
            intent.putExtra("myDataKey", "6")
            intent.putExtra("locnSwitch", receivedData)
            startActivity(intent)
        }

        cardbtn5.setOnClickListener {

            val intent = Intent(this, NearbyService::class.java)
            intent.putExtra("myDataKey", "7")
            intent.putExtra("locnSwitch", receivedData)
            startActivity(intent)
        }


        cardbtn6.setOnClickListener {

            val intent = Intent(this, NearbyService::class.java)
            intent.putExtra("myDataKey", "8")
            intent.putExtra("locnSwitch", receivedData)
            startActivity(intent)
        }





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


