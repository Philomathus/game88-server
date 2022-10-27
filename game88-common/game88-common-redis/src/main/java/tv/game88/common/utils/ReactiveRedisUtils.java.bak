package tv.game88.common.utils;

import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.domain.geo.BoundingBox;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.RedisElementReader;
import org.springframework.data.redis.serializer.RedisElementWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings( "unused" )
@Log4j2
@Component
public class ReactiveRedisUtils {
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @Autowired
    public ReactiveRedisUtils( ReactiveStringRedisTemplate reactiveStringRedisTemplate ) {
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
    }

    // ---------------------- key操作 ---------------------

    /**
     * 键模式匹配
     * <p>
     * 不建议使用，阻塞性返回模式匹配获取到的键，会严重影响Redis的整体性能，建议改用{@link #scan}
     * </p>
     *
     * @param s 模式匹配值
     *
     * @return 匹配的键列表
     */
    @Deprecated
    public Flux<String> keys( String s ) {
        return reactiveStringRedisTemplate.keys( s );
    }

    /**
     * 给key附加过期时间
     */
    public Mono<Boolean> expire( String key, Duration timeout ) {
        return reactiveStringRedisTemplate.expire( key, timeout );
    }

    /**
     * 给key指定到期时间
     */
    public Mono<Boolean> expireAt( String key, Instant expireAt ) {
        return reactiveStringRedisTemplate.expireAt( key, expireAt );
    }

    /**
     * 移除指定key的过期时间
     */
    public Mono<Boolean> persist( String key ) {
        return reactiveStringRedisTemplate.persist( key );
    }

    /**
     * 获取指定key的过期时间
     */
    public Mono<Duration> getExpire( String key ) {
        return reactiveStringRedisTemplate.getExpire( key );
    }

    /**
     * 修改key名
     */
    public Mono<Boolean> rename( String key, String newKey ) {
        return reactiveStringRedisTemplate.rename( key, newKey );
    }

    /**
     * 修改key名，如果key不存在，将报错
     */
    public Mono<Boolean> renameIfAbsent( String key, String newKey ) {
        return reactiveStringRedisTemplate.renameIfAbsent( key, newKey );
    }

    /**
     * 删除一个或多个键
     * <p>如果已知键的值体量特别小，可以使用del命令，否则请使用 {@link #unlink}</p>
     */
    public Mono<Long> delete( Publisher<String> keys ) {
        return reactiveStringRedisTemplate.delete( keys );
    }

    /**
     * 删除一个键
     * <p>如果已知键的值体量特别小，可以使用del命令，否则请使用 {@link #unlink}</p>
     */
    public Mono<Long> delete( String key ) {
        return reactiveStringRedisTemplate.delete( key );
    }

    /**
     * 是否存在key
     */
    public Mono<Boolean> exists( String key ) {
        return reactiveStringRedisTemplate.hasKey( key );
    }

    /**
     * 从当前数据库中随机返回一个 key，当数据库为空时，返回null。
     */
    public Mono<String> randomKey() {
        return reactiveStringRedisTemplate.randomKey();
    }

    /**
     * 返回 key 所储存的值的类型，当key不存在时，返回类型是{@link DataType "none"}
     */
    public Mono<DataType> type( String key ) {
        return reactiveStringRedisTemplate.type( key );
    }

    /**
     * 删除一个或多个key-value，但是，相比DEL会产生阻塞，该命令会在另一个线程中回收内存，因此它是非阻塞的。
     */
    public Mono<Long> unlink( Publisher<String> keys ) {
        return reactiveStringRedisTemplate.unlink( keys );
    }

    /**
     * 删除一个或多个key-value，但是，相比DEL会产生阻塞，该命令会在另一个线程中回收内存，因此它是非阻塞的。
     */
    public Mono<Long> unlink( String key ) {
        return reactiveStringRedisTemplate.unlink( key );
    }

    /**
     * 扫描返回所有键
     *
     * @return 所有键
     */
    public Flux<String> scan() {
        return reactiveStringRedisTemplate.scan();
    }

    /**
     * 扫描返回所有键
     *
     * @param scanOptions 模式匹配
     *
     * @return 所有匹配到的键
     */
    public Flux<String> scan( ScanOptions scanOptions ) {
        return reactiveStringRedisTemplate.scan( scanOptions );
    }

    /**
     * 将sourceKey的值复制到targetKey中
     *
     * @param sourceKey 原键
     * @param targetKey 目标键
     *                  <p>如果目标键已经存在，将报错</p>
     * @param replace   是否将sourceKey删除
     *                  <p>如果为true则类似计算机的剪切功能，而如果为false则类似计算机的粘贴功能</p>
     *
     * @return 是否执行成功
     */
    public Mono<Boolean> copy( String sourceKey, String targetKey, boolean replace ) {
        return reactiveStringRedisTemplate.copy( sourceKey, targetKey, replace );
    }

    /**
     * 将key从当前选中的数据库移动到指定的数据库
     * <p>当key已经在目标数据库中存在时，或者在源数据库中不存在时，此命令不执行任何操作。因此，可以将MOVE命令用作锁定原语</p>
     *
     * @param key     键
     * @param dbIndex 目标数据库索引
     *
     * @return 是否迁移成功
     */
    public Mono<Boolean> move( String key, int dbIndex ) {
        return reactiveStringRedisTemplate.move( key, dbIndex );
    }

    public <T> Flux<T> execute( RedisScript<T> script ) {
        return reactiveStringRedisTemplate.execute( script );
    }

    public <T> Flux<T> execute( RedisScript<T> script, List<String> keys ) {
        return reactiveStringRedisTemplate.execute( script, keys );
    }

    public <T> Flux<T> execute( RedisScript<T> script, List<String> keys, List<?> args ) {
        return reactiveStringRedisTemplate.execute( script, keys, args );
    }

    public <T> Flux<T> execute( RedisScript<T> script, List<String> keys, List<?> args, RedisElementWriter<?> argsWriter,
                                RedisElementReader<T> resultReader ) {
        return reactiveStringRedisTemplate.execute( script, keys, args, argsWriter, resultReader );
    }

    public <T> Flux<T> execute( ReactiveRedisCallback<T> action ) {
        return reactiveStringRedisTemplate.execute( action );
    }

    public <T> Flux<T> execute( ReactiveRedisCallback<T> action, boolean exposeConnection ) {
        return reactiveStringRedisTemplate.execute( action, exposeConnection );
    }

    public <T> Flux<T> executeInSession( ReactiveRedisSessionCallback<String, String, T> action ) {
        return reactiveStringRedisTemplate.executeInSession( action );
    }

    public <T> Mono<T> createMono( ReactiveRedisCallback<T> callback ) {
        return reactiveStringRedisTemplate.createMono( callback );
    }

    // ---------------------- 消息发布订阅功能 ---------------------

    /**
     * 将消息发布到给定的频道
     *
     * @param channel 频道名称
     * @param message 消息文本
     *
     * @return 收到消息的客户端数量
     * <p>请注意，在Redis群集中，仅对与发布客户端连接到同一节点的客户端进行计数</p>
     */
    public Mono<Long> convertAndSend( String channel, String message ) {
        return reactiveStringRedisTemplate.convertAndSend( channel, message );
    }

    public Flux<? extends ReactiveSubscription.Message<String, String>> listenTo( Topic... topics ) {
        return reactiveStringRedisTemplate.listenTo( topics );
    }

    public Flux<? extends ReactiveSubscription.Message<String, String>> listenToChannel( String... channels ) {
        return reactiveStringRedisTemplate.listenToChannel( channels );
    }

    public Flux<? extends ReactiveSubscription.Message<String, String>> listenToPattern( String... patterns ) {
        return reactiveStringRedisTemplate.listenToPattern( patterns );
    }

    public Mono<Flux<? extends ReactiveSubscription.Message<String, String>>> listenToChannel( Topic... topics ) {
        return reactiveStringRedisTemplate.listenToLater( topics );
    }

    public Mono<Flux<? extends ReactiveSubscription.Message<String, String>>> listenToChannelLater( String... channels ) {
        return reactiveStringRedisTemplate.listenToChannelLater( channels );
    }

    public Mono<Flux<? extends ReactiveSubscription.Message<String, String>>> listenToPatternLater( String... patterns ) {
        return reactiveStringRedisTemplate.listenToPatternLater( patterns );
    }

    // ---------------------- string操作 ---------------------

    /**
     * 设置 String 类型 key-value
     */
    public Mono<Boolean> strSet( String key, String value ) {
        return reactiveStringRedisTemplate.opsForValue().set( key, value );
    }

