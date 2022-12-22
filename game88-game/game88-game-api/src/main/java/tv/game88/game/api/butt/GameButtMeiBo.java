package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;

import java.math.BigDecimal;

@Log4j2
@Repository( value = ConstantsGame.MEIBO + "GameProcessor" )
public class GameButtMeiBo extends AbstractGameButt {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        return null;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        return false;
    }
}
