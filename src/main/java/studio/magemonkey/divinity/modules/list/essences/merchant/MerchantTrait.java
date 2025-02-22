package studio.magemonkey.divinity.modules.list.essences.merchant;

import studio.magemonkey.divinity.api.DivinityAPI;
import studio.magemonkey.divinity.modules.api.socketing.merchant.MerchantSocket;
import studio.magemonkey.divinity.modules.list.essences.EssencesManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@TraitName("essences-merchant")
public class MerchantTrait extends Trait {

    public MerchantTrait() {
        super("essences-merchant");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {

            EssencesManager essencesManager = DivinityAPI.getModuleManager().getEssenceManager();
            if (essencesManager == null) return;

            MerchantSocket merchant = essencesManager.getMerchant();
            if (merchant != null) {
                Player player = e.getClicker();
                merchant.openMerchantGUI(player, false);
            }
        }
    }
}
