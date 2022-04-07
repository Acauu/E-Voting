package com.skripsi.voting.FaceDetection

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.util.*

import com.skripsi.voting.FaceRecognition.FaceNet.*



class MTCNN(private val assetManager: AssetManager) {
    //paramater
    private val factor = 0.709f
    private val PNetThreshold = 0.6f
    private val RNetThreshold = 0.7f
    private val ONetThreshold = 0.7f

    //waktu kerja androidnya
    // waktu proses terakhir gambarnya dalam ms
    var lastProcessTime: Long = 0
    private var inferenceInterface: TensorFlowInferenceInterface? = null
    fun close() {
        inferenceInterface!!.close()
    }

    private fun loadModel(): Boolean {
        //AssetManager
        try {
            inferenceInterface = TensorFlowInferenceInterface(assetManager, MODEL_FILE)
            Log.d("Facenet", "[*]load model success")
        } catch (e: Exception) {
            Log.e("Facenet", "[*]load model failed$e")
            return false
        }
        return true
    }
    // data akan dibaca nilai piksel bitmapnya , preporses (-127.5/128) dan dikonersi ke one-dimesional array

    private fun normalizeImage(bitmap: Bitmap): FloatArray {
        val w = bitmap.width
        val h = bitmap.height
        val floatValues = FloatArray(w * h * 3)
        val intValues = IntArray(w * h)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val imageMean = 127.5f
        val imageStd = 128f
        for (i in intValues.indices) {
            val `val` = intValues[i]
            floatValues[i * 3 + 0] = ((`val` shr 16 and 0xFF) - imageMean) / imageStd
            floatValues[i * 3 + 1] = ((`val` shr 8 and 0xFF) - imageMean) / imageStd
            floatValues[i * 3 + 2] = ((`val` and 0xFF) - imageMean) / imageStd
        }
        return floatValues
    }



//       deteksi wajah,minSize adalah nilai piksel wajah terkecil

