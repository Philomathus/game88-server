package tv.game88.game.app;

import tv.game88.common.utils.JsonUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Main99 {

    public static void main( String[] args ) {
        Map<String, List<Integer>> config = Map.of( "manager", Arrays.asList( 8800, 8801, 8802, 8803 ), "server",
                Arrays.asList( 8811, 8812, 8813 ) );

        Map<Integer, String> configModule = Map.of( 1, "platform", 2, "pay", 3, "game" );

        try ( FileReader fileReader = new FileReader( "/Users/meng.jun/Downloads/99.csv" ); BufferedReader bufferedReader =
                new BufferedReader( fileReader ) ) {
            String              line;
            List<MonitorConfig> monitorConfigs = new ArrayList<>();
            while ( ( line = bufferedReader.readLine() ) != null ) {
                String[] split = line.split( " " );
                String   name  = split[ 0 ]; //
                String   ip    = split[ 1 ];

                for ( String configName : config.keySet() ) {
                    List<Integer> ports = config.get( configName );
                    if ( !name.contains( configName ) ) {
                        continue;
                    }
                    for ( Integer port : ports ) {
                        String type   = port > 8810 ? "app" : "admin";
                        String module = configModule.getOrDefault( port % 10, "" );
                        MonitorConfig monitorConfig = MonitorConfig.builder()
                                .monitor( MonitorConfig.Monitor.builder()
                                        .host( ip )
                                        .name( name + "_game99" + ( "".equals( module ) ? "" : "-" ) + module + "-" + type )
                                        .app( "springboot3" )
                                        .status( 1 )
                                        .intervals( 60 )
                                        .tags( Collections.singletonList( 6 ) )
                                        .collector( "child-collector-1" )
                                        .build() )
                                .params( Arrays.asList( MonitorConfig.Param.builder()
                                        .field( "ssl" )
                                        .type( 1 )
                                        .value( "false" )
                                        .build(), MonitorConfig.Param.builder()
                                        .field( "base_path" )
                                        .type( 1 )
                                        .value( "/actuator" )
                                        .build(), MonitorConfig.Param.builder()
                                        .field( "host" )
                                        .type( 1 )
                                        .value( ip )
                                        .build(), MonitorConfig.Param.builder()
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
                MonitorConfig monitorConfig = MonitorConfig.builder()
                        .monitor( MonitorConfig.Monitor.builder()
                                .host( ip )
                                .name( name + "_" + ip )
                                .app( "almalinux" )
                                .status( 1 )
                                .intervals( 60 )
                                .tags( Collections.singletonList( 5 ) )
                                .collector( "child-collector-1" )
                                .build() )
                        .params( Arrays.asList( MonitorConfig.Param.builder()
                                .field( "reuseConnection" )
                                .type( 1 )
                                .value( "true" )
                                .build(), MonitorConfig.Param.builder()
                                .field( "username" )
                                .type( 1 )
                                .value( "root" )
                                .build(), MonitorConfig.Param.builder()
                                .field( "host" )
                                .type( 1 )
                                .value( ip )
                                .build(), MonitorConfig.Param.builder()
                                .field( "port" )
                                .type( 0 )
                                .value( "22" )
                                .build(), MonitorConfig.Param.builder()
                                .field( "timeout" )
                                .type( 0 )
                                .value( "6000" )
                                .build(), MonitorConfig.Param.builder()
                                .field( "password" )
                                .type( 1 )
                                .build(), MonitorConfig.Param.builder()
                                .field( "privateKey" )
                                .type( 1 )
                                .value( "-----BEGIN RSA PRIVATE "
                                        + "KEY-----\nMIIEogIBAAKCAQEAxJKu3RbMgr8Z37kO5yfM56V9YW3tEbFmTlAPKbFMTmJO8Njl"
                                        + "\nvS9Fr8EvVkgMX0f7YBpZMH28KCJrcIKpkcY5XRWiEjqfj77T78OLuyktOFVR7R9Y\nxwmE3"
                                        + "+en7EPos5Ot2fiaktfMNOSsYDUJgaI1d4zntL7m9RYmAddD0mhyOYZZ+XkY"
                                        + "\nX0YpuXkyJ7bhDRzKAoSrOJuWI2qrvChEptFv9ysRCJTYytCKrqkFPXjEWZ36Bn/t\nbdNEbpc8yZVO"
                                        + "+zB9kT3oFmjFkfj4LWT32Thr+auTV8UFxLer1aHepE0qr6tgaSFV\nQKmrf/eWP1CvkVAx+LNvgQ/VD"
                                        + "/G5A6RErGTfSQIDAQABAoIBACeVVgsFRUdqzf4E\nGOEpXZSj+Xx+E0gFzhElA"
                                        + "/ikUDZUi4rqUlDnTQQYRbz9IyioqRDwHVerahE25SWx"
                                        + "\n2g2VugVpjspW7byXc7wLR373yrhWTfoO3uStoQkpb+IexqQqr859xjqdo+xew+tX"
                                        + "\nosNs85FQWXRHIODnhd3HCEw9Zgr5DZQ6rd/WUFqCU5rHOcKJ3JYPhybSaMKrerTk\nx"
                                        + "/FVBsGkSpyPOskoAMxOqs+x0bFbJYRB9d9Vx7uviwinaadjFBXZlsSead/dCCle\nSMCdgwTd8p8m"
                                        + "/IqFm89kXfAFQErsLnHOhV9Bw34rZteUARCuiB2w+skezu/QDdKo\n8QLrogUCgYEA5bSLWZBfFKw"
                                        + "/eH98bllWiTBfx+Ap0RONqAQG4uNYgIQ88V3p/N0E\nNB/yty/yrZij6i3jdVbheIMjhsxjg8coK7+/Jy"
                                        + "/SGgBMYXDP8IA0cSnyw1AvOzkm"
                                        + "\nzyO6ebbqtMAadmqNuD8IL9KfgixIumiLs0USbhw0hTukuc0v0w2qYccCgYEA2xM0\nqef7J+QRgp87E"
                                        + "+g3KyeIjXtK2GuEENg+nk25m5Oxa93ECA1607+WQatu+lnqn4+H"
                                        + "\nQ6F5kHRrGnzxgG0vauHknyTBuZazPfFRToWvUCMsnHx8tw8zPT2RjNdxAN0IDTPi\nSh8dGCBIBC"
                                        + "/+7MkGbw/xnaBXA5TtMg+zl4H3tm8CgYBYLZSIzzkQsIa09QuJONZV"
                                        + "\nV7KNAepLjlwEsXcLRgbFXv4eEdUu8pEYiWMdG1Tnev8BeJgmhTGMl5rdtjxFbABp"
                                        + "\nuvPgUSUwQgkQJsRMKIr5HgdJHtDixS+2uJpu+t7igBQofQLmRZwY75u31tQcauGl\n8c"
                                        + "//YOhwdNLpWHERbd+IJQKBgDqKulMA+9ugFZHLTTU3o65zhQkRvmFw8byynKEe\n8g+B"
                                        + "+zJw85aXS5BULSnlTuW1q33yE9pdxyd0BL7yTnLcOoScUsS4RNB8Ve+ikfCi\nxsNqctxqY8VjWXTvA"
                                        + "/sYA0mQxzsH91uJTvQIhFYiIURTP9Xj89QEadw6Ktxq17oP\n8AsfAoGAGKbX2CxMs5xyNVVmL"
                                        + "/uNsmQU0vje4e3gtq4dbL+tezCxmxobtz369B8k\nfE4ImL5kBUGFYerAL93aXz6IZ5VHiyBQu2nZicK"
                                        + "/9cS6QhTv2tUqrqSTnnCwOa79\ncSZS0tkblsMEORqnN4CyziX1CStbaRRPUOUxs7f7T5zgueqnXms=\n"
                                        + "-----END RSA PRIVATE KEY-----" )
                                .build() ) )
                        .metrics( Arrays.asList( "basic", "cpu", "memory", "disk", "interface", "disk_free", "top_cpu_process",
                                "top_mem_process" ) )
                        .detected( false )
                        .build();
                monitorConfigs.add( monitorConfig );
            }
            System.out.println( JsonUtil.object2Json( monitorConfigs ) );
        } catch ( Exception e ) {
            System.err.println( "Error reading file: " + e.getMessage() );
        }
    }
}
