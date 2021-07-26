package com.hardextech.memorygame

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize, private val cards: List<MemoryCards>,
    private val cardClickedListener: CardClickedListener

) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {
    companion object{
        // companion object are singleton for defining constant
        private const val MARGIN_SIZE =10
        private const val TAG = "MemoryBoardAdapter"
    }

   interface CardClickedListener{
        fun onCardClicked(position: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardHeight = parent.height/boardSize.getHeight()-(2* MARGIN_SIZE)
        val cardWidth = parent.width/boardSize.getWidth()-(2* MARGIN_SIZE)
        val cardLength = min(cardWidth, cardHeight)
        val view =LayoutInflater.from(context).inflate(R.layout.memory_board_adapter, parent, false)
        val layoutParams = view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width= cardLength
        layoutParams.height= cardLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)

        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(position)
    }

    override fun getItemCount() = boardSize.numCards


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private  val imageButton = itemView.findViewById<ImageButton>(R.id.imageButton)

        fun bind(position: Int) {
            val  memoryCards = cards[position]
           imageButton.setImageResource(if (cards[position].isFaceUp)  cards[position].identifier else R.drawable.ic_launcher_background )

            // The alpha refers to the opacity. How visible is the imageButton
            imageButton.alpha = if (memoryCards.isMatched) .4f else 1.0f
            val colorStateList = if (memoryCards.isMatched) ContextCompat.getColorStateList(context, R.color.color_overshadow_image) else null
            ViewCompat.setBackgroundTintList(imageButton, colorStateList)


             imageButton.setOnClickListener {
             Log.i(TAG, "Clicked on the position $position")
                 cardClickedListener.onCardClicked(position)



         }

        }



    }

}
