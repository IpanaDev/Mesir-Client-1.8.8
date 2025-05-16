package net.minecraft.block.state;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public interface IBlockState
{
    Collection<IProperty> getPropertyNames();

    <T extends Comparable<T>> T getValue(IProperty<T> property);

    <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value);

    <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property);

    ImmutableMap<IProperty, Comparable> getProperties();

    Block getBlock();

    default IBlockState withMirror(Mirror mirror) {
        return getBlock().withMirror(this, mirror);
    }

    default IBlockState withRotation(Rotation rotation) {
        return getBlock().withRotation(this, rotation);
    }
}
