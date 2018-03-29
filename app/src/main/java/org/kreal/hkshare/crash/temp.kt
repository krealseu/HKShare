package org.kreal.hkshare.crash


///**
// * Created by lthee on 2017/10/14.
// */
//HttpServerRequestCallback { request, response ->
//    val file = NativeHttpFile(File("/sdcard/123.mp4"), "/file")
//    if (!file.isFile) {
//        response.code(404).send("not found")
//        return@HttpServerRequestCallback
//    }
//    val requestHeader = request.headers
//    val responseHeader = mutableMapOf<String, String>()
//    val reponseStatus: NanoHTTPD.Response.Status
//    // deal with ETag
//    val IfNoneMatch = requestHeader?.get("If-None-Match".toLowerCase())
//    val fileETage = file.eTag
//    responseHeader.put("ETag", fileETage)
//    if (IfNoneMatch != null) {
//        if (IfNoneMatch.contentEquals(fileETage)) {
//            response.code(304)
//            return@HttpServerRequestCallback
//        }
//    }
//    // detail range
//    val range = requestHeader?.get("Range".toLowerCase())
//    val fSize = file.length()
//    val pos = LongArray(2)
//    pos[0] = 0
//    pos[1] = fSize - 1L
//    if (range != null) {
//        var isstandard = true
//        //初始化Range的个参数，并判断是否正规
//        if (range.matches("bytes=\\d*-\\d*$".toRegex())) {
//            val tmp = range.replace("bytes=", "")
//            val posStr = tmp.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
//            if (posStr.size == 1)
//                if (tmp.startsWith("-")) {
//                    pos[0] = fSize - java.lang.Long.parseLong(posStr[0])
//                    pos[1] = fSize - 1L
//                } else {
//                    pos[0] = java.lang.Long.parseLong(posStr[0])
//                    pos[1] = fSize - 1L
//                }
//            else if (posStr.size.toLong() == 2L) {
//                for (i in posStr.indices)
//                    pos[i] = java.lang.Long.parseLong(posStr[i])
//            } else
//                pos[0] = -1L
//            if (pos[0] < 0 || pos[0] > pos[1] || pos[1] > fSize)
//                isstandard = false
//        } else {
//            isstandard = false
//        }
//        //对是否正规进行判断
//        if (!isstandard) {
//            response.code(416)
//            response.headers.add("Content-Type", file.getMimeType())
//            return@HttpServerRequestCallback
//        }
//        //deal with If Match
//        val ifMatch = requestHeader?.get("If-Match".toLowerCase())
//        if (ifMatch != null) {
//            if (!ifMatch!!.matches(fileETage.toRegex())) {
//                response.code(412)
//                response.headers.add("Content-Type", file.getMimeType())
//                return@HttpServerRequestCallback
//            }
//        }
//        //detail with if Range
//        val ifRange = requestHeader?.get("If-Range".toLowerCase())
//        if (ifRange != null) {
//            if (!ifRange!!.matches(fileETage.toRegex())) {
//                pos[0] = 0
//                pos[1] = fSize - 1L
//            }
//        }
//        reponseStatus = NanoHTTPD.Response.Status.PARTIAL_CONTENT
//        response.code(206)
//    } else {
//        response.code(200)
//        reponseStatus = NanoHTTPD.Response.Status.OK
//    }
//    val contentRange = String.format("bytes %d-%d/%d", pos[0], pos[1], fSize)
//    Log.i("asdf", contentRange)
//    responseHeader.put("Content-Range", contentRange)
//    responseHeader.put("Content-Length", (pos[1] - pos[0] + 1L).toString())
//
//    //deal heard
//    responseHeader.put("Accept-Ranges", "bytes")
//    responseHeader.put("Content-Type", file.getMimeType())
//    val date = SimpleDateFormat()
//    responseHeader.put("Last-Modified", file.lastModified().toString())
//
//    //处理响应体
//    val inputstream = file.getInputStream(0)
//
//    for ((key, value) in responseHeader) {
//        response.headers.add(key, value)
////                Log.i("asdf",value)
//    }
//    Log.i("asdf", "start")
////            response.writeHead()
//    Util.pump(inputstream, pos[1] - pos[0] + 1L, response, CompletedCallback { })
////            response.sendStream(inputstream,file.length())
////
////            response.onCompleted(Exception())
////            val response = HKHttpServive.HKResponse(reponseStatus, file.getMimeType(), inputstream, pos[1] - pos[0] + 1L)
//
//}