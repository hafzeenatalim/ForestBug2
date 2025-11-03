package com.games.forestbug.vkhgm

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val berries = mutableListOf<Berry>()
    private var bugX = 0f
    private var bugY = 0f
    private val bugSize = 200f
    private val bugDisplaySize = 280f
    
    private var currentLevel = 1
    private var score = 0
    private var berriesCaught = 0
    private var baseSpeed = 5f
    private var currentSpeed = baseSpeed
    private var gameRunning = false
    private var isGameOver = false
    private var isPaused = false
    
    private var timeToNextLevel = 0
    private var isShowingLevelTimer = false
    
    private val bugDrawables = listOf(
        R.drawable.bug_level_1,
        R.drawable.bug_level_2,
        R.drawable.bug_level_3,
        R.drawable.bug_level_4,
        R.drawable.bug_level_5
    )
    
    private val berryDrawables = listOf(
        R.drawable.berry_level_1,
        R.drawable.berry_level_2,
        R.drawable.berry_level_3,
        R.drawable.berry_level_4,
        R.drawable.berry_level_5
    )
    
    private val backgroundDrawables = listOf(
        R.drawable.background_level_1,
        R.drawable.background_level_2,
        R.drawable.background_level_3,
        R.drawable.background_level_4,
        R.drawable.background_level_5
    )
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(5f, 2f, 2f, Color.BLACK)
    }
    
    private val smallTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(5f, 2f, 2f, Color.BLACK)
    }
    
    private var berrySpawnCounter = 0
    private val berrySpawnRate = 60
    
    var onScoreChanged: ((Int) -> Unit)? = null
    var onLevelChanged: ((Int) -> Unit)? = null
    var onGameOver: (() -> Unit)? = null
    var onNewLevelUnlocked: ((Int) -> Unit)? = null
    
    private var startLevel = 1
    
    init {
        post {
            bugX = width / 2f - bugDisplaySize / 2f
            bugY = height - bugDisplaySize - 100f
        }
    }
    
    fun setStartLevel(level: Int) {
        startLevel = level.coerceIn(1, 5)
    }
    
    fun startGame() {
        gameRunning = true
        isGameOver = false
        isPaused = false
        currentLevel = startLevel
        score = when(startLevel) {
            1 -> 0
            2 -> 1000
            3 -> 2000
            4 -> 3500
            5 -> 5000
            else -> 0
        }
        berriesCaught = when(startLevel) {
            1 -> 0
            2 -> 100
            3 -> 200
            4 -> 350
            5 -> 500
            else -> 0
        }
        baseSpeed = 5f
        currentSpeed = baseSpeed + (berriesCaught / 25) * 0.8f
        berries.clear()
        bugX = width / 2f - bugDisplaySize / 2f
        bugY = height - bugDisplaySize - 100f
        
        onScoreChanged?.invoke(score)
        onLevelChanged?.invoke(currentLevel)
        
        gameLoop()
    }
    
    fun pauseGame() {
        gameRunning = false
        isPaused = true
    }
    
    fun resumeGame() {
        if (!isGameOver) {
            gameRunning = true
            isPaused = false
            gameLoop()
        }
    }
    
    fun isGameRunning(): Boolean {
        return gameRunning
    }
    
    private fun gameLoop() {
        if (!gameRunning) return
        
        updateGame()
        invalidate()
        postDelayed({ gameLoop() }, 16)
    }
    
    private fun updateGame() {
        if (isShowingLevelTimer) {
            timeToNextLevel--
            if (timeToNextLevel <= 0) {
                isShowingLevelTimer = false
            }
        }
        
        berrySpawnCounter++
        if (berrySpawnCounter >= berrySpawnRate / (currentLevel * 0.5f + 1)) {
            berrySpawnCounter = 0
            spawnBerry()
        }
        
        val iterator = berries.iterator()
        while (iterator.hasNext()) {
            val berry = iterator.next()
            berry.y += currentSpeed
            
            if (checkCollision(berry)) {
                iterator.remove()
                onBerryCaught()
            }
            else if (berry.y > height) {
                iterator.remove()
                endGame()
            }
        }
    }
    
    private fun spawnBerry() {
        val berrySize = 160f
        val x = Random.nextFloat() * (width - berrySize)
        berries.add(Berry(x, -berrySize, berrySize))
    }
    
    private fun checkCollision(berry: Berry): Boolean {
        val berryRect = RectF(berry.x, berry.y, berry.x + berry.size, berry.y + berry.size)
        val bugRect = RectF(bugX, bugY, bugX + bugSize, bugY + bugSize)
        return RectF.intersects(berryRect, bugRect)
    }
    
    private fun onBerryCaught() {
        score += 10
        berriesCaught++
        onScoreChanged?.invoke(score)
        
        if (berriesCaught % 25 == 0) {
            currentSpeed += 0.8f
        }
        
        val newLevel = when {
            score >= 5000 -> 5
            score >= 3500 -> 4
            score >= 2000 -> 3
            score >= 1000 -> 2
            else -> 1
        }
        
        if (newLevel > currentLevel) {
            isShowingLevelTimer = true
            timeToNextLevel = 120
            currentLevel = newLevel
            onLevelChanged?.invoke(currentLevel)
            
            val isNewlyUnlocked = checkAndSaveProgress(currentLevel)
            if (isNewlyUnlocked) {
                onNewLevelUnlocked?.invoke(currentLevel)
            }
        }
    }
    
    private fun checkAndSaveProgress(level: Int): Boolean {
        val prefs = context.getSharedPreferences("GameProgress", android.content.Context.MODE_PRIVATE)
        val currentMax = prefs.getInt("maxUnlockedLevel", 1)
        
        if (level > currentMax) {
            prefs.edit().putInt("maxUnlockedLevel", level).apply()
            return true
        }
        return false
    }
    
    private fun endGame() {
        gameRunning = false
        isGameOver = true
        onGameOver?.invoke()
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val background = context.getDrawable(backgroundDrawables[currentLevel - 1])
        background?.setBounds(0, 0, width, height)
        background?.draw(canvas)
        
        if (!gameRunning && berries.isEmpty() && !isGameOver && !isPaused) {
            return
        }
        
        val bugDrawable = context.getDrawable(bugDrawables[currentLevel - 1])
        val offsetX = (bugDisplaySize - bugSize) / 2f
        val offsetY = (bugDisplaySize - bugSize) / 2f
        bugDrawable?.setBounds(
            (bugX - offsetX).toInt(),
            (bugY - offsetY).toInt(),
            (bugX + bugSize + offsetX).toInt(),
            (bugY + bugSize + offsetY).toInt()
        )
        bugDrawable?.draw(canvas)
        
        val berryDrawable = context.getDrawable(berryDrawables[currentLevel - 1])
        for (berry in berries) {
            berryDrawable?.setBounds(
                berry.x.toInt(),
                berry.y.toInt(),
                (berry.x + berry.size).toInt(),
                (berry.y + berry.size).toInt()
            )
            berryDrawable?.draw(canvas)
        }
        
        canvas.drawText("Score: $score", 30f, 80f, textPaint)
        canvas.drawText("Level: $currentLevel", 30f, 150f, smallTextPaint)
        canvas.drawText("Speed: ${String.format("%.1f", currentSpeed)}", 30f, 210f, smallTextPaint)
        
        val nextLevelScore = when(currentLevel) {
            1 -> 1000
            2 -> 2000
            3 -> 3500
            4 -> 5000
            else -> 0
        }
        
        if (currentLevel < 5) {
            val pointsNeeded = nextLevelScore - score
            canvas.drawText("Next level: $pointsNeeded pts", 30f, 270f, smallTextPaint)
        } else {
            canvas.drawText("Max level!", 30f, 270f, smallTextPaint)
        }
        
        if (isShowingLevelTimer) {
            val levelText = "LEVEL $currentLevel"
            
            val bigTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 120f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }
            
            val levelWidth = bigTextPaint.measureText(levelText)
            
            val bgPaint = Paint().apply {
                color = Color.parseColor("#CC000000")
                style = Paint.Style.FILL
            }
            canvas.drawRect(
                width / 2f - levelWidth / 2f - 60f,
                height / 2f - 100f,
                width / 2f + levelWidth / 2f + 60f,
                height / 2f + 40f,
                bgPaint
            )
            
            canvas.drawText(levelText, width / 2f - levelWidth / 2f, height / 2f, bigTextPaint)
        }
        
        if (isGameOver) {
            val gameOverText = "GAME OVER"
            val textWidth = textPaint.measureText(gameOverText)
            canvas.drawText(gameOverText, width / 2f - textWidth / 2f, height / 2f - 150f, textPaint)
            
            val scoreText = "Score: $score"
            val scoreWidth = smallTextPaint.measureText(scoreText)
            canvas.drawText(scoreText, width / 2f - scoreWidth / 2f, height / 2f - 50f, smallTextPaint)
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (gameRunning && !isGameOver) {
                    bugX = (event.x - bugSize / 2f).coerceIn(0f, width - bugSize)
                    val offsetX = (bugDisplaySize - bugSize) / 2f
                    bugX = bugX.coerceIn(offsetX, width - bugSize - offsetX)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    data class Berry(
        var x: Float,
        var y: Float,
        val size: Float
    )
}

