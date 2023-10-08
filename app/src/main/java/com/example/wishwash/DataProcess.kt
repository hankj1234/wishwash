package com.example.wishwash

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.PriorityQueue
import kotlin.math.max
import kotlin.math.min

class DataProcess(val context: Context) {

    lateinit var classes: Array<String>

    companion object {
        const val BATCH_SIZE = 1
        const val INPUT_SIZE = 640
        const val PIXEL_SIZE = 3
        const val FILE_NAME = "wishwash.onnx"
        const val LABEL_NAME = "yolov8n.txt"
    }
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
    fun imageToBitmap(image: ImageProxy): Bitmap {
        val yuvImage = YuvImage(
            image.planes[0].buffer.toByteArray(),
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val yuvBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(yuvBytes, 0, yuvBytes.size)
    }

    fun bitmapToFloatBuffer(bitmap: Bitmap): FloatBuffer {
        val imageSTD = 255.0f

        var processedBitmap = bitmap
        if (bitmap.width != INPUT_SIZE || bitmap.height != INPUT_SIZE) {
            processedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        }

        val buffer = FloatBuffer.allocate(BATCH_SIZE * PIXEL_SIZE * INPUT_SIZE * INPUT_SIZE)
        val area = INPUT_SIZE * INPUT_SIZE
        val bitmapData = IntArray(area)
        processedBitmap.getPixels(bitmapData, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val idx = INPUT_SIZE * i + j
                val pixelValue = bitmapData[idx]
                buffer.put(idx, ((pixelValue shr 16 and 0xff) / imageSTD))
                buffer.put(idx + area, ((pixelValue shr 8 and 0xff) / imageSTD))
                buffer.put(idx + area * 2, ((pixelValue and 0xff) / imageSTD))
            }
        }

        buffer.rewind()
        return buffer
    }

    fun loadModel() {
        try {
            val assetManager = context.assets
            val outputFile = File(context.filesDir, FILE_NAME)

            assetManager.open(FILE_NAME).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: Exception) {
            // Handle the error or inform the user
            e.printStackTrace()
        }
    }

    fun loadLabel() {
        try {
            BufferedReader(InputStreamReader(context.assets.open(LABEL_NAME))).use { reader ->
                val classList = ArrayList<String>()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    classList.add(line!!)
                }
                classes = classList.toTypedArray()
            }
        } catch (e: Exception) {
            // Handle the error or inform the user
            e.printStackTrace()
        }
    }

    fun outputsToNPMSPredictions(outputs: Array<*>): ArrayList<Result> {
        val confidenceThreshold = 0.45f
        val results = ArrayList<Result>()
        val rows: Int
        val cols: Int

        (outputs[0] as Array<*>).also {
            rows = it.size
            cols = (it[0] as FloatArray).size
        }

        //배열의 형태를 [84 8400] -> [8400 84] 로 변환
        val output = Array(cols) { FloatArray(rows) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                output[j][i] = ((((outputs[0]) as Array<*>)[i]) as FloatArray)[j]
            }
        }

        for (i in 0 until cols) {
            var detectionClass: Int = -1
            var maxScore = 0f
            val classArray = FloatArray(classes.size)
            // label 만 따로 빼서 1차원 배열을 만든다.(0~3은 좌표값임)
            System.arraycopy(output[i], 4, classArray, 0, classes.size)
            // label 중에서 가장 큰 값을 선정한다.
            for (j in classes.indices) {
                if (classArray[j] > maxScore) {
                    detectionClass = j
                    maxScore = classArray[j]
                }
            }

            //만약 80개의 coco dataset 중 가장 큰 확률값이 특정값을 (현재는 45% 확률) 넘어서면 해당 값을 저장한다.
            if (maxScore > confidenceThreshold) {
                val xPos = output[i][0]
                val yPos = output[i][1]
                val width = output[i][2]
                val height = output[i][3]
                //사각형은 화면 밖으로 나갈 수 없으니 화면을 넘기면 최대 화면 값을 가지게 한다.
                val rectF = RectF(
                    max(0f, xPos - width / 2f),
                    max(0f, yPos - height / 2f),
                    min(INPUT_SIZE - 1f, xPos + width / 2f),
                    min(INPUT_SIZE - 1f, yPos + height / 2f)
                )
                val result = Result(detectionClass, maxScore, rectF)
                results.add(result)
            }
        }
        return nms(results)
    }

    private fun nms(results: ArrayList<Result>): ArrayList<Result> {
        val list = ArrayList<Result>()

        for (i in classes.indices) {
            //1.클래스 (라벨들) 중에서 가장 높은 확률값을 가졌던 클래스 찾기
            val pq = PriorityQueue<Result>(50) { o1, o2 ->
                o1.score.compareTo(o2.score)
            }
            val classResults = results.filter { it.classIndex == i }
            pq.addAll(classResults)

            //NMS 처리
            while (pq.isNotEmpty()) {
                // 큐 안에 속한 최대 확률값을 가진 class 저장
                val detections = pq.toTypedArray()
                val max = detections[0]
                list.add(max)
                pq.clear()

                // 교집합 비율 확인하고 50%넘기면 제거
                for (k in 1 until detections.size) {
                    val detection = detections[k]
                    val rectF = detection.rectF
                    val iouThresh = 0.5f
                    if (boxIOU(max.rectF, rectF) < iouThresh) {
                        pq.add(detection)
                    }
                }
            }
        }
        return list
    }

    // 겹치는 비율 (교집합/합집합)
    private fun boxIOU(a: RectF, b: RectF): Float {
        return boxIntersection(a, b) / boxUnion(a, b)
    }

    //교집합
    private fun boxIntersection(a: RectF, b: RectF): Float {
        // x1, x2 == 각 rect 객체의 중심 x or y값, w1, w2 == 각 rect 객체의 넓이 or 높이
        val w = overlap(
            (a.left + a.right) / 2f, a.right - a.left,
            (b.left + b.right) / 2f, b.right - b.left
        )
        val h = overlap(
            (a.top + a.bottom) / 2f, a.bottom - a.top,
            (b.top + b.bottom) / 2f, b.bottom - b.top
        )

        return if (w < 0 || h < 0) 0f else w * h
    }

    //합집합
    private fun boxUnion(a: RectF, b: RectF): Float {
        val i: Float = boxIntersection(a, b)
        return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i
    }

    //서로 겹치는 부분의 길이
    private fun overlap(x1: Float, w1: Float, x2: Float, w2: Float): Float {
        val l1 = x1 - w1 / 2
        val l2 = x2 - w2 / 2
        val left = max(l1, l2)
        val r1 = x1 + w1 / 2
        val r2 = x2 + w2 / 2
        val right = min(r1, r2)
        return right - left
    }
}