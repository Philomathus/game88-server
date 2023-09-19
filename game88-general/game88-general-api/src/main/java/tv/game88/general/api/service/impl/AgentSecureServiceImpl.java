package tv.game88.general.api.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.general.api.entity.AgentSecure;
import tv.game88.general.api.entity.AgentSecureOss;
import tv.game88.general.api.mapper.AgentSecureMapper;
import tv.game88.general.api.mapper.AgentSecureOssMapper;
import tv.game88.general.api.service.AgentSecureService;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

/**
 * 域名加密管理Service业务层处理
 *
 * @author 77tv
 * @date 2021-04-01
 */
@Service
@Master
public class AgentSecureServiceImpl extends ServiceImpl<AgentSecureMapper, AgentSecure> implements AgentSecureService {

    @Resource
    private AgentSecureOssMapper agentSecureOssMapper;

    /**
     * 查询域名加密管理列表
     *
     * @param agentSecure 域名加密管理
     *
     * @return 域名加密管理
     */
    @Override
    public List<AgentSecure> selectAgentSecureList( AgentSecure agentSecure ) {
        return this.baseMapper.selectAgentSecureList( agentSecure );
    }

    @Override
    public String uploadAgent( String agent ) {
        //根据代理号查询所有的代理域名oss
        AgentSecureOss agentSecureOss = new AgentSecureOss();
        agentSecureOss.setAgent( agent );
        List<AgentSecureOss> agentSecureOssList = agentSecureOssMapper.selectAgentSecureOssList( agentSecureOss );
        //遍历代理域名oss,上传对应的文件
        if ( !agentSecureOssList.isEmpty() ) {
            for ( AgentSecureOss secureOss : agentSecureOssList ) {
                //生成上传文件,之后获取文件的流
                //查询加密后的url
                AgentSecure agentSecure = this.baseMapper.selectById( agent );
                //对应的加密是否存在
                if ( agentSecure != null ) {
                    String secureUrls = agentSecure.getSecureUrls();
                    File   newFile    = new File( System.getProperty( "java.io.tmpdir" ) + agent + ".txt" );
                    //创建输出流，写入数据
                    Writer out = null;
                    try {
                        if ( !newFile.exists() ) {
                            newFile.createNewFile();
                        }
                        out = new FileWriter( newFile );
                        out.write( secureUrls );
                        out.close();
                        //生成文件的fileKey

                        String fileKey = "77ym/" + agent + ".txt";
                        uploadOss( new FileInputStream( newFile ), fileKey, secureOss );
                        newFile.delete();
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return "上传成功";

    }

    private String uploadOss( InputStream inputStream, String fileKey, AgentSecureOss secureOss ) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build( secureOss.getEndpoint(), secureOss.getAccessKey(),
                secureOss.getAccessSecret() );
        // 上传文件流
        ossClient.putObject( secureOss.getBucket(), fileKey, inputStream );
        // 关闭client
        ossClient.shutdown();
        return "/" + fileKey;
    }
}