    private fun bitmapResize(bm: Bitmap, scale: Float): Bitmap {
        val width = bm.width
        val height = bm.height
        // create a matrix for the manipulation。matrixnya akan menspesifikasikan perubahan bitmap
        val matrix = Matrix()
        // resize bitmapnya
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, true
        )
    }

    //to be flipped before input, and output to be flipped too
    private fun PNetForward(
        bitmap: Bitmap,
        PNetOutProb: Array<FloatArray>,
        PNetOutBias: Array<Array<FloatArray>>
    ): Int {
        val w = bitmap.width
        val h = bitmap.height
        val PNetIn = normalizeImage(bitmap)
        Utils.flip_diag(PNetIn, h, w, 3) //flip diagonal
        inferenceInterface!!.feed(PNetInName, PNetIn, 1, w.toLong(), h.toLong(), 3)
        inferenceInterface!!.run(PNetOutName, false)
        val PNetOutSizeW = Math.ceil(w * 0.5 - 5).toInt()
        val PNetOutSizeH = Math.ceil(h * 0.5 - 5).toInt()
        val PNetOutP = FloatArray(PNetOutSizeW * PNetOutSizeH * 2)
        val PNetOutB = FloatArray(PNetOutSizeW * PNetOutSizeH * 4)
        inferenceInterface!!.fetch(PNetOutName[0], PNetOutP)
        inferenceInterface!!.fetch(PNetOutName[1], PNetOutB)
        //konversi ke 2/3 dimensional array
        Utils.flip_diag(PNetOutP, PNetOutSizeW, PNetOutSizeH, 2)
        Utils.flip_diag(PNetOutB, PNetOutSizeW, PNetOutSizeH, 4)
        Utils.expand(PNetOutB, PNetOutBias)
        Utils.expandProb(PNetOutP, PNetOutProb)

//        percepat 3 ms
//        for (int y=0;y<PNetOutSizeH;y++)
//            for (int x=0;x<PNetOutSizeW;x++){
//               int idx=PNetOutSizeH*x+y;
//               PNetOutProb[y][x]=PNetOutP[idx*2+1];
//               for(int i=0;i<4;i++)
//                   PNetOutBias[y][x][i]=PNetOutB[idx*4+i];
//            }

        return 0
    }

    //Non-Maximum Suppression
    //nms，hapus bounding box yang tidak sesuai
    private fun nms(boxes: Vector<Box>, threshold: Float, method: String) {
        //NMS.pairwise comparison
        //int delete_cnt=0;
        val cnt = 0
        for (i in boxes.indices) {
            val box = boxes[i]
            if (!box.deleted) {
                //score<0 front rectangle dihilangkan
                for (j in i + 1 until boxes.size) {
                    val box2 = boxes[j]
                    if (!box2.deleted) {
                        val x1 = Math.max(box.box[0], box2.box[0])
                        val y1 = Math.max(box.box[1], box2.box[1])
                        val x2 = Math.min(box.box[2], box2.box[2])
                        val y2 = Math.min(box.box[3], box2.box[3])
                        if (x2 < x1 || y2 < y1) continue
                        val areaIoU = (x2 - x1 + 1) * (y2 - y1 + 1)
                        var iou = 0f
                        if (method == "Union") iou =
                            1.0f * areaIoU / (box.area() + box2.area() - areaIoU) else if (method == "Min") {
                            iou = 1.0f * areaIoU / Math.min(box.area(), box2.area())
                            Log.i(TAG, "[*]iou=$iou")
                        }
                        if (iou >= threshold) { //hapus bounding box dengan probabilitas yang paling kecil
                            if (box.score > box2.score) box2.deleted = true else box.deleted = true
                            //delete_cnt++;
                        }
                    }
                }
            }
        }
        //Log.i(TAG,"[*]sum:"+boxes.size()+" delete:"+delete_cnt);
    }

    private fun generateBoxes(
        prob: Array<FloatArray>,
        bias: Array<Array<FloatArray>>,
        scale: Float,
        threshold: Float,
        boxes: Vector<Box>
    ): Int {
        val h = prob.size
        val w: Int = prob[0].size
        //Log.i(TAG,"[*]height:"+prob.length+" width:"+prob[0].length);
        for (y in 0 until h) for (x in 0 until w) {
            val score = prob[y][x]
            //hanya terima nilai prob >threadshold(0.6 here)
            if (score > threshold) {
                val box = Box()
                //score
                box.score = score
                //box
                box.box[0] = Math.round(x * 2 / scale)
                box.box[1] = Math.round(y * 2 / scale)
                box.box[2] = Math.round((x * 2 + 11) / scale)
                box.box[3] = Math.round((y * 2 + 11) / scale)
                //bbr
                for (i in 0..3) box.bbr[i] = bias[y][x][i]
                //add
                boxes.addElement(box)
            }
        }
        return 0
    }

    private fun BoundingBoxReggression(boxes: Vector<Box>) {
        for (i in boxes.indices) boxes[i].calibrate()
    }

    //Pnet + Bounding Box Regression + Non-Maximum Regression
