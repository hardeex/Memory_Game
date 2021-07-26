package com.hardextech.memorygame

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class rvCustomGameAdapter(
    private val context: Context,
    private val imageUris: List<Uri>,
private  val boardSize: BoardSize,
    private val customImageClickListener: CustomImageClickListener
): RecyclerView.Adapter<rvCustomGameAdapter.ViewHolder>() {

interface CustomImageClickListener{
    fun onPlaceHolderClick()
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view=LayoutInflater.from(context).inflate(R.layout.custom_card_image, parent, false)
        val cardWidth =parent.width/boardSize.getWidth()
        val cardHeight =parent.width/boardSize.getHeight()
        val cardSideLength = min(cardWidth, cardHeight)
        val layoutParams = view.findViewById<ImageView>(R.id.ivcustomImage).layoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       if (position<imageUris.size){
           holder.bind(imageUris[position])
       } else{
           holder.bind()
       }
    }

    override fun getItemCount(): Int = boardSize.getNumPairs()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivcustomImage)

        fun bind(uri: Uri) {
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener(null)

        }
        fun bind() {
            ivCustomImage.setOnClickListener {
                // launch the intent for the user to select photos
                customImageClickListener.onPlaceHolderClick()
            }

        }

    }
}