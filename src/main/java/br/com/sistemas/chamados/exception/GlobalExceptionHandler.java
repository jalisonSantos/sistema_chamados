package br.com.sistemas.chamados.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException ex) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regraNegocio(RegraNegocioException ex) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhes);
    }

    /** JSON malformado ou valor de enum inexistente (ex.: prioridade "URGENTE"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> jsonInvalido(HttpMessageNotReadableException ex) {
        return montar(HttpStatus.BAD_REQUEST, "JSON inválido ou valor não reconhecido", List.of());
    }

    /** Ex.: excluir um registro que ainda é referenciado por chamados. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException ex) {
        return montar(HttpStatus.CONFLICT,
            "Operação viola a integridade dos dados (registro em uso ou duplicado)", List.of());
    }

    private ResponseEntity<ErroResposta> montar(
            HttpStatus status, String erro, List<String> detalhes) {
        return ResponseEntity.status(status)
            .body(new ErroResposta(LocalDateTime.now(), status.value(), erro, detalhes));
    }
}
