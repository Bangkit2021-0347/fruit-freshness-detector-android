package com.andreastnnd.habisin

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andreastnnd.habisin.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE = "extra_image"
        const val EXTRA_FRESHNESS_LEVEL = "extra_freshness_level"
        const val EXTRA_PRICE = "extra_price"
    }

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val image = intent.getStringExtra(EXTRA_IMAGE)
        var freshnessLevel = intent.getStringExtra(EXTRA_FRESHNESS_LEVEL)
        freshnessLevel = "$freshnessLevel %"
        var price = intent.getStringExtra(EXTRA_PRICE)
        price = "Rp. $price"

        binding.apply {
            imageView.setImageURI(Uri.parse(image))
            tvFreshnessLevel.text = freshnessLevel
            tvPrice.text = price
        }
    }
}