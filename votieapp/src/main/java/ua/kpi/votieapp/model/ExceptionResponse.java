package ua.kpi.votieapp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"errors"})
@Builder
@Data
public class ExceptionResponse {

    @JsonProperty("errors")
    private List<Error> errors;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyOrder({"code", "detail"})
    @Builder
    @Data
    public static class Error {

        @JsonProperty("code")
        private Integer code;

        @JsonProperty("detail")
        private String detail;
    }
}

