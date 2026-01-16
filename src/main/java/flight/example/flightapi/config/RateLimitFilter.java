package flight.example.flightapi.config;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

   // Map lưu bucket theo IP (hoặc user ID nếu có auth)
   private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

   // Giới hạn: 20 request mỗi phút per IP
   private Bucket createBucket() {
      return Bucket.builder()
            .addLimit(
                  Bandwidth.builder()
                        .capacity(20) // Dung lượng bucket: 20 token
                        .refillGreedy(20, Duration.ofMinutes(1)) // Refill 20 token mỗi 1 phút (greedy)
                        .build())
            .build();
   }

   @Override
   protected void doFilterInternal(HttpServletRequest request,
         HttpServletResponse response,
         FilterChain filterChain)
         throws ServletException, IOException {

      String clientIp = request.getRemoteAddr(); // Lấy IP client

      Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

      if (bucket.tryConsume(1)) {
         // Còn token → cho qua
         filterChain.doFilter(request, response);
      } else {
         // Hết token → trả 429 Too Many Requests
         response.setStatus(429);
         response.setContentType("text/plain");
         response.getWriter().write("Too many requests. Please wait 1 minute.");
      }
   }
}