    /**
     * 设置 String 类型 key-value 并添加过期时间
     *
     * @param timeout 过期时间
     */
    public Mono<Boolean> strSet( String key, String value, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForValue().set( key, value, timeout );
    }

    /**
     * 只在键不存在的情况下，将键的值设置为value。<br/> 若键已经存在，则不做任何动作。
     */
    public Mono<Boolean> strSetIfAbsent( String key, String value ) {
        return reactiveStringRedisTemplate.opsForValue().setIfAbsent( key, value );
    }

    /**
     * 只在键不存在的情况下，将键的值设置为value，并过期时间。<br/> 若键已经存在， 则不做任何动作。
     *
     * @param timeout 过期时间
     */
    public Mono<Boolean> strSetIfAbsent( String key, String value, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForValue().setIfAbsent( key, value, timeout );
    }

    /**
     * 只在键存在的情况下，将键的值设置为value。<br/> 若键不存在，则不做任何动作。
     */
    public Mono<Boolean> strSetIfPresent( String key, String value ) {
        return reactiveStringRedisTemplate.opsForValue().setIfPresent( key, value );
    }

    /**
     * 只在键存在的情况下，将键的值设置为value，并过期时间。<br/> 若键不存在，则不做任何动作。
     *
     * @param timeout 过期时间
     */
    public Mono<Boolean> strSetIfPresent( String key, String value, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForValue().setIfPresent( key, value, timeout );
    }

    /**
     * 批量添加 key-value，如果键已经存在，则会覆盖原有的值
     */
    public Mono<Boolean> strMSet( Map<String, String> keyAndValue ) {
        return reactiveStringRedisTemplate.opsForValue().multiSet( keyAndValue );
    }

    /**
     * 批量添加 key-value 只要有一个键已经存在，则全部不添加
     */
    public Mono<Boolean> strMSetIfAbsent( Map<String, String> keyAndValue ) {
        return reactiveStringRedisTemplate.opsForValue().multiSetIfAbsent( keyAndValue );
    }

    /**
     * 获取 String 类型 key-value
     */
    public Mono<String> strGet( String key ) {
        return reactiveStringRedisTemplate.opsForValue().get( key );
    }

    /**
     * 获取String类型的键的值，然后删除此键
     * <p>如果此键不存在,则返回null</p>
     *
     * @since Redis Version: 6.2
     */
    public Mono<String> strGetAndDelete( String key ) {
        return reactiveStringRedisTemplate.opsForValue().getAndDelete( key );
    }

    /**
     * 获取String类型的键的值，然后附加过期时间
     * <p>如果此键不存在,则返回null</p>
     *
     * @since Redis Version: 6.2
     */
    public Mono<String> strGetAndExpire( String key, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForValue().getAndExpire( key, timeout );
    }

    /**
     * 获取String类型的键的值，然后移除过期时间
     * <p>如果此键不存在,则返回null</p>
     *
     * @since Redis Version: 6.2
     */
    public Mono<String> strGetAndPersist( String key ) {
        return reactiveStringRedisTemplate.opsForValue().getAndPersist( key );
    }

    /**
     * 批量获取键的值，如果某个键不存在，则以null的形式返回，数量会保持一致
     */
    public Mono<List<String>> strMGet( Collection<String> keys ) {
        return reactiveStringRedisTemplate.opsForValue().multiGet( keys );
    }

    /**
     * 对一个键的值进行加 1 操作并返回修改后的值，如果该键不存在 将创建一个键并赋值 1 如果键储存的值不能被解释为数字，将报错
     */
    public Mono<Long> strIncrement( String key ) {
        return reactiveStringRedisTemplate.opsForValue().increment( key );
    }

    /**
     * 对一个键的值进行加number操作并返回修改后的值，如果该键不存在 将创建一个键并赋值该 number
     */
    public Mono<Long> strIncrement( String key, long number ) {
        return reactiveStringRedisTemplate.opsForValue().increment( key, number );
    }

    /**
     * 对一个键的值进行加number操作并返回修改后的值，如果该键不存在 将创建一个键并赋值该 number
     */
    public Mono<Double> strIncrement( String key, double number ) {
        return reactiveStringRedisTemplate.opsForValue().increment( key, number );
    }

    /**
     * 对一个键的值进行减1操作并返回修改后的值，如果该键不存在 将创建一个键并赋值-1 如果键储存的值不能被解释为数字，将报错
     */
    public Mono<Long> strDecrement( String key ) {
        return reactiveStringRedisTemplate.opsForValue().decrement( key );
    }

    /**
     * 对一个键的值进行加操作并返回修改后的值，如果该键不存在 将创建一个键并赋值 -number 如果键储存的值不能被解释为数字，将报错
     */
    public Mono<Long> strDecrement( String key, long number ) {
        return reactiveStringRedisTemplate.opsForValue().decrement( key, number );
    }

    /**
     * 对一个String类型键的值进行追加操作，如果该键不存在 将创建一个键并赋值value，返回value的长度
     */
    public Mono<Long> strAppend( String key, String value ) {
        return reactiveStringRedisTemplate.opsForValue().append( key, value );
    }

    /**
     * 获取 String 类型 value 指定的偏移量
     *
     * @see <a href="http://doc.redisfans.com/string/getrange.html">Document：GETRANGE</a>
     */
    public Mono<String> strGetRange( String key, long start, long end ) {
        return reactiveStringRedisTemplate.opsForValue().get( key, start, end );
    }

    /**
     * 设置 String 类型 key-value，将 value 设置到指定的偏移量上
     *
     * @see <a href="http://doc.redisfans.com/string/setrange.html">Document：SETRANGE</a>
     */
    public Mono<Long> strSetRange( String key, String value, long offset ) {
        return reactiveStringRedisTemplate.opsForValue().set( key, value, offset );
    }

    /**
     * 返回键的值的长度，如果该 key 不存在，返回 0
     */
    public Mono<Long> strSize( String key ) {
        return reactiveStringRedisTemplate.opsForValue().size( key );
    }

    /**
     * 设置或清除存储在键的字符串值中offset的bit。 当键不存在时，创建一个新的字符串值
     *
     * @see <a href="http://doc.redisfans.com/string/getbit.html">Document：SETBIT</a>
     */
    public Mono<Boolean> strSetBit( String key, long offset, boolean value ) {
        return reactiveStringRedisTemplate.opsForValue().setBit( key, offset, value );
    }

    /**
     * 对键所储存的字符串值，获取指定偏移量上的bit。当 offset 比字符串值的长度大，或者 key 不存在时，返回false
     *
     * @see <a href="http://doc.redisfans.com/string/getbit.html">Document：GETBIT</a>
     */
    public Mono<Boolean> strGetBit( String key, long offset ) {
        return reactiveStringRedisTemplate.opsForValue().getBit( key, offset );
    }

    /**
     * 把Redis字符串当作位数组，并能对变长位宽和任意未字节对齐的指定整型位域进行寻址。
     *
     * @see <a href="http://www.redis.cn/commands/bitfield.html">Document：BITFIELD</a>
     * @since Redis Version: 3.2.0
     */
    public Mono<List<Long>> strBitField( String key, BitFieldSubCommands command ) {
        return reactiveStringRedisTemplate.opsForValue().bitField( key, command );
    }

    // ---------------------- hash操作 ---------------------

    /**
     * 添加 Hash 键值对
     */
    public Mono<Boolean> hSet( String key, String hashKey, String value ) {
        return reactiveStringRedisTemplate.opsForHash().put( key, hashKey, value );
    }

    /**
     * 批量添加 hash 的键值对 有则覆盖,没有则添加
     */
    public Mono<Boolean> hMSet( String key, Map<String, String> map ) {
        return reactiveStringRedisTemplate.opsForHash().putAll( key, map );
    }

    /**
     * 添加 hash 键值对. 若有任意hashKey存在，则不做任何动作。
     */
    public Mono<Boolean> hSetIfAbsent( String key, String hashKey, String value ) {
        return reactiveStringRedisTemplate.opsForHash().putIfAbsent( key, hashKey, value );
    }

    /**
     * 删除指定键的HashKey
     *
     * @return 删除成功的 数量
     */
    public Mono<Long> hRemove( String key, String... hashKeys ) {
        return reactiveStringRedisTemplate.opsForHash().remove( key, hashKeys );
    }

    /**
     * 判断hashKey是否存在于键内
     */
    public Mono<Boolean> hHasKey( String key, String hashKey ) {
        return reactiveStringRedisTemplate.opsForHash().hasKey( key, hashKey );
    }

