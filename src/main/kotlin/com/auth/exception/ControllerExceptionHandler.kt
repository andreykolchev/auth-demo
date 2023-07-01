package com.auth.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ControllerExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    fun error(ex: Exception): ResponseEntity<String?>? {
        return when (ex) {
            is ErrorException -> ResponseEntity(ex.localizedMessage, HttpStatus.BAD_REQUEST)
            is RuntimeException -> ResponseEntity(ex.localizedMessage, HttpStatus.BAD_REQUEST)
            else -> ResponseEntity(ex.localizedMessage, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}