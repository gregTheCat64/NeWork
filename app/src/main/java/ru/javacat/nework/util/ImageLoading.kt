package ru.javacat.nework.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.javacat.nework.R

fun ImageView.load(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_baseline_no_photography_24)
        .centerCrop()
        .timeout(10_000)
        .transform(*transforms)
        .into(this)

fun ImageView.loadFull(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .transform(*transforms)
        .into(this)


fun ImageView.loadCircleCrop(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    load(url, CircleCrop(), *transforms)

fun ImageView.loadAvatar(url: String,  vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .circleCrop()
        .placeholder(R.drawable.ic_baseline_account_circle_24)
        .timeout(10_000)
        .transform(*transforms)
        .into(this)