    /**
     * 获取指定键下的hashKey的值
     */
    public Mono<Object> hGet( String key, String hashKey ) {
        return reactiveStringRedisTemplate.opsForHash().get( key, hashKey );
    }

    /**
     * 获取指定键下的hashKey的值，如果某个hashKey不存在，则值会是 null
     */
    public Mono<List<Object>> hMGet( String key, Collection<Object> hashKeys ) {
        return reactiveStringRedisTemplate.opsForHash().multiGet( key, hashKeys );
    }

    /**
     * 给指定键下的hashKey的值做加操作
     */
    public Mono<Long> hIncrement( String key, String hashKey, long number ) {
        return reactiveStringRedisTemplate.opsForHash().increment( key, hashKey, number );
    }

    /**
     * 给指定键下的hashKey的值做加操作
     */
    public Mono<Double> hIncrement( String key, Object hashKey, double number ) {
        return reactiveStringRedisTemplate.opsForHash().increment( key, hashKey, number );
    }

    /**
     * 返回一个键下随机的hashKey的值
     *
     * @since Redis Version: 6.2
     */
    public Mono<Object> hRandomKey( String key ) {
        return reactiveStringRedisTemplate.opsForHash().randomKey( key );
    }

    /**
     * 返回指定数量的键下随机的hashKey的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<Object> hRandomKeys( String key, long count ) {
        return reactiveStringRedisTemplate.opsForHash().randomKeys( key, count );
    }

    /**
     * 返回一个键下随机的键值对
     *
     * @since Redis Version: 6.2
     */
    public Mono<Map.Entry<Object, Object>> hRandomEntry( String key ) {
        return reactiveStringRedisTemplate.opsForHash().randomEntry( key );
    }

    /**
     * 返回一个键下随机的键值对
     *
     * @since Redis Version: 6.2
     */
    public Flux<Map.Entry<Object, Object>> hRandomEntries( String key, long count ) {
        return reactiveStringRedisTemplate.opsForHash().randomEntries( key, count );
    }

    /**
     * 获取指定键下的键值对数量
     */
    public Mono<Long> hSize( String key ) {
        return reactiveStringRedisTemplate.opsForHash().size( key );
    }

    /**
     * 获取键下所有 hashKey 字段名
     */
    public Flux<Object> hKeys( String key ) {
        return reactiveStringRedisTemplate.opsForHash().keys( key );
    }

    /**
     * 获取键下的 hashKey 的值
     */
    public Flux<Object> hValues( String key ) {
        return reactiveStringRedisTemplate.opsForHash().values( key );
    }

    /**
     * 获取键下的所有键值对
     */
    public Flux<Map.Entry<Object, Object>> hGetAll( String key ) {
        return reactiveStringRedisTemplate.opsForHash().entries( key );
    }

    /**
     * 增量迭代 hash 下面的 键值对
     */
    public Flux<Map.Entry<Object, Object>> hScan( String key ) {
        return reactiveStringRedisTemplate.opsForHash().scan( key );
    }

    /**
     * 增量迭代 hash 下面的 键值对
     */
    public Flux<Map.Entry<Object, Object>> hScan( String key, ScanOptions options ) {
        return reactiveStringRedisTemplate.opsForHash().scan( key, options );
    }

    // ---------------------- list有序集合 ---------------------

    /**
     * 获取指定列表数量
     */
    public Mono<Long> lSize( String key ) {
        return reactiveStringRedisTemplate.opsForList().size( key );
    }

    /**
     * 获取列表指定下标内的所有值
     */
    public Flux<String> lRange( String key, long start, long end ) {
        return reactiveStringRedisTemplate.opsForList().range( key, start, end );
    }

    /**
     * 删除列表指定下标以外的所有元素
     */
    public Mono<Boolean> lTrim( String key, long start, long end ) {
        return reactiveStringRedisTemplate.opsForList().trim( key, start, end );
    }

    /**
     * 指定 list 从左入栈
     *
     * @return 当前队列的长度
     */
    public Mono<Long> lLeftPush( String key, String value ) {
        return reactiveStringRedisTemplate.opsForList().leftPush( key, value );
    }

    /**
     * <p>
     * 将值 value 插入到列表 key 当中，位于值 pivot 之前。
     * <br/> 当 pivot 不存在于列表 key 时，不执行任何操作。
     * <br/> 当 key 不存在时， key 被视为空列表，不执行任何操作。
     * <br/> 如果key不是列表类型，返回一个错误。
     * </p>
     *
     * @return 当前队列的长度
     */
    public Mono<Long> lLeftPush( String key, String pivot, String value ) {
        return reactiveStringRedisTemplate.opsForList().leftPush( key, pivot, value );
    }

    /**
     * 从左边依次入栈 导入顺序按照 Collection 顺序 如: a b c => c b a
     */
    public Mono<Long> lLeftPushAll( String key, String... values ) {
        return reactiveStringRedisTemplate.opsForList().leftPushAll( key, values );
    }

    /**
     * 从左边依次入栈 导入顺序按照 Collection 顺序 如: a b c => c b a
     */
    public Mono<Long> lLeftPushAll( String key, Collection<String> values ) {
        return reactiveStringRedisTemplate.opsForList().leftPushAll( key, values );
    }

    /**
     * 指定 list 从左入栈，当 key 不存在时，什么也不做，返回 0 。
     *
     * @return 当前队列的长度
     */
    public Mono<Long> lLeftPushIfPresent( String key, String value ) {
        return reactiveStringRedisTemplate.opsForList().leftPushIfPresent( key, value );
    }

    /**
     * 指定 list 从左出栈 如果列表没有元素，会返回null
     *
     * @return 出栈的值
     */
    public Mono<String> lLeftPop( String key ) {
        return reactiveStringRedisTemplate.opsForList().leftPop( key );
    }

    /**
     * 指定 list 从左出栈 如果列表没有元素，会堵塞到列表一直有元素或者超时为止，若超时则返回null
     *
     * @return 出栈的值
     */
    public Mono<String> lLeftPop( String key, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForList().leftPop( key, timeout );
    }

    /**
     * 指定 list 从右入栈
     *
     * @return 当前队列的长度
     */
    public Mono<Long> lRightPush( String key, String value ) {
        return reactiveStringRedisTemplate.opsForList().rightPush( key, value );
    }

    /**
     * <p>
     * 将值 value 插入到列表 key 当中，位于值 pivot 之后。
     * <br/> 当 pivot 不存在于列表 key 时，不执行任何操作。
     * <br/> 当 key 不存在时， key 被视为空列表，不执行任何操作。
     * <br/> 如果key不是列表类型，返回一个错误。
     * </p>
     *
     * @return 当前队列的长度
     */
    public Mono<Long> lRightPush( String key, String pivot, String value ) {
        return reactiveStringRedisTemplate.opsForList().rightPush( key, pivot, value );
    }

    /**
     * 从右边依次入栈 导入顺序按照 Collection 顺序 如: a b c => a b c
     */
    public Mono<Long> lRightPushAll( String key, String... values ) {
        return reactiveStringRedisTemplate.opsForList().rightPushAll( key, values );
    }

    /**
     * 从右边依次入栈 导入顺序按照 Collection 顺序 如: a b c => a b c
     */
    public Mono<Long> lRightPushAll( String key, Collection<String> values ) {
        return reactiveStringRedisTemplate.opsForList().rightPushAll( key, values );
    }

    /**
     * 指定 list 从右入栈，当 key 不存在时，什么也不做，返回 0 。
     *
     * @return 当前队列的长度
     */
    public Mono<Long> lRightPushIfPresent( String key, String value ) {
        return reactiveStringRedisTemplate.opsForList().rightPushIfPresent( key, value );
    }

    /**
     * 指定 list 从右出栈 如果列表没有元素，会返回null
     *
     * @return 出栈的值
     */
    public Mono<String> lRightPop( String key ) {
        return reactiveStringRedisTemplate.opsForList().rightPop( key );
    }

    /**
     * 指定 list 从右出栈 如果列表没有元素，会堵塞到列表一直有元素或者超时为止，若超时则返回null
     *
     * @return 出栈的值
     */
    public Mono<String> lRightPop( String key, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForList().rightPop( key, timeout );
    }

    /**
     * 将列表 key 下标为 index 的元素的值设置为 value 。
     * <br/> 当 index 参数超出范围，或对一个空列表( key 不存在)进行 LSET 时，返回false。
     */
    public Mono<Boolean> lSet( String key, long index, String value ) {
        return reactiveStringRedisTemplate.opsForList().set( key, index, value );
    }

    /**
     * 删除 key 中 值为 value 的 count 个数.
     *
     * @return 成功删除的个数
     */
    public Mono<Long> lDelete( String key, long count, Object value ) {
        return reactiveStringRedisTemplate.opsForList().remove( key, count, value );
    }

