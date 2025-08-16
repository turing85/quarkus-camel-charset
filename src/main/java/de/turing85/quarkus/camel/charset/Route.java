package de.turing85.quarkus.camel.charset;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.cron;

@ApplicationScoped
@Slf4j
public class Route extends RouteBuilder {
  public static final Charset CHARSET_ISO_8859_15 = Charset.forName("ISO-8859-15");
  public static final String HEADER_HEADER_SET = "headerSet";

  private boolean setHeader = false;

  @Override
  public void configure() {
    // @formatter:off
    from(cron("every-ten-seconds").schedule("0/1 * * * * ? "))
        .process(exchange -> {
          exchange.getIn().setBody("Hellü Wörld".getBytes(CHARSET_ISO_8859_15));
          if (setHeader) {
            exchange.getIn().setHeader(Exchange.CONTENT_ENCODING, "foobar");
          }
          exchange.getIn().setHeader(HEADER_HEADER_SET, setHeader);
          setHeader = !setHeader;
        })
        .setBody(this::convertBodyToString)
        .log("body: ${body}")
        .log("headerSet: ${headers.headerSet}");
    // @formatter:on
  }

  private String convertBodyToString(Exchange exchange) {
    // @formatter:off
    exchange.getIn().getHeaders().computeIfAbsent(
        Exchange.CHARSET_NAME,
        unused -> Optional
            .ofNullable(exchange.getIn().getHeader(Exchange.CONTENT_ENCODING, String.class))
            .filter(Route::charsetIsSupportedNoThrow)
            .orElse(StandardCharsets.UTF_8.name()));
    // @formatter:on
    return exchange.getIn().getBody(String.class);
  }

  private static boolean charsetIsSupportedNoThrow(String charsetName) {
    try {
      return Charset.isSupported(charsetName);
    } catch (Exception e) {
      return false;
    }
  }
}
