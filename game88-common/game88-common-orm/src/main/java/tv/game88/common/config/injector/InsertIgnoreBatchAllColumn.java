package tv.game88.common.config.injector;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author: EnumaElish
 * @Date: 2021/6/1 14:08
 * @Description: 参考InsertBatchSomeColumn实现，就替换了sqlTemplate
 * <p> 不同的数据库支持度不一样!!!  只在 mysql 下测试过!!!  只在 mysql 下测试过!!!  只在 mysql 下测试过!!! </p>
 * <p> 除了主键是 <strong> 数据库自增的未测试 </strong> 外理论上都可以使用!!! </p>
 * <p> 如果你使用自增有报错或主键值无法回写到entity,就不要跑来问为什么了,因为我也不知道!!! </p>
 * <p>
 * 自己的通用 mapper 如下使用:
 * <pre>
 * int fastSaveIgnoreBatch(List<T> entityList);
 * </pre>
 * </p>
 *
 * <li> 注意1: 不要加任何注解 !! </li>
 * <li> 注意2: 自选字段 insert !!,如果个别字段在 entity 里为 null 但是数据库中有配置默认值, insert 后数据库字段是为 null 而不是默认值 </li>
 *
 * <p>
 * 常用的构造入参:
 * </p>
 *
 * <li> 例1: new InsertIgnoreBatchAllColumn(t -> true) , 表示用于全字段 </li>
 * <li> 例2: new InsertIgnoreBatchAllColumn(t -> !t.isLogicDelete()) , 表示非逻辑删除字段外全字段 </li>
 * <li> 例3: new InsertIgnoreBatchAllColumn(t -> t.getFieldFill() != FieldFill.UPDATE) , 表示填充策略为 UPDATE 外的全字段 </li>
 */
public class InsertIgnoreBatchAllColumn extends AbstractMethod {

    /**
     * mapper 对应的方法名
     */
    private static final String MAPPER_METHOD = "insertIgnoreBatchAllColumn";

    @Setter
    @Accessors( chain = true )
    private Predicate<TableFieldInfo> predicate;

    public InsertIgnoreBatchAllColumn() {
        super( MAPPER_METHOD );
    }

    public InsertIgnoreBatchAllColumn( Predicate<TableFieldInfo> predicate ) {
        super( MAPPER_METHOD );
        this.predicate = predicate;
    }

    public InsertIgnoreBatchAllColumn( String name, Predicate<TableFieldInfo> predicate ) {
        super( name );
        this.predicate = predicate;
    }

    @SuppressWarnings( "Duplicates" )
    @Override
    public MappedStatement injectMappedStatement( Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo ) {
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        SqlMethod    sqlMethod    = SqlMethod.INSERT_ONE;
        String       sqlTemplate  = "<script>\nINSERT IGNORE INTO %s %s VALUES %s\n</script>";

        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        String insertSqlColumn = tableInfo.getKeyInsertSqlColumn( true, null, false )
                + this.filterTableFieldInfo( fieldList, predicate, TableFieldInfo::getInsertSqlColumn, EMPTY );
        String columnScript = LEFT_BRACKET + insertSqlColumn.substring( 0, insertSqlColumn.length() - 1 ) + RIGHT_BRACKET;
        String insertSqlProperty = tableInfo.getKeyInsertSqlProperty( true, ENTITY_DOT, false )
                + this.filterTableFieldInfo( fieldList, predicate, i -> i.getInsertSqlProperty( ENTITY_DOT ), EMPTY );
        insertSqlProperty = LEFT_BRACKET + insertSqlProperty.substring( 0, insertSqlProperty.length() - 1 ) + RIGHT_BRACKET;
        String valuesScript = SqlScriptUtils.convertForeach( insertSqlProperty, "list", null, ENTITY, COMMA );
        String keyProperty  = null;
        String keyColumn    = null;
        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if ( tableInfo.havePK() ) {
            if ( tableInfo.getIdType() == IdType.AUTO ) {
                /* 自增主键 */
                keyGenerator = Jdbc3KeyGenerator.INSTANCE;
                keyProperty  = tableInfo.getKeyProperty();
                // 去除转义符
                keyColumn = SqlInjectionUtils.removeEscapeCharacter( tableInfo.getKeyColumn() );
            } else {
                if ( null != tableInfo.getKeySequence() ) {
                    keyGenerator = TableInfoHelper.genKeyGenerator( this.methodName, tableInfo, builderAssistant );
                    keyProperty  = tableInfo.getKeyProperty();
                    keyColumn    = tableInfo.getKeyColumn();
                }
            }
        }
        String    sql       = String.format( sqlTemplate, tableInfo.getTableName(), columnScript, valuesScript );
        SqlSource sqlSource = super.createSqlSource( configuration, sql, modelClass );
        return this.addInsertMappedStatement( mapperClass, modelClass, methodName, sqlSource, keyGenerator, keyProperty,
                keyColumn );
    }
}