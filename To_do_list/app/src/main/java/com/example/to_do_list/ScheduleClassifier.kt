package com.example.to_do_list

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

class ScheduleClassifier(ctx: Context) {
    private val interpreter: Interpreter
    private val tokenizer: BertTokenizer
    private val labels = listOf("기타", "금융", "만남", "운동", "업무", "중요", "학업")

    init {
        val options = Interpreter.Options().apply {
            numThreads = 2
            // 필요시 NNAPI 위임 사용
            // setUseNNAPI(true)
        }
        val model = FileUtil.loadMappedFile(ctx, "schedule_cls.tflite")
        interpreter = Interpreter(model, options)
        tokenizer = BertTokenizer(ctx.assets)
    }

    fun classify(text: String): String {
        // 1. 텍스트를 토큰 ID 배열로 변환
        val tokenIds = tokenizer.encode(text)

        // 2. 모델 입력 형식에 맞게 2D 배열로 변경 (INT32로 유지)
        val inputArray = arrayOf(tokenIds)

        // 3. 모델 출력 형식 준비
        val outputArray = Array(1) { FloatArray(labels.size) }

        // 4. 추론 실행
        interpreter.run(inputArray, outputArray)

        // 5. 가장 높은 확률을 가진 인덱스 찾기
        val bestIndex = outputArray[0].indices.maxByOrNull { outputArray[0][it] } ?: 0

        return labels[bestIndex]
    }
} 