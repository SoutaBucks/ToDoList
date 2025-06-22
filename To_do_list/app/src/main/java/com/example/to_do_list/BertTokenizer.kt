package com.example.to_do_list

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.BufferedReader
import java.io.InputStreamReader

private const val MAX_LEN = 128 // 모델의 입력 길이에 맞춰야 합니다.

/**
 * 간단한 BERT 스타일 토크나이저
 * vocab.txt 파일을 읽어 단어를 ID로 변환하고, 패딩을 추가합니다.
 */
class BertTokenizer(private val assets: AssetManager) {
    private val vocab: MutableMap<String, Int> = mutableMapOf()
    private val unknownToken = "[UNK]"

    init {
        loadVocab()
    }

    private fun loadVocab() {
        BufferedReader(InputStreamReader(assets.open("vocab.txt"))).useLines { lines ->
            lines.forEachIndexed { index, line ->
                vocab[line.trim()] = index
            }
        }
        Log.d("BertTokenizer", "Loaded vocab with ${vocab.size} tokens")
        Log.d("BertTokenizer", "Unknown token ID: ${vocab[unknownToken]}")
    }

    fun encode(text: String): IntArray {
        // 1. 텍스트 전처리
        val preprocessedText = text.lowercase().trim()
        
        // 2. 안전한 토큰 ID 가져오기
        val unknownId = vocab[unknownToken] ?: 1
        val padId = 0
        
        // 3. 텍스트를 문자 단위로 분리하고 안전하게 ID 변환
        val tokenIds = mutableListOf<Int>()
        
        for (char in preprocessedText) {
            val charStr = char.toString()
            val tokenId = vocab[charStr] ?: unknownId
            
            // vocab 크기를 벗어나지 않도록 안전장치 (더 엄격하게)
            val maxVocabId = 1000 // 안전한 최대 vocab ID로 제한
            if (tokenId < maxVocabId) {
                tokenIds.add(tokenId)
            } else {
                Log.w("BertTokenizer", "Token ID $tokenId for '$charStr' exceeds safe limit, using UNK")
                tokenIds.add(unknownId)
            }
        }

        // 4. 고정 길이(MAX_LEN)로 패딩/자르기
        val paddedIds = tokenIds.toMutableList()
        while (paddedIds.size < MAX_LEN) {
            paddedIds.add(padId)
        }

        val result = paddedIds.take(MAX_LEN).toIntArray()
        Log.d("BertTokenizer", "Encoded '$preprocessedText' -> ${result.take(10).joinToString()}")
        return result
    }
} 