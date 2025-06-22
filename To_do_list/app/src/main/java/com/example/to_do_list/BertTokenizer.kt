package com.example.to_do_list

import android.content.Context
import android.content.res.AssetManager
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
    }

    fun encode(text: String): IntArray {
        // 1. 텍스트를 단어로 분리 (간단한 공백 기준)
        val tokens = text.split(" ")

        // 2. 각 단어를 ID로 변환
        val tokenIds = tokens.map { vocab[it] ?: vocab[unknownToken]!! }

        // 3. 고정 길이(MAX_LEN)로 패딩/자르기
        val paddedIds = tokenIds.toMutableList()
        while (paddedIds.size < MAX_LEN) {
            paddedIds.add(0) // 0번 ID로 패딩
        }

        return paddedIds.take(MAX_LEN).toIntArray()
    }
} 