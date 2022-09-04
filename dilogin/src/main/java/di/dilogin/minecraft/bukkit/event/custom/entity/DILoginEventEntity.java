package di.dilogin.minecraft.bukkit.event.custom.entity;

import di.dilogin.entity.DIUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.player.PlayerJoinEvent;

@Getter
@Setter
@AllArgsConstructor
public class DILoginEventEntity {

    private DIUser user;
    private PlayerJoinEvent event;

}
