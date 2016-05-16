//package graduationProject.redis;
//
//import java.util.List;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    JedisConnectionFactory jedisConnectionFactory() {
//        return new JedisConnectionFactory();
//    }
//
//    //    @Bean
//    //    public RedisTemplate<String, Pod> redisTemplate(RedisConnectionFactory factory) {
//    //        RedisTemplate<String, Pod> template = new RedisTemplate<String, Pod>();
//    //        template.setConnectionFactory(jedisConnectionFactory());
//    //        template.setKeySerializer(new StringRedisSerializer());
//    //        template.setValueSerializer(new RedisObjectSerializer());
//    //        return template;
//    //    }
//
//    @Bean
//    public RedisTemplate<String, testRedis> redisTemplate1(RedisConnectionFactory factory) {
//        RedisTemplate<String, testRedis> template = new RedisTemplate<String, testRedis>();
//        template.setConnectionFactory(jedisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new RedisObjectSerializer());
//        return template;
//    }
//
//    @Bean
//    public RedisTemplate<String, ServiceRedis> redisTemplate2(RedisConnectionFactory factory) {
//        RedisTemplate<String, ServiceRedis> template = new RedisTemplate<String, ServiceRedis>();
//        template.setConnectionFactory(jedisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new RedisObjectSerializer());
//        return template;
//    }
//
//    @Bean
//    public RedisTemplate<String, List<String>> redisTemplate3(RedisConnectionFactory factory) {
//        RedisTemplate<String, List<String>> template = new RedisTemplate<String, List<String>>();
//        template.setConnectionFactory(jedisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new RedisObjectSerializer());
//        return template;
//    }
//
//    @Bean
//    public RedisTemplate<String, PodRedis> redisTemplate4(RedisConnectionFactory factory) {
//        RedisTemplate<String, PodRedis> template = new RedisTemplate<String, PodRedis>();
//        template.setConnectionFactory(jedisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new RedisObjectSerializer());
//        return template;
//    }
//
//    @Bean
//    public RedisTemplate<String, RequestRedis> redisTemplate5(RedisConnectionFactory factory) {
//        RedisTemplate<String, RequestRedis> template = new RedisTemplate<String, RequestRedis>();
//        template.setConnectionFactory(jedisConnectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new RedisObjectSerializer());
//        return template;
//    }
//}
