package tv.game88.common.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.domain.geo.BoundingBox;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings( "unused" )
@Log4j2
@Component
public class RedisUtils {
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisUtils( StringRedisTemplate stringRedisTemplate ) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public RedisConnectionFactory getConnectionFactory() {
        return stringRedisTemplate.getConnectionFactory();
    }

    public <T> T execute( RedisCallback<T> redisCallback ) {
        return stringRedisTemplate.execute( redisCallback );
    }

    public <T> T execute( SessionCallback<T> sessionCallback ) {
        return stringRedisTemplate.execute( sessionCallback );
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
    public Set<String> keys( String s ) {
        return stringRedisTemplate.keys( s );
    }

    /**
     * 给key附加过期时间
     */
    public Boolean expire( String key, Duration timeout ) {
        return stringRedisTemplate.expire( key, timeout );
    }

    /**
     * 给key指定到期时间
     */
    public Boolean expireAt( String key, Instant expireAt ) {
        return stringRedisTemplate.expireAt( key, expireAt );
    }

    /**
     * 移除指定key的过期时间
     */
    public Boolean persist( String key ) {
        return stringRedisTemplate.persist( key );
    }

    /**
     * 获取指定key的过期时间
     */
    public Long getExpire( String key ) {
        return stringRedisTemplate.getExpire( key );
    }

    /**
     * 修改key名
     */
    public void rename( String key, String newKey ) {
        stringRedisTemplate.rename( key, newKey );
    }

    /**
     * 修改key名，如果key不存在，将报错
     */
    public Boolean renameIfAbsent( String key, String newKey ) {
        return stringRedisTemplate.renameIfAbsent( key, newKey );
    }

    /**
     * 删除一个或多个键
     * <p>如果已知键的值体量特别小，可以使用del命令，否则请使用 {@link #unlink}</p>
     */
    public Long delete( Collection<String> keys ) {
        return stringRedisTemplate.delete( keys );
    }

    /**
     * 删除一个键
     * <p>如果已知键的值体量特别小，可以使用del命令，否则请使用 {@link #unlink}</p>
     */
    public Boolean delete( String key ) {
        return stringRedisTemplate.delete( key );
    }

    /**
     * 是否存在key
     */
    public Boolean exists( String key ) {
        return stringRedisTemplate.hasKey( key );
    }

    /**
     * 从当前数据库中随机返回一个 key，当数据库为空时，返回null。
     */
    public String randomKey() {
        return stringRedisTemplate.randomKey();
    }

    /**
     * 返回 key 所储存的值的类型，当key不存在时，返回类型是{@link DataType "none"}
     */
    public DataType type( String key ) {
        return stringRedisTemplate.type( key );
    }

    /**
     * 删除一个或多个key-value，但是，相比DEL会产生阻塞，该命令会在另一个线程中回收内存，因此它是非阻塞的。
     */
    public Long unlink( Collection<String> keys ) {
        return stringRedisTemplate.unlink( keys );
    }

    /**
     * 删除一个或多个key-value，但是，相比DEL会产生阻塞，该命令会在另一个线程中回收内存，因此它是非阻塞的。
     */
    public Boolean unlink( String key ) {
        return stringRedisTemplate.unlink( key );
    }

    /**
     * 扫描返回所有键
     *
     * @return 所有键
     */
    public Cursor<String> scan() {
        return stringRedisTemplate.scan( ScanOptions.NONE );
    }

    /**
     * 扫描返回所有键
     *
     * @param scanOptions 模式匹配
     *
     * @return 所有匹配到的键
     */
    public Cursor<String> scan( ScanOptions scanOptions ) {
        return stringRedisTemplate.scan( scanOptions );
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
    public Boolean copy( String sourceKey, String targetKey, boolean replace ) {
        return stringRedisTemplate.copy( sourceKey, targetKey, replace );
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
    public Boolean move( String key, int dbIndex ) {
        return stringRedisTemplate.move( key, dbIndex );
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
    public void convertAndSend( String channel, String message ) {
        stringRedisTemplate.convertAndSend( channel, message );
    }

    // ---------------------- string操作 ---------------------

    /**
     * 设置 String 类型 key-value
     */
    public void strSet( String key, String value ) {
        stringRedisTemplate.opsForValue().set( key, value );
    }

    /**
     * 设置 String 类型 key-value 并添加过期时间
     *
     * @param timeout 过期时间
     */
    public void strSet( String key, String value, Duration timeout ) {
        stringRedisTemplate.opsForValue().set( key, value, timeout );
    }

    /**
     * 只在键不存在的情况下，将键的值设置为value。<br/> 若键已经存在，则不做任何动作。
     */
    public Boolean strSetIfAbsent( String key, String value ) {
        return stringRedisTemplate.opsForValue().setIfAbsent( key, value );
    }

    /**
     * 只在键不存在的情况下，将键的值设置为value，并过期时间。<br/> 若键已经存在， 则不做任何动作。
     *
     * @param timeout 过期时间
     */
    public Boolean strSetIfAbsent( String key, String value, Duration timeout ) {
        return stringRedisTemplate.opsForValue().setIfAbsent( key, value, timeout );
    }

    /**
     * 只在键存在的情况下，将键的值设置为value。<br/> 若键不存在，则不做任何动作。
     */
    public Boolean strSetIfPresent( String key, String value ) {
        return stringRedisTemplate.opsForValue().setIfPresent( key, value );
    }

    /**
     * 只在键存在的情况下，将键的值设置为value，并过期时间。<br/> 若键不存在，则不做任何动作。
     *
     * @param timeout 过期时间
     */
    public Boolean strSetIfPresent( String key, String value, Duration timeout ) {
        return stringRedisTemplate.opsForValue().setIfPresent( key, value, timeout );
    }

    /**
     * 批量添加 key-value，如果键已经存在，则会覆盖原有的值
     */
    public void strMSet( Map<String, String> keyAndValue ) {
        stringRedisTemplate.opsForValue().multiSet( keyAndValue );
    }

    /**
     * 批量添加 key-value 只要有一个键已经存在，则全部不添加
     */
    public Boolean strMSetIfAbsent( Map<String, String> keyAndValue ) {
        return stringRedisTemplate.opsForValue().multiSetIfAbsent( keyAndValue );
    }

    /**
     * 获取 String 类型 key-value
     */
    public String strGet( String key ) {
        return stringRedisTemplate.opsForValue().get( key );
    }

    /**
     * 获取String类型的键的值，然后删除此键
     * <p>如果此键不存在,则返回null</p>
     *
     * @since Redis Version: 6.2
     */
    public String strGetAndDelete( String key ) {
        return stringRedisTemplate.opsForValue().getAndDelete( key );
    }

    /**
     * 获取String类型的键的值，然后附加过期时间
     * <p>如果此键不存在,则返回null</p>
     *
     * @since Redis Version: 6.2
     */
    public String strGetAndExpire( String key, Duration timeout ) {
        return stringRedisTemplate.opsForValue().getAndExpire( key, timeout );
    }

    /**
     * 获取String类型的键的值，然后移除过期时间
     * <p>如果此键不存在,则返回null</p>
     *
     * @since Redis Version: 6.2
     */
    public String strGetAndPersist( String key ) {
        return stringRedisTemplate.opsForValue().getAndPersist( key );
    }

    /**
     * 批量获取键的值，如果某个键不存在，则以null的形式返回，数量会保持一致
     */
    public List<String> strMGet( Collection<String> keys ) {
        return stringRedisTemplate.opsForValue().multiGet( keys );
    }

    /**
     * 对一个键的值进行加 1 操作并返回修改后的值，如果该键不存在 将创建一个键并赋值 1 如果键储存的值不能被解释为数字，将报错
     */
    public Long strIncrement( String key ) {
        return stringRedisTemplate.opsForValue().increment( key );
    }

    /**
     * 对一个键的值进行加number操作并返回修改后的值，如果该键不存在 将创建一个键并赋值该 number
     */
    public Long strIncrement( String key, long number ) {
        return stringRedisTemplate.opsForValue().increment( key, number );
    }

    /**
     * 对一个键的值进行加number操作并返回修改后的值，如果该键不存在 将创建一个键并赋值该 number
     */
    public Double strIncrement( String key, double number ) {
        return stringRedisTemplate.opsForValue().increment( key, number );
    }

    /**
     * 对一个键的值进行减1操作并返回修改后的值，如果该键不存在 将创建一个键并赋值-1 如果键储存的值不能被解释为数字，将报错
     */
    public Long strDecrement( String key ) {
        return stringRedisTemplate.opsForValue().decrement( key );
    }

    /**
     * 对一个键的值进行加操作并返回修改后的值，如果该键不存在 将创建一个键并赋值 -number 如果键储存的值不能被解释为数字，将报错
     */
    public Long strDecrement( String key, long number ) {
        return stringRedisTemplate.opsForValue().decrement( key, number );
    }

    /**
     * 对一个String类型键的值进行追加操作，如果该键不存在 将创建一个键并赋值value，返回value的长度
     */
    public Integer strAppend( String key, String value ) {
        return stringRedisTemplate.opsForValue().append( key, value );
    }

    /**
     * 获取 String 类型 value 指定的偏移量
     *
     * @see <a href="http://doc.redisfans.com/string/getrange.html">Document：GETRANGE</a>
     */
    public String strGetRange( String key, long start, long end ) {
        return stringRedisTemplate.opsForValue().get( key, start, end );
    }

    /**
     * 设置 String 类型 key-value，将 value 设置到指定的偏移量上
     *
     * @see <a href="http://doc.redisfans.com/string/setrange.html">Document：SETRANGE</a>
     */
    public void strSetRange( String key, String value, long offset ) {
        stringRedisTemplate.opsForValue().set( key, value, offset );
    }

    /**
     * 返回键的值的长度，如果该 key 不存在，返回 0
     */
    public Long strSize( String key ) {
        return stringRedisTemplate.opsForValue().size( key );
    }

    /**
     * 设置或清除存储在键的字符串值中offset的bit。 当键不存在时，创建一个新的字符串值
     *
     * @see <a href="http://doc.redisfans.com/string/getbit.html">Document：SETBIT</a>
     */
    public Boolean strSetBit( String key, long offset, boolean value ) {
        return stringRedisTemplate.opsForValue().setBit( key, offset, value );
    }

    /**
     * 对键所储存的字符串值，获取指定偏移量上的bit。当 offset 比字符串值的长度大，或者 key 不存在时，返回false
     *
     * @see <a href="http://doc.redisfans.com/string/getbit.html">Document：GETBIT</a>
     */
    public Boolean strGetBit( String key, long offset ) {
        return stringRedisTemplate.opsForValue().getBit( key, offset );
    }

    /**
     * 把Redis字符串当作位数组，并能对变长位宽和任意未字节对齐的指定整型位域进行寻址。
     *
     * @see <a href="http://www.redis.cn/commands/bitfield.html">Document：BITFIELD</a>
     * @since Redis Version: 3.2.0
     */
    public List<Long> strBitField( String key, BitFieldSubCommands command ) {
        return stringRedisTemplate.opsForValue().bitField( key, command );
    }

    // ---------------------- hash操作 ---------------------

    /**
     * 添加 Hash 键值对
     */
    public void hSet( String key, String hashKey, String value ) {
        stringRedisTemplate.opsForHash().put( key, hashKey, value );
    }

    /**
     * 批量添加 hash 的键值对 有则覆盖,没有则添加
     */
    public void hMSet( String key, Map<String, String> map ) {
        stringRedisTemplate.opsForHash().putAll( key, map );
    }

    /**
     * 添加 hash 键值对. 若有任意hashKey存在，则不做任何动作。
     */
    public Boolean hSetIfAbsent( String key, String hashKey, String value ) {
        return stringRedisTemplate.opsForHash().putIfAbsent( key, hashKey, value );
    }

    /**
     * 删除指定键的HashKey
     *
     * @return 删除成功的 数量
     */
    public Long hRemove( String key, Object... hashKeys ) {
        return stringRedisTemplate.opsForHash().delete( key, hashKeys );
    }

    /**
     * 判断hashKey是否存在于键内
     */
    public Boolean hHasKey( String key, String hashKey ) {
        return stringRedisTemplate.opsForHash().hasKey( key, hashKey );
    }

    /**
     * 获取指定键下的hashKey的值
     */
    public Object hGet( String key, String hashKey ) {
        return stringRedisTemplate.opsForHash().get( key, hashKey );
    }

    /**
     * 获取指定键下的hashKey的值，如果某个hashKey不存在，则值会是 null
     */
    public List<Object> hMGet( String key, Collection<Object> hashKeys ) {
        return stringRedisTemplate.opsForHash().multiGet( key, hashKeys );
    }

    /**
     * 给指定键下的hashKey的值做加操作
     */
    public Long hIncrement( String key, String hashKey, long number ) {
        return stringRedisTemplate.opsForHash().increment( key, hashKey, number );
    }

    /**
     * 给指定键下的hashKey的值做加操作
     */
    public Double hIncrement( String key, Object hashKey, double number ) {
        return stringRedisTemplate.opsForHash().increment( key, hashKey, number );
    }

    /**
     * 返回一个键下随机的hashKey的值
     *
     * @since Redis Version: 6.2
     */
    public Object hRandomKey( String key ) {
        return stringRedisTemplate.opsForHash().randomKey( key );
    }

    /**
     * 返回指定数量的键下随机的hashKey的值
     *
     * @since Redis Version: 6.2
     */
    public Object hRandomKeys( String key, long count ) {
        return stringRedisTemplate.opsForHash().randomKeys( key, count );
    }

    /**
     * 返回一个键下随机的键值对
     *
     * @since Redis Version: 6.2
     */
    public Map.Entry<Object, Object> hRandomEntry( String key ) {
        return stringRedisTemplate.opsForHash().randomEntry( key );
    }

    /**
     * 返回一个键下随机的键值对
     *
     * @since Redis Version: 6.2
     */
    public Map<Object, Object> hRandomEntries( String key, long count ) {
        return stringRedisTemplate.opsForHash().randomEntries( key, count );
    }

    /**
     * 获取指定键下的键值对数量
     */
    public Long hSize( String key ) {
        return stringRedisTemplate.opsForHash().size( key );
    }

    /**
     * 获取键下所有 hashKey 字段名
     */
    public Set<Object> hKeys( String key ) {
        return stringRedisTemplate.opsForHash().keys( key );
    }

    /**
     * 获取键下的 hashKey 的值
     */
    public List<Object> hValues( String key ) {
        return stringRedisTemplate.opsForHash().values( key );
    }

    /**
     * 获取键下的所有键值对
     */
    public Map<Object, Object> hGetAll( String key ) {
        return stringRedisTemplate.opsForHash().entries( key );
    }

    /**
     * 增量迭代 hash 下面的 键值对
     */
    public Cursor<Map.Entry<Object, Object>> hScan( String key ) {
        return stringRedisTemplate.opsForHash().scan( key, ScanOptions.NONE );
    }

    /**
     * 增量迭代 hash 下面的 键值对
     */
    public Cursor<Map.Entry<Object, Object>> hScan( String key, ScanOptions options ) {
        return stringRedisTemplate.opsForHash().scan( key, options );
    }

    // ---------------------- list有序集合 ---------------------

    /**
     * 获取指定列表数量
     */
    public Long lSize( String key ) {
        return stringRedisTemplate.opsForList().size( key );
    }

    /**
     * 获取列表指定下标内的所有值
     */
    public List<String> lRange( String key, long start, long end ) {
        return stringRedisTemplate.opsForList().range( key, start, end );
    }

    /**
     * 删除列表指定下标以外的所有元素
     */
    public void lTrim( String key, long start, long end ) {
        stringRedisTemplate.opsForList().trim( key, start, end );
    }

    /**
     * 指定 list 从左入栈
     *
     * @return 当前队列的长度
     */
    public Long lLeftPush( String key, String value ) {
        return stringRedisTemplate.opsForList().leftPush( key, value );
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
    public Long lLeftPush( String key, String pivot, String value ) {
        return stringRedisTemplate.opsForList().leftPush( key, pivot, value );
    }

    /**
     * 从左边依次入栈 导入顺序按照 Collection 顺序 如: a b c => c b a
     */
    public Long lLeftPushAll( String key, String... values ) {
        return stringRedisTemplate.opsForList().leftPushAll( key, values );
    }

    /**
     * 从左边依次入栈 导入顺序按照 Collection 顺序 如: a b c => c b a
     */
    public Long lLeftPushAll( String key, Collection<String> values ) {
        return stringRedisTemplate.opsForList().leftPushAll( key, values );
    }

    /**
     * 指定 list 从左入栈，当 key 不存在时，什么也不做，返回 0 。
     *
     * @return 当前队列的长度
     */
    public Long lLeftPushIfPresent( String key, String value ) {
        return stringRedisTemplate.opsForList().leftPushIfPresent( key, value );
    }

    /**
     * 指定 list 从左出栈 如果列表没有元素，会返回null
     *
     * @return 出栈的值
     */
    public String lLeftPop( String key ) {
        return stringRedisTemplate.opsForList().leftPop( key );
    }

    /**
     * 指定 list 从左出栈 如果列表没有元素，会堵塞到列表一直有元素或者超时为止，若超时则返回null
     *
     * @return 出栈的值
     */
    public String lLeftPop( String key, Duration timeout ) {
        return stringRedisTemplate.opsForList().leftPop( key, timeout );
    }

    /**
     * 指定 list 从右入栈
     *
     * @return 当前队列的长度
     */
    public Long lRightPush( String key, String value ) {
        return stringRedisTemplate.opsForList().rightPush( key, value );
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
    public Long lRightPush( String key, String pivot, String value ) {
        return stringRedisTemplate.opsForList().rightPush( key, pivot, value );
    }

    /**
     * 从右边依次入栈 导入顺序按照 Collection 顺序 如: a b c => a b c
     */
    public Long lRightPushAll( String key, String... values ) {
        return stringRedisTemplate.opsForList().rightPushAll( key, values );
    }

    /**
     * 从右边依次入栈 导入顺序按照 Collection 顺序 如: a b c => a b c
     */
    public Long lRightPushAll( String key, Collection<String> values ) {
        return stringRedisTemplate.opsForList().rightPushAll( key, values );
    }

    /**
     * 指定 list 从右入栈，当 key 不存在时，什么也不做，返回 0 。
     *
     * @return 当前队列的长度
     */
    public Long lRightPushIfPresent( String key, String value ) {
        return stringRedisTemplate.opsForList().rightPushIfPresent( key, value );
    }

    /**
     * 指定 list 从右出栈 如果列表没有元素，会返回null
     *
     * @return 出栈的值
     */
    public String lRightPop( String key ) {
        return stringRedisTemplate.opsForList().rightPop( key );
    }

    /**
     * 指定 list 从右出栈 如果列表没有元素，会堵塞到列表一直有元素或者超时为止，若超时则返回null
     *
     * @return 出栈的值
     */
    public String lRightPop( String key, Duration timeout ) {
        return stringRedisTemplate.opsForList().rightPop( key, timeout );
    }

    /**
     * 将列表 key 下标为 index 的元素的值设置为 value 。
     * <br/> 当 index 参数超出范围，或对一个空列表( key 不存在)进行 LSET 时，返回false。
     */
    public void lSet( String key, long index, String value ) {
        stringRedisTemplate.opsForList().set( key, index, value );
    }

    /**
     * 删除 key 中 值为 value 的 count 个数.
     *
     * @return 成功删除的个数
     */
    public Long lDelete( String key, long count, Object value ) {
        return stringRedisTemplate.opsForList().remove( key, count, value );
    }

    /**
     * 根据下标获取值
     */
    public String lIndex( String key, long index ) {
        return stringRedisTemplate.opsForList().index( key, index );
    }

    /**
     * 返回列表中指定值第一次出现的索引
     *
     * @since Redis Version: 6.0.6
     */
    public Long lIndexOf( String key, String value ) {
        return stringRedisTemplate.opsForList().indexOf( key, value );
    }

    /**
     * 返回列表中指定值最后一次出现的索引
     *
     * @since Redis Version: 6.0.6
     */
    public Long lIndexOfLast( String key, String value ) {
        return stringRedisTemplate.opsForList().lastIndexOf( key, value );
    }

    /**
     * <p>以原子方式返回并删除存储在sourceKey列表的第一个/最后一个元素（头/尾取决于 from 参数），</p>
     * <p>并将元素推送到存储的destinationKey列表的第一个/最后一个元素（头/尾取决于 to 参数）。</p>
     *
     * @since Redis Version: 6.2
     */
    public String hMove( ListOperations.MoveFrom<String> from, ListOperations.MoveTo<String> to ) {
        return stringRedisTemplate.opsForList().move( from, to );
    }

    /**
     * <p>以原子方式返回并删除存储在sourceKey列表的第一个/最后一个元素（头/尾取决于 from 参数），</p>
     * <p>并将元素推送到存储的destinationKey列表的第一个/最后一个元素（头/尾取决于 to 参数）。</p>
     * <strong>阻止连接，直到元素可用或达到超时<strong/>
     *
     * @since Redis Version: 6.2
     */
    public String hMove( ListOperations.MoveFrom<String> from, ListOperations.MoveTo<String> to, Duration timeout ) {
        return stringRedisTemplate.opsForList().move( from, to, timeout );
    }

    /**
     * <p>以原子方式返回并删除存储在sourceKey列表的第一个/最后一个元素（头/尾取决于 from 参数），</p>
     * <p>并将元素推送到存储的destinationKey列表的第一个/最后一个元素（头/尾取决于 to 参数）。</p>
     *
     * @since Redis Version: 6.2
     */
    public String hMove( String sourceKey, RedisListCommands.Direction from, String destinationKey,
                         RedisListCommands.Direction to ) {
        return stringRedisTemplate.opsForList().move( sourceKey, from, destinationKey, to );
    }

    /**
     * <p>以原子方式返回并删除存储在sourceKey列表的第一个/最后一个元素（头/尾取决于 from 参数），</p>
     * <p>并将元素推送到存储的destinationKey列表的第一个/最后一个元素（头/尾取决于 to 参数）。</p>
     * <strong>阻止连接，直到元素可用或达到超时<strong/>
     *
     * @since Redis Version: 6.2
     */
    public String hMove( String sourceKey, RedisListCommands.Direction from, String destinationKey,
                         RedisListCommands.Direction to, Duration timeout ) {
        return stringRedisTemplate.opsForList().move( sourceKey, from, destinationKey, to, timeout );
    }

    // ---------------------- set操作 无序不重复集合 ---------------------

    /**
     * 添加 set 元素，如果已经存在将忽略添加，如果key不存在则创建新set
     */
    public Long sAdd( String key, String... values ) {
        return stringRedisTemplate.opsForSet().add( key, values );
    }

    /**
     * 删除一个或多个集合中的指定值
     *
     * @return 成功删除数量
     */
    public Long sRemove( String key, Object... values ) {
        return stringRedisTemplate.opsForSet().remove( key, values );
    }

    /**
     * 随机移除一个元素
     */
    public String sRandomPop( String key ) {
        return stringRedisTemplate.opsForSet().pop( key );
    }

    /**
     * 随机移除 count 个元素
     */
    public List<String> sRandomPop( String key, long count ) {
        return stringRedisTemplate.opsForSet().pop( key, count );
    }

    /**
     * 将 key 中的 value 迁移到 destKey 中
     *
     * @return 返回成功与否
     */
    public Boolean sMove( String key, String value, String destKey ) {
        return stringRedisTemplate.opsForSet().move( key, value, destKey );
    }

    /**
     * 集合的数量。当集合 key 不存在时，返回 0 。
     */
    public Long sSize( String key ) {
        return stringRedisTemplate.opsForSet().size( key );
    }

    /**
     * 判断 set 集合中 是否有 value
     */
    public Boolean sIsMember( String key, String value ) {
        return stringRedisTemplate.opsForSet().isMember( key, value );
    }

    /**
     * 判断 set 集合中 是否有 values
     */
    public Map<Object, Boolean> sIsMember( String key, Object... values ) {
        return stringRedisTemplate.opsForSet().isMember( key, values );
    }

    /**
     * 返回 key 和 otherKeys 的交集
     */
    public Set<String> sIntersect( String key, String otherKey ) {
        return stringRedisTemplate.opsForSet().intersect( key, otherKey );
    }

    /**
     * 返回 key 和 otherKeys 的交集
     */
    public Set<String> sIntersect( String key, Collection<String> otherKeys ) {
        return stringRedisTemplate.opsForSet().intersect( key, otherKeys );
    }

    /**
     * 返回 keys 之间的交集
     */
    public Set<String> sIntersect( Collection<String> keys ) {
        return stringRedisTemplate.opsForSet().intersect( keys );
    }

    /**
     * 将 key 与 otherKey 的交集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Long sIntersectAndStore( String key, String otherKey, String destKey ) {
        return stringRedisTemplate.opsForSet().intersectAndStore( key, otherKey, destKey );
    }

    /**
     * 将 key 与 otherKey 的交集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Long sIntersectAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return stringRedisTemplate.opsForSet().intersectAndStore( key, otherKeys, destKey );
    }

    /**
     * 将 key 与 otherKey 的交集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Long sIntersectAndStore( Collection<String> keys, String destKey ) {
        return stringRedisTemplate.opsForSet().intersectAndStore( keys, destKey );
    }

    /**
     * 返回 key 和 othere 的并集
     */
    public Set<String> sUnion( String key, String otherKey ) {
        return stringRedisTemplate.opsForSet().union( key, otherKey );
    }

    /**
     * 返回 key 和 otherKeys 的并集
     *
     * @param otherKeys key 的集合
     */
    public Set<String> sUnion( String key, Collection<String> otherKeys ) {
        return stringRedisTemplate.opsForSet().union( key, otherKeys );
    }

    /**
     * 将 key 与 otherKey 的并集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Long sUnionAndStore( String key, String otherKey, String destKey ) {
        return stringRedisTemplate.opsForSet().unionAndStore( key, otherKey, destKey );
    }

    /**
     * 将 key 与 otherKey 的并集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Long sUnionAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return stringRedisTemplate.opsForSet().unionAndStore( key, otherKeys, destKey );
    }

    /**
     * 将 keys 的并集,保存到 destKey 中
     *
     * @return destKey 数量
     */
    public Long sUnionAndStore( Collection<String> keys, String destKey ) {
        return stringRedisTemplate.opsForSet().unionAndStore( keys, destKey );
    }

    /**
     * 获取两个集合的差集
     */
    public Set<String> sDifference( String key, String otherkey ) {
        return stringRedisTemplate.opsForSet().difference( key, otherkey );
    }

    /**
     * 获取 key 和 集合 otherKeys 的差集
     */
    public Set<String> sDifference( String key, Collection<String> otherKeys ) {
        return stringRedisTemplate.opsForSet().difference( key, otherKeys );
    }

    /**
     * 将  key 与 otherkey 的差集 ,添加到 destKey 集合中
     *
     * @return 返回差集的数量
     */
    public Long sDifferenceAndStore( String key, String otherkey, String destKey ) {
        return stringRedisTemplate.opsForSet().differenceAndStore( key, otherkey, destKey );
    }

    /**
     * 将 key 和 集合  collections 中的 key 集合的差集 添加到 destKey 集合中
     *
     * @return 返回差集的数量
     */
    public Long sDifferenceAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return stringRedisTemplate.opsForSet().differenceAndStore( key, otherKeys, destKey );
    }

    /**
     * 将 keys 的差集 添加到 destKey 集合中
     *
     * @return 返回差集的数量
     */
    public Long sDifferenceAndStore( Collection<String> keys, String destKey ) {
        return stringRedisTemplate.opsForSet().differenceAndStore( keys, destKey );
    }

    /**
     * 返回集合中所有元素
     */
    public Set<String> sMembers( String key ) {
        return stringRedisTemplate.opsForSet().members( key );
    }

    /**
     * 增量迭代返回集合中所有元素
     */
    public Cursor<String> sScan( String key ) {
        return stringRedisTemplate.opsForSet().scan( key, ScanOptions.NONE );
    }

    /**
     * 增量迭代返回集合中所有元素
     */
    public Cursor<String> sScan( String key, ScanOptions options ) {
        return stringRedisTemplate.opsForSet().scan( key, options );
    }

    /**
     * 随机获取一个元素
     */
    public String sRandom( String key ) {
        return stringRedisTemplate.opsForSet().randomMember( key );
    }

    /**
     * 随机获取指定数量的元素,同一个元素可能会选中两次
     */
    public List<String> sRandom( String key, long count ) {
        return stringRedisTemplate.opsForSet().randomMembers( key, count );
    }

    /**
     * 随机获取指定数量的元素,去重(同一个元素只能选择两一次)
     */
    public Set<String> sRandomDistinct( String key, long count ) {
        return stringRedisTemplate.opsForSet().distinctRandomMembers( key, count );
    }

    // ---------------------- zset操作 根据 socre 排序 ---------------------

    /**
     * 添加 ZSet 元素
     */
    public Boolean zAdd( String key, String value, double score ) {
        return stringRedisTemplate.opsForZSet().add( key, value, score );
    }

    /**
     * 批量添加 ZSet 元素 <br/>
     * <code>
     * Set tupless = new HashSet<>(); <br/> ZSetOperations.TypedTuple objectTypedTuple1 = new DefaultTypedTuple<>("zset-5",9.6);
     * <br/> tupless.add(objectTypedTuple1); <br/> ZSetOperations.TypedTuple objectTypedTuple2 = new
     * DefaultTypedTuple<>("zset-6",9.5); <br/> tupless.add(objectTypedTuple2);
     * </code>
     */
    public Long zAddAll( String key, Set<ZSetOperations.TypedTuple<String>> tuples ) {
        return stringRedisTemplate.opsForZSet().add( key, tuples );
    }

    /**
     * Zset 删除一个或多个元素
     */
    public Long zRemove( String key, Object... values ) {
        return stringRedisTemplate.opsForZSet().remove( key, values );
    }

    /**
     * 对指定的 zset 的 value 值 , socre 属性做增减操作
     */
    public Double zIncrementScore( String key, String value, double score ) {
        return stringRedisTemplate.opsForZSet().incrementScore( key, value, score );
    }

    /**
     * 从Zset中获取一个随机元素。
     *
     * @since Redis Version: 6.2
     */
    public String zRandomMember( String key ) {
        return stringRedisTemplate.opsForZSet().randomMember( key );
    }

    /**
     * 从Zset中获取去重的随机元素
     *
     * @since Redis Version: 6.2
     */
    public Set<String> zRandomDistinctMembers( String key, long count ) {
        return stringRedisTemplate.opsForZSet().distinctRandomMembers( key, count );
    }

    /**
     * 从Zset中获取随机元素
     *
     * @since Redis Version: 6.2
     */
    public List<String> zRandomMembers( String key, long count ) {
        return stringRedisTemplate.opsForZSet().randomMembers( key, count );
    }

    /**
     * 从Zset中获取一个随机的带分数的元素。
     *
     * @since Redis Version: 6.2
     */
    public ZSetOperations.TypedTuple<String> zRandomMemberWithScore( String key ) {
        return stringRedisTemplate.opsForZSet().randomMemberWithScore( key );
    }

    /**
     * 从Zset中获取随机去重的带分数的元素。
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zRandomDistinctMembersWithScore( String key, long count ) {
        return stringRedisTemplate.opsForZSet().distinctRandomMembersWithScore( key, count );
    }

    /**
     * 从Zset中获取随机的带分数的元素。
     *
     * @since Redis Version: 6.2
     */
    public List<ZSetOperations.TypedTuple<String>> zRandomMembersWithScore( String key, long count ) {
        return stringRedisTemplate.opsForZSet().randomMembersWithScore( key, count );
    }

    /**
     * 获取 key 中指定 value 的排名(从小到大排序)
     */
    public Long zRank( String key, String value ) {
        return stringRedisTemplate.opsForZSet().rank( key, value );
    }

    /**
     * 获取 key 中指定 value 的排名(从大到小排序)
     */
    public Long zReverseRank( String key, String value ) {
        return stringRedisTemplate.opsForZSet().reverseRank( key, value );
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从小到大,只有列名)
     */
    public Set<String> zRange( String key, long start, long end ) {
        return stringRedisTemplate.opsForZSet().range( key, start, end );
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从小到大,带上分数)
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeWithScores( String key, long start, long end ) {
        return stringRedisTemplate.opsForZSet().rangeWithScores( key, start, end );
    }

    /**
     * 返回从小到大分数范围内的元素不带分数的集合
     */
    public Set<String> zRangeByScore( String key, double min, double max ) {
        return stringRedisTemplate.opsForZSet().rangeByScore( key, min, max );
    }

    /**
     * 返回从小到大分数范围内 指定 count 数量的元素不带分数的集合
     */
    public Set<String> zRangeByScore( String key, double min, double max, long offset, long count ) {
        return stringRedisTemplate.opsForZSet().rangeByScore( key, min, max, offset, count );
    }

    /**
     * 获取从小到大分数范围内的 [min,max] 的排序结果集合
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores( String key, double min, double max ) {
        return stringRedisTemplate.opsForZSet().rangeByScoreWithScores( key, min, max );
    }

    /**
     * 返回从小到大分数范围内 指定 count 数量的元素集合
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores( String key, double min, double max, long offset,
                                                                           long count ) {
        return stringRedisTemplate.opsForZSet().rangeByScoreWithScores( key, min, max, offset, count );
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从大到小,只有列名)
     */
    public Set<String> zReverseRange( String key, long start, long end ) {
        return stringRedisTemplate.opsForZSet().reverseRange( key, start, end );
    }

    /**
     * 获取索引区间内的排序结果集合(从0开始,从大到小,带上分数)
     */
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeWithScores( String key, long start, long end ) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores( key, start, end );
    }

    /**
     * 获取分数范围内的 [min,max] 的排序结果集合 (从大到小,集合不带分数)
     */
    public Set<String> zReverseRangeByScore( String key, double min, double max ) {
        return stringRedisTemplate.opsForZSet().reverseRangeByScore( key, min, max );
    }

    /**
     * 获取分数范围内的 [min,max] 的排序结果集合 (从大到小,集合带分数)
     */
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeByScoreWithScores( String key, double min, double max ) {
        return stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores( key, min, max );
    }

    /**
     * 返回 分数范围内 指定 count 数量的元素集合, 并且从 offset 下标开始(从大到小,不带分数的集合)
     */
    public Set<String> zReverseRangeByScore( String key, double min, double max, long offset, long count ) {
        return stringRedisTemplate.opsForZSet().reverseRangeByScore( key, min, max, offset, count );
    }

    /**
     * 返回 分数范围内 指定 count 数量的元素集合, 并且从 offset 下标开始(从大到小,带分数的集合)
     */
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeByScoreWithScores( String key, double min, double max,
                                                                                  long offset, long count ) {
        return stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores( key, min, max, offset, count );
    }

    /**
     * 增量迭代Zset下的带分数集合
     */
    public Cursor<ZSetOperations.TypedTuple<String>> zScan( String key ) {
        return stringRedisTemplate.opsForZSet().scan( key, ScanOptions.NONE );
    }

    /**
     * 增量迭代Zset下的带分数集合
     */
    public Cursor<ZSetOperations.TypedTuple<String>> zScan( String key, ScanOptions options ) {
        return stringRedisTemplate.opsForZSet().scan( key, options );
    }

    /**
     * 返回指定分数区间 [min,max] 的元素个数
     */
    public Long zCount( String key, double min, double max ) {
        return stringRedisTemplate.opsForZSet().count( key, min, max );
    }

    /**
     * 返回value区间内的元素个数，按value字典排序
     */
    public Long zLexCount( String key, RedisZSetCommands.Range range ) {
        return stringRedisTemplate.opsForZSet().lexCount( key, range );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最低分数的值。
     *
     * @since Redis Version: 6.2
     */
    public ZSetOperations.TypedTuple<String> zPopMin( String key ) {
        return stringRedisTemplate.opsForZSet().popMin( key );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最低分数的值。
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zPopMin( String key, long count ) {
        return stringRedisTemplate.opsForZSet().popMin( key, count );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最低分数的值。
     *
     * @param timeout 等待直到 key 列表中的条目可用的最大持续时间。 必须是0秒或更大的秒数，不能为空。<br/>
     *                0秒超时可用于无限期等待。 不支持零到一秒之间的持续时间。
     *
     * @since Redis Version: 6.2
     */
    public ZSetOperations.TypedTuple<String> zPopMin( String key, Duration timeout ) {
        return stringRedisTemplate.opsForZSet().popMin( key, timeout );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最高分数的值。
     *
     * @since Redis Version: 6.2
     */
    public ZSetOperations.TypedTuple<String> zPopMax( String key ) {
        return stringRedisTemplate.opsForZSet().popMax( key );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最高分数的值。
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zPopMax( String key, long count ) {
        return stringRedisTemplate.opsForZSet().popMax( key, count );
    }

    /**
     * 从 key 的排序集中删除并返回其分数具有最高分数的值。
     *
     * @param timeout 等待直到 key 列表中的条目可用的最大持续时间。 必须是0秒或更大的秒数，不能为空。<br/>
     *                0秒超时可用于无限期等待。 不支持零到一秒之间的持续时间。
     *
     * @since Redis Version: 6.2
     */
    public ZSetOperations.TypedTuple<String> zPopMax( String key, Duration timeout ) {
        return stringRedisTemplate.opsForZSet().popMax( key, timeout );
    }

    /**
     * 返回 zset 集合数量
     */
    public Long zSize( String key ) {
        return stringRedisTemplate.opsForZSet().size( key );
    }

    /**
     * 获取指定成员的 score 值
     */
    public Double zScore( String key, String value ) {
        return stringRedisTemplate.opsForZSet().score( key, value );
    }

    /**
     * 获取指定成员的 score 值
     *
     * @since Redis Version: 6.2
     */
    public List<Double> zScore( String key, Object... values ) {
        return stringRedisTemplate.opsForZSet().score( key, values );
    }

    /**
     * 删除指定索引位置的成员,其中索引按( 从小到大 )
     */
    public Long zRemoveRange( String key, long start, long end ) {
        return stringRedisTemplate.opsForZSet().removeRange( key, start, end );
    }

    /**
     * 删除指定范围的成员,其中成员Value按( 字典排序从小到大 )
     */
    public Long zRemoveRangeByLex( String key, RedisZSetCommands.Range range ) {
        return stringRedisTemplate.opsForZSet().removeRangeByLex( key, range );
    }

    /**
     * 删除指定分数的成员,其中分数按( 从小到大 )
     */
    public Long zRemoveRangeByScore( String key, double min, double max ) {
        return stringRedisTemplate.opsForZSet().removeRangeByScore( key, min, max );
    }

    /**
     * 获取两个Zset内的差集值
     *
     * @since Redis Version: 6.2
     */
    public Set<String> zDifference( String key, String otherKey ) {
        return stringRedisTemplate.opsForZSet().difference( key, otherKey );
    }

    /**
     * 获取多个Zset内的差集值
     *
     * @since Redis Version: 6.2
     */
    public Set<String> zDifference( String key, Collection<String> otherKeys ) {
        return stringRedisTemplate.opsForZSet().difference( key, otherKeys );
    }

    /**
     * 获取两个Zset内的差集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zDifferenceWithScores( String key, String otherKey ) {
        return stringRedisTemplate.opsForZSet().differenceWithScores( key, otherKey );
    }

    /**
     * 获取多个Zset内的差集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zDifferenceWithScores( String key, Collection<String> otherKeys ) {
        return stringRedisTemplate.opsForZSet().differenceWithScores( key, otherKeys );
    }

    /**
     * 将多个Zset内的差集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Long zDifferenceAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return stringRedisTemplate.opsForZSet().differenceAndStore( key, otherKeys, destKey );
    }

    /**
     * 获取两个Zset内的交集值
     *
     * @since Redis Version: 6.2
     */
    public Set<String> zIntersect( String key, String otherKey ) {
        return stringRedisTemplate.opsForZSet().intersect( key, otherKey );
    }

    /**
     * 获取多个Zset内的交集值
     *
     * @since Redis Version: 6.2
     */
    public Set<String> zIntersect( String key, Collection<String> otherKeys ) {
        return stringRedisTemplate.opsForZSet().intersect( key, otherKeys );
    }

    /**
     * 获取两个Zset内的交集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zIntersectWithScores( String key, String otherKey ) {
        return stringRedisTemplate.opsForZSet().intersectWithScores( key, otherKey );
    }

    /**
     * 获取多个Zset内的交集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zIntersectWithScores( String key, Collection<String> otherKeys ) {
        return stringRedisTemplate.opsForZSet().intersectWithScores( key, otherKeys );
    }

    /**
     * 获取多个Zset内的交集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zIntersectWithScores( String key, Collection<String> otherKeys,
                                                                        RedisZSetCommands.Aggregate aggregate ) {
        return stringRedisTemplate.opsForZSet().intersectWithScores( key, otherKeys, aggregate );
    }

    /**
     * 获取多个Zset内的交集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zIntersectWithScores( String key, Collection<String> otherKeys,
                                                                        RedisZSetCommands.Aggregate aggregate,
                                                                        RedisZSetCommands.Weights weights ) {
        return stringRedisTemplate.opsForZSet().intersectWithScores( key, otherKeys, aggregate, weights );
    }

    /**
     * 将两个Zset内的交集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Long zIntersectAndStore( String key, String otherKey, String destKey ) {
        return stringRedisTemplate.opsForZSet().intersectAndStore( key, otherKey, destKey );
    }

    /**
     * 将多个Zset内的交集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Long zIntersectAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return stringRedisTemplate.opsForZSet().intersectAndStore( key, otherKeys, destKey );
    }

    /**
     * 将多个Zset内的交集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Long zIntersectAndStore( String key, Collection<String> otherKeys, String destKey,
                                    RedisZSetCommands.Aggregate aggregate ) {
        return stringRedisTemplate.opsForZSet().intersectAndStore( key, otherKeys, destKey, aggregate );
    }

    /**
     * 将多个Zset内的交集值和分数放入destKey中
     *
     * @since Redis Version: 6.2
     */
    public Long zIntersectAndStore( String key, Collection<String> otherKeys, String destKey,
                                    RedisZSetCommands.Aggregate aggregate, RedisZSetCommands.Weights weights ) {
        return stringRedisTemplate.opsForZSet().intersectAndStore( key, otherKeys, destKey, aggregate, weights );
    }

    /**
     * 获取两个Zset内的并集值
     *
     * @since Redis Version: 6.2
     */
    public Set<String> zUnion( String key, String otherKey ) {
        return stringRedisTemplate.opsForZSet().union( key, otherKey );
    }

    /**
     * 获取多个Zset内的并集值
     *
     * @since Redis Version: 6.2
     */
    public Set<String> zUnion( String key, Collection<String> otherKeys ) {
        return stringRedisTemplate.opsForZSet().union( key, otherKeys );
    }

    /**
     * 获取两个Zset内的并集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zUnionWithScores( String key, String otherKey ) {
        return stringRedisTemplate.opsForZSet().unionWithScores( key, otherKey );
    }

    /**
     * 获取多个Zset内的并集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zUnionWithScores( String key, Collection<String> otherKeys ) {
        return stringRedisTemplate.opsForZSet().unionWithScores( key, otherKeys );
    }

    /**
     * 获取多个Zset内的并集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zUnionWithScores( String key, Collection<String> otherKeys,
                                                                    RedisZSetCommands.Aggregate aggregate ) {
        return stringRedisTemplate.opsForZSet().unionWithScores( key, otherKeys, aggregate );
    }

    /**
     * 获取多个Zset内的并集带分数的值
     *
     * @since Redis Version: 6.2
     */
    public Set<ZSetOperations.TypedTuple<String>> zUnionWithScores( String key, Collection<String> otherKeys,
                                                                    RedisZSetCommands.Aggregate aggregate,
                                                                    RedisZSetCommands.Weights weights ) {
        return stringRedisTemplate.opsForZSet().unionWithScores( key, otherKeys, aggregate, weights );
    }

    /**
     * 将两个Zset内的并集值和分数放入destKey中
     */
    public Long zUnionAndStore( String key, String otherKey, String destKey ) {
        return stringRedisTemplate.opsForZSet().unionAndStore( key, otherKey, destKey );
    }

    /**
     * 将多个Zset内的并集值和分数放入destKey中
     */
    public Long zUnionAndStore( String key, Collection<String> otherKeys, String destKey ) {
        return stringRedisTemplate.opsForZSet().unionAndStore( key, otherKeys, destKey );
    }

    /**
     * 将多个Zset内的并集值和分数放入destKey中
     */
    public Long zUnionAndStore( String key, Collection<String> otherKeys, String destKey,
                                RedisZSetCommands.Aggregate aggregate ) {
        return stringRedisTemplate.opsForZSet().unionAndStore( key, otherKeys, destKey, aggregate );
    }

    /**
     * 将多个Zset内的并集值和分数放入destKey中
     */
    public Long zUnionAndStore( String key, Collection<String> otherKeys, String destKey, RedisZSetCommands.Aggregate aggregate
            , RedisZSetCommands.Weights weights ) {
        return stringRedisTemplate.opsForZSet().unionAndStore( key, otherKeys, destKey, aggregate, weights );
    }

    // ---------------------- geo 地理位置信息 ---------------------

    /**
     * 删除一个或多个成员
     */
    public Long geoRemove( String key, String... members ) {
        return stringRedisTemplate.opsForGeo().remove( key, members );
    }

    /**
     * 将具有给定成员名称的一个点添加到键
     */
    public Long geoAdd( String key, Point point, String member ) {
        return stringRedisTemplate.opsForGeo().add( key, point, member );
    }

    /**
     * 将具有给定成员名称的点添加到键
     *
     * @see RedisGeoCommands.GeoLocation
     */
    public Long geoAdd( String key, RedisGeoCommands.GeoLocation<String> location ) {
        return stringRedisTemplate.opsForGeo().add( key, location );
    }

    /**
     * 将具有给定成员名称的多个点添加到键
     */
    public Long geoAdd( String key, Map<String, Point> memberCoordinateMap ) {
        return stringRedisTemplate.opsForGeo().add( key, memberCoordinateMap );
    }

    /**
     * 将具有给定成员名称的多个点添加到键
     */
    public Long geoAdd( String key, Iterable<RedisGeoCommands.GeoLocation<String>> locations ) {
        return stringRedisTemplate.opsForGeo().add( key, locations );
    }

    /**
     * 获取两个成员之间的距离
     */
    public Distance geoDistance( String key, String member1, String member2 ) {
        return stringRedisTemplate.opsForGeo().distance( key, member1, member2 );
    }

    /**
     * 获取给定指标中两个成员之间的距离。
     */
    public Distance geoDistance( String key, String member1, String member2, Metric metric ) {
        return stringRedisTemplate.opsForGeo().distance( key, member1, member2, metric );
    }

    /**
     * 获取一个成员的位置GeoHash标识。
     */
    public List<String> geoHash( String key, String member ) {
        return stringRedisTemplate.opsForGeo().hash( key, member );
    }

    /**
     * 获取多个成员的位置GeoHash标识。
     */
    public List<String> geoHash( String key, String... members ) {
        return stringRedisTemplate.opsForGeo().hash( key, members );
    }

    /**
     * 获取一个成员的位置的点标识
     */
    public List<Point> geoPosition( String key, String member ) {
        return stringRedisTemplate.opsForGeo().position( key, member );
    }

    /**
     * 获取多个成员的位置的点标识
     */
    public List<Point> geoPosition( String key, String... members ) {
        return stringRedisTemplate.opsForGeo().position( key, members );
    }

    /**
     * 获取限定圆圈内的成员位置。
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoSearch( String key, Circle within ) {
        return stringRedisTemplate.opsForGeo().search( key, within );
    }

    /**
     *
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoSearch( String key, GeoReference<String> reference,
                                                                       Distance radius ) {
        return stringRedisTemplate.opsForGeo().search( key, reference, radius );
    }

    /**
     *
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoSearch( String key, GeoReference<String> reference,
                                                                       Distance radius,
                                                                       RedisGeoCommands.GeoRadiusCommandArgs args ) {
        return stringRedisTemplate.opsForGeo().search( key, reference, radius, args );
    }

    /**
     *
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoSearch( String key, GeoReference<String> reference,
                                                                       BoundingBox boundingBox ) {
        return stringRedisTemplate.opsForGeo().search( key, reference, boundingBox );
    }

    /**
     *
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoSearch( String key, GeoReference<String> reference,
                                                                       BoundingBox boundingBox,
                                                                       RedisGeoCommands.GeoRadiusCommandArgs args ) {
        return stringRedisTemplate.opsForGeo().search( key, reference, boundingBox, args );
    }

    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoSearch( String key, GeoReference<String> reference,
                                                                       GeoShape geoPredicate,
                                                                       RedisGeoCommands.GeoRadiusCommandArgs args ) {
        return stringRedisTemplate.opsForGeo().search( key, reference, geoPredicate, args );
    }

    public Long geoSearchAndStore( String key, String destKey, Circle within ) {
        return stringRedisTemplate.opsForGeo().searchAndStore( key, destKey, within );
    }

    public Long geoSearchAndStore( String key, String destKey, GeoReference<String> reference, Distance radius ) {
        return stringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, radius );
    }

    public Long geoSearchAndStore( String key, String destKey, GeoReference<String> reference, Distance radius,
                                   RedisGeoCommands.GeoSearchStoreCommandArgs args ) {
        return stringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, radius, args );
    }

    public Long geoSearchAndStore( String key, String destKey, GeoReference<String> reference, BoundingBox boundingBox ) {
        return stringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, boundingBox );
    }

    public Long geoSearchAndStore( String key, String destKey, GeoReference<String> reference, BoundingBox boundingBox,
                                   RedisGeoCommands.GeoSearchStoreCommandArgs args ) {
        return stringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, boundingBox, args );
    }

    public Long geoSearchAndStore( String key, String destKey, GeoReference<String> reference, GeoShape geoPredicate,
                                   RedisGeoCommands.GeoSearchStoreCommandArgs args ) {
        return stringRedisTemplate.opsForGeo().searchAndStore( key, destKey, reference, geoPredicate, args );
    }

    // ---------------------- HyperLogLog 基数统计 ---------------------

    public Long hyperAdd( String key, String... values ) {
        return stringRedisTemplate.opsForHyperLogLog().add( key, values );
    }

    public Long hyperSize( String... keys ) {
        return stringRedisTemplate.opsForHyperLogLog().size( keys );
    }

    /**
     * 将给定SourceKey的所有值合并到 Destination 键中
     */
    public Long hyperUnion( String destination, String... sourceKeys ) {
        return stringRedisTemplate.opsForHyperLogLog().union( destination, sourceKeys );
    }

    // ---------------------- Stream 发布/订阅消息 ---------------------

    public Long streamAcknowledge( String key, String group, String... recordIds ) {
        return stringRedisTemplate.opsForStream().acknowledge( key, group, recordIds );
    }

    public Long streamAcknowledge( String key, String group, RecordId... recordIds ) {
        return stringRedisTemplate.opsForStream().acknowledge( key, group, recordIds );
    }

    public Long streamAcknowledge( String group, Record<String, ?> record ) {
        return stringRedisTemplate.opsForStream().acknowledge( group, record );
    }

    public RecordId streamAdd( String key, Map<String, String> content ) {
        return stringRedisTemplate.opsForStream().add( key, content );
    }

    public RecordId streamAdd( MapRecord<String, String, String> record ) {
        return stringRedisTemplate.opsForStream().add( record );
    }

    public RecordId streamAdd( Record<String, ?> record ) {
        return stringRedisTemplate.opsForStream().add( record );
    }

    public Long streamDelete( String key, String... recordIds ) {
        return stringRedisTemplate.opsForStream().delete( key, recordIds );
    }

    public Long streamDelete( Record<String, ?> record ) {
        return stringRedisTemplate.opsForStream().delete( record );
    }

    public Long streamDelete( String key, RecordId... recordIds ) {
        return stringRedisTemplate.opsForStream().delete( key, recordIds );
    }

    public String streamCreateGroup( String key, String group ) {
        return stringRedisTemplate.opsForStream().createGroup( key, group );
    }

    public String streamCreateGroup( String key, ReadOffset readOffset, String group ) {
        return stringRedisTemplate.opsForStream().createGroup( key, readOffset, group );
    }

    public Boolean streamDestroyGroup( String key, String group ) {
        return stringRedisTemplate.opsForStream().destroyGroup( key, group );
    }

    public Boolean streamDeleteConsumer( String key, Consumer consumer ) {
        return stringRedisTemplate.opsForStream().deleteConsumer( key, consumer );
    }

    public StreamInfo.XInfoConsumers streamConsumers( String key, String group ) {
        return stringRedisTemplate.opsForStream().consumers( key, group );
    }

    public StreamInfo.XInfoGroups streamGroups( String key ) {
        return stringRedisTemplate.opsForStream().groups( key );
    }

    public StreamInfo.XInfoStream streamInfo( String key ) {
        return stringRedisTemplate.opsForStream().info( key );
    }

    public PendingMessagesSummary streamPending( String key, String group ) {
        return stringRedisTemplate.opsForStream().pending( key, group );
    }

    public PendingMessages streamPending( String key, Consumer consumer ) {
        return stringRedisTemplate.opsForStream().pending( key, consumer );
    }

    public PendingMessages streamPending( String key, String group, Range<?> range, long count ) {
        return stringRedisTemplate.opsForStream().pending( key, group, range, count );
    }

    public PendingMessages streamPending( String key, Consumer consumer, Range<?> range, long count ) {
        return stringRedisTemplate.opsForStream().pending( key, consumer, range, count );
    }

    public Long streamSize( String key ) {
        return stringRedisTemplate.opsForStream().size( key );
    }

    public List<MapRecord<String, Object, Object>> streamRange( String key, Range<String> range ) {
        return stringRedisTemplate.opsForStream().range( key, range );
    }

    public List<MapRecord<String, Object, Object>> streamRange( String key, Range<String> range, RedisZSetCommands.Limit limit ) {
        return stringRedisTemplate.opsForStream().range( key, range, limit );
    }

    public <V> List<ObjectRecord<String, V>> streamRange( Class<V> targetType, String key, Range<String> range ) {
        return stringRedisTemplate.opsForStream().range( targetType, key, range );
    }

    public <V> List<ObjectRecord<String, V>> streamRange( Class<V> targetType, String key, Range<String> range,
                                                          RedisZSetCommands.Limit limit ) {
        return stringRedisTemplate.opsForStream().range( targetType, key, range, limit );
    }

    public List<MapRecord<String, Object, Object>> streamRead( StreamOffset<String> stream ) {
        return stringRedisTemplate.opsForStream().read( stream );
    }

    public <V> List<ObjectRecord<String, V>> streamRead( Class<V> targetType, StreamOffset<String> stream ) {
        return stringRedisTemplate.opsForStream().read( targetType, stream );
    }

    public List<MapRecord<String, Object, Object>> streamRead( StreamOffset<String>... streams ) {
        return stringRedisTemplate.opsForStream().read( streams );
    }

    public <V> List<ObjectRecord<String, V>> streamRead( Class<V> targetType, StreamOffset<String>... streams ) {
        return stringRedisTemplate.opsForStream().read( targetType, streams );
    }

    public List<MapRecord<String, Object, Object>> streamRead( StreamReadOptions readOptions, StreamOffset<String>... streams ) {
        return stringRedisTemplate.opsForStream().read( readOptions, streams );
    }

    public <V> List<ObjectRecord<String, V>> streamRead( Class<V> targetType, StreamReadOptions readOptions,
                                                         StreamOffset<String>... streams ) {
        return stringRedisTemplate.opsForStream().read( targetType, readOptions, streams );
    }

    public List<MapRecord<String, Object, Object>> streamRead( Consumer consumer, StreamOffset<String>... streams ) {
        return stringRedisTemplate.opsForStream().read( consumer, streams );
    }

    public <V> List<ObjectRecord<String, V>> streamRead( Class<V> targetType, Consumer consumer,
                                                         StreamOffset<String>... streams ) {
        return stringRedisTemplate.opsForStream().read( targetType, consumer, streams );
    }

    public List<MapRecord<String, Object, Object>> streamRead( Consumer consumer, StreamReadOptions readOptions,
                                                               StreamOffset<String>... streams ) {
        return stringRedisTemplate.opsForStream().read( consumer, readOptions, streams );
    }

    public <V> List<ObjectRecord<String, V>> streamRead( Class<V> targetType, Consumer consumer, StreamReadOptions readOptions,
                                                         StreamOffset<String>... streams ) {
        return stringRedisTemplate.opsForStream().read( targetType, consumer, readOptions, streams );
    }

    public List<MapRecord<String, Object, Object>> streamReverseRange( String key, Range<String> range ) {
        return stringRedisTemplate.opsForStream().reverseRange( key, range );
    }

    public List<MapRecord<String, Object, Object>> streamReverseRange( String key, Range<String> range,
                                                                       RedisZSetCommands.Limit limit ) {
        return stringRedisTemplate.opsForStream().reverseRange( key, range, limit );
    }

    public <V> List<ObjectRecord<String, V>> streamReverseRange( Class<V> targetType, String key, Range<String> range ) {
        return stringRedisTemplate.opsForStream().reverseRange( targetType, key, range );
    }

    public <V> List<ObjectRecord<String, V>> streamReverseRange( Class<V> targetType, String key, Range<String> range,
                                                                 RedisZSetCommands.Limit limit ) {
        return stringRedisTemplate.opsForStream().reverseRange( targetType, key, range, limit );
    }

    public Long streamTrim( String key, long count ) {
        return stringRedisTemplate.opsForStream().trim( key, count );
    }

    public Long streamTrim( String key, long count, boolean approximateTrimming ) {
        return stringRedisTemplate.opsForStream().trim( key, count, approximateTrimming );
    }

    public <V> ObjectRecord<String, V> streamMap( MapRecord<String, Object, Object> record, Class<V> targetType ) {
        return stringRedisTemplate.opsForStream().map( record, targetType );
    }

    public <V> HashMapper<V, Object, Object> streamGetHashMapper( Class<V> targetType ) {
        return stringRedisTemplate.opsForStream().getHashMapper( targetType );
    }

    public MapRecord<String, Object, Object> streamDeserializeRecord( ByteRecord record ) {
        return stringRedisTemplate.opsForStream().deserializeRecord( record );
    }

    // ---------------------- Redis 分布式锁 ---------------------

    public Boolean lock( String key, int timeOutSec ) {
        return strSetIfAbsent( "lock:".concat( key ), key, Duration.ofSeconds( timeOutSec ) );
    }

    public Boolean unLock( String key ) {
        return delete( "lock:".concat( key ) );
    }
}