//    run setelah NMS，lalu bounding box Regression
//     (1) For each scale , use NMS with threshold=0.5
//     (2) For all candidates , use NMS with threshold=0.7
//     (3) Calibrate Bounding Box
//     input image CNN，koordinatnya seperti[0..width,0]。Bitmap akan difold jadi setengah sebelum jaringannya
//     dijalankan, kalau sama hasilnya true.

    private fun PNet(bitmap: Bitmap, minSize: Int): Vector<Box> {
        val whMin = Math.min(bitmap.width, bitmap.height)
        var currentFaceSize =
            minSize.toFloat() //currentFaceSize=minSize/(factor^k) k=0,1,2... sampai whMin
        val totalBoxes = Vector<Box>()
        //gambar Paramid and Feed ke Pnet
        while (currentFaceSize <= whMin) {
            val scale = 12.0f / currentFaceSize
            //(1)Image Resize
            val bm = bitmapResize(bitmap, scale)
            val w = bm.width
            val h = bm.height
            //(2)RUN CNN
            val PNetOutSizeW = (Math.ceil(w * 0.5 - 5) + 0.5).toInt()
            val PNetOutSizeH = (Math.ceil(h * 0.5 - 5) + 0.5).toInt()
            val PNetOutProb = Array(PNetOutSizeH) {
                FloatArray(
                    PNetOutSizeW
                )
            }
            val PNetOutBias = Array(PNetOutSizeH) {
                Array(PNetOutSizeW) {
                    FloatArray(4)
                }
            }
            PNetForward(bm, PNetOutProb, PNetOutBias)
            //(3)Analisa Data
            val curBoxes = Vector<Box>()
            generateBoxes(PNetOutProb, PNetOutBias, scale, PNetThreshold, curBoxes)
            //Log.i(TAG,"[*]CNN Output Box number:"+curBoxes.size()+" Scale:"+scale);
            //(4)nms 0.5
            nms(curBoxes, 0.5f, "Union")
            //(5)add to totalBoxes
            for (i in curBoxes.indices) if (!curBoxes[i].deleted) totalBoxes.addElement(curBoxes[i])
            //Face Size proporsional increase
            currentFaceSize /= factor
        }
        //NMS 0.7
        nms(totalBoxes, 0.7f, "Union")
        //BBR
        BoundingBoxReggression(totalBoxes)
        return Utils.updateBoxes(totalBoxes)
    }


    //kotaknya dicrop (out of bounds juga diproses) dan di- resize sampai ukurannya itu size x size
    // dan disimpan di variabel data bentuk float Array
    var tmp_bm: Bitmap? = null
    private fun crop_and_resize(bitmap: Bitmap, box: Box, size: Int, data: FloatArray) {
        //(2)crop and resize
        val matrix = Matrix()
        val scale = 1.0f * size / box.width()
        matrix.postScale(scale, scale)
        val croped = Bitmap.createBitmap(
            bitmap,
            box.left(),
            box.top(),
            box.width(),
            box.height(),
            matrix,
            true
        )
        //(3)save
        val pixels_buf = IntArray(size * size)
        croped.getPixels(pixels_buf, 0, croped.width, 0, 0, croped.width, croped.height)
        val imageMean = 127.5f
        val imageStd = 128f
        for (i in pixels_buf.indices) {
            val `val` = pixels_buf[i]
            data[i * 3 + 0] = ((`val` shr 16 and 0xFF) - imageMean) / imageStd
            data[i * 3 + 1] = ((`val` shr 8 and 0xFF) - imageMean) / imageStd
            data[i * 3 + 2] = ((`val` and 0xFF) - imageMean) / imageStd
        }
    }


