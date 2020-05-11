package io.github.lokka30.phantomcombat.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class WorldGuardUtil {

    private PhantomCombat instance;

    public WorldGuardUtil(PhantomCombat instance) {
        this.instance = instance;
    }

    //Get all regions at an Entities' location.
    //Method by Eyrian
    public ApplicableRegionSet getRegionSet(LivingEntity livingEntity) {
        Location loc = livingEntity.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(livingEntity.getWorld()));

        return Objects.requireNonNull(regions).getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
    }

    //Sorts a RegionSet by priority, lowest to highest.
    //Method by Eyrian
    public ProtectedRegion[] sortRegionsByPriority(ApplicableRegionSet regset) {
        ProtectedRegion[] regionarray = new ProtectedRegion[0];
        List<ProtectedRegion> regionList = new ArrayList<>();

        if (regset.size() == 0)
            return regionarray;
        else if (regset.size() == 1) {
            regionarray = new ProtectedRegion[1];
            return regset.getRegions().toArray(regionarray);
        }

        for (ProtectedRegion r : regset) {
            regionList.add(r);
        }

        regionList.sort(Comparator.comparingInt(ProtectedRegion::getPriority));

        return regionList.toArray(regionarray);
    }

    //Check if region is applicable for region levelling.
    //Method by Eyrian
    public boolean isPVPDenied(LivingEntity livingEntity) {

        //Check if WorldGuard-plugin exists
        if (instance.hasWorldGuard) {
            //Sorted region array, highest priority comes last.
            ProtectedRegion[] regions = sortRegionsByPriority(getRegionSet(livingEntity));

            //Check region flags on integrity.
            for (ProtectedRegion region : regions) {
                if (region.getFlag(Flags.PVP) == StateFlag.State.DENY) {
                    return true;
                }
            }
        }
        return false;
    }
}
