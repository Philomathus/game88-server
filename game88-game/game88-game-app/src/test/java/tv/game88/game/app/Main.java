package tv.game88.game.app;

import tv.game88.common.utils.JsonUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Main {

    public static void main( String[] args ) {
        Map<String, List<Integer>> config = Map.of( "manager", Arrays.asList( 8800, 8801, 8802, 8803, 8804 ), "server",
                Arrays.asList( 8811, 8812, 8813, 8814 ) );

        Map<Integer, String> configModule = Map.of( 1, "platform", 2, "pay", 3, "game", 4, "lottery" );

        try ( FileReader fileReader = new FileReader( "/Users/meng.jun/Downloads/88.csv" ); BufferedReader bufferedReader =
                new BufferedReader( fileReader ) ) {
            String              line;
            List<MonitorConfig> monitorConfigs = new ArrayList<>();
            while ( ( line = bufferedReader.readLine() ) != null ) {
                String[] split = line.split( " " );
                String   name  = split[ 0 ];
                String   ip    = split[ 1 ];

                for ( String configName : config.keySet() ) {
                    List<Integer> ports = config.get( configName );
                    if ( !name.contains( configName ) ) {
                        continue;
                    }
                    for ( Integer port : ports ) {
                        String type = port > 8810 ? "app" : "admin";
                        String module = configModule.getOrDefault( port % 10, "" );
                        MonitorConfig monitorConfig = MonitorConfig
                                .builder()
                                .monitor( MonitorConfig.Monitor
                                        .builder()
                                        .host( ip )
                                        .name( name + "_game88" + ("".equals( module ) ? "" : "-") + module + "-" + type )
                                        .app( "springboot3" )
                                        .status( 1 )
                                        .intervals( 60 )
                                        .tags( Collections.singletonList( 6 ) )
                                        .collector( "child-collector-1" )
                                        .build() )
                                .params( Arrays.asList( MonitorConfig.Param
                                        .builder()
                                        .field( "ssl" )
                                        .type( 1 )
                                        .value( "false" )
                                        .build(), MonitorConfig.Param
                                        .builder()
                                        .field( "base_path" )
                                        .type( 1 )
                                        .value( "/actuator" )
                                        .build(), MonitorConfig.Param
                                        .builder()
                                        .field( "host" )
                                        .type( 1 )
                                        .value( ip )
                                        .build(), MonitorConfig.Param
                                        .builder()
                                        .field( "port" )
                                        .type( 0 )
                                        .value( port.toString() )
                                        .build() ) )
                                .metrics( Arrays.asList( "available", "threads", "memory_used", "health" ) )
                                .detected( false )
                                .build();
                        monitorConfigs.add( monitorConfig );
                    }
                }
            }
            System.out.println( JsonUtil.object2Json( monitorConfigs ) );
        } catch ( Exception e ) {
            System.err.println( "Error reading file: " + e.getMessage() );
        }
    }
}