//     RNET run neural networknya lalu skornya dicetak di boxes

    private fun RNetForward(RNetIn: FloatArray, boxes: Vector<Box>) {
        val num = RNetIn.size / 24 / 24 / 3
        //feed & run
        inferenceInterface!!.feed(RNetInName, RNetIn, num.toLong(), 24, 24, 3)
        inferenceInterface!!.run(RNetOutName, false)
        //fetch
        val RNetP = FloatArray(num * 2)
        val RNetB = FloatArray(num * 4)
        inferenceInterface!!.fetch(RNetOutName[0], RNetP)
        inferenceInterface!!.fetch(RNetOutName[1], RNetB)
        //konversi
        for (i in 0 until num) {
            boxes[i].score = RNetP[i * 2 + 1]
            for (j in 0..3) boxes[i].bbr[j] = RNetB[i * 4 + j]
        }
    }

    //Refine Net
    private fun RNet(bitmap: Bitmap, boxes: Vector<Box>): Vector<Box> {
        //RNet Input Init
        val num = boxes.size
        val RNetIn = FloatArray(num * 24 * 24 * 3)
        val curCrop = FloatArray(24 * 24 * 3)
        var RNetInIdx = 0
        for (i in 0 until num) {
            crop_and_resize(bitmap, boxes[i], 24, curCrop)
            Utils.flip_diag(curCrop, 24, 24, 3)
            //Log.i(TAG,"[*]Pixels values:"+curCrop[0]+" "+curCrop[1]);
            for (j in curCrop.indices) RNetIn[RNetInIdx++] = curCrop[j]
        }
        //Run RNet
        RNetForward(RNetIn, boxes)
        //RNetThreshold
        for (i in 0 until num) if (boxes[i].score < RNetThreshold) boxes[i].deleted = true
        //Nms
        nms(boxes, 0.7f, "Union")
        BoundingBoxReggression(boxes)
        return Utils.updateBoxes(boxes)
    }

    /*
     * ONet run neural network dan scor biasnya disimpan dalam boxes
     */
    private fun ONetForward(ONetIn: FloatArray, boxes: Vector<Box>) {
        val num = ONetIn.size / 48 / 48 / 3
        //feed & run
        inferenceInterface!!.feed(ONetInName, ONetIn, num.toLong(), 48, 48, 3)
        inferenceInterface!!.run(ONetOutName, false)
        //fetch
        val ONetP = FloatArray(num * 2) //prob
        val ONetB = FloatArray(num * 4) //bias
        val ONetL = FloatArray(num * 10) //landmark
        inferenceInterface!!.fetch(ONetOutName[0], ONetP)
        inferenceInterface!!.fetch(ONetOutName[1], ONetB)
        inferenceInterface!!.fetch(ONetOutName[2], ONetL)
        //konversi
        for (i in 0 until num) {
            //prob
            boxes[i].score = ONetP[i * 2 + 1]
            //bias
            for (j in 0..3) boxes[i].bbr[j] = ONetB[i * 4 + j]

            //landmark
            for (j in 0..4) {
                val x = boxes[i].left() + (ONetL[i * 10 + j] * boxes[i].width()).toInt()
                val y = boxes[i].top() + (ONetL[i * 10 + j + 5] * boxes[i].height()).toInt()
                boxes[i].landmark[j] = Point(x, y)
                //Log.i(TAG,"[*] landmarkd "+x+ "  "+y);
            }
        }
    }

    //ONet
    private fun ONet(bitmap: Bitmap, boxes: Vector<Box>): Vector<Box> {
        //ONet Input Init
        val num = boxes.size
        val ONetIn = FloatArray(num * 48 * 48 * 3)
        val curCrop = FloatArray(48 * 48 * 3)
        var ONetInIdx = 0
        for (i in 0 until num) {
            crop_and_resize(bitmap, boxes[i], 48, curCrop)
            Utils.flip_diag(curCrop, 48, 48, 3)
            for (j in curCrop.indices) ONetIn[ONetInIdx++] = curCrop[j]
        }
        //Run ONet
        ONetForward(ONetIn, boxes)
        //ONetThreshold
        for (i in 0 until num) if (boxes[i].score < ONetThreshold) boxes[i].deleted = true
        BoundingBoxReggression(boxes)
        //Nms
        nms(boxes, 0.7f, "Min")
        return Utils.updateBoxes(boxes)
    }

    private fun square_limit(boxes: Vector<Box>, w: Int, h: Int) {
        //box untuk deteksinya
        for (i in boxes.indices) {
            boxes[i].toSquareShape()
            boxes[i].limit_square(w, h)
        }
    }


//     paramater：
//     bitmap:gambar yang akan diproses
//     minFaceSize: pixel gambarnya.(semakin besar nilainya, semakin cepat waktu deteksi)
//     return：
//     face frame

    fun detectFaces(bitmap: Bitmap, minFaceSize: Int): Vector<Box> {
        val t_start = System.currentTimeMillis()
        //1. PNet mencari kandidat bounding box
        var boxes = PNet(bitmap, minFaceSize)
        square_limit(boxes, bitmap.width, bitmap.height)
        //2. RNet
        boxes = RNet(bitmap, boxes)
        square_limit(boxes, bitmap.width, bitmap.height)
        //3. ONet
        boxes = ONet(bitmap, boxes)
        //hasilnya adalah bounding box
        Log.i(TAG, "[*]Mtcnn Detection Time:" + (System.currentTimeMillis() - t_start))
        lastProcessTime = System.currentTimeMillis() - t_start
        return boxes
    }

    companion object {
        //MODEL PATH
        private const val MODEL_FILE = "file:///android_asset/mtcnn_freezed_model.pb"

        //tensor name
        private const val PNetInName = "pnet/input:0"
        private val PNetOutName = arrayOf("pnet/prob1:0", "pnet/conv4-2/BiasAdd:0")
        private const val RNetInName = "rnet/input:0"
        private val RNetOutName = arrayOf("rnet/prob1:0", "rnet/conv5-2/conv5-2:0")
        private const val ONetInName = "onet/input:0"
        private val ONetOutName =
            arrayOf("onet/prob1:0", "onet/conv6-2/conv6-2:0", "onet/conv6-3/conv6-3:0")
        private const val TAG = "MTCNN"
    }

    init {
        loadModel()
    }
}