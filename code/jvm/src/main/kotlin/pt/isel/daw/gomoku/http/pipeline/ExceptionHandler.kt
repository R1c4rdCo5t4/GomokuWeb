package pt.isel.daw.gomoku.http.pipeline

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import pt.isel.daw.gomoku.domain.exceptions.GameException
import pt.isel.daw.gomoku.domain.exceptions.LobbyException
import pt.isel.daw.gomoku.domain.exceptions.UserException
import pt.isel.daw.gomoku.http.media.Problem
import pt.isel.daw.gomoku.http.pipeline.authentication.AuthenticationInterceptor.Companion.WWW_AUTHENTICATE_HEADER
import pt.isel.daw.gomoku.http.pipeline.authentication.RequestTokenProcessor.Companion.SCHEME
import pt.isel.daw.gomoku.http.pipeline.logger.Logger.logger
import java.net.URI

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handleValidationException(request: HttpServletRequest, ex: MethodArgumentNotValidException) =
        ex.handle(
            request = request,
            status = HttpStatus.BAD_REQUEST,
            title = "Invalid Argument",
            detail = ex.bindingResult.fieldErrors.firstOrNull()?.let { "${it.field.title()} ${it.defaultMessage}" }
        )

    @ExceptionHandler(value = [MethodArgumentTypeMismatchException::class])
    fun handleTypeMismatchException(request: HttpServletRequest, ex: MethodArgumentTypeMismatchException) =
        ex.handle(
            request = request,
            status = HttpStatus.BAD_REQUEST,
            detail = "Invalid argument for parameter ${ex.name}"
        )

    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    fun handleHttpMessageNotReadableException(request: HttpServletRequest, ex: HttpMessageNotReadableException) =
        ex.handle(
            request = request,
            status = HttpStatus.BAD_REQUEST,
            type = "invalid-request-body",
            title = "Invalid request body",
            detail = when (val cause = ex.rootCause) {
                is MismatchedInputException -> "Missing property '${cause.path.first().fieldName}'"
                is JsonParseException -> "Please check the request body and try again."
                else -> null
            }
        )

    @ExceptionHandler(value = [HttpRequestMethodNotSupportedException::class])
    fun handleHttpRequestMethodNotSupportedException(
        request: HttpServletRequest,
        ex: HttpRequestMethodNotSupportedException
    ) =
        ex.handle(
            request = request,
            status = HttpStatus.METHOD_NOT_ALLOWED,
            type = "method-not-allowed",
            title = "Method Not Allowed",
            detail = "The method ${ex.method} is not allowed for the requested resource."
        )

    @ExceptionHandler(value = [MissingServletRequestParameterException::class])
    fun handleMissingRequestValueException(
        request: HttpServletRequest,
        ex: MissingServletRequestParameterException
    ) =
        ex.handle(
            request = request,
            status = HttpStatus.BAD_REQUEST,
            type = "missing-request-value",
            title = "Missing Request Value",
            detail = "The request is missing the required parameter '${ex.parameterName}'"
        )

    @ExceptionHandler(
        value = [
            IllegalArgumentException::class,
            GameException.GameAlreadyOverException::class,
            GameException.NotPlayerTurnException::class,
            GameException.InvalidMoveException::class,
            GameException.UserNotInGameException::class,
            UserException.InvalidTokenException::class,
            UserException.UserAlreadyExistsException::class,
            LobbyException.UserNotInLobbyException::class,
            LobbyException.UserAlreadyInLobbyException::class,
            LobbyException.UserAlreadyInGameException::class
        ]
    )
    fun handleBadRequest(request: HttpServletRequest, ex: Exception) =
        ex.handle(
            request = request,
            status = HttpStatus.BAD_REQUEST
        )

    @ExceptionHandler(
        value = [
            UserException.UnauthorizedException::class,
            UserException.InvalidCredentialsException::class,
            UserException.TokenNotFoundException::class
        ]
    )
    fun handleUnauthorized(request: HttpServletRequest, ex: Exception): ResponseEntity<*> {
        return ex.handle(
            request = request,
            status = HttpStatus.UNAUTHORIZED,
            headers = HttpHeaders().apply {
                set(WWW_AUTHENTICATE_HEADER, SCHEME)
            }
        )
    }

    @ExceptionHandler(
        value = [
            LobbyException.LobbyIsFullException::class
        ]
    )
    fun handleForbidden(request: HttpServletRequest, ex: Exception): ResponseEntity<Problem> =
        ex.handle(
            request = request,
            status = HttpStatus.FORBIDDEN
        )

    @ExceptionHandler(
        value = [
            GameException.GameNotFoundException::class,
            UserException.UserNotFoundException::class
        ]
    )
    fun handleNotFound(request: HttpServletRequest, ex: Exception) =
        ex.handle(
            request = request,
            status = HttpStatus.NOT_FOUND
        )

    @ExceptionHandler(
        value = [
            LobbyException.RuleNotImplementedException::class
        ]
    )
    fun handleNotImplementedException(request: HttpServletRequest, ex: Exception) =
        ex.handle(
            request = request,
            status = HttpStatus.NOT_IMPLEMENTED
        )

    @ExceptionHandler(value = [Exception::class])
    fun handleUncaughtException(request: HttpServletRequest, ex: Exception) =
        ex.handle(
            request = request,
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            type = "internal-server-error",
            title = "Internal Server Error",
            detail = "Something went wrong, please try again later."
        ).also { ex.printStackTrace() }

    companion object {
        private const val PROBLEMS_DOCS_URI = "https://github.com/isel-leic-daw/2023-daw-leic51d-08/tree/main/docs/problems/"

        private fun Exception.handle(
            request: HttpServletRequest,
            status: HttpStatus,
            type: String = toProblemType(),
            title: String = getName(),
            detail: String? = message,
            headers: HttpHeaders? = null
        ): ResponseEntity<Problem> =
            Problem(
                type = URI.create(PROBLEMS_DOCS_URI + type),
                title = title,
                detail = detail,
                instance = URI.create(request.requestURI)
            ).toResponse(status, headers).also {
                logger.warn("Handled Exception: {}", message)
            }

        private fun Exception.getName(): String =
            (this::class.simpleName ?: "Unknown")
                .replace("Exception", "")
                .replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]} ${it.groupValues[2]}" }

        private fun Exception.toProblemType(): String = getName().replace(" ", "-").lowercase()

        private fun String.title() = replaceFirstChar { it.titlecase() }
    }
}
