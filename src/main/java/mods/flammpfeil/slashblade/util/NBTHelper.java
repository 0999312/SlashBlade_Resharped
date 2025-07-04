package mods.flammpfeil.slashblade.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class NBTHelper {

    public static Vec3 getVector3d(CompoundTag tag, String key) {
        ListTag listnbt = tag.getList(key, 6);
        return new Vec3(listnbt.getDouble(0), listnbt.getDouble(1), listnbt.getDouble(2));
    }

    public static void putVector3d(CompoundTag tag, String key, Vec3 value) {
        tag.put(key, newDoubleNBTList(value.x, value.y, value.z));
    }

    public static ListTag newDoubleNBTList(Vec3 value) {
        return newDoubleNBTList(value.x, value.y, value.z);
    }

    public static ListTag newDoubleNBTList(double... numbers) {
        ListTag listnbt = new ListTag();

        for (double dValue : numbers) {
            listnbt.add(DoubleTag.valueOf(dValue));
        }

        return listnbt;
    }

    public static class NBTCoupler {
        CompoundTag instance;
        NBTCoupler parent = null;

        protected NBTCoupler(CompoundTag tag) {
            this.instance = tag;
        }

        @SafeVarargs
        public final <T> NBTCoupler put(String key, T... value) {
            writeNBT(instance, key, value);
            return this;
        }

        @SafeVarargs
        public final <T> NBTCoupler get(String key, Consumer<T> dest, T... values) {
            return this.get(key, dest, false, values);
        }

        @SafeVarargs
        public final <T> NBTCoupler get(String key, Consumer<T> dest, boolean isNullable, T... values) {
            readNBT(this.instance, key, dest, isNullable, values);
            return this;
        }

        public NBTCoupler remove(String key) {
            if (this.instance.hasUUID(key)) {
                this.instance.remove(key + "Most");
                this.instance.remove(key + "Least");

            } else {
                this.instance.remove(key);
            }
            return this;
        }

        public NBTCoupler getChild(String key) {
            CompoundTag tag;

            if (this.instance.contains(key, 10)) {
                tag = this.instance.getCompound(key);
            } else {
                tag = new CompoundTag();
                this.instance.put(key, tag);
            }

            return NBTHelper.getNBTCoupler(tag);
        }

        public NBTCoupler getParent() {
            return Objects.requireNonNullElse(parent, this);
        }

        public CompoundTag getRawCompound() {
            return this.instance;
        }

        public CompoundTag getRawCompound(String key) {
            if (this.instance.contains(key, 10)) {
                return this.instance.getCompound(key);
            } else {
                CompoundTag nbt = new CompoundTag();
                this.instance.put(key, nbt);
                return nbt;
            }
        }

        public NBTCoupler doRawCompound(String key, Consumer<CompoundTag> action) {
            if (this.instance.contains(key, 10)) {
                action.accept(this.instance.getCompound(key));
            }

            return this;
        }
    }

    public static NBTCoupler getNBTCoupler(CompoundTag tag) {
        return new NBTCoupler(tag);
    }

    @SafeVarargs
    public static <T> void writeNBT(CompoundTag dest, String key, T... value) {
        if (value == null || value.length != 1 || value[0] == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.getClass().getComponentType();

        if (type.equals(Integer.class)) {
            dest.putInt(key, (Integer) value[0]);
        } else if (type.equals(Float.class)) {
            dest.putFloat(key, (Float) value[0]);
        } else if (type.equals(Short.class)) {
            dest.putShort(key, (Short) value[0]);
        } else if (type.equals(Byte.class)) {
            dest.putByte(key, (Byte) value[0]);
        } else if (type.equals(Long.class)) {
            dest.putLong(key, (Long) value[0]);
        } else if (type.equals(Double.class)) {
            dest.putDouble(key, (Double) value[0]);
        } else if (type.equals(Boolean.class)) {
            dest.putBoolean(key, (Boolean) value[0]);
        } else {
            if (type.equals(UUID.class)) {
                dest.putUUID(key, (UUID) value[0]);
            } else if (type.equals(byte[].class)) {
                dest.putByteArray(key, (byte[]) value[0]);
            } else if (type.equals(int[].class)) {
                dest.putIntArray(key, (int[]) value[0]);
            } else if (type.equals(long[].class)) {
                dest.putLongArray(key, (long[]) value[0]);
            } else if (type.equals(CompoundTag.class)) {
                dest.put(key, (CompoundTag) value[0]);
            } else if (type.equals(String.class)) {
                dest.putString(key, (String) value[0]);
            } else if (type.equals(Vec3.class)) {
                putVector3d(dest, key, (Vec3) value[0]);
            }
        }
    }

    @SafeVarargs
    public static <T> void readNBT(CompoundTag src, String key, Consumer<T> dest, T... values) {
        readNBT(src, key, dest, false, values);
    }

    @SafeVarargs
    public static <T> void readNBT(CompoundTag src, String key, Consumer<T> dest, boolean isNullable,
                                   T... defaultValue) {
        if (isNullable) {
            dest.accept(castValue(key, src, defaultValue).orElse(null));
        } else {
            castValue(key, src, defaultValue).ifPresent(dest);
        }
    }

    @SafeVarargs
    public static <T> Optional<T> castValue(String key, CompoundTag src, T... defaultValue) {
        if (defaultValue == null) {
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) defaultValue.getClass().getComponentType();

        Object result = null;
        int typeId = -1;

        if (type.equals(Integer.class)) {
            typeId = 99;
            result = src.getInt(key);
        } else if (type.equals(Float.class)) {
            typeId = 99;
            result = src.getFloat(key);
        } else if (type.equals(Short.class)) {
            typeId = 99;
            result = src.getShort(key);
        } else if (type.equals(Byte.class)) {
            typeId = 99;
            result = src.getByte(key);
        } else if (type.equals(Long.class)) {
            typeId = 99;
            result = src.getLong(key);
        } else if (type.equals(Double.class)) {
            typeId = 99;
            result = src.getDouble(key);
        } else if (type.equals(Boolean.class)) {
            typeId = 99;
            result = src.getBoolean(key);
        } else if (src.contains(key)) {
            if (type.equals(UUID.class)) {
                typeId = -2;
                if (src.hasUUID(key)) {
                    result = src.getUUID(key);
                }
            } else if (type.equals(byte[].class)) {
                typeId = 7;
                result = src.getByteArray(key);
            } else if (type.equals(int[].class)) {
                typeId = 11;
                result = src.getIntArray(key);
            } else if (type.equals(long[].class)) {
                typeId = 12;
                result = src.getLongArray(key);
            } else if (type.equals(CompoundTag.class)) {
                typeId = 10;
                result = src.getCompound(key);
            } else if (type.equals(String.class)) {
                typeId = 8;
                result = src.getString(key);
            }
        } else if (type.equals(Vec3.class)) {
            typeId = 6;
            result = getVector3d(src, key);
        }

        if (0 < defaultValue.length) {
            boolean exists = (typeId == -2) ? src.hasUUID(key) : src.contains(key, typeId);
            if (!exists) {
                result = defaultValue;
            }
        }

        return Optional.ofNullable((T) result);
    }
}
