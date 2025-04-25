package io.dmitrysulman.logback.access.reactor.netty

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import reactor.netty.http.server.logging.AccessLogArgProvider
import java.io.PrintWriter
import java.util.*

class HttpServletResponseAdapter(
    private val argProvider: AccessLogArgProvider
) : HttpServletResponse {
    override fun getCharacterEncoding(): String {
        TODO("Not yet implemented")
    }

    override fun getContentType(): String {
        TODO("Not yet implemented")
    }

    override fun getOutputStream(): ServletOutputStream {
        TODO("Not yet implemented")
    }

    override fun getWriter(): PrintWriter {
        TODO("Not yet implemented")
    }

    override fun setCharacterEncoding(charset: String?) {
        TODO("Not yet implemented")
    }

    override fun setContentLength(len: Int) {
        TODO("Not yet implemented")
    }

    override fun setContentLengthLong(len: Long) {
        TODO("Not yet implemented")
    }

    override fun setContentType(type: String?) {
        TODO("Not yet implemented")
    }

    override fun setBufferSize(size: Int) {
        TODO("Not yet implemented")
    }

    override fun getBufferSize(): Int {
        TODO("Not yet implemented")
    }

    override fun flushBuffer() {
        TODO("Not yet implemented")
    }

    override fun resetBuffer() {
        TODO("Not yet implemented")
    }

    override fun isCommitted(): Boolean {
        TODO("Not yet implemented")
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun setLocale(loc: Locale?) {
        TODO("Not yet implemented")
    }

    override fun getLocale(): Locale {
        TODO("Not yet implemented")
    }

    override fun addCookie(cookie: Cookie?) {
        TODO("Not yet implemented")
    }

    override fun containsHeader(name: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun encodeURL(url: String?): String {
        TODO("Not yet implemented")
    }

    override fun encodeRedirectURL(url: String?): String {
        TODO("Not yet implemented")
    }

    override fun sendError(sc: Int, msg: String?) {
        TODO("Not yet implemented")
    }

    override fun sendError(sc: Int) {
        TODO("Not yet implemented")
    }

    override fun sendRedirect(location: String?) {
        TODO("Not yet implemented")
    }

    override fun setDateHeader(name: String?, date: Long) {
        TODO("Not yet implemented")
    }

    override fun addDateHeader(name: String?, date: Long) {
        TODO("Not yet implemented")
    }

    override fun setHeader(name: String?, value: String?) {
        TODO("Not yet implemented")
    }

    override fun addHeader(name: String?, value: String?) {
        TODO("Not yet implemented")
    }

    override fun setIntHeader(name: String?, value: Int) {
        TODO("Not yet implemented")
    }

    override fun addIntHeader(name: String?, value: Int) {
        TODO("Not yet implemented")
    }

    override fun setStatus(sc: Int) {
        TODO("Not yet implemented")
    }

    override fun getStatus(): Int {
        TODO("Not yet implemented")
    }

    override fun getHeader(name: String?): String {
        TODO("Not yet implemented")
    }

    override fun getHeaders(name: String?): MutableCollection<String> {
        TODO("Not yet implemented")
    }

    override fun getHeaderNames(): MutableCollection<String> {
        TODO("Not yet implemented")
    }
}