package com.hardextech.memorygame

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    companion object{
        private  const val TAG = "MainActivity"
        private  const val CREATE_REQUEST_CODE = 2019
    }

    private lateinit var parentLayout: ConstraintLayout
    private lateinit var memoryGame: MemoryGame
    private lateinit var tvMoves: TextView
    private lateinit var tvNumPairs: TextView
    private  lateinit var rvBoard: RecyclerView
    private var boardSize:BoardSize = BoardSize.EASY
    private lateinit var adapter: MemoryBoardAdapter // To enable us access the adapter from multiple sources and not just from the onCreate method only

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Declaring the variables
        tvMoves=findViewById(R.id.tvMoves)
        tvNumPairs=findViewById(R.id.tvNumPairs)
        rvBoard=findViewById(R.id.rvBoard)
        parentLayout=findViewById(R.id.parentLayout)

        setUpBoardGame()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.refresh ->{
                if (memoryGame.getNumMoves()>0 && !memoryGame.isWon()){
                    showDialogBox("Do you want to quit your current game?", null) {
                        setUpBoardGame()
                    }
                } else{
                    setUpBoardGame()
                }
                return true

            }
            R.id.change_game_size ->{
                showGameLevelsDialogue()
                return true
            }
            R.id.customGame ->{
                createCustomMemoryGame()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun createCustomMemoryGame() {
        val newBoardSizeView = LayoutInflater.from(this).inflate(R.layout.new_board_sie_layout, null)
        val radioGroup = newBoardSizeView.findViewById<RadioGroup>(R.id.radioGroup_Parent)

        showDialogBox("CREATE NEW MEMORY GAME", newBoardSizeView) {
            val desiredBoardSize = when (radioGroup.checkedRadioButtonId) {
                R.id.radioButton_Easy -> BoardSize.EASY
                R.id.radioButton_Medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            //setUpBoardGame()
            // Navigate to the custom activity layout
            val intent = Intent(this, createCustomActivity::class.java).putExtra(CUSTOMGAME_BOARDSIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)

        }
        // Navigate to the custom activity layout
//        intent= Intent(this, CustomActivity::class.java)
//        // to get data from the CustomActivity, we are going to use the startActivityForResult()
//        startActivityForResult(intent, CREATE_REQUEST_CODE)



    }

    private fun showGameLevelsDialogue() {
        val newBoardSize = LayoutInflater.from(this).inflate(R.layout.new_board_sie_layout, null)
        val radioGroup = newBoardSize.findViewById<RadioGroup>(R.id.radioGroup_Parent)
       when (boardSize){
           BoardSize.EASY -> radioGroup.check(R.id.radioButton_Easy)
           BoardSize.MEDIUM ->radioGroup.check(R.id.radioButton_Medium)
           BoardSize.HARD ->radioGroup.check(R.id.radioButton_Hard)
       }
        showDialogBox("CHOOSE NEW GAME SIZE", newBoardSize, View.OnClickListener {
          boardSize= when(radioGroup.checkedRadioButtonId){
               R.id.radioButton_Easy -> BoardSize.EASY
               R.id.radioButton_Medium -> BoardSize.MEDIUM
               else-> BoardSize.HARD
          }
            setUpBoardGame()
        })
    }

    private fun showDialogBox(title: String, view: View?, positiveValueClickListener: View.OnClickListener) {
        AlertDialog.Builder(this).setTitle(title).setView(view).setNegativeButton("CANCEL", null).setPositiveButton("OK"){_,_ ->
           positiveValueClickListener.onClick(null)
        }.show()
    }

    private fun setUpBoardGame() {
        when(boardSize){
            BoardSize.EASY -> {
                tvMoves.text = "EASY: 4 * 2"
                tvNumPairs.text= "PAIRS: 0/4"
            }
            BoardSize.MEDIUM -> {
                tvMoves.text = "MEDIUM:6 * 3"
                tvNumPairs.text= "PAIRS: 0/9"
            }
            BoardSize.HARD -> {
            tvMoves.text = "HARD: 6 * 4"
            tvNumPairs.text= "PAIRS: 0/12"
            }
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_nill))
        // initializing the memoryGame class
        memoryGame = MemoryGame(boardSize)

        // Setting the RecyclerView
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards,
            object : MemoryBoardAdapter.CardClickedListener {
                override fun onCardClicked(position: Int) {
                    updateGameWithFlip(position)
                }

            } )
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())

    }

    private fun updateGameWithFlip(position: Int) {
        if (memoryGame.isWon()){
            Snackbar.make(parentLayout, "You've Won Already!!!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isAlreadyFaceUp(position)){
            Snackbar.make(parentLayout, "Invalid Move....", Snackbar.LENGTH_SHORT).show()
            return
        }
       if  (memoryGame.flipCard(position)){
           Log.i(TAG, "Found a Match! Numpairs found ${memoryGame.numPairsFound}")
           val color = ArgbEvaluator().evaluate(
               memoryGame.numPairsFound.toFloat()/boardSize.getNumPairs(),
               ContextCompat.getColor(this, R.color.color_progress_nill),
               ContextCompat.getColor(this, R.color.color_progress_full)
           ) as Int
           tvNumPairs.setTextColor(color)
           tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound}/ ${boardSize.getNumPairs()}"
           if (memoryGame.isWon()){
               Snackbar.make(parentLayout, "CONGRATULATIONS!!! You've Won", Snackbar.LENGTH_LONG).show()
           }
       }
        tvMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }

}
