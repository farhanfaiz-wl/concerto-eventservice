package ai.concerto.event.auth.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventServiceFilter extends OncePerRequestFilter {

  private static final String OPTIONS = "OPTIONS";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {

      MDC.put("request_timestamp_ms", String.valueOf(System.currentTimeMillis()));
      MDC.put("correlation_id", UUID.randomUUID().toString());
      MDC.put("channel", extractChannel(request));
      MDC.put("method", request.getMethod());
      MDC.put("path", request.getServletPath());
      MDC.put("component_id", "11");
      MDC.put("component_name", "eventservice");
      MDC.put("component_version", "0.1");
      MDC.put("status_code", String.valueOf(response.getStatus()));


      response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
      response.setHeader("Access-Control-Allow-Credentials", "true");
      response.setHeader("Access-Control-Allow-Methods",
          "GET ,POST, PATCH, PUT, DELETE, OPTIONS, HEAD");
      response.setHeader("Access-Control-Allow-Headers",
          request.getHeader("access-control-request-headers"));

      if (OPTIONS.equalsIgnoreCase(request.getMethod())) {
        response.setStatus(HttpServletResponse.SC_OK);
      } else {
        filterChain.doFilter(request, response);
      }

    } catch (Exception e) {
      log.info("Error occurred due to : {}", e);
    } finally {
      MDC.clear();
    }
  }

  private String extractChannel(HttpServletRequest request) {
    List<String> url;
    HashSet<String> channelSet = new HashSet<>(Arrays.asList("alexa", "chatbot", "email",
        "facebook", "slack", "sms", "voice", "whatsapp"));
    url = Arrays.asList(request.getRequestURL().toString().split("/"));

    for (String channel : url) {
      if (channelSet.contains(channel)) {
        return channel;
      } else if (request.getRequestURL().toString().contains("conversations")) {
        return "API";
      } else if ((request.getRequestURL().toString().contains("bot")
          && request.getRequestURL().toString().contains("search"))
          || (request.getRequestURL().toString().contains("bot")
              && request.getRequestURL().toString().contains("autocomplete"))) {
        return "Search_API";

      }
    }
    return "";
  }

  @Override
  protected boolean isAsyncDispatch(final HttpServletRequest request) {
    return false;
  }

  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return false;
  }

}