    /**
     * 根据下标获取值
     */
    public Mono<String> lIndex( String key, long index ) {
        return reactiveStringRedisTemplate.opsForList().index( key, index );
    }

    /**
     * 返回列表中指定值第一次出现的索引
     *
     * @since Redis Version: 6.0.6
     */
    public Mono<Long> lIndexOf( String key, String value ) {
        return reactiveStringRedisTemplate.opsForList().indexOf( key, value );
    }

    /**
     * 返回列表中指定值最后一次出现的索引
     *
     * @since Redis Version: 6.0.6
     */
    public Mono<Long> lIndexOfLast( String key, String value ) {
        return reactiveStringRedisTemplate.opsForList().lastIndexOf( key, value );
    }

    /**
     * <p>以原子方式返回并删除存储在sourceKey列表的第一个/最后一个元素（头/尾取决于 from 参数），</p>
     * <p>并将元素推送到存储的destinationKey列表的第一个/最后一个元素（头/尾取决于 to 参数）。</p>
     *
     * @since Redis Version: 6.2
     */
    public Mono<String> hMove( ListOperations.MoveFrom<String> from, ListOperations.MoveTo<String> to ) {
        return reactiveStringRedisTemplate.opsForList().move( from, to );
    }

    /**
     * <p>以原子方式返回并删除存储在sourceKey列表的第一个/最后一个元素（头/尾取决于 from 参数），</p>
     * <p>并将元素推送到存储的destinationKey列表的第一个/最后一个元素（头/尾取决于 to 参数）。</p>
     * <strong>阻止连接，直到元素可用或达到超时<strong/>
     *
     * @since Redis Version: 6.2
     */
    public Mono<String> hMove( ListOperations.MoveFrom<String> from, ListOperations.MoveTo<String> to, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForList().move( from, to, timeout );
    }

    /**
     * <p>以原子方式返回并删除存储在sourceKey列表的第一个/最后一个元素（头/尾取决于 from 参数），</p>
     * <p>并将元素推送到存储的destinationKey列表的第一个/最后一个元素（头/尾取决于 to 参数）。</p>
     *
     * @since Redis Version: 6.2
     */
    public Mono<String> hMove( String sourceKey, ReactiveListCommands.Direction from, String destinationKey,
                               ReactiveListCommands.Direction to ) {
        return reactiveStringRedisTemplate.opsForList().move( sourceKey, from, destinationKey, to );
    }

    /**
     * <p>以原子方式返回并删除存储在sourceKey列表的第一个/最后一个元素（头/尾取决于 from 参数），</p>
     * <p>并将元素推送到存储的destinationKey列表的第一个/最后一个元素（头/尾取决于 to 参数）。</p>
     * <strong>阻止连接，直到元素可用或达到超时<strong/>
     *
     * @since Redis Version: 6.2
     */
    public Mono<String> hMove( String sourceKey, ReactiveListCommands.Direction from, String destinationKey,
                               ReactiveListCommands.Direction to, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForList().move( sourceKey, from, destinationKey, to, timeout );
    }

    // ---------------------- set操作 无序不重复集合 ---------------------

    /**
     * 添加 set 元素，如果已经存在将忽略添加，如果key不存在则创建新set
     */
    public Mono<Long> sAdd( String key, String... values ) {
        return reactiveStringRedisTemplate.opsForSet().add( key, values );
    }

    /**
     * 删除一个或多个集合中的指定值
     *
     * @return 成功删除数量
     */
    public Mono<Long> sRemove( String key, Object... values ) {
        return reactiveStringRedisTemplate.opsForSet().remove( key, values );
    }

    /**
     * 随机移除一个元素
     */
    public Mono<String> sRandomPop( String key ) {
        return reactiveStringRedisTemplate.opsForSet().pop( key );
    }

    /**
     * 随机移除 count 个元素
     */
    public Flux<String> sRandomPop( String key, long count ) {
        return reactiveStringRedisTemplate.opsForSet().pop( key, count );
    }

    /**
     * 将 key 中的 value 迁移到 destKey 中
     *
     * @return 返回成功与否
     */
    public Mono<Boolean> sMove( String key, String value, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().move( key, value, destKey );
    }

    /**
     * 集合的数量。当集合 key 不存在时，返回 0 。
     */
    public Mono<Long> sSize( String key ) {
        return reactiveStringRedisTemplate.opsForSet().size( key );
    }

    /**
     * 判断 set 集合中 是否有 value
     */
    public Mono<Boolean> sIsMember( String key, String value ) {
        return reactiveStringRedisTemplate.opsForSet().isMember( key, value );
    }

    /**
     * 判断 set 集合中 是否有 values
     */
    public Mono<Map<Object, Boolean>> sIsMember( String key, Object... values ) {
        return reactiveStringRedisTemplate.opsForSet().isMember( key, values );
    }

    /**
     * 返回 key 和 otherKeys 的交集
     */
    public Flux<String> sIntersect( String key, String otherKey ) {
        return reactiveStringRedisTemplate.opsForSet().intersect( key, otherKey );
    }

    /**
     * 返回 key 和 otherKeys 的交集
     */
    public Flux<String> sIntersect( String key, Collection<String> otherKeys ) {
        return reactiveStringRedisTemplate.opsForSet().intersect( key, otherKeys );
    }

    /**
     * 返回 keys 之间的交集
     */
    public Flux<String> sIntersect( Collection<String> keys ) {
        return reactiveStringRedisTemplate.opsForSet().intersect( keys );
    }

    /**
     * 将 key 与 otherKey 的交集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Mono<Long> sIntersectAndStore( String key, String otherKey, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().intersectAndStore( key, otherKey, destKey );
    }

    /**
     * 将 key 与 otherKey 的交集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Mono<Long> sIntersectAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().intersectAndStore( key, otherKeys, destKey );
    }

    /**
     * 将 key 与 otherKey 的交集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Mono<Long> sIntersectAndStore( Collection<String> keys, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().intersectAndStore( keys, destKey );
    }

    /**
     * 返回 key 和 othere 的并集
     */
    public Flux<String> sUnion( String key, String otherKey ) {
        return reactiveStringRedisTemplate.opsForSet().union( key, otherKey );
    }

    /**
     * 返回 key 和 otherKeys 的并集
     *
     * @param otherKeys key 的集合
     */
    public Flux<String> sUnion( String key, Collection<String> otherKeys ) {
        return reactiveStringRedisTemplate.opsForSet().union( key, otherKeys );
    }

    /**
     * 将 key 与 otherKey 的并集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Mono<Long> sUnionAndStore( String key, String otherKey, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().unionAndStore( key, otherKey, destKey );
    }

    /**
     * 将 key 与 otherKey 的并集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Mono<Long> sUnionAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().unionAndStore( key, otherKeys, destKey );
    }

    /**
     * 将 keys 的并集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Mono<Long> sUnionAndStore( Collection<String> keys, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().unionAndStore( keys, destKey );
    }

    /**
     * 获取两个集合的差集
     */
    public Flux<String> sDifference( String key, String otherkey ) {
        return reactiveStringRedisTemplate.opsForSet().difference( key, otherkey );
    }

    /**
     * 获取 key 和 集合 otherKeys 的差集
     */
    public Flux<String> sDifference( String key, Collection<String> otherKeys ) {
        return reactiveStringRedisTemplate.opsForSet().difference( key, otherKeys );
    }

    /**
     * 将  key 与 otherkey 的差集 ,添加到 destKey 集合中
     *
     * @return 返回差集的数量
     */
    public Mono<Long> sDifferenceAndStore( String key, String otherkey, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().differenceAndStore( key, otherkey, destKey );
    }

