package org.tensorflow.lite.examples.objectdetection
import android.content.Context
import com.bumptech.glide.Glide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import java.io.IOException


class ImagePagerAdapter(private val context: Context, private val imageNames: List<String>, private val foldername: String) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.image_view, container, false)

        val imageView = view.findViewById<ImageView>(R.id.imageView)

        val imagePathName = foldername + "-" + imageNames[position]



        try {
            Glide.with(context)
                .load("https://mlrecog-062v.onrender.com/images/$imagePathName")
                .into(imageView)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        container.addView(view)

        return view
    }

    override fun getCount(): Int {
        return imageNames.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}