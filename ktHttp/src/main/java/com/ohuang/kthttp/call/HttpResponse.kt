package com.ohuang.kthttp.call

import okhttp3.Headers
import okhttp3.Response
import okhttp3.ResponseBody

class HttpResponse<T>(val rawResponse: Response, val body: T?, val errorBody: ResponseBody?) {

    /** The raw response from the HTTP client.  */
    fun raw(): Response {
        return rawResponse
    }

    /** HTTP status code.  */
    fun code(): Int {
        return rawResponse.code
    }

    /** HTTP status message or null if unknown.  */
    fun message(): String {
        return rawResponse.message
    }

    /** HTTP headers.  */
    fun headers(): Headers {
        return rawResponse.headers
    }

    /** Returns true if [.code] is in the range [200..300).  */
    fun isSuccessful(): Boolean {
        return rawResponse.isSuccessful
    }

    override fun toString(): String {
        return rawResponse.toString()
    }
}