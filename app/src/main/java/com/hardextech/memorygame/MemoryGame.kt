package com.hardextech.memorygame



class MemoryGame(private  val  boardSize: BoardSize) {


    val  cards: List<MemoryCards>
    var numPairsFound = 0
    private  var numCardFlip = 0
    private var indexOfSinglrSelectedCard: Int? = null

    init {
        val choosenImage = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomizedImages = (choosenImage + choosenImage).shuffled()
       cards =randomizedImages.map { MemoryCards(it) }
         }

    fun flipCard(position: Int): Boolean {
        numCardFlip++
        var foundMatched = false
        if (indexOfSinglrSelectedCard==null){
            restoredCards()
            indexOfSinglrSelectedCard=position
        } else{
            // The situation where exactly one card isFaceUp
            foundMatched = checkForMatch(indexOfSinglrSelectedCard!!, position)
            indexOfSinglrSelectedCard=null
        }
        val card = cards[position]
        card.isFaceUp=!card.isFaceUp
        return foundMatched
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if ((cards[position1].identifier) != cards[position2].identifier){
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }

    private fun restoredCards() {
        for (displayAllCard in cards){
            if (!displayAllCard.isMatched){
                displayAllCard.isFaceUp = false
            }

        }
    }

    fun isWon(): Boolean {

        return numPairsFound == boardSize.getNumPairs()
    }

    fun isAlreadyFaceUp(position: Int): Boolean {

        return  cards[position].isFaceUp

    }

    fun getNumMoves(): Int {
        return  numCardFlip/2
    }


}