    /**
     * 将 key 和 集合  collections 中的 key 集合的差集 添加到 destKey 集合中
     *
     * @return 返回差集的数量
     */
    public Mono<Long> sDifferenceAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().differenceAndStore( key, otherKeys, destKey );
    }

    /**
     * 将 keys 的差集 添加到 destKey 集合中
     *
     * @return 返回差集的数量
     */
    public Mono<Long> sDifferenceAndStore( Collection<String> keys, String destKey ) {
        return reactiveStringRedisTemplate.opsForSet().differenceAndStore( keys, destKey );
    }

    /**
     * 返回集合中所有元素
     */
    public Flux<String> sMembers( String key ) {
        return reactiveStringRedisTemplate.opsForSet().members( key );
    }

    /**
     * 增量迭代返回集合中所有元素
     */
    public Flux<String> sScan( String key ) {
        return reactiveStringRedisTemplate.opsForSet().scan( key );
    }

    /**
     * 增量迭代返回集合中所有元素
     */
    public Flux<String> sScan( String key, ScanOptions options ) {
        return reactiveStringRedisTemplate.opsForSet().scan( key, options );
    }

    /**
     * 随机获取一个元素
     */
    public Mono<String> sRandom( String key ) {
        return reactiveStringRedisTemplate.opsForSet().randomMember( key );
    }

    /**
     * 随机获取指定数量的元素,同一个元素可能会选中两次
     */
    public Flux<String> sRandom( String key, long count ) {
        return reactiveStringRedisTemplate.opsForSet().randomMembers( key, count );
    }

    /**
     * 随机获取指定数量的元素,去重(同一个元素只能选择两一次)
     */
    public Flux<String> sRandomDistinct( String key, long count ) {
        return reactiveStringRedisTemplate.opsForSet().distinctRandomMembers( key, count );
    }

    // ---------------------- zset操作 根据 socre 排序 ---------------------

    /**
     * 添加 ZSet 元素
     */
    public Mono<Boolean> zAdd( String key, String value, double score ) {
        return reactiveStringRedisTemplate.opsForZSet().add( key, value, score );
    }

    /**
     * 批量添加 ZSet 元素 <br/>
     * <code>
     * Set tupless = new HashSet<>(); <br/> ZSetOperations.TypedTuple objectTypedTuple1 = new DefaultTypedTuple<>("zset-5",9.6);
     * <br/> tupless.add(objectTypedTuple1); <br/> ZSetOperations.TypedTuple objectTypedTuple2 = new
     * DefaultTypedTuple<>("zset-6",9.5); <br/> tupless.add(objectTypedTuple2);
     * </code>
     */
    public Mono<Long> zAddAll( String key, Set<ZSetOperations.TypedTuple<String>> tuples ) {
        return reactiveStringRedisTemplate.opsForZSet().addAll( key, tuples );
    }

    /**
     * Zset 删除一个或多个元素
     */
    public Mono<Long> zRemove( String key, Object... values ) {
        return reactiveStringRedisTemplate.opsForZSet().remove( key, values );
    }

    /**
     * 对指定的 zset 的 value 值 , socre 属性做增减操作
     */
    public Mono<Double> zIncrementScore( String key, String value, double score ) {
        return reactiveStringRedisTemplate.opsForZSet().incrementScore( key, value, score );
    }

    /**
     * 从Zset中获取一个随机元素。
     *
     * @since Redis Version: 6.2
     */
    public Mono<String> zRandomMember( String key ) {
        return reactiveStringRedisTemplate.opsForZSet().randomMember( key );
    }

    /**
     * 从Zset中获取去重的随机元素
     *
     * @since Redis Version: 6.2
     */
    public Flux<String> zRandomDistinctMembers( String key, long count ) {
        return reactiveStringRedisTemplate.opsForZSet().distinctRandomMembers( key, count );
    }

    /**
     * 从Zset中获取随机元素
     *
     * @since Redis Version: 6.2
     */
    public Flux<String> zRandomMembers( String key, long count ) {
        return reactiveStringRedisTemplate.opsForZSet().randomMembers( key, count );
    }

    /**
     * 从Zset中获取一个随机的带分数的元素。
     *
     * @since Redis Version: 6.2
     */
    public Mono<ZSetOperations.TypedTuple<String>> zRandomMemberWithScore( String key ) {
        return reactiveStringRedisTemplate.opsForZSet().randomMemberWithScore( key );
    }

    /**
     * 从Zset中获取随机去重的带分数的元素。
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zRandomDistinctMembersWithScore( String key, long count ) {
        return reactiveStringRedisTemplate.opsForZSet().distinctRandomMembersWithScore( key, count );
    }

    /**
     * 从Zset中获取随机的带分数的元素。
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zRandomMembersWithScore( String key, long count ) {
        return reactiveStringRedisTemplate.opsForZSet().randomMembersWithScore( key, count );
    }

    /**
     * 获取 key 中指定 value 的排名(从小到大排序)
     */
    public Mono<Long> zRank( String key, String value ) {
        return reactiveStringRedisTemplate.opsForZSet().rank( key, value );
    }

    /**
     * 获取 key 中指定 value 的排名(从大到小排序)
     */
    public Mono<Long> zReverseRank( String key, String value ) {
        return reactiveStringRedisTemplate.opsForZSet().reverseRank( key, value );
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从小到大,只有列名)
     */
    public Flux<String> zRange( String key, Range<Long> range ) {
        return reactiveStringRedisTemplate.opsForZSet().range( key, range );
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从小到大,带上分数)
     */
    public Flux<ZSetOperations.TypedTuple<String>> zRangeWithScores( String key, Range<Long> range ) {
        return reactiveStringRedisTemplate.opsForZSet().rangeWithScores( key, range );
    }

    /**
     * 返回从小到大分数范围内的元素不带分数的集合
     */
    public Flux<String> zRangeByScore( String key, Range<Double> range ) {
        return reactiveStringRedisTemplate.opsForZSet().rangeByScore( key, range );
    }

    /**
     * 返回从小到大分数范围内 指定 count 数量的元素不带分数的集合
     */
    public Flux<String> zRangeByScore( String key, Range<Double> range, RedisZSetCommands.Limit limit ) {
        return reactiveStringRedisTemplate.opsForZSet().rangeByScore( key, range, limit );
    }

    /**
     * 获取从小到大分数范围内的 [min,max] 的排序结果集合
     */
    public Flux<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores( String key, Range<Double> range ) {
        return reactiveStringRedisTemplate.opsForZSet().rangeByScoreWithScores( key, range );
    }

    /**
     * 返回从小到大分数范围内 指定 count 数量的元素集合
     */
    public Flux<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores( String key, Range<Double> range,
                                                                            RedisZSetCommands.Limit limit ) {
        return reactiveStringRedisTemplate.opsForZSet().rangeByScoreWithScores( key, range, limit );
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从大到小,只有列名)
     */
    public Flux<String> zReverseRange( String key, Range<Long> range ) {
        return reactiveStringRedisTemplate.opsForZSet().reverseRange( key, range );
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从大到小,带上分数)
     */
    public Flux<ZSetOperations.TypedTuple<String>> zReverseRangeWithScores( String key, Range<Long> range ) {
        return reactiveStringRedisTemplate.opsForZSet().reverseRangeWithScores( key, range );
    }

    /**
     * 获取分数范围内的 [min,max] 的排序结果集合 (从大到小,集合不带分数)
     */
    public Flux<String> zReverseRangeByScore( String key, Range<Double> range ) {
        return reactiveStringRedisTemplate.opsForZSet().reverseRangeByScore( key, range );
    }

    /**
     * 获取分数范围内的 [min,max] 的排序结果集合 (从大到小,集合带分数)
     */
    public Flux<ZSetOperations.TypedTuple<String>> zReverseRangeByScoreWithScores( String key, Range<Double> range ) {
        return reactiveStringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores( key, range );
    }

    /**
     * 返回 分数范围内 指定 count 数量的元素集合, 并且从 offset 下标开始(从大到小,不带分数的集合)
     */
    public Flux<String> zReverseRangeByScore( String key, Range<Double> range, RedisZSetCommands.Limit limit ) {
        return reactiveStringRedisTemplate.opsForZSet().reverseRangeByScore( key, range, limit );
    }

    /**
     * 返回 分数范围内 指定 count 数量的元素集合, 并且从 offset 下标开始(从大到小,带分数的集合)
     */
    public Flux<ZSetOperations.TypedTuple<String>> zReverseRangeByScoreWithScores( String key, Range<Double> range,
                                                                                   RedisZSetCommands.Limit limit ) {
        return reactiveStringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores( key, range, limit );
    }

    /**
     * 增量迭代Zset下的带分数集合
     */
    public Flux<ZSetOperations.TypedTuple<String>> zScan( String key ) {
        return reactiveStringRedisTemplate.opsForZSet().scan( key );
    }

    /**
     * 增量迭代Zset下的带分数集合
     */
    public Flux<ZSetOperations.TypedTuple<String>> zScan( String key, ScanOptions options ) {
        return reactiveStringRedisTemplate.opsForZSet().scan( key, options );
    }

    /**
     * 返回指定分数区间 [min,max] 的元素个数
     */
    public Mono<Long> zCount( String key, Range<Double> range ) {
        return reactiveStringRedisTemplate.opsForZSet().count( key, range );
    }

    /**
     * 返回value区间内的元素个数，按value字典排序
     */
    public Mono<Long> zLexCount( String key, Range<String> range ) {
        return reactiveStringRedisTemplate.opsForZSet().lexCount( key, range );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最低分数的值。
     *
     * @since Redis Version: 6.2
     */
    public Mono<ZSetOperations.TypedTuple<String>> zPopMin( String key ) {
        return reactiveStringRedisTemplate.opsForZSet().popMin( key );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最低分数的值。
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zPopMin( String key, long count ) {
        return reactiveStringRedisTemplate.opsForZSet().popMin( key, count );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最低分数的值。
     *
     * @param timeout 等待直到 key 列表中的条目可用的最大持续时间。 必须是0秒或更大的秒数，不能为空。<br/>
     *                0秒超时可用于无限期等待。 不支持零到一秒之间的持续时间。
     *
     * @since Redis Version: 6.2
     */
    public Mono<ZSetOperations.TypedTuple<String>> zPopMin( String key, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForZSet().popMin( key, timeout );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最高分数的值。
     *
     * @since Redis Version: 6.2
     */
    public Mono<ZSetOperations.TypedTuple<String>> zPopMax( String key ) {
        return reactiveStringRedisTemplate.opsForZSet().popMax( key );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最高分数的值。
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zPopMax( String key, long count ) {
        return reactiveStringRedisTemplate.opsForZSet().popMax( key, count );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最高分数的值。
     *
     * @param timeout 等待直到 key 列表中的条目可用的最大持续时间。 必须是0秒或更大的秒数，不能为空。<br/>
     *                0秒超时可用于无限期等待。 不支持零到一秒之间的持续时间。
     *
     * @since Redis Version: 6.2
     */
    public Mono<ZSetOperations.TypedTuple<String>> zPopMax( String key, Duration timeout ) {
        return reactiveStringRedisTemplate.opsForZSet().popMax( key, timeout );
    }

    /**
     * 返回 zset 集合数量
     */
    public Mono<Long> zSize( String key ) {
        return reactiveStringRedisTemplate.opsForZSet().size( key );
    }

    /**
     * 获取指定成员的 score 值
     */
    public Mono<Double> zScore( String key, String value ) {
        return reactiveStringRedisTemplate.opsForZSet().score( key, value );
    }

    /**
     * 获取指定成员的 score 值
     *
     * @since Redis Version: 6.2
     */
    public Mono<List<Double>> zScore( String key, Object... values ) {
        return reactiveStringRedisTemplate.opsForZSet().score( key, values );
    }

    /**
     * 删除指定索引位置的成员,其中索引按( 从小到大 )
     */
    public Mono<Long> zRemoveRange( String key, Range<Long> range ) {
        return reactiveStringRedisTemplate.opsForZSet().removeRange( key, range );
    }

    /**
     * 删除指定范围的成员,其中成员Value按( 字典排序从小到大 )
     */
    public Mono<Long> zRemoveRangeByLex( String key, Range<String> range ) {
        return reactiveStringRedisTemplate.opsForZSet().removeRangeByLex( key, range );
    }

    /**
     * 删除指定分数的成员,其中分数按( 从小到大 )
     */
    public Mono<Long> zRemoveRangeByScore( String key, Range<Double> range ) {
        return reactiveStringRedisTemplate.opsForZSet().removeRangeByScore( key, range );
    }

    /**
     * 获取两个Zset内的差集值
     *
     * @since Redis Version: 6.2
     */
    public Flux<String> zDifference( String key, String otherKey ) {
        return reactiveStringRedisTemplate.opsForZSet().difference( key, otherKey );
    }

    /**
     * 获取多个Zset内的差集值
     *
     * @since Redis Version: 6.2
     */
    public Flux<String> zDifference( String key, Collection<String> otherKeys ) {
        return reactiveStringRedisTemplate.opsForZSet().difference( key, otherKeys );
    }

    /**
     * 获取两个Zset内的差集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zDifferenceWithScores( String key, String otherKey ) {
        return reactiveStringRedisTemplate.opsForZSet().differenceWithScores( key, otherKey );
    }

    /**
     * 获取多个Zset内的差集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zDifferenceWithScores( String key, Collection<String> otherKeys ) {
        return reactiveStringRedisTemplate.opsForZSet().differenceWithScores( key, otherKeys );
    }

    /**
     * 将两个Zset内的差集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Mono<Long> zDifferenceAndStore( String key, String otherKey, String destKey ) {
        return reactiveStringRedisTemplate.opsForZSet().differenceAndStore( key, otherKey, destKey );
    }

    /**
     * 将多个Zset内的差集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Mono<Long> zDifferenceAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return reactiveStringRedisTemplate.opsForZSet().differenceAndStore( key, otherKeys, destKey );
    }

    /**
     * 获取两个Zset内的交集值
     *
     * @since Redis Version: 6.2
     */
    public Flux<String> zIntersect( String key, String otherKey ) {
        return reactiveStringRedisTemplate.opsForZSet().intersect( key, otherKey );
    }

    /**
     * 获取多个Zset内的交集值
     *
     * @since Redis Version: 6.2
     */
    public Flux<String> zIntersect( String key, Collection<String> otherKeys ) {
        return reactiveStringRedisTemplate.opsForZSet().intersect( key, otherKeys );
    }

    /**
     * 获取两个Zset内的交集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zIntersectWithScores( String key, String otherKey ) {
        return reactiveStringRedisTemplate.opsForZSet().intersectWithScores( key, otherKey );
    }

    /**
     * 获取多个Zset内的交集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zIntersectWithScores( String key, Collection<String> otherKeys ) {
        return reactiveStringRedisTemplate.opsForZSet().intersectWithScores( key, otherKeys );
    }

    /**
     * 获取多个Zset内的交集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zIntersectWithScores( String key, Collection<String> otherKeys,
                                                                         RedisZSetCommands.Aggregate aggregate ) {
        return reactiveStringRedisTemplate.opsForZSet().intersectWithScores( key, otherKeys, aggregate );
    }

    /**
     * 获取多个Zset内的交集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zIntersectWithScores( String key, Collection<String> otherKeys,
                                                                         RedisZSetCommands.Aggregate aggregate,
                                                                         RedisZSetCommands.Weights weights ) {
        return reactiveStringRedisTemplate.opsForZSet().intersectWithScores( key, otherKeys, aggregate, weights );
    }

    /**
     * 将两个Zset内的交集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Mono<Long> zIntersectAndStore( String key, String otherKey, String destKey ) {
        return reactiveStringRedisTemplate.opsForZSet().intersectAndStore( key, otherKey, destKey );
    }

    /**
     * 将多个Zset内的交集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Mono<Long> zIntersectAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return reactiveStringRedisTemplate.opsForZSet().intersectAndStore( key, otherKeys, destKey );
    }

    /**
     * 将多个Zset内的交集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Mono<Long> zIntersectAndStore( String key, Collection<String> otherKeys, String destKey,
                                          RedisZSetCommands.Aggregate aggregate ) {
        return reactiveStringRedisTemplate.opsForZSet().intersectAndStore( key, otherKeys, destKey, aggregate );
    }

    /**
     * 将多个Zset内的交集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Mono<Long> zIntersectAndStore( String key, Collection<String> otherKeys, String destKey,
                                          RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights ) {
        return reactiveStringRedisTemplate.opsForZSet().intersectAndStore( key, otherKeys, destKey, aggregate, weights );
    }

    /**
     * 获取两个Zset内的并集值
     *
     * @since Redis Version: 6.2
     */
    public Flux<String> zUnion( String key, String otherKey ) {
        return reactiveStringRedisTemplate.opsForZSet().union( key, otherKey );
    }

    /**
     * 获取多个Zset内的并集值
     *
     * @since Redis Version: 6.2
     */
    public Flux<String> zUnion( String key, Collection<String> otherKeys ) {
        return reactiveStringRedisTemplate.opsForZSet().union( key, otherKeys );
    }

    /**
     * 获取两个Zset内的并集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zUnionWithScores( String key, String otherKey ) {
        return reactiveStringRedisTemplate.opsForZSet().unionWithScores( key, otherKey );
    }

    /**
     * 获取多个Zset内的并集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zUnionWithScores( String key, Collection<String> otherKeys ) {
        return reactiveStringRedisTemplate.opsForZSet().unionWithScores( key, otherKeys );
    }

    /**
     * 获取多个Zset内的并集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zUnionWithScores( String key, Collection<String> otherKeys,
                                                                     RedisZSetCommands.Aggregate aggregate ) {
        return reactiveStringRedisTemplate.opsForZSet().unionWithScores( key, otherKeys, aggregate );
    }

    /**
     * 获取多个Zset内的并集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Flux<ZSetOperations.TypedTuple<String>> zUnionWithScores( String key, Collection<String> otherKeys,
                                                                     RedisZSetCommands.Aggregate aggregate,
                                                                     RedisZSetCommands.Weights weights ) {
        return reactiveStringRedisTemplate.opsForZSet().unionWithScores( key, otherKeys, aggregate, weights );
    }

    /**
     * 将两个Zset内的并集值和分数放入destKey中
     */
    public Mono<Long> zUnionAndStore( String key, String otherKey, String destKey ) {
        return reactiveStringRedisTemplate.opsForZSet().unionAndStore( key, otherKey, destKey );
    }

    /**
     * 将多个Zset内的并集值和分数放入destKey中
     */
    public Mono<Long> zUnionAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return reactiveStringRedisTemplate.opsForZSet().unionAndStore( key, otherKeys, destKey );
    }

    /**
     * 将多个Zset内的并集值和分数放入destKey中
     */
    public Mono<Long> zUnionAndStore( String key, Collection<String> otherKeys, String destKey,
                                      RedisZSetCommands.Aggregate aggregate ) {
        return reactiveStringRedisTemplate.opsForZSet().unionAndStore( key, otherKeys, destKey, aggregate );
    }

    /**
     * 将多个Zset内的并集值和分数放入destKey中
     */
    public Mono<Long> zUnionAndStore( String key, Collection<String> otherKeys, String destKey,
                                      RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights ) {
        return reactiveStringRedisTemplate.opsForZSet().unionAndStore( key, otherKeys, destKey, aggregate, weights );
    }

    // ---------------------- geo 地理位置信息 ---------------------

    /**
     * 删除一个或多个成员
     */
    public Mono<Long> geoRemove( String key, String... members ) {
        return reactiveStringRedisTemplate.opsForGeo().remove( key, members );
    }

    /**
     * 将具有给定成员名称的一个点添加到键
     */
    public Mono<Long> geoAdd( String key, Point point, String member ) {
        return reactiveStringRedisTemplate.opsForGeo().add( key, point, member );
    }

    /**
     * 将具有给定成员名称的点添加到键
     *
     * @see RedisGeoCommands.GeoLocation
     */
    public Mono<Long> geoAdd( String key, RedisGeoCommands.GeoLocation<String> location ) {
        return reactiveStringRedisTemplate.opsForGeo().add( key, location );
    }

    /**
     * 将具有给定成员名称的多个点添加到键
     */
    public Mono<Long> geoAdd( String key, Map<String, Point> memberCoordinateMap ) {
        return reactiveStringRedisTemplate.opsForGeo().add( key, memberCoordinateMap );
    }

    /**
     * 将具有给定成员名称的多个点添加到键
     */
    public Mono<Long> geoAdd( String key, Iterable<RedisGeoCommands.GeoLocation<String>> locations ) {
        return reactiveStringRedisTemplate.opsForGeo().add( key, locations );
    }

    /**
     * 将具有给定成员名称的多个点添加到键
     */
    public Flux<Long> geoAdd( String key, Publisher<? extends Collection<RedisGeoCommands.GeoLocation<String>>> locations ) {
        return reactiveStringRedisTemplate.opsForGeo().add( key, locations );
    }

    /**
     * 获取两个成员之间的距离
     */
    public Mono<Distance> geoDistance( String key, String member1, String member2 ) {
        return reactiveStringRedisTemplate.opsForGeo().distance( key, member1, member2 );
    }

    /**
     * 获取给定指标中两个成员之间的距离。
     */
    public Mono<Distance> geoDistance( String key, String member1, String member2, Metric metric ) {
        return reactiveStringRedisTemplate.opsForGeo().distance( key, member1, member2, metric );
    }

    /**
     * 获取一个成员的位置GeoHash标识。
     */
    public Mono<String> geoHash( String key, String member ) {
        return reactiveStringRedisTemplate.opsForGeo().hash( key, member );
    }

    /**
     * 获取多个成员的位置GeoHash标识。
     */
    public Mono<List<String>> geoHash( String key, String... members ) {
        return reactiveStringRedisTemplate.opsForGeo().hash( key, members );
    }

    /**
     * 获取一个成员的位置的点标识
     */
    public Mono<Point> geoPosition( String key, String member ) {
        return reactiveStringRedisTemplate.opsForGeo().position( key, member );
    }

    /**
     * 获取多个成员的位置的点标识
     */
    public Mono<List<Point>> geoPosition( String key, String... members ) {
        return reactiveStringRedisTemplate.opsForGeo().position( key, members );
    }

    /**
     * 获取限定圆圈内的成员位置。
     */
    public Flux<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoSearch( String key, Circle within ) {
        return reactiveStringRedisTemplate.opsForGeo().search( key, within );
    }

    /**
     *
     */
    public Flux<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoSearch( String key, GeoReference<String> reference,
                                                                            Distance radius ) {
        return reactiveStringRedisTemplate.opsForGeo().search( key, reference, radius );
    }

    /**
     *
     */
    public Flux<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoSearch( String key, GeoReference<String> reference,
                                                                            Distance radius,
                                                                            RedisGeoCommands.GeoRadiusCommandArgs args ) {
        return reactiveStringRedisTemplate.opsForGeo().search( key, reference, radius, args );
    }

    /**
     *
     */
    public Flux<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoSearch( String key, GeoReference<String> reference,
                                                                            BoundingBox boundingBox ) {
        return reactiveStringRedisTemplate.opsForGeo().search( key, reference, boundingBox );
    }

    /**
     *
     */
    public Flux<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoSearch( String key, GeoReference<String> reference,
                                                                            BoundingBox boundingBox,
                                                                            RedisGeoCommands.GeoRadiusCommandArgs args ) {
        return reactiveStringRedisTemplate.opsForGeo().search( key, reference, boundingBox, args );
    }

    public Flux<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoSearch( String key, GeoReference<String> reference,
                                                                            GeoShape geoPredicate,
                                                                            RedisGeoCommands.GeoRadiusCommandArgs args ) {
        return reactiveStringRedisTemplate.opsForGeo().search( key, reference, geoPredicate, args );
    }

    public Mono<Long> geoSearchAndStore( String key, String destKey, Circle within ) {
        return reactiveStringRedisTemplate.opsForGeo().searchAndStore( key, destKey, within );
    }

    public Mono<Long> geoSearchAndStore( String key, String destKey, GeoReference<String> reference, Distance radius ) {
        return reactiveStringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, radius );
    }

    public Mono<Long> geoSearchAndStore( String key, String destKey, GeoReference<String> reference, Distance radius,
                                         RedisGeoCommands.GeoSearchStoreCommandArgs args ) {
        return reactiveStringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, radius, args );
    }

    public Mono<Long> geoSearchAndStore( String key, String destKey, GeoReference<String> reference, BoundingBox boundingBox ) {
        return reactiveStringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, boundingBox );
    }

    public Mono<Long> geoSearchAndStore( String key, String destKey, GeoReference<String> reference, BoundingBox boundingBox,
                                         RedisGeoCommands.GeoSearchStoreCommandArgs args ) {
        return reactiveStringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, boundingBox, args );
    }

    public Mono<Long> geoSearchAndStore( String key, String destKey, GeoReference<String> reference, GeoShape geoPredicate,
                                         RedisGeoCommands.GeoSearchStoreCommandArgs args ) {
        return reactiveStringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, geoPredicate, args );
    }

    // ---------------------- HyperLogLog 基数统计 ---------------------

    public Mono<Long> hyperAdd( String key, String... values ) {
        return reactiveStringRedisTemplate.opsForHyperLogLog().add( key, values );
    }

    public Mono<Long> hyperSize( String... keys ) {
        return reactiveStringRedisTemplate.opsForHyperLogLog().size( keys );
    }

    /**
     * 将给定SourceKey的所有值合并到 Destination 键中
     */
    public Mono<Boolean> hyperUnion( String destination, String... sourceKeys ) {
        return reactiveStringRedisTemplate.opsForHyperLogLog().union( destination, sourceKeys );
    }

    // ---------------------- Stream 发布/订阅消息 ---------------------

    public Mono<Long> streamAcknowledge( String key, String group, String... recordIds ) {
        return reactiveStringRedisTemplate.opsForStream().acknowledge( key, group, recordIds );
    }

    public Mono<Long> streamAcknowledge( String key, String group, RecordId... recordIds ) {
        return reactiveStringRedisTemplate.opsForStream().acknowledge( key, group, recordIds );
    }

    public Mono<Long> streamAcknowledge( String group, Record<String, ?> record ) {
        return reactiveStringRedisTemplate.opsForStream().acknowledge( group, record );
    }

    public Flux<RecordId> streamAdd( String key, Publisher<Map<String, String>> bodyPublisher ) {
        return reactiveStringRedisTemplate.opsForStream().add( key, bodyPublisher );
    }

    public Mono<RecordId> streamAdd( String key, Map<String, String> content ) {
        return reactiveStringRedisTemplate.opsForStream().add( key, content );
    }

    public Mono<RecordId> streamAdd( MapRecord<String, String, String> record ) {
        return reactiveStringRedisTemplate.opsForStream().add( record );
    }

    public Mono<RecordId> streamAdd( Record<String, ?> record ) {
        return reactiveStringRedisTemplate.opsForStream().add( record );
    }

    public Mono<Long> streamDelete( String key, String... recordIds ) {
        return reactiveStringRedisTemplate.opsForStream().delete( key, recordIds );
    }

    public Mono<Long> streamDelete( Record<String, ?> record ) {
        return reactiveStringRedisTemplate.opsForStream().delete( record );
    }

    public Mono<Long> streamDelete( String key, RecordId... recordIds ) {
        return reactiveStringRedisTemplate.opsForStream().delete( key, recordIds );
    }

    public Mono<String> streamCreateGroup( String key, String group ) {
        return reactiveStringRedisTemplate.opsForStream().createGroup( key, group );
    }

    public Mono<String> streamCreateGroup( String key, ReadOffset readOffset, String group ) {
        return reactiveStringRedisTemplate.opsForStream().createGroup( key, readOffset, group );
    }

    public Mono<String> streamDestroyGroup( String key, String group ) {
        return reactiveStringRedisTemplate.opsForStream().destroyGroup( key, group );
    }

    public Mono<String> streamDeleteConsumer( String key, Consumer consumer ) {
        return reactiveStringRedisTemplate.opsForStream().deleteConsumer( key, consumer );
    }

    public Flux<StreamInfo.XInfoConsumer> streamConsumers( String key, String group ) {
        return reactiveStringRedisTemplate.opsForStream().consumers( key, group );
    }

    public Flux<StreamInfo.XInfoGroup> streamGroups( String key ) {
        return reactiveStringRedisTemplate.opsForStream().groups( key );
    }

    public Mono<StreamInfo.XInfoStream> streamInfo( String key ) {
        return reactiveStringRedisTemplate.opsForStream().info( key );
    }

    public Mono<PendingMessagesSummary> streamPending( String key, String group ) {
        return reactiveStringRedisTemplate.opsForStream().pending( key, group );
    }

    public Mono<PendingMessages> streamPending( String key, Consumer consumer ) {
        return reactiveStringRedisTemplate.opsForStream().pending( key, consumer );
    }

    public Mono<PendingMessages> streamPending( String key, String group, Range<?> range, long count ) {
        return reactiveStringRedisTemplate.opsForStream().pending( key, group, range, count );
    }

    public Mono<PendingMessages> streamPending( String key, Consumer consumer, Range<?> range, long count ) {
        return reactiveStringRedisTemplate.opsForStream().pending( key, consumer, range, count );
    }

    public Mono<Long> streamSize( String key ) {
        return reactiveStringRedisTemplate.opsForStream().size( key );
    }

    public Flux<MapRecord<String, Object, Object>> streamRange( String key, Range<String> range ) {
        return reactiveStringRedisTemplate.opsForStream().range( key, range );
    }

    public Flux<MapRecord<String, Object, Object>> streamRange( String key, Range<String> range, RedisZSetCommands.Limit limit ) {
        return reactiveStringRedisTemplate.opsForStream().range( key, range, limit );
    }

    public <V> Flux<ObjectRecord<String, V>> streamRange( Class<V> targetType, String key, Range<String> range ) {
        return reactiveStringRedisTemplate.opsForStream().range( targetType, key, range );
    }

    public <V> Flux<ObjectRecord<String, V>> streamRange( Class<V> targetType, String key, Range<String> range,
                                                          RedisZSetCommands.Limit limit ) {
        return reactiveStringRedisTemplate.opsForStream().range( targetType, key, range, limit );
    }

    public Flux<MapRecord<String, Object, Object>> streamRead( StreamOffset<String> stream ) {
        return reactiveStringRedisTemplate.opsForStream().read( stream );
    }

    public <V> Flux<ObjectRecord<String, V>> streamRead( Class<V> targetType, StreamOffset<String> stream ) {
        return reactiveStringRedisTemplate.opsForStream().read( targetType, stream );
    }

    public Flux<MapRecord<String, Object, Object>> streamRead( StreamOffset<String>... streams ) {
        return reactiveStringRedisTemplate.opsForStream().read( streams );
    }

    public <V> Flux<ObjectRecord<String, V>> streamRead( Class<V> targetType, StreamOffset<String>... streams ) {
        return reactiveStringRedisTemplate.opsForStream().read( targetType, streams );
    }

    public Flux<MapRecord<String, Object, Object>> streamRead( StreamReadOptions readOptions, StreamOffset<String>... streams ) {
        return reactiveStringRedisTemplate.opsForStream().read( readOptions, streams );
    }

    public <V> Flux<ObjectRecord<String, V>> streamRead( Class<V> targetType, StreamReadOptions readOptions,
                                                         StreamOffset<String>... streams ) {
        return reactiveStringRedisTemplate.opsForStream().read( targetType, readOptions, streams );
    }

    public Flux<MapRecord<String, Object, Object>> streamRead( Consumer consumer, StreamOffset<String>... streams ) {
        return reactiveStringRedisTemplate.opsForStream().read( consumer, streams );
    }

    public <V> Flux<ObjectRecord<String, V>> streamRead( Class<V> targetType, Consumer consumer,
                                                         StreamOffset<String>... streams ) {
        return reactiveStringRedisTemplate.opsForStream().read( targetType, consumer, streams );
    }

    public Flux<MapRecord<String, Object, Object>> streamRead( Consumer consumer, StreamReadOptions readOptions,
                                                               StreamOffset<String>... streams ) {
        return reactiveStringRedisTemplate.opsForStream().read( consumer, readOptions, streams );
    }

    public <V> Flux<ObjectRecord<String, V>> streamRead( Class<V> targetType, Consumer consumer, StreamReadOptions readOptions,
                                                         StreamOffset<String>... streams ) {
        return reactiveStringRedisTemplate.opsForStream().read( targetType, consumer, readOptions, streams );
    }

    public Flux<MapRecord<String, Object, Object>> streamReverseRange( String key, Range<String> range ) {
        return reactiveStringRedisTemplate.opsForStream().reverseRange( key, range );
    }

    public Flux<MapRecord<String, Object, Object>> streamReverseRange( String key, Range<String> range,
                                                                       RedisZSetCommands.Limit limit ) {
        return reactiveStringRedisTemplate.opsForStream().reverseRange( key, range, limit );
    }

    public <V> Flux<ObjectRecord<String, V>> streamReverseRange( Class<V> targetType, String key, Range<String> range ) {
        return reactiveStringRedisTemplate.opsForStream().reverseRange( targetType, key, range );
    }

    public <V> Flux<ObjectRecord<String, V>> streamReverseRange( Class<V> targetType, String key, Range<String> range,
                                                                 RedisZSetCommands.Limit limit ) {
        return reactiveStringRedisTemplate.opsForStream().reverseRange( targetType, key, range, limit );
    }

    public Mono<Long> streamTrim( String key, long count ) {
        return reactiveStringRedisTemplate.opsForStream().trim( key, count );
    }

    public Mono<Long> streamTrim( String key, long count, boolean approximateTrimming ) {
        return reactiveStringRedisTemplate.opsForStream().trim( key, count, approximateTrimming );
    }

    public <V> ObjectRecord<String, V> streamMap( MapRecord<String, Object, Object> record, Class<V> targetType ) {
        return reactiveStringRedisTemplate.opsForStream().map( record, targetType );
    }

    public <V> HashMapper<V, Object, Object> streamGetHashMapper( Class<V> targetType ) {
        return reactiveStringRedisTemplate.opsForStream().getHashMapper( targetType );
    }

    public MapRecord<String, Object, Object> streamDeserializeRecord( ByteBufferRecord record ) {
        return reactiveStringRedisTemplate.opsForStream().deserializeRecord( record );
    }

    // ---------------------- Redis 分布式锁 ---------------------

    public Mono<Boolean> lock( String key, int timeOutSec ) {
        return strSetIfAbsent( "lock:".concat( key ), key, Duration.ofSeconds( timeOutSec ) );
    }

    public Mono<Long> unLock( String key ) {
        return delete( "lock:".concat( key ) );
    }
}
