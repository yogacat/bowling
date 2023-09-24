package com.genios.bowling.controller;

import com.genios.bowling.exception.NoFreeLinesException;
import com.genios.bowling.exception.roll.RollAlreadyExistsException;
import com.genios.bowling.record.response.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Converts exceptions from the application into response entity.
 */
@ControllerAdvice
public class ExceptionHandlingController {

    /**
     * Converts validation exceptions into response entity.
     *
     * @param ex {@link MethodArgumentNotValidException} validation exception
     * @return ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(NoFreeLinesException.class)
    public ResponseEntity<ErrorMessage> handleNoFreeLinesException(NoFreeLinesException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(RollAlreadyExistsException.class)
    public ResponseEntity<ErrorMessage> handleDuplicatedRollException(RollAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(ex.getMessage()));
    }
}
