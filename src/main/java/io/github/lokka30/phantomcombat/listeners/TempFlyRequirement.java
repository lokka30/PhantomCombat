package io.github.lokka30.phantomcombat.listeners;

import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.fly.result.ResultDeny;
import com.moneybags.tempfly.util.V;

public class TempFlyRequirement implements RequirementProvider {

	public TempFlyRequirement() {
		TempFly.getAPI().registerRequirementProvider(this);
	}

	@Override
	public boolean handles(InquiryType type) {
		return true;
	}
	
	public void enterCombat(Player p) {
		TempFly.getAPI().getUser(p).submitFlightResult(new ResultDeny(FlightResult.DenyReason.COMBAT, this, RequirementProvider.InquiryType.OUT_OF_SCOPE, V.requireFailCombat, !V.damageCombat));
	}
	
	public void exitCombat(Player p) {
		TempFly.getAPI().getUser(p).submitFlightResult(new ResultAllow(this, RequirementProvider.InquiryType.OUT_OF_SCOPE, V.requirePassCombat));
	}
	
}
