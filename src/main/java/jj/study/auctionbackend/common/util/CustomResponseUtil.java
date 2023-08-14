package jj.study.auctionbackend.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jj.study.auctionbackend.domain.common.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@Slf4j
public class CustomResponseUtil {

    public static void fail(HttpServletResponse response, String message, HttpStatus httpStatus) {
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO<?> responseDTO = new ResponseDTO<Object>(-1, message, null);
        try {
            String responseBody = objectMapper.writeValueAsString(responseDTO);
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(httpStatus.value());
            response.getWriter().println(responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
