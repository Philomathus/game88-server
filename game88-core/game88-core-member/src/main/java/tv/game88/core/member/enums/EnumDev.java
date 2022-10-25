package tv.game88.core.member.enums;

/**
 * 登录设备
 */
public enum EnumDev {
    /** 登录相关 */
    IOS(1,"IOS"),
    Android(2,"安卓"),
    ;
    private int type ;
    private String des;

    public int getType() {
        return type;
    }

    public String getDes() {
        return des;
    }

    public static String  getDes(Integer type){
        for(EnumDev d:EnumDev.values()){
            if(d.getType()==type){
                return d.getDes();
            }

        }
        return "其他";
    }

    EnumDev( int type, String des) {
        this.type = type;
        this.des = des;
    }

}
