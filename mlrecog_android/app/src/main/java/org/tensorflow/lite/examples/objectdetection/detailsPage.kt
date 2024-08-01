package org.tensorflow.lite.examples.objectdetection

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class detailsPage : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val receivedData = intent.getStringExtra("DataKey")
        setContentView(R.layout.loading_progress_bar)





        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.custom_title_layout)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_minus) // set custom icon for back button

        }


        progressBar = findViewById(R.id.progressBar)

        // Set ProgressBar visibility to visible before making the API request
//        progressBar.visibility = View.VISIBLE

        // Make API request using Volley
        val queue = Volley.newRequestQueue(this)
        val url = "https://mlrecog-062v.onrender.com/details"

        val jsonObject = JSONObject()


        jsonObject.put("name", receivedData)


        val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,
            { response ->

                // Handle response received from API request
                // Set ProgressBar visibility to gone and show response to the user

                showContent()


                val jsonArray = response.getJSONArray("image_name")
                val imageNames = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getString(i)
                    imageNames.add(item)
                }
                val foldername = receivedData?.replace(" ", "").toString()
//                var imageNames =  response.get("image_name").toString()
//                imageNames = imageNames.substring(1)
//                imageNames = imageNames.dropLast(1)
//                Toast.makeText(this, listOf(imageNames)[0], Toast.LENGTH_LONG).show()




                val viewPager = findViewById<ViewPager>(R.id.carousel_viewpager)
                val dotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)
                val myTextView = findViewById<TextView>(R.id.textview_bullets)
                val title = findViewById<TextView>(R.id.title_textview)
                val description = findViewById<TextView>(R.id.description_textview)


                val adapter = ImagePagerAdapter(this, imageNames, foldername)
                viewPager.adapter = adapter
                dotsIndicator.attachTo(viewPager)

                title.text = response.getString("title")

                val bullet = "\u27A4 " // Unicode character for bullet symbol
                val builder = SpannableStringBuilder()
                builder.append("$bullet" + "Age:" + response.getString("age") +  "\n")
                builder.append("$bullet" + "Location:" + response.getString("location") +  "\n")

                myTextView.text = builder
                description.text = response.getString("description")





            },
            { error ->
                // Handle error received from API request
                // Set ProgressBar visibility to gone and show error message to the user
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: Server Request Failed", Toast.LENGTH_SHORT).show()
            })

        queue.add(request)





// Set headers if needed
//        request.headers["Authorization"] = "Bearer " + token








    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun showContent() {
        progressBar.visibility = View.GONE
        setContentView(R.layout.activity_details_page)
    }
}