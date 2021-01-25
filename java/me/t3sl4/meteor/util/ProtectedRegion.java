package me.t3sl4.meteor.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sk89q.worldguard.protection.regions.RegionType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.util.ChangeTracked;
import com.sk89q.worldguard.util.Normal;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public abstract class ProtectedRegion implements ChangeTracked, Comparable<ProtectedRegion> {
    public static final String GLOBAL_REGION = "__global__";

    private static final Pattern VALID_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_,'\\-\\+/]{1,}$");

    protected BlockVector3 min;

    protected BlockVector3 max;

    private final String id;

    private final boolean transientRegion;

    private int priority = 0;

    private ProtectedRegion parent;

    private DefaultDomain owners = new DefaultDomain();

    private DefaultDomain members = new DefaultDomain();

    private ConcurrentMap<Flag<?>, Object> flags = new ConcurrentHashMap<>();

    private boolean dirty = true;

    ProtectedRegion(String id, boolean transientRegion) {
        Preconditions.checkNotNull(id);
        if (!isValidId(id))
            throw new IllegalArgumentException("Invalid region ID: " + id);
        this.id = Normal.normalize(id);
        this.transientRegion = transientRegion;
    }

    protected void setMinMaxPoints(List<BlockVector3> points) {
        int minX = ((BlockVector3)points.get(0)).getBlockX();
        int minY = ((BlockVector3)points.get(0)).getBlockY();
        int minZ = ((BlockVector3)points.get(0)).getBlockZ();
        int maxX = minX;
        int maxY = minY;
        int maxZ = minZ;
        for (BlockVector3 v : points) {
            int x = v.getBlockX();
            int y = v.getBlockY();
            int z = v.getBlockZ();
            if (x < minX)
                minX = x;
            if (y < minY)
                minY = y;
            if (z < minZ)
                minZ = z;
            if (x > maxX)
                maxX = x;
            if (y > maxY)
                maxY = y;
            if (z > maxZ)
                maxZ = z;
        }
        setDirty(true);
        this.min = BlockVector3.at(minX, minY, minZ);
        this.max = BlockVector3.at(maxX, maxY, maxZ);
    }

    public String getId() {
        return this.id;
    }

    public BlockVector3 getMinimumPoint() {
        return this.min;
    }

    public BlockVector3 getMaximumPoint() {
        return this.max;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        setDirty(true);
        this.priority = priority;
    }

    @Nullable
    public ProtectedRegion getParent() {
        return this.parent;
    }

    public void setParent(@Nullable ProtectedRegion parent) throws CircularInheritanceException {
        setDirty(true);
        if (parent == null) {
            this.parent = null;
            return;
        }
        if (parent == this)
            throw new CircularInheritanceException();
        ProtectedRegion p = parent.getParent();
        while (p != null) {
            if (p == this)
                throw new CircularInheritanceException();
            p = p.getParent();
        }
        this.parent = parent;
    }

    public void clearParent() {
        setDirty(true);
        this.parent = null;
    }

    public DefaultDomain getOwners() {
        return this.owners;
    }

    public void setOwners(DefaultDomain owners) {
        Preconditions.checkNotNull(owners);
        setDirty(true);
        this.owners = new DefaultDomain(owners);
    }

    public DefaultDomain getMembers() {
        return this.members;
    }

    public void setMembers(DefaultDomain members) {
        Preconditions.checkNotNull(members);
        setDirty(true);
        this.members = new DefaultDomain(members);
    }

    public boolean hasMembersOrOwners() {
        return (this.owners.size() > 0 || this.members.size() > 0);
    }

    public boolean isOwner(LocalPlayer player) {
        Preconditions.checkNotNull(player);
        if (this.owners.contains(player))
            return true;
        ProtectedRegion curParent = getParent();
        while (curParent != null) {
            if (curParent.getOwners().contains(player))
                return true;
            curParent = curParent.getParent();
        }
        return false;
    }

    @Deprecated
    public boolean isOwner(String playerName) {
        Preconditions.checkNotNull(playerName);
        if (this.owners.contains(playerName))
            return true;
        ProtectedRegion curParent = getParent();
        while (curParent != null) {
            if (curParent.getOwners().contains(playerName))
                return true;
            curParent = curParent.getParent();
        }
        return false;
    }

    public boolean isMember(LocalPlayer player) {
        Preconditions.checkNotNull(player);
        if (isOwner(player))
            return true;
        return isMemberOnly(player);
    }

    @Deprecated
    public boolean isMember(String playerName) {
        Preconditions.checkNotNull(playerName);
        if (isOwner(playerName))
            return true;
        if (this.members.contains(playerName))
            return true;
        ProtectedRegion curParent = getParent();
        while (curParent != null) {
            if (curParent.getMembers().contains(playerName))
                return true;
            curParent = curParent.getParent();
        }
        return false;
    }

    public boolean isMemberOnly(LocalPlayer player) {
        Preconditions.checkNotNull(player);
        if (this.members.contains(player))
            return true;
        ProtectedRegion curParent = getParent();
        while (curParent != null) {
            if (curParent.getMembers().contains(player))
                return true;
            curParent = curParent.getParent();
        }
        return false;
    }

    @Nullable
    public <T extends Flag<V>, V> V getFlag(T flag) {
        V val;
        Preconditions.checkNotNull(flag);
        Object obj = this.flags.get(flag);
        if (obj != null) {
            val = (V)obj;
        } else {
            return null;
        }
        return val;
    }

    public <T extends Flag<V>, V> void setFlag(T flag, @Nullable V val) {
        Preconditions.checkNotNull(flag);
        setDirty(true);
        if (val == null) {
            this.flags.remove(flag);
        } else {
            this.flags.put((Flag<?>)flag, val);
        }
    }

    public Map<Flag<?>, Object> getFlags() {
        return this.flags;
    }

    public void setFlags(Map<Flag<?>, Object> flags) {
        Preconditions.checkNotNull(flags);
        setDirty(true);
        this.flags = new ConcurrentHashMap<>(flags);
    }

    public void copyFrom(ProtectedRegion other) {
        Preconditions.checkNotNull(other);
        setMembers(other.getMembers());
        setOwners(other.getOwners());
        setFlags(other.getFlags());
        setPriority(other.getPriority());
        try {
            setParent(other.getParent());
        } catch (CircularInheritanceException circularInheritanceException) {}
    }

    public boolean contains(BlockVector2 position) {
        Preconditions.checkNotNull(position);
        return contains(BlockVector3.at(position.getBlockX(), this.min.getBlockY(), position.getBlockZ()));
    }

    public boolean contains(int x, int y, int z) {
        return contains(BlockVector3.at(x, y, z));
    }

    public boolean containsAny(List<BlockVector2> positions) {
        Preconditions.checkNotNull(positions);
        for (BlockVector2 pt : positions) {
            if (contains(pt))
                return true;
        }
        return false;
    }

    public List<ProtectedRegion> getIntersectingRegions(Collection<ProtectedRegion> regions) {
        Preconditions.checkNotNull(regions, "regions");
        List<ProtectedRegion> intersecting = Lists.newArrayList();
        Area thisArea = toArea();
        for (ProtectedRegion region : regions) {
            if (!region.isPhysicalArea())
                continue;
            if (intersects(region, thisArea))
                intersecting.add(region);
        }
        return intersecting;
    }

    protected boolean intersects(ProtectedRegion region, Area thisArea) {
        if (intersectsBoundingBox(region)) {
            Area testArea = region.toArea();
            testArea.intersect(thisArea);
            return !testArea.isEmpty();
        }
        return false;
    }

    protected boolean intersectsBoundingBox(ProtectedRegion region) {
        BlockVector3 rMaxPoint = region.getMaximumPoint();
        BlockVector3 min = getMinimumPoint();
        if (rMaxPoint.getBlockX() < min.getBlockX())
            return false;
        if (rMaxPoint.getBlockY() < min.getBlockY())
            return false;
        if (rMaxPoint.getBlockZ() < min.getBlockZ())
            return false;
        BlockVector3 rMinPoint = region.getMinimumPoint();
        BlockVector3 max = getMaximumPoint();
        if (rMinPoint.getBlockX() > max.getBlockX())
            return false;
        if (rMinPoint.getBlockY() > max.getBlockY())
            return false;
        if (rMinPoint.getBlockZ() > max.getBlockZ())
            return false;
        return true;
    }

    protected boolean intersectsEdges(ProtectedRegion region) {
        List<BlockVector2> pts1 = getPoints();
        List<BlockVector2> pts2 = region.getPoints();
        BlockVector2 lastPt1 = pts1.get(pts1.size() - 1);
        BlockVector2 lastPt2 = pts2.get(pts2.size() - 1);
        for (BlockVector2 aPts1 : pts1) {
            for (BlockVector2 aPts2 : pts2) {
                Line2D line1 = new Line2D.Double(lastPt1.getBlockX(), lastPt1.getBlockZ(), aPts1.getBlockX(), aPts1.getBlockZ());
                if (line1.intersectsLine(lastPt2
                        .getBlockX(), lastPt2
                        .getBlockZ(), aPts2
                        .getBlockX(), aPts2
                        .getBlockZ()))
                    return true;
                lastPt2 = aPts2;
            }
            lastPt1 = aPts1;
        }
        return false;
    }

    public boolean isTransient() {
        return this.transientRegion;
    }

    public boolean isDirty() {
        if (isTransient())
            return false;
        return (this.dirty || this.owners.isDirty() || this.members.isDirty());
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        this.owners.setDirty(dirty);
        this.members.setDirty(dirty);
    }

    public int compareTo(ProtectedRegion other) {
        if (getPriority() > other.getPriority())
            return -1;
        if (getPriority() < other.getPriority())
            return 1;
        return getId().compareTo(other.getId());
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return "ProtectedRegion{id='" + this.id + "', type='" +

                getType() + '\'' + '}';
    }

    public static boolean isValidId(String id) {
        Preconditions.checkNotNull(id);
        return VALID_ID_PATTERN.matcher(id).matches();
    }

    public abstract boolean isPhysicalArea();

    public abstract List<BlockVector2> getPoints();

    public abstract int volume();

    public abstract boolean contains(BlockVector3 paramBlockVector3);

    public abstract RegionType getType();

    abstract Area toArea();

    public static class CircularInheritanceException extends Exception {}
}

