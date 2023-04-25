package tv.game88.game.api.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.member.dto.RspVipSet;
import tv.game88.core.member.mapper.ConfigVipMapper;
import tv.game88.game.api.dto.ReqGameType;
import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.dto.RspGamePlatform;
import tv.game88.game.api.dto.RspGameType;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.mapper.GameInfoMapper;
import tv.game88.game.api.mapper.GamePlatformMapper;
import tv.game88.game.api.mapper.GameTypeMapper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 游戏缓存
 */
@Component
@Log4j2
public class GameCacheManager {

	@Resource
	private StringRedisTemplate stringRedisTemplate;
	@Resource
	private GameInfoMapper gameInfoMapper;

	@Resource
	GameTypeMapper gameTypeMapper ;

	@Resource
	GamePlatformMapper gamePlatformMapper ;

	@Resource
	ConfigVipMapper configVipMapper ;

	public List<RspGameType> getGamesTypes() {
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		String                          val        = operations.get( Constants.CX_GAME.concat( "type:list" ) );
		return JsonUtil.json2Array( val, new TypeReference<List<RspGameType>>() {} );
	}

	/**
	 * 初始化游戏类型和游戏的关联
	 */
	public void initGameGroup() {
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		for ( RspGameType gameType : getGamesTypes() ) {
			List<String> gameList =
					gameInfoMapper.findListByType( Long.toString(gameType.getId()) )
							.stream()
							.map( RspGameInfo::getIdasString )
							.collect( Collectors.toList() );
			operations.set( Constants.CX_GAME.concat( "group:" ).concat( Long.toString(gameType.getId()) ), JsonUtil.object2Json( gameList ) );
		}
	}
	@PostConstruct
	public void init(){
		log.info("初始化游戏redis高速缓存开始");
		long bTime = System.currentTimeMillis();
		reSetGames();
		log.info("初始化游戏redis缓存结束,用时 {}毫秒",System.currentTimeMillis()-bTime);
		initGamePlatforms();
		initVip();

	}
	public void initVip(){
		List<RspVipSet> list =  configVipMapper.findListForCache();
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		operations.set(Constants.CX_VIP.concat("list"), JsonUtil.object2Json(list));
		for(RspVipSet p:list){
			operations.set(Constants.CX_VIP.concat(String.valueOf(p.getLevel())), JsonUtil.object2Json(p));
		}

	}

	public void reSetGames(){
		//初始化游戏 id:game
		initGames();
		//初始化游戏类型
		initGamesTypes();
		//初始化游戏类型和游戏的关联 typeId:gamesid
		initGameGroup();
	}


	//初始化游戏
	public void initGames(){

		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();

		for(RspGameInfo game : gameInfoMapper.findTypeList()){
			operations.set(Constants.CX_GAME.concat("id:").concat(Long.toString(game.getId())), JsonUtil.object2Json(game));
			if(game.getPlatformId()==3){
				operations.set(Constants.CX_GAME.concat("liveId"), Long.toString(game.getId()));
			}
		}
	}

	public void setGameInfo(GameInfo gameInfo ){
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		RspGameInfo game = new RspGameInfo();
		BeanUtils.copyProperties(gameInfo,game);
		operations.set(Constants.CX_GAME.concat("id:").concat(Long.toString(game.getId())), JsonUtil.object2Json(game));
		if(game.getPlatformId() != null && game.getPlatformId()==3){
			operations.set(Constants.CX_GAME.concat("liveId"), Long.toString(game.getId()));
		}
	}


	public void initGamesTypes(){
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		List<RspGameType> typeList =  gameTypeMapper.findList(new ReqGameType());
		operations.set(Constants.CX_GAME.concat("type:list"), JsonUtil.object2Json(typeList.stream().collect(Collectors.toList())));
	}

	/*public List<RspGameType> getGamesTypes(){
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		String val = operations.get(Constants.CX_GAME.concat("type:list"));
		return JSON.parseArray(val, RspGameType.class);
	}*/

	/**
	 * 初始化游戏类型和游戏的关联
	 */
	/*public void initGameGroup(){
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		for(RspGameType gameType : getGamesTypes() ){
			List<String> gameList =  gameInfoMapper.findListByType(gameType.getId()).stream().map(RspGameInfo::getId).collect(Collectors.toList());
			operations.set(Constants.CX_GAME.concat("group:").concat(gameType.getId()), JsonUtil.object2Json(gameList));
		}
	}*/

	public void initGamePlatforms(){

		List<String> allIds = new ArrayList<>();
		Map<String,List<String>> groupMap = new HashMap<>();

		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		for(RspGamePlatform p: gamePlatformMapper.findSimpleLists()){
			if(p.getGame_typeID()==null){
				continue;
			}
			allIds.add(String.valueOf(p.getId()));
			operations.set(Constants.CX_GAME.concat("platformId:").concat(String.valueOf(p.getId())), JsonUtil.object2Json(p));
			if(!groupMap.containsKey(p.getGame_typeID())){
				groupMap.put(p.getGame_typeID(), new ArrayList<>() );
			}
			groupMap.get(p.getGame_typeID()).add(String.valueOf(p.getId()));
		}

		operations.set(Constants.CX_GAME.concat("platformIds:list"), JsonUtil.object2Json(allIds));
		for(String typeId : groupMap.keySet()){
			operations.set(Constants.CX_GAME.concat("platformGroups:").concat(typeId), JsonUtil.object2Json(groupMap.get(typeId)));
		}
	}
	public String getICGToken() {
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		return operations.get( Constants.PLATFORM_TOKEN.concat( "icg" ) );
	}

	public Long add( String useID, String game ) {
		return stringRedisTemplate.opsForSet().add( Constants.PLATFORM_TOKEN.concat( game ).concat( ":users" ), useID );
	}

	public List<RspGamePlatform> getGamePlatformList( String gameTypeId ) {
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		List<String>                    ids;
		if ( StringUtils.isEmpty( gameTypeId ) ) {
			ids = JsonUtil.json2Array(operations.get(Constants.CX_GAME.concat("platformIds:list")), new TypeReference<List<String>>() {
			});
		} else {
			ids = JsonUtil.json2Array( operations.get( Constants.CX_GAME.concat( "platformGroups:" ).concat( gameTypeId ) ),
					new TypeReference<List<String>>() {} );
		}
		List<RspGamePlatform> list = new ArrayList<>();
		if ( ids != null ) {
			for ( String pid : ids ) {
				list.add( JsonUtil.json2Object( operations.get( Constants.CX_GAME.concat( "platformId:" ).concat( String.valueOf( pid ) ) ), RspGamePlatform.class ) );
			}
		}
		return list;
	}
